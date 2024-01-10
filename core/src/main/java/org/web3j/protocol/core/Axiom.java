package org.web3j.protocol.core;

import org.web3j.protocol.core.methods.response.AXMIncentiveAddrResponse;
import org.web3j.protocol.core.methods.response.AxiomStatus;

public interface Axiom {

    Request<?, AxiomStatus> status();

    Request<?, AXMIncentiveAddrResponse> getIncentiveAddr();
}
