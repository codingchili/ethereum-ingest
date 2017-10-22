package com.codingchili.ethereumingest.model;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.ElasticMap;
import com.codingchili.core.storage.HazelMap;
import com.codingchili.core.storage.IndexedMapPersisted;
import com.codingchili.core.storage.IndexedMapVolatile;
import com.codingchili.core.storage.MongoDBMap;
import com.fasterxml.jackson.annotation.JsonIgnore;

import static com.codingchili.ethereumingest.model.ApplicationConfig.OSType.WINDOWS;
import static com.codingchili.ethereumingest.model.ApplicationConfig.StorageType.ELASTICSEARCH;

/**
 * Representation of configuration file.s
 */
public class ApplicationConfig implements Configurable {
    private static String path = "application.json";
    private String startBlock = "0";
    private String blockEnd = "1500";
    private StorageType storage = ELASTICSEARCH;
    private String ipc = "\\\\.\\pipe\\geth.ipc";
    private OSType os = WINDOWS;
    private String txIndex = "eth-tx";
    private String blockIndex = "eth-block";
    private Long backpressure = Long.MAX_VALUE;
    private boolean txImport = true;
    private boolean blockImport = true;

    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public boolean isTxImport() {
        return txImport;
    }

    public void setTxImport(boolean txImport) {
        this.txImport = txImport;
    }

    public boolean isBlockImport() {
        return blockImport;
    }

    public void setBlockImport(boolean blockImport) {
        this.blockImport = blockImport;
    }

    public String getBlockEnd() {
        return blockEnd;
    }

    public void setBlockEnd(String blockEnd) {
        this.blockEnd = blockEnd;
    }

    public String getIpc() {
        return ipc;
    }

    public void setIpc(String ipc) {
        this.ipc = ipc;
    }

    public OSType getOs() {
        return os;
    }

    public void setOs(OSType os) {
        this.os = os;
    }

    public StorageType getStorage() {
        return storage;
    }

    public void setStorage(StorageType storage) {
        this.storage = storage;
    }

    public String getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(String startBlock) {
        this.startBlock = startBlock;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        ApplicationConfig.path = path;
    }

    public static ApplicationConfig get() {
        return Configurations.get(path, ApplicationConfig.class);
    }

    public String getBlockIndex() {
        return blockIndex;
    }

    public String getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(String txIndex) {
        this.txIndex = txIndex;
    }

    public void setBlockIndex(String blockIndex) {
        this.blockIndex = blockIndex;
    }

    @JsonIgnore
    public Class<? extends AsyncStorage> getStoragePlugin() {
        switch (storage) {
            case MONGODB:
                return MongoDBMap.class;
            case ELASTICSEARCH:
                return ElasticMap.class;
            case HAZELCAST:
                return HazelMap.class;
            case SQLITE:
                return IndexedMapPersisted.class;
            case MEMORY:
                return IndexedMapVolatile.class;
        }
        throw new IllegalArgumentException("Missing 'storage' in 'application.config'");
    }

    public Long getBackpressure() {
        return backpressure;
    }

    public void setBackpressure(Long backpressure) {
        this.backpressure = backpressure;
    }

    public enum OSType {
        UNIX, WINDOWS;
    }

    public enum StorageType {
        MONGODB,
        ELASTICSEARCH,
        HAZELCAST,
        SQLITE,
        MEMORY
    }
}
