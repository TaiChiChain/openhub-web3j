package org.web3j.protocol.core.methods.response;

import org.web3j.protocol.core.Response;

public class AXMIncentiveAddrResponse extends Response<String> {

    public String getIncentiveAddress() {
        return getResult();
    }
}
