# ethereum-ingest [![Build Status](https://travis-ci.org/codingchili/ethereum-ingest.svg?branch=master)](https://travis-ci.org/codingchili/ethereum-ingest)
Ingests blocks and transactions from the Ethereum blockchain into ElasticSearch, MongoDB, Hazelcast, CQEngine and SQLite!

Tested with
- ElasticSeach 5.6.2
- MongoDB 3.10
- HazelCast 3.6.3
- geth 1.7.1.

Build with
```
gradle jar
```
Requires chili-core through jitpack or local repo.

Start geth with rpc enabled
```
geth --rpcapi personal,db,eth,net,web3 --rpc --testnet
```

Run with
```
java -jar <filename>.jar
```

Set configuration in application.json.

Default configuration
```
{
  "startBlock" : "1964770",
  "blockEnd" : "1964900",
  "storage" : "ELASTICSEARCH",
  "ipc" : "\\\\.\\pipe\\geth.ipc",
  "os" : "WINDOWS",
  "txIndex" : "ether-tx-demo-iix",
  "blockIndex" : "etdder-block-demo-iix",
  "backpressureBlocks" : 6,
  "backPressureTx" : 32,
  "txImport" : true,
  "blockImport" : true
}
```
Backpressure for blocks and tx should not exceed 200 when multiplied with eachother. If you want to
increase these values further you need to make sure the storage is capable of handling that many connections.

Storage can be any of the following
- MONGODB
- ELASTICSEARCH
- HAZELCAST
- SQLITE
- MEMORY

os can be any of the following, required for ipc to work correctly
- UNIX
- WINDOWS

Imports can be executed multiple times over the same block range without resulting in duplicates.