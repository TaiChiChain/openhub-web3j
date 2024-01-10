package org.web3j.crypto.transaction.type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

import static org.web3j.crypto.transaction.type.TransactionType.IncentiveTx;

public class IncentiveTransaction extends LegacyTransaction {

    private String incentiveAddress;

    private long chainId;
    private BigInteger maxPriorityFeePerGas;
    private BigInteger maxFeePerGas;

    public IncentiveTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            String incentiveAddress) {
        super(IncentiveTx, nonce, null, gasLimit, to, value, data);
        this.incentiveAddress = incentiveAddress;
        this.chainId = chainId;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.maxFeePerGas = maxFeePerGas;
    }

    @Override
    public List<RlpType> asRlpValues(Sign.SignatureData signatureData) {
        List<RlpType> result = new ArrayList<>();

        result.add(RlpString.create(getChainId()));

        result.add(RlpString.create(getNonce()));

        // add maxPriorityFeePerGas and maxFeePerGas if this is an EIP-1559 transaction
        result.add(RlpString.create(getMaxPriorityFeePerGas()));
        result.add(RlpString.create(getMaxFeePerGas()));

        result.add(RlpString.create(getGasLimit()));

        // an empty to address (contract creation) should not be encoded as a numeric 0 value
        String to = getTo();
        if (to != null && to.length() > 0) {
            // addresses that start with zeros should be encoded with the zeros included, not
            // as numeric values
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(getValue()));

        // value field will already be hex encoded, so we need to convert into binary first
        byte[] data = Numeric.hexStringToByteArray(getData());
        result.add(RlpString.create(data));

        // access list
        result.add(new RlpList());

        // add incentiveAddress if this is an IncentiveTransaction
        if (incentiveAddress != null && !incentiveAddress.isEmpty()) {
            result.add(RlpString.create(Numeric.hexStringToByteArray(incentiveAddress)));
        } else {
            result.add(RlpString.create(""));
        }

        if (signatureData != null) {
            result.add(RlpString.create(Sign.getRecId(signatureData, getChainId())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
        }

        return result;
    }

    public static IncentiveTransaction createEtherTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            String incentiveAddress) {
        return new IncentiveTransaction(
                chainId,
                nonce,
                gasLimit,
                to,
                value,
                "",
                maxPriorityFeePerGas,
                maxFeePerGas,
                incentiveAddress);
    }

    public static IncentiveTransaction createTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            String incentiveAddress) {

        return new IncentiveTransaction(
                chainId,
                nonce,
                gasLimit,
                to,
                value,
                data,
                maxPriorityFeePerGas,
                maxFeePerGas,
                incentiveAddress);
    }

    public String getIncentiveAddress() {
        return incentiveAddress;
    }

    @Override
    public BigInteger getGasPrice() {
        throw new UnsupportedOperationException("not available for 1559 transaction");
    }

    public long getChainId() {
        return chainId;
    }

    public BigInteger getMaxPriorityFeePerGas() {
        return maxPriorityFeePerGas;
    }

    public BigInteger getMaxFeePerGas() {
        return maxFeePerGas;
    }
}
