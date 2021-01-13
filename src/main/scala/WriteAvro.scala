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

import org.apache.avro.io.EncoderFactory
import org.apache.avro.reflect.{ReflectData, ReflectDatumWriter}

import java.io.ByteArrayOutputStream

/**
 * Example script to write avro entry using default encoder. It showed that output is different with Spark-avro output.
 */
object WriteAvro extends App {
  val entry = Entry("row1", 2L, 3.0, "4", 5)
  val writer = new ReflectDatumWriter[Entry](classOf[Entry], ReflectData.AllowNull.get())
  val os = new ByteArrayOutputStream()
  val encoder = EncoderFactory.get().binaryEncoder(os, null)

  writer.write(entry, encoder)
  // {"type":"record","name":"Entry","fields":[{"name":"id","type":["null","string"],"default":null},{"name":"value1","type":"long"},{"name":"value2","type":"double"},{"name":"value3","type":["null","string"],"default":null},{"name":"value4","type":"int"}]}

  encoder.flush()
  println(os.toByteArray.mkString("Array(", ", ", ")"))
}
