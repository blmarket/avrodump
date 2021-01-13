/*
 *  This file is part of Avrodump.
 *
 *  Avrodump is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Avrodump is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero Public License for more details.
 *
 *  You should have received a copy of the GNU Affero Public License
 *  along with Avrodump.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.apache.avro.Schema
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.avro.SchemaConverters
import org.rocksdb._

import java.nio.charset.StandardCharsets

object Main extends App {
  val spark = SparkSession.builder()
    .appName("test")
    .master("local[*]")
    .getOrCreate()
  spark.sparkContext.setLogLevel("WARN")

  import org.apache.spark.sql.avro.to_avro
  import org.apache.spark.sql.functions._
  import spark.implicits._

  val df = spark.range(10000000L).map(id =>
    Entry(id.toString, id, (id * 2).toDouble, id.toString.reverse, (id / 10).toInt)
  ).orderBy(rand()) // Shuffle to make sure keys are unordered

  val avroSchema: Schema = SchemaConverters.toAvroType(df.schema)

  println(avroSchema)

  // pray to_avro generates same Avro schema.
  val ds = df.select($"id".as[String], to_avro(struct($"*")).as[Array[Byte]])
    .sortWithinPartitions($"id")
  val ds2 = ds.rdd.mapPartitionsWithIndex((idx, part) => {
    val writer = new SstFileWriter(new EnvOptions(), new Options())
    val value1 = s"/tmp/sst$idx"
    writer.open(value1)
    part.foreach((row: (String, Array[Byte])) => {
      val key = row._1.getBytes(StandardCharsets.UTF_8)
      val value = row._2
      writer.put(key, value)
    })
    writer.finish()
    writer.close()

    Seq(value1).toIterator
  })

  val opts = new Options()
  val db = RocksDB.open(opts, "/tmp/db") // Basically new records will upsert.

  import scala.collection.JavaConverters._

  private val ingestOpts = new IngestExternalFileOptions()
  ingestOpts.setMoveFiles(true)
  ingestOpts.setAllowBlockingFlush(true)
  db.ingestExternalFile(ds2.collect().toList.asJava, ingestOpts)

  db.compactRange()
  db.syncWal()
  db.closeE()
}
