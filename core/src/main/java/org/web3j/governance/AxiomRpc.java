package org.web3j.governance;

import java.io.IOException;
import java.util.Collections;

import org.web3j.governance.rpc.AxiomStatus;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;

public class AxiomRpc {
    private final Web3jService web3jService;

    public AxiomRpc(Web3jService web3jService) {
        this.web3jService = web3jService;
    }

    public String getAxmStatus() throws IOException {
        Request<?, AxiomStatus> request =
                new Request<>("axm_status", Collections.emptyList(), web3jService, AxiomStatus.class);

        AxiomStatus response = request.send();

        if (response.hasError()) {
            throw new IOException("Error fetching axm status: " + response.getError().getMessage());
        }
        return response.getResult().get("status");
    }
}
