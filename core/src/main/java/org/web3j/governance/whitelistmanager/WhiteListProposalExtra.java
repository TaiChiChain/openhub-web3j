package org.web3j.governance.whitelistmanager;

import java.util.List;

public class WhiteListProposalExtra {
    private List<WhiteListProvider> providers;

    public WhiteListProposalExtra(List<WhiteListProvider> providers) {
        this.providers = providers;
    }

    public WhiteListProposalExtra() {}

    public List<WhiteListProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<WhiteListProvider> providers) {
        this.providers = providers;
    }
}
