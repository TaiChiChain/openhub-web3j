package org.web3j.governance.model;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.reflection.Parameterized;

public class ProposalVO extends DynamicStruct {
    public BigInteger ID;
    public BigInteger Type;
    public BigInteger Strategy;
    public String Proposer;
    public String Title;
    public String Desc;
    public BigInteger BlockNumber;
    public BigInteger TotalVotes;
    public List<String> PassVotes;
    public List<String> RejectVotes;
    public BigInteger Status;
    public String Extra;
    public BigInteger CreatedBlockNumber;
    public BigInteger EffectiveBlockNumber;

    public BigInteger getID() {
        return ID;
    }

    public BigInteger getType() {
        return Type;
    }

    public BigInteger getStrategy() {
        return Strategy;
    }

    public String getProposer() {
        return Proposer;
    }

    public String getTitle() {
        return Title;
    }

    public String getDesc() {
        return Desc;
    }

    public BigInteger getBlockNumber() {
        return BlockNumber;
    }

    public BigInteger getTotalVotes() {
        return TotalVotes;
    }

    public List<String> getPassVotes() {
        return PassVotes;
    }

    public List<String> getRejectVotes() {
        return RejectVotes;
    }

    public BigInteger getStatus() {
        return Status;
    }

    public String getExtra() {
        return Extra;
    }

    public BigInteger getCreatedBlockNumber() {
        return CreatedBlockNumber;
    }

    public BigInteger getEffectiveBlockNumber() {
        return EffectiveBlockNumber;
    }

    public ProposalVO(
            Uint ID,
            Int Type,
            Int Strategy,
            Utf8String Proposer,
            Utf8String Title,
            Utf8String Desc,
            Uint BlockNumber,
            Int TotalVotes,
            @Parameterized(type = org.web3j.abi.datatypes.Utf8String.class)
                    DynamicArray<Utf8String> PassVotes,
            @Parameterized(type = org.web3j.abi.datatypes.Utf8String.class)
                    DynamicArray<Utf8String> RejectVotes,
            Uint Status,
            Utf8String Extra,
            Uint CreatedBlockNumber,
            Uint EffectiveBlockNumber) {
        super(
                ID,
                Type,
                Strategy,
                Proposer,
                Title,
                Desc,
                BlockNumber,
                TotalVotes,
                PassVotes,
                RejectVotes,
                Status,
                Extra,
                CreatedBlockNumber,
                EffectiveBlockNumber);
        this.ID = ID.getValue();
        this.Type = Type.getValue();
        this.Strategy = Strategy.getValue();
        this.Proposer = Proposer.getValue();
        this.Title = Title.getValue();
        this.Desc = Desc.getValue();
        this.BlockNumber = BlockNumber.getValue();
        this.TotalVotes = TotalVotes.getValue();
        this.Status = Status.getValue();
        this.Extra = Extra.getValue();
        this.CreatedBlockNumber = CreatedBlockNumber.getValue();
        this.EffectiveBlockNumber = EffectiveBlockNumber.getValue();
        this.PassVotes =
                PassVotes.getValue().stream()
                        .map(Utf8String::getValue)
                        .collect(Collectors.toList());
        this.RejectVotes =
                RejectVotes.getValue().stream()
                        .map(Utf8String::getValue)
                        .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "{"
                + "ID="
                + ID
                + ", Type="
                + Type
                + ", Strategy="
                + Strategy
                + ", Proposer='"
                + Proposer
                + '\''
                + ", PassVotes="
                + PassVotes
                + ", RejectVotes="
                + RejectVotes
                + ", Title='"
                + Title
                + '\''
                + ", Desc='"
                + Desc
                + '\''
                + ", BlockNumber="
                + BlockNumber
                + ", TotalVotes="
                + TotalVotes
                + ", Status="
                + Status
                + ", Extra='"
                + Extra
                + '\''
                + ", CreatedBlockNumber="
                + CreatedBlockNumber
                + ", EffectiveBlockNumber="
                + EffectiveBlockNumber
                + '}';
    }
}
