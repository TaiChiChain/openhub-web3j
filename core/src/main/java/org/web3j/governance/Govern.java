package org.web3j.governance;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.governance.constants.GovernanceConstant;
import org.web3j.governance.constants.ProposalType;
import org.web3j.governance.councilmanager.CouncilProposalExtra;
import org.web3j.governance.exceptions.GovernanceException;
import org.web3j.governance.whitelistmanager.WLProposalExtra;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
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

    public <T> ProposeResult propose(
            ProposalType type, String title, String desc, Long blockNum, T extraData)
            throws GovernanceException {
        if (type == null || !checkExtraData(type, extraData)) {
            return new ProposeResult(null, null, null, "Invalid extraData");
        }

        Function function =
                getEncodedProposalFunction(type, title, desc, blockNum, getByteArray(extraData));
        String encodedFunction = FunctionEncoder.encode(function);
        String address = getSystemContractAddr(type);
        if (address == null) {
            return new ProposeResult(null, null, null, "Invalid proposalType");
        }
        EthSendTransaction ethSendTransaction;
        try {
            ethSendTransaction = sendTransaction(address, encodedFunction);
        } catch (IOException e) {
            return new ProposeResult(null, null, null, e.getMessage());
        }

        if (ethSendTransaction.hasError()) {
            return new ProposeResult(null, null, null, "Transaction error: " + ethSendTransaction.getError());
        }

        String txHash = ethSendTransaction.getTransactionHash();
        Optional<TransactionReceipt> receiptOptional = fetchTransactionReceipt(txHash);

        if (!receiptOptional.isPresent()) {
            // Transaction receipt not generated
            return new ProposeResult(txHash, type, null, "Transaction receipt not generated");
        }
        if (!receiptOptional.get().getStatus().equals("0x1")) {
            // Transaction failed
            return new ProposeResult(txHash, type, null, "Transaction failed");
        }

        Optional<String> proposalIdHexOptional = getProposalIdHex(receiptOptional);

        if (!proposalIdHexOptional.isPresent()) {
            // Proposal id not generated
            return new ProposeResult(txHash, type, null, "Proposal id not generated");
        }

        return new ProposeResult(txHash, type, new BigInteger(proposalIdHexOptional.get(), 16), null);
    }

    public String voteOnProposal(ProposalType type, Long proposalId, boolean approve) {
        String encodedFunction = getEncodedVoteFunction(proposalId, approve ? 1 : 0);
        EthSendTransaction ethSendTransaction;
        try {
            ethSendTransaction = sendTransaction(getSystemContractAddr(type), encodedFunction);
        } catch (IOException e) {
            throw new GovernanceException(e.getMessage());
        }
        if (ethSendTransaction.hasError()) {
            throw new GovernanceException(ethSendTransaction.getError().getMessage());
        }
        Optional<TransactionReceipt> transactionReceipt = fetchTransactionReceipt(ethSendTransaction.getTransactionHash());
        if (!transactionReceipt.isPresent()) {
            throw new GovernanceException("Transaction receipt not generated");
        }
        if (!transactionReceipt.get().getStatus().equals("0x1")) {
            throw new GovernanceException("Transaction failed");
        }
        return ethSendTransaction.getTransactionHash();
    }

    private static String getSystemContractAddr(ProposalType type) {
        String address = "";
        if (type == ProposalType.CouncilElect) {
            address = GovernanceConstant.ST_GOVERNANCE_COUNCIL_ADDRESS;
        } else if (type == ProposalType.WhiteListProviderAdd
                || type == ProposalType.WhiteListProviderRemove) {
            address = GovernanceConstant.ST_GOVERNANCE_WHITELIST_PROVIDER_ADDRESS;
        } else {
            return null;
        }
        return address;
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

    private static Function getEncodedProposalFunction(
            ProposalType type, String title, String desc, Long blockNum, byte[] data) {
        return new Function(
                "propose",
                Arrays.asList(
                        new Uint8(BigInteger.valueOf(type.getType())),
                        new Utf8String(title),
                        new Utf8String(desc),
                        new Uint64(BigInteger.valueOf(blockNum)),
                        new DynamicBytes(data)),
                Arrays.asList(new TypeReference<Uint64>() {
                }));
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

    private <T> boolean checkExtraData(ProposalType type, T extraData) {
        if (ProposalType.CouncilElect == type) {
            return extraData instanceof CouncilProposalExtra;
        } else if (ProposalType.WhiteListProviderAdd == type
                || ProposalType.WhiteListProviderRemove == type) {
            return extraData instanceof WLProposalExtra;
        } else {
            throw new RuntimeException("proposal type is not supported");
        }
    }

    private static String getEncodedVoteFunction(Long proposalId, int approveInteger) {
        return FunctionEncoder.encode(
                new Function(
                        "vote",
                        Arrays.asList(
                                new Uint64(BigInteger.valueOf(proposalId)),
                                new Uint8(BigInteger.valueOf(approveInteger)),
                                new DynamicBytes(new byte[]{})),
                        Collections.emptyList()));
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

    //    public ProposalDetails queryProposal(Long proposalId) {
    //        return null;
    //    }
}
