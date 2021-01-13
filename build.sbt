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

name := "avrodump"

version := "0.1"

scalaVersion := "2.12.13"

libraryDependencies ++= List(
  "org.apache.spark" %% "spark-sql" % "2.4.7",
  "org.apache.spark" %% "spark-avro" % "2.4.7",
  "org.rocksdb" % "rocksdbjni" % "6.15.2"
)
