package org.web3j.governance.councilmanager;

import java.util.List;

public class CouncilProposalExtra {
    private List<Candidate> candidates;

    // Constructor, getters, and setters
    public CouncilProposalExtra(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
}
