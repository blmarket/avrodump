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
import org.apache.avro.io.{BinaryDecoder, DecoderFactory}
import org.apache.avro.reflect.ReflectDatumReader
import org.rocksdb.RocksDB

import java.nio.charset.StandardCharsets
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Random, Try}

object MThreadReadRocks extends App {
  val schemaJson = "{\"type\":\"record\",\"name\":\"topLevelRecord\",\"fields\":[{\"name\":\"id\",\"type\":[\"string\",\"null\"]},{\"name\":\"value1\",\"type\":\"long\"},{\"name\":\"value2\",\"type\":\"double\"},{\"name\":\"value3\",\"type\":[\"string\",\"null\"]},{\"name\":\"value4\",\"type\":\"int\"}]}"
  val avroSchema = new Schema.Parser().parse(schemaJson)

  val tasks = Range(0, 10).map(id => {
    Future {
      val db = RocksDB.openReadOnly("/tmp/db")
      val reader = new ReflectDatumReader[Entry](classOf[Entry])
      reader.setSchema(avroSchema) // Spark generated schema is different with schema generated from Avro

      for (i <- Range(1, 300001)) {
        val keyStr = Random.nextInt(10000000).toString
        val key = keyStr.getBytes(StandardCharsets.UTF_8)
        val bytes = db.get(key)
        var decoder: BinaryDecoder = null
        decoder = DecoderFactory.get().binaryDecoder(bytes, decoder)
        val item = reader.read(Entry(null, 0L, 0.0, "", 0), decoder)

        require(item.id.equals(keyStr))

        if ((i % 10000) == 0) {
          println(s"$id $i $item")
        }
      }
      db.closeE()
      id
    }
  })

  println(Try(tasks.foreach(fut => {
    Await.result(fut, Duration.Inf)
  })))
}
