/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import scala.io.Source
import com.quantifind.charts.Highcharts._

case class Cleanup(time: Double, size: Long, reclaimed: Long)

/**
 * Analyse cleanup log messages
 */
def cleanupMetrics(fileName: String) = {
  val ms = """\((\d+) ms\)"""
  val bytes = """\((-?\d+) bytes\)"""
  val skip = """[^\(]*"""
  val linePattern = (skip + ms + skip + bytes + skip + bytes + ".*").r

  def parse(line: String) = line match {
    case linePattern(time, size, reclaimed) => Cleanup(time.toDouble/1000, size.toLong, reclaimed.toLong)
  }

  Source.fromFile(fileName).getLines |?
          grep! "cleanup completed".r |
          parse
}

/**
  * Plot time and space of a cleanup metric generated by cleanupMetrics
  */
def plotCleanupMetric(metric: List[Cleanup]) {
  line(metric | (_.time))
  legend(List("time"))
  yAxis("seconds")
  title("Cleanup time")
  unhold

  line(metric | (_.size))
  hold
  line(metric | (_.reclaimed))
  legend(List("size", "reclaimed"))
  yAxis("bytes")
  title("Cleanup space")
  unhold
}
