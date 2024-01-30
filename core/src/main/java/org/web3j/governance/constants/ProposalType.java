package org.web3j.governance.constants;

public enum ProposalType {
    CouncilElect(0),

    NodeUpgrade(1),

    NodeAdd(2),

    NodeRemove(3),

    WhiteListProviderAdd(4),

    WhiteListProviderRemove(5),

    GasUpdate(6);

    private final Integer type;

    ProposalType(Integer type) {
        this.type = type;
    }

    public static ProposalType findByName(String name) {
        for (ProposalType proposalType : values()) {
            if (proposalType.name().equals(name)) {
                return proposalType;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }
}
