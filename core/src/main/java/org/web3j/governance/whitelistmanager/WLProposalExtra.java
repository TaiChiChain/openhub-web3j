package org.web3j.governance.whitelistmanager;

import java.util.List;

public class WLProposalExtra {
    private List<WhiteListProvider> providers;

    public WLProposalExtra(List<WhiteListProvider> providers) {
        this.providers = providers;
    }

    public WLProposalExtra() {}

    public List<WhiteListProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<WhiteListProvider> providers) {
        this.providers = providers;
    }
}
