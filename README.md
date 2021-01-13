Avrodump
--------

Write-once read-many key-value storage build and accessor proof-of-concept.

## Write-once

Code implemented at Main.scala

Used apache spark and case class for model. Possibly it should support all
arbitrary data types which spark Encoder supports. It is designed to parallelize
workload using spark partitions. Within each partition it will sort by key,
write to single SST file. After writing all SST files, driver will create a
database and collect all SST files to create single RocksDB instance. Driver is
the only bottleneck, 

## Read-many

Code implemented at MThreadReadRocks.scala

Thanks to readonly open, it's possible to use multiple threads to open single
database. This enables scalability, or even allows serverless(with use of shared
file system like EFS). From the example code 10 concurrent threads will run to
open databases, deserialize entry read from RocksDB, verify it's expected value.

## License

AGPL-3.0-or-later, See COPYING

## TODO

* [ ] Provide another example leveraging Jackson, maybe for easier API?
* [ ] How to store/load Avro schema (in JSON format)? Currently it's hardcoded.
* [ ] Compaction on driver is running on single thread. Do we need to
      [parallelize it](https://rocksdb.org/blog/2016/01/29/compaction_pri.html)?
