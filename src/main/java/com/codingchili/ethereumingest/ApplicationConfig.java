package com.codingchili.ethereumingest;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.storage.ElasticMap;
import com.codingchili.core.storage.HazelMap;
import com.codingchili.core.storage.IndexedMapPersisted;
import com.codingchili.core.storage.IndexedMapVolatile;
import com.codingchili.core.storage.MongoDBMap;

import static com.codingchili.ethereumingest.ApplicationConfig.OSType.WINDOWS;
import static com.codingchili.ethereumingest.ApplicationConfig.StorageType.ELASTICSEARCH;

/**
 * Representation of configuration file.s
 */
public class ApplicationConfig implements Configurable {
    private static String path = "application.json";
    private String startBlock = "0";
    private StorageType storage = ELASTICSEARCH;
    private String ipc = "\\\\.\\pipe\\geth.ipc";
    private OSType os = WINDOWS;
    private String txIndex = "eth-tx";
    private String blockIndex = "eth-block";
    private Long backpressure = Long.MAX_VALUE;
    private boolean txImport = true;

    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public boolean isTxImport() {
        return txImport;
    }

    public void setTxImport(boolean txImport) {
        this.txImport = txImport;
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

    public Class getStoragePlugin() {
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
        MEMORY,
        JSONFILE
    }
}
