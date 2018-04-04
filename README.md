# ethereum-ingest [![Build Status](https://travis-ci.org/codingchili/ethereum-ingest.svg?branch=master)](https://travis-ci.org/codingchili/ethereum-ingest)
Imports blocks and transactions from the Ethereum blockchain into ElasticSearch, MongoDB, Hazelcast, CQEngine and SQLite!

Demo video: [YouTube](https://www.youtube.com/watch?v=FFI9OnW9IuI)

![ethereum ingest GUI](https://raw.githubusercontent.com/codingchili/ethereum-ingest/master/eth-ingest-gui.png)

Tested with
- ElasticSeach 6.2.3
- MongoDB 3.4.10
- HazelCast 3.6.3
- geth 1.8.2

### Building
Build with
```
./gradlew jar
```
Requires chili-core through jitpack or local repo.

### Importing
The first step is to start your ethereum IPC client, for geth use:
```
geth --rpcapi personal,db,eth,net,web3 --rpc --testnet
```

Start the importer with:
```
java -jar <filename>.jar --import
java -jar <filename>.jar --gui
java -jar <filename>.jar --help
```
* --import: starts an import using application.json.
* --gui: starts the application with the graphical user interface.

### Configuring
Set configuration in application.json before running --import. WHen using the graphical application the configuration is saved automatically.

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

### Contributing
Submit an issue or a PR ! :blue_heart:
