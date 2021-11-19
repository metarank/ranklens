package ai.metarank.ranklens.dataset

import ai.metarank.ranklens.dataset.RankLens.Movie
import ai.metarank.ranklens.dataset.TolokaRanking.Task
import better.files.File
import io.circe.{Codec, Decoder}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.parser._

import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

case class RankLens(movies: List[Movie], actions: List[Task]) {
  def write(dir: File) = {
    val movieStream = dir.createChild("metadata.jsonl", createParents = true).newFileOutputStream()
    movies.foreach(m => movieStream.write((m.asJson.noSpacesSortKeys + "\n").getBytes(StandardCharsets.UTF_8)))
    movieStream.close()

    val actionStream = dir.createChild("ranking.jsonl", createParents = true).newFileOutputStream()
    actions.foreach(a => actionStream.write((a.asJson.noSpacesSortKeys + "\n").getBytes(StandardCharsets.UTF_8)))
    actionStream.close()
  }
}

object RankLens {
  case class Movie(
      id: Int,
      tmdbId: Int,
      title: String,
      budget: Long,
      genres: List[Genre],
      overview: String,
      tmdbPopularity: Double,
      tmdbVoteCount: Long,
      tmdbVoteAverage: Double,
      releaseDate: String,
      revenue: Double,
      runtime: Int,
      topActors: List[Cast],
      director: Option[Cast],
      writer: Option[Cast],
      tags: List[String],
      poster: Option[String]
  )
  case class Cast(id: Int, name: String, gender: Int, popularity: Double)
  case class Genre(id: Int, name: String)

  implicit val genreCodec: Codec[Genre] = deriveCodec
  implicit val castCodec: Codec[Cast]   = deriveCodec
  implicit val movieCodec: Codec[Movie] = deriveCodec

  def apply(ml: MovieLens, tmdb: Tmdb, toloka: TolokaRanking) = {
    val movies = for {
      mlMovie   <- ml.movies
      tmdbId    <- mlMovie.tmdb
      tmdbMovie <- tmdb.movieMap.get(tmdbId).orElse(tmdb.imdbMap.get(mlMovie.imdb.get))
      movieCast <- tmdb.castMap.get(tmdbId)
    } yield {
      Movie(
        id = mlMovie.id,
        tmdbId = tmdbId,
        title = tmdbMovie.original_title,
        genres = tmdbMovie.genres.map(g => Genre(g.id, g.name)),
        budget = tmdbMovie.budget,
        overview = tmdbMovie.overview,
        tmdbPopularity = tmdbMovie.popularity,
        tmdbVoteCount = tmdbMovie.vote_count,
        tmdbVoteAverage = tmdbMovie.vote_average,
        releaseDate = LocalDate.parse(tmdbMovie.release_date, Tmdb.dateFormat).format(DateTimeFormatter.ISO_DATE),
        revenue = tmdbMovie.revenue,
        runtime = tmdbMovie.runtime,
        topActors = movieCast.cast
          .map(c => Cast(c.id, c.name, c.gender, c.popularity))
          .sortBy(-_.popularity)
          .take(3),
        director = movieCast.crew
          .filter(_.job.contains("Director"))
          .sortBy(-_.popularity)
          .headOption
          .map(c => Cast(c.id, c.name, c.gender, c.popularity)),
        writer = movieCast.crew
          .filter(_.job.contains("Screenplay"))
          .sortBy(-_.popularity)
          .headOption
          .map(c => Cast(c.id, c.name, c.gender, c.popularity)),
        tags = mlMovie.tags,
        poster = tmdbMovie.poster_path.map(suffix => "https://image.tmdb.org/t/p/original" + suffix)
      )
    }
    val rankedMovies = toloka.tasks.flatMap(_.shown).toSet
    new RankLens(movies.filter(id => rankedMovies.contains(id.id)), toloka.tasks)
  }

  def read(dir: File) = {
    val movies  = parseJSONL[Movie](dir / "metadata.jsonl")
    val actions = parseJSONL[Task](dir / "ranking.jsonl")
    new RankLens(movies, actions)
  }

  private def parseJSONL[T: Decoder](file: File): List[T] = {
    file.lineIterator
      .map(line => decode[T](line))
      .foldLeft(List.empty[T])((acc, result) =>
        result match {
          case Left(err) =>
            println(s"cannot parse: $err")
            acc
          case Right(value) => value +: acc
        }
      )
  }
}
