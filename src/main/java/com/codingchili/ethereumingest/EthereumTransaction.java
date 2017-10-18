package com.codingchili.ethereumingest;

import com.codingchili.core.storage.Storable;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;

public class EthereumTransaction extends Transaction implements Storable {
    public static final String ONE_BILLION = "1000000000";
    private String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String id() {
        return this.getTransactionIndexRaw();
    }

    @Override
    public BigInteger getNonce() {
        return new BigInteger("0");
    }

    @Override
    public BigInteger getBlockNumber() {
        return bigFromHex(getBlockNumberRaw());
    }

    @Override
    public BigInteger getTransactionIndex() {
        return bigFromHex(getTransactionIndexRaw());
    }

    @Override
    public BigInteger getGas() {
        return bigFromHex(getGasRaw());
    }

    @Override
    public BigInteger getGasPrice() {
        return bigFromHex(getGasPriceRaw());
    }

    @Override
    public BigInteger getValue() {
        return bigFromHex(getValueRaw());
    }

    private static BigInteger bigFromHex(String hex) {
        if (hex != null && hex.length() > 2) {
            BigInteger resolved = new BigInteger(hex.substring(2), 16);
            return BigInteger.valueOf(resolved.divide(new BigInteger(ONE_BILLION)).longValue());
        } else {
            return new BigInteger("0");
        }
    }
}