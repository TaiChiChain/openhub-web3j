package org.web3j.governance.councilmanager;

public class Candidate {
    private String address;
    private int weight;
    private String name;

    // Constructor, getters, and setters
    public Candidate(String address, int weight, String name) {
        this.address = address;
        this.weight = weight;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
