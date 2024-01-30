package org.web3j.governance.model;

import java.math.BigInteger;

import org.web3j.governance.constants.ProposalType;

public class ProposeResVO {
    private String txHash;
    private ProposalType type;
    private BigInteger proposalId;

    public ProposeResVO() {}

    public ProposeResVO(String txHash, ProposalType type, BigInteger proposalId) {
        this.txHash = txHash;
        this.type = type;
        this.proposalId = proposalId;
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
}
