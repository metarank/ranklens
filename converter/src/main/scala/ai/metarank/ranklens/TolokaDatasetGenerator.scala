package ai.metarank.ranklens

import ai.metarank.ranklens.dataset.MovieLens
import ai.metarank.ranklens.dataset.MovieLens.Movie
import better.files.File
import io.circe.{Decoder, Encoder, Json, JsonObject}
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._

import scala.util.{Random, Try}

object TolokaDatasetGenerator {
  val NOT_OLDER_THAN_YEAR       = 1980
  val FILM_POPULARITY_THRESHOLD = 1000
  val TAGS_PER_MOVIE            = 10
  val FILMS_PER_LISTING         = 24

  case class TmdbResponse(id: String, tmdbid: String, title: String, image_url: String, ru_title: String)
  implicit val tmdbDecoder: Decoder[TmdbResponse] = deriveDecoder

  case class Task(id: Int, tag: String, movies: List[Movie]) {
    def asString = {
      val pairs = for {
        movie <- movies
      } yield {
        List(
          movie.id.toString,
          movie.titleRu.getOrElse(movie.title),
          movie.poster.getOrElse("")
        )
      }
      (id.toString +: pairs.flatten).mkString("\t")
    }
  }
  object Task {
    def header: String =
      (List("INPUT:task") ++ (0 until 24).flatMap(i => List(s"INPUT:id_$i", s"INPUT:title_ru_$i", s"INPUT:image_$i")))
        .mkString("\t")
  }

  case class Field(`type`: String, hidden: Boolean, required: Boolean)
  implicit val enc: Encoder[Field] = deriveEncoder

  val TAG_BLACKLIST = Set(
    "based on a book",
    "based on novel or book",
    "duringcreditsstinger",
    "nudity (topless)",
    "based on a true story",
    "aftercreditsstinger",
    "quirky",
    "woman director",
    "bittersweet",
    "true story",
    "clv",
    "predictable",
    "visually appealing",
    "boring",
    "inspirational",
    "great soundtrack",
    "los angeles"
  )

  def main(args: Array[String]): Unit = {
    val fields = (for {
      i <- 0 until FILMS_PER_LISTING
    } yield {
      List(
        s"id_$i"       -> Field("string", false, true),
        s"image_$i"    -> Field("url", false, true),
        s"title_ru_$i" -> Field("string", false, true)
      )
    }).flatten.toList
    val json = (fields ++ List("task" -> Field("string", false, true))).toMap.asJson.noSpacesSortKeys
    val br   = 1
    args.toList match {
      case dir :: Nil =>
        val movies      = MovieLens(File(dir), NOT_OLDER_THAN_YEAR, TAGS_PER_MOVIE, FILM_POPULARITY_THRESHOLD).movies
        val popularTags = loadPopularTags(movies)
        var id          = 0
        val tasks = for {
          tag <- popularTags
          _   <- 0 until 100
        } yield {
          id += 1
          Task(id, tag, chooseMovies(movies, tag))
        }
        val targets = movies.map(m => s"""${m.id},${m.tmdb.map(_.toString).getOrElse("")},"${m.title}"""")
        File("/tmp/targets.csv").write(targets.mkString("\n"))
        File("/tmp/tasks.csv").write(Task.header + "\n" + tasks.map(_.asString).mkString("\n"))
      case _ => println("usage: TolokaDatasetGenerator <movielens dir>")
    }
  }

  def chooseMovies(movies: List[Movie], tag: String) = {
    val candidates = movies.filter(_.tags.contains(tag)).sortBy(-_.ratings).take(100)
    Random.shuffle(candidates).take(24)
  }

  def loadPopularTags(movies: List[Movie]) = {
    movies
      .flatMap(_.tags)
      .groupBy(identity)
      .map { case (tag, cnt) =>
        tag -> cnt.size
      }
      .toList
      .sortBy(-_._2)
      .map(_._1)
      .filter(x => !TAG_BLACKLIST.contains(x))
      .take(100)
  }

}
