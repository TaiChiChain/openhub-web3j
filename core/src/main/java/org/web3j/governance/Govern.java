package org.web3j.governance;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.governance.constants.GovernanceConstant;
import org.web3j.governance.constants.ProposalType;
import org.web3j.governance.exceptions.GovernanceException;
import org.web3j.governance.model.GovernResult;
import org.web3j.governance.model.ProposalVO;
import org.web3j.governance.model.ProposeResVO;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;

import static org.web3j.governance.constants.GovernanceConstant.MAX_ATTEMPTS;
import static org.web3j.governance.constants.GovernanceConstant.SLEEP_DURATION;

public class Govern {
    private Web3j web3j;
    private Credentials credentials;
    private ContractGasProvider contractGasProvider;

    public Govern(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.contractGasProvider = contractGasProvider;
    }

    /**
     * Generates a proposal and sends a transaction to the governance contract.
     *
     * @param type the type of the proposal
     * @param title the title of the proposal
     * @param desc the description of the proposal
     * @param blockNum the block number
     * @param extraData the extra data for the proposal
     * @return the result of the proposal
     */
    public <T> GovernResult<ProposeResVO> propose(
            ProposalType type, String title, String desc, Long blockNum, T extraData) {
        try {
            String encodedFunction =
                    encodeProposeFunction(type, title, desc, blockNum, getByteArray(extraData));
            EthSendTransaction ethSendTransaction =
                    sendTransaction(
                            GovernanceConstant.ST_GOVERNANCE_CONTRACT_ADDRESS, encodedFunction);

            if (ethSendTransaction.hasError()) {
                return GovernResult.failure(
                        "Transaction error: " + ethSendTransaction.getError().getMessage());
            }

            String txHash = ethSendTransaction.getTransactionHash();
            Optional<TransactionReceipt> receiptOptional = fetchTransactionReceipt(txHash);

            if (!receiptOptional.isPresent()) {
                return GovernResult.failure("Transaction receipt not generated");
            }
            if (!"0x1".equals(receiptOptional.get().getStatus())) {
                return GovernResult.failure("Transaction failed");
            }

            Optional<String> proposalIdHexOptional = getProposalIdHex(receiptOptional);

            if (!proposalIdHexOptional.isPresent()) {
                return GovernResult.failure("Proposal id not generated");
            }

            ProposeResVO proposeResVO =
                    new ProposeResVO(txHash, type, new BigInteger(proposalIdHexOptional.get(), 16));
            return GovernResult.success(proposeResVO);

        } catch (IOException e) {
            return GovernResult.failure("IO Exception: " + e.getMessage());
        } catch (GovernanceException e) {
            return GovernResult.failure("Governance Exception: " + e.getMessage());
        }
    }

    /**
     * Vote on a proposal.
     *
     * @param proposalId The ID of the proposal to vote on.
     * @param approve A boolean indicating whether to approve or reject the proposal.
     * @return A GovernResult containing the transaction hash if successful, or an error message if
     *     the vote fails.
     */
    public GovernResult<String> vote(Long proposalId, boolean approve) {
        String encodedFunction = encodeVoteFunction(proposalId, approve ? 1 : 0);
        try {
            EthSendTransaction ethSendTransaction =
                    sendTransaction(
                            GovernanceConstant.ST_GOVERNANCE_CONTRACT_ADDRESS, encodedFunction);

            if (ethSendTransaction.hasError()) {
                return GovernResult.failure(
                        "Transaction Error: " + ethSendTransaction.getError().getMessage());
            }

            String transactionHash = ethSendTransaction.getTransactionHash();
            Optional<TransactionReceipt> transactionReceiptOpt =
                    fetchTransactionReceipt(transactionHash);

            if (!transactionReceiptOpt.isPresent()) {
                return GovernResult.failure(
                        "Transaction receipt not generated for hash: " + transactionHash);
            }

            TransactionReceipt transactionReceipt = transactionReceiptOpt.get();
            if (!"0x1".equals(transactionReceipt.getStatus())) {
                return GovernResult.failure("Transaction failed");
            }

            return GovernResult.success(transactionHash);

        } catch (IOException e) {
            return GovernResult.failure("IO Exception: " + e.getMessage());
        } catch (GovernanceException e) {
            return GovernResult.failure("Governance Exception: " + e.getMessage());
        }
    }

    /**
     * Retrieves the latest proposal ID from the specified contract address.
     *
     * @param contractAddress the address of the contract
     * @return the latest proposal ID as a GovernResult object
     * @throws IOException if an IO error occurs during the function execution
     */
    public GovernResult<BigInteger> getLatestProposalID(String contractAddress) throws IOException {
        String encodedFunction = FunctionEncoder.encode(getLatestProposalIDFunction());
        String responseValue = callReadOnlyFunction(contractAddress, encodedFunction);

        List<Type> response =
                FunctionReturnDecoder.decode(
                        responseValue, getLatestProposalIDFunction().getOutputParameters());

        if (!response.isEmpty()) {
            BigInteger proposalID = (BigInteger) response.get(0).getValue();
            return GovernResult.success(proposalID);
        } else {
            return GovernResult.failure("Proposal ID not found");
        }
    }

    /**
     * Retrieves a proposal by its ID.
     *
     * @param proposalId the ID of the proposal to retrieve
     * @return a GovernResult object containing the proposal, if found
     */
    public GovernResult<ProposalVO> getProposal(Long proposalId) {
        try {
            Function function = getProposalFunction(proposalId);
            Transaction transaction =
                    Transaction.createEthCallTransaction(
                            GovernanceConstant.ZERO_ADDRESS,
                            GovernanceConstant.ST_GOVERNANCE_CONTRACT_ADDRESS,
                            FunctionEncoder.encode(function));
            EthCall ethCall =
                    web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results =
                    FunctionReturnDecoder.decode(
                            ethCall.getValue(), function.getOutputParameters());
            if (results.size() != 0) {
                String jsonString = results.get(0).toString();
                ObjectMapper objectMapper = new ObjectMapper();
                ProposalVO proposalVO = objectMapper.readValue(jsonString, ProposalVO.class);
                return GovernResult.success(proposalVO);
            }
        } catch (Exception e) {
            return GovernResult.failure(e.getMessage());
        }
        return GovernResult.failure("Proposal not found");
    }

    private Function getProposalFunction(Long proposalId) {
        return new Function(
                GovernanceConstant.PROPOSAL_METHOD_NAME,
                Collections.singletonList(new Uint64(proposalId)),
                Collections.singletonList(new TypeReference<ProposalVO>() {}));
    }

    private String callReadOnlyFunction(String contractAddress, String encodedFunction)
            throws IOException {
        Transaction transaction =
                Transaction.createEthCallTransaction(null, contractAddress, encodedFunction);
        EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
        return ethCall.getValue();
    }

    private Optional<TransactionReceipt> fetchTransactionReceipt(String txHash)
            throws GovernanceException {
        Optional<TransactionReceipt> receiptOptional = Optional.empty();
        int attempts = 0;
        while (!receiptOptional.isPresent() && attempts < MAX_ATTEMPTS) {
            try {
                EthGetTransactionReceipt receiptResult =
                        web3j.ethGetTransactionReceipt(txHash).send();
                receiptOptional = receiptResult.getTransactionReceipt();
                Thread.sleep(SLEEP_DURATION);
                attempts++;
            } catch (Exception e) {
                throw new GovernanceException("Error fetching transaction receipt");
            }
        }
        return receiptOptional;
    }

    private Optional<String> getProposalIdHex(Optional<TransactionReceipt> receiptOptional) {
        return receiptOptional
                .flatMap(receipt -> Optional.ofNullable(receipt.getLogs().get(0)))
                .flatMap(log -> Optional.ofNullable(log.getTopics().get(1)))
                .map(topic -> topic.substring(2));
    }

    private String encodeProposeFunction(
            ProposalType type, String title, String desc, Long blockNum, byte[] data) {
        return FunctionEncoder.encode(
                new Function(
                        GovernanceConstant.PROPOSE_METHOD_NAME,
                        Arrays.asList(
                                new Uint8(BigInteger.valueOf(type.getType())),
                                new Utf8String(title),
                                new Utf8String(desc),
                                new Uint64(BigInteger.valueOf(blockNum)),
                                new DynamicBytes(data)),
                        Arrays.asList(new TypeReference<Uint64>() {})));
    }

    @NotNull
    private <V> byte[] getByteArray(V extra) {
        try {
            String jsonExtraData = new ObjectMapper().writeValueAsString(extra);
            byte[] jsonBytes = jsonExtraData.getBytes(StandardCharsets.UTF_8);
            return Numeric.hexStringToByteArray(Numeric.toHexString(jsonBytes));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String encodeVoteFunction(Long proposalId, int approveInteger) {
        return FunctionEncoder.encode(
                new Function(
                        GovernanceConstant.VOTE_METHOD_NAME,
                        Arrays.asList(
                                new Uint64(BigInteger.valueOf(proposalId)),
                                new Uint8(BigInteger.valueOf(approveInteger))),
                        Collections.emptyList()));
    }

    private Function getLatestProposalIDFunction() {
        return new Function(
                GovernanceConstant.PROPOSAL_LATE_ID_METHOD_NAME,
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint64>() {}));
    }

    private EthSendTransaction sendTransaction(String systemContractAddr, String encodedFunction)
            throws IOException {

        BigInteger nonce = getTransactionCount();
        BigInteger gasPrice;
        BigInteger gasLimit;

        if (contractGasProvider == null) {
            EthBlock.Block block = getLatestBlock();
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
            gasLimit = block.getGasLimit();
        } else {
            gasPrice = contractGasProvider.getGasPrice(encodedFunction);
            gasLimit = contractGasProvider.getGasLimit(encodedFunction);
        }

        RawTransaction rawTransaction =
                RawTransaction.createTransaction(
                        nonce, gasPrice, gasLimit, systemContractAddr, encodedFunction);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        return web3j.ethSendRawTransaction(hexValue).send();
    }

    private BigInteger getTransactionCount() throws IOException {
        return web3j.ethGetTransactionCount(
                        credentials.getAddress(), DefaultBlockParameterName.LATEST)
                .send()
                .getTransactionCount();
    }

    private EthBlock.Block getLatestBlock() throws IOException {
        return web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, true).send().getBlock();
    }
}
