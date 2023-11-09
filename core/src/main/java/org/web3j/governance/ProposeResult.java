package org.web3j.governance;

import java.math.BigInteger;

import org.web3j.governance.constants.ProposalType;

public class ProposeResult {
    private String txHash;
    private ProposalType type;
    private BigInteger proposalId;

    private String errMessage;

    private Boolean success;

    public ProposeResult() {}

    public ProposeResult(String txHash, ProposalType type, BigInteger proposalId, String errMessage) {
        this.txHash = txHash;
        this.type = type;
        this.proposalId = proposalId;
        this.errMessage = errMessage;
        this.success = errMessage == null;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public ProposalType getType() {
        return type;
    }

    public void setType(ProposalType type) {
        this.type = type;
    }

    public BigInteger getProposalId() {
        return proposalId;
    }

    public void setProposalId(BigInteger proposalId) {
        this.proposalId = proposalId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
