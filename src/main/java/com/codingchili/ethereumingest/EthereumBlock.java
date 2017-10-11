package com.codingchili.ethereumingest;

import com.codingchili.core.storage.Storable;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
    private String timestamp;

    public EthereumBlock(EthBlock.Block block) {
        this.number = block.getNumber().longValue();
        this.hash = block.getHash();
        this.difficulty = block.getDifficultyRaw();
        this.author = block.getAuthor();
        this.miner = block.getMiner();
        this.timestamp = ZonedDateTime.ofInstant(Instant.ofEpochSecond(block.getTimestamp().intValue()),
                ZoneId.systemDefault()).toOffsetDateTime().toString();
        this.size = block.getSize().longValue();
    }

    @Override
    public String id() {
        return hash;
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
