package ai.metarank.ranklens

import better.files.File
import TolokaDatasetGenerator._
import ai.metarank.ranklens.dataset.MovieLens
import ai.metarank.ranklens.dataset.Tmdb._
import cats.effect.{ExitCode, IO, IOApp}
import io.circe.{Codec, Encoder}
import io.circe.generic.semiauto._
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe._
import org.http4s.client.Client
import cats.implicits._
import io.circe.syntax._

import scala.concurrent.duration._

/** A TMDB database export tool. Exports:
  * - movie metadata
  * - cast
  * - localized titles
  * - posters
  */

object TmdbTool extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = args match {
    case movielensDir :: topN :: key :: outDir :: Nil =>
      val ml  = MovieLens(File(movielensDir), FILM_POPULARITY_THRESHOLD, TAGS_PER_MOVIE, FILM_POPULARITY_THRESHOLD)
      val top = ml.movies.sortBy(-_.ratings).take(topN.toInt).flatMap(_.tmdb)
      println(s"selected $topN movies to fetch metadata")
      for {
        endpoint <- IO.fromEither(Uri.fromString("https://api.themoviedb.org"))
        result <- BlazeClientBuilder[IO]
          .withMaxTotalConnections(10)
          .withMaxWaitQueueLimit(50000)
          .withRequestTimeout(1000.second)
          .resource
          .use(client =>
            for {
              tmdb <- IO(TmdbClient(key, client, endpoint))
              meta <- top.zipWithIndex.map(m => tmdb.movie(m._1).flatTap(_ => IO(println(s"meta=$m")))).parSequence
              translation <- top.zipWithIndex
                .map(m => tmdb.translation(m._1).flatTap(_ => IO(println(s"trans=$m"))))
                .parSequence
              cast   <- top.zipWithIndex.map(m => tmdb.credits(m._1).flatTap(_ => IO(println(s"cast=$m")))).parSequence
              images <- top.zipWithIndex.map(m => tmdb.images(m._1).flatTap(_ => IO(println(s"img=$m")))).parSequence
            } yield {
              (meta.flatten, translation.flatten, cast.flatten, images.flatten)
            }
          )
      } yield {
        val (meta, translation, cast, images) = result
        val dir                               = File(outDir)
        val metaFile                          = dir.createChild("tmdb_movies.json", createParents = true)
        metaFile.write(meta.asJson.spaces2SortKeys)
        val transFile = dir.createChild("tmdb_translations.json", createParents = true)
        transFile.write(translation.asJson.spaces2SortKeys)
        val castFile = dir.createChild("tmdb_cast.json", createParents = true)
        castFile.write(cast.asJson.spaces2SortKeys)
        val imagesFile = dir.createChild("tmdb_images.json", createParents = true)
        imagesFile.write(images.asJson.spaces2SortKeys)
        println(s"wrote $metaFile")
        ExitCode.Success
      }
    case _ =>
      IO.raiseError(new IllegalArgumentException("expected: TmdbTool <movielens dir> <topN> <tmdb key> <out dir>"))

  }

  case class TmdbClient(key: String, client: Client[IO], endpoint: Uri) {
    implicit val movieJson       = jsonOf[IO, Option[MovieMetadata]]
    implicit val translationJson = jsonOf[IO, Option[MovieTranslations]]
    implicit val castJson        = jsonOf[IO, Option[MovieCast]]
    implicit val imagesJson      = jsonOf[IO, Option[MovieImages]]

    def movie(id: Int) = {
      val path = (endpoint / "3" / "movie" / id.toString).withQueryParam("api_key", key)
      client.expect[Option[MovieMetadata]](path).handleError(err(_, id))
    }

    def translation(id: Int) = {
      val path = (endpoint / "3" / "movie" / id.toString / "translations").withQueryParam("api_key", key)
      client.expect[Option[MovieTranslations]](path).handleError(err(_, id))
    }

    def credits(id: Int) = {
      val path = (endpoint / "3" / "movie" / id.toString / "credits").withQueryParam("api_key", key)
      client.expect[Option[MovieCast]](path).handleError(err(_, id))
    }

    def images(id: Int) = {
      val path = (endpoint / "3" / "movie" / id.toString / "images").withQueryParam("api_key", key)
      client.expect[Option[MovieImages]](path).handleError(err(_, id))
    }

    def err[T](e: Throwable, id: Int): Option[T] = {
      println(s"cannot get $id/credits: ${e.getMessage}")
      None
    }
  }
}
