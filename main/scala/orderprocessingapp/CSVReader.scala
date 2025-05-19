package orderprocessingapp

import scala.io.{BufferedSource, Source}

object CSVReader {
  def readCSV(filepath: String): List[String] = {
    val source: BufferedSource = Source.fromFile(filepath)
    val lines: List[String] = source.getLines().drop(1).toList.tail
    source.close()
    lines
  }
}
