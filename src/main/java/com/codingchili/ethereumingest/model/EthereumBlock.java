package com.codingchili.ethereumingest.model;

import com.codingchili.core.storage.Storable;
import org.web3j.protocol.core.methods.response.EthBlock;

import static com.codingchili.ethereumingest.importer.ApplicationContext.timestampFrom;

/**
 * Contains block data.
 */
public class EthereumBlock implements Storable {
    private String hash;
    private Long number;
    private Long size;
    private String difficulty;
    private String author;
    private String miner;
    private Integer txcount;
    private String timestamp;

    public EthereumBlock(EthBlock.Block block) {
        this.number = block.getNumber().longValue();
        this.hash = block.getHash();
        this.difficulty = block.getDifficultyRaw();
        this.author = block.getAuthor();
        this.miner = block.getMiner();
        this.timestamp = timestampFrom(block.getTimestamp().longValue());
        this.size = block.getSize().longValue();
        this.txcount = block.getTransactions().size();
    }

    @Override
    public String id() {
        return hash;
    }

    public Integer getTxcount() {
        return txcount;
    }

    public void setTxcount(Integer txcount) {
        this.txcount = txcount;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }
}
