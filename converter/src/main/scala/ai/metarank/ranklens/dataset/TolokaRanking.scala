package ai.metarank.ranklens.dataset

import ai.metarank.ranklens.dataset.TolokaRanking.Task
import better.files.File
import io.circe.Codec
import org.apache.commons.io.IOUtils

import scala.collection.JavaConverters._
import java.nio.charset.StandardCharsets
import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream
import io.circe.generic.semiauto._
import io.circe.syntax._

case class TolokaRanking(tasks: List[Task]) {
  def write(file: File) = {
    val lines = tasks.map(t => t.asJson.noSpacesSortKeys)
    file.write(lines.mkString("\n"))
  }
}

object TolokaRanking {
  val tsFormat = DateTimeFormatter.ISO_DATE_TIME
  case class Task(ts: Long, id: Int, user: String, shown: List[Int], liked: List[Int])

  implicit val taskCodec: Codec[Task] = deriveCodec

  def apply(file: File) = {
    val stream     = new GZIPInputStream(file.newFileInputStream)
    val lines      = IOUtils.lineIterator(stream, StandardCharsets.UTF_8).asScala
    val header     = lines.next().split('\t').zipWithIndex.toMap
    val headerSize = header.size
    val results = for {
      line <- lines.toList
      cols = line.split('\t') if (cols.length == headerSize)
    } yield {
      val ids = for {
        id    <- 0 until 24
        index <- header.get(s"INPUT:id_$id")
      } yield {
        cols(index).toInt
      }
      val liked = for {
        id    <- 0 until 24
        index <- header.get(s"OUTPUT:result_$id")
        if cols(index) == "true"
      } yield {
        index
      }
      val task = cols(header("INPUT:task"))
      val ts   = cols(header("ASSIGNMENT:started"))
      val user = cols(header("ASSIGNMENT:worker_id"))
      Task(
        ts = LocalDateTime.parse(ts, tsFormat).toEpochSecond(ZoneOffset.UTC),
        id = task.toInt,
        user = user,
        shown = ids.toList,
        liked = liked.toList
      )
    }
    stream.close()
    new TolokaRanking(results)
  }
}
