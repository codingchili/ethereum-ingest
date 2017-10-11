package com.codingchili.ethereumingest;

import com.codingchili.core.configuration.Configurable;
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
    private String path = "application.config";
    private StorageType storage = ELASTICSEARCH;
    private String ipc = "\\\\.\\pipe\\geth.ipc";
    private OSType os = WINDOWS;
    private String index = "ethereum-ingest";

    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
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

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
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

    public enum OSType {
        UNIX, WINDOWS
    }

    public enum StorageType {
        MONGODB,
        ELASTICSEARCH,
        HAZELCAST,
        SQLITE,
        MEMORY
    }
}
