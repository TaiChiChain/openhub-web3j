package org.web3j.governance.whitelistmanager;

public class WhiteListProvider {
    private String address;

    public WhiteListProvider(String address) {
        this.address = address;
    }

    public WhiteListProvider() {}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
