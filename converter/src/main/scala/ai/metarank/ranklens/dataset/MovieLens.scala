package ai.metarank.ranklens.dataset

import ai.metarank.ranklens.dataset.MovieLens.Movie
import better.files.File

import scala.util.Try

case class MovieLens(movies: List[Movie]) {
  val movieMap = movies.map(m => m.id -> m).toMap
}

object MovieLens {
  case class Movie(
      id: Int,
      title: String,
      year: Option[Int],
      genres: List[String],
      tmdb: Option[Int] = None,
      imdb: Option[String] = None,
      tags: List[String] = Nil,
      ratings: Int = 0,
      score: Double = 0.0,
      poster: Option[String] = None,
      titleRu: Option[String] = None
  )
  object Movie {
    val yearPattern = """\(([0-9]+)\)""".r
    def fromLine(line: String) = {
      val c1       = line.indexOf(',')
      val c2       = line.lastIndexOf(',')
      val id       = line.take(c1)
      val genres   = line.drop(c2 + 1).split('|').map(_.toLowerCase)
      val titleRaw = line.substring(c1 + 1, c2).replaceAll("\"", "")
      val title    = yearPattern.replaceAllIn(titleRaw, "").trim
      val year = yearPattern.findFirstMatchIn(titleRaw) match {
        case Some(m) => Some(m.group(1).toInt)
        case _       => None
      }
      Movie(
        id = id.toInt,
        title = title,
        year = year,
        genres = genres.toList
      )
    }
  }
  case class Rating(count: Int, avg: Double)
  case class Link(tmdb: Int, imdb: String)

  def apply(dir: File, yearThreshold: Int, tagsPerMovie: Int, popThreshold: Int): MovieLens = {
    val movies =
      File(dir + "/movies.csv").lineIterator
        .drop(1)
        .map(Movie.fromLine)
        .toList
        .filter(_.year.getOrElse(0) >= yearThreshold)
    println("loaded movies.csv")
    val tags = File(dir + "/tags.csv").lineIterator
      .drop(1)
      .map(line => {
        val tok = line.split(',')
        tok(1).toInt -> tok(2).toLowerCase.trim
      })
      .toList
      .groupBy(_._1)
      .map {
        case (id, tags) => {
          val movieTags = tags
            .map(_._2)
            .groupBy(identity)
            .map { case (tag, values) =>
              tag -> values.size
            }
            .toList
            .sortBy(-_._2)
          val tagThreshold = movieTags.head._2 / 5.0
          val out = movieTags
            .filter(_._2 > tagThreshold)
            .take(tagsPerMovie)
            .map(_._1)
          if (id == 1) {
            val br = 1
          }
          id -> out
        }
      }
    println("loaded tags.csv")

    val ratings = File(dir + "/ratings.csv").lineIterator
      .drop(1)
      .map(line => {
        val tokens = line.split(',')
        tokens(1).toInt -> tokens(2).toDouble
      })
      .toList
      .groupBy(_._1)
      .map { case (id, ratings) =>
        id -> Rating(ratings.size, ratings.map(_._2).sum / ratings.size)
      }
    println("loaded ratings.csv")
    val links = File(dir + "/links.csv").lineIterator
      .drop(1)
      .flatMap(line => {
        val tokens = line.split(',')
        for {
          id   <- Try(tokens(0).toInt).toOption
          tmdb <- Try(tokens(2).toInt).toOption
          imdb = tokens(1)
        } yield {
          id -> Link(tmdb, imdb)
        }
      })
      .toMap
    println("loaded links.csv")

    val all = for {
      movie <- movies
    } yield {
      movie.copy(
        tmdb = links.get(movie.id).map(_.tmdb),
        imdb = links.get(movie.id).map(_.imdb),
        tags = tags.getOrElse(movie.id, Nil),
        ratings = ratings.get(movie.id).map(_.count).getOrElse(0),
        score = ratings.get(movie.id).map(_.avg).getOrElse(0.0)
      )
    }
    new MovieLens(all.filter(_.ratings > popThreshold))
  }
}
