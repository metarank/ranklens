package ai.metarank.ranklens.dataset

import ai.metarank.ranklens.dataset.Tmdb.{Cast, MovieCast, MovieImages, MovieMetadata, MovieTranslations, Translation}
import better.files.File
import io.circe.{Codec, Decoder}
import io.circe.generic.semiauto.deriveCodec
import io.circe.syntax._
import org.apache.commons.io.IOUtils
import io.circe.parser._

import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

case class Tmdb(
    movies: List[MovieMetadata],
    cast: List[MovieCast],
    images: List[MovieImages],
    translations: List[MovieTranslations]
) {
  val movieMap = movies.map(m => m.id -> m).toMap
  val imdbMap  = movies.map(m => m.imdb_id -> m).toMap
  val castMap  = cast.map(c => c.id -> c).toMap
  def write(dir: File) = {
    writeFile(dir.createChild("tmdb_movies.json.gz", createParents = true), movies.asJson.spaces2SortKeys)
    writeFile(dir.createChild("tmdb_cast.json.gz", createParents = true), cast.asJson.spaces2SortKeys)
    writeFile(dir.createChild("tmdb_images.json.gz", createParents = true), images.asJson.spaces2SortKeys)
    writeFile(dir.createChild("tmdb_translations.json.gz", createParents = true), translations.asJson.spaces2SortKeys)
  }

  private def writeFile(file: File, content: String) = {
    val stream = new GZIPOutputStream(file.newFileOutputStream())
    stream.write(content.getBytes(StandardCharsets.UTF_8))
    stream.close()
  }
}

object Tmdb {
  case class Genre(id: Int, name: String)
  case class MovieMetadata(
      budget: Long,
      genres: List[Genre],
      id: Int,
      imdb_id: String,
      original_title: String,
      overview: String,
      popularity: Double,
      poster_path: Option[String],
      release_date: String,
      revenue: Long,
      runtime: Int,
      vote_average: Double,
      vote_count: Long
  )
  case class MovieTranslations(id: Int, translations: List[Translation])
  case class Translation(iso_3166_1: String, iso_639_1: String, data: TranslationData)
  case class TranslationData(title: Option[String], overview: String)

  case class MovieCast(id: Int, cast: List[Cast], crew: List[Cast])
  case class Cast(
      gender: Int,
      id: Int,
      known_for_department: String,
      name: String,
      popularity: Double,
      job: Option[String],
      character: Option[String]
  )

  case class Backdrop(height: Int, iso_639_1: Option[String], file_path: String, width: Int)
  case class MovieImages(id: Int, backdrops: List[Backdrop], logos: List[Backdrop])

  implicit val translationDataCodec: Codec[TranslationData] = deriveCodec
  implicit val translationCodec: Codec[Translation]         = deriveCodec
  implicit val movieTransCodec: Codec[MovieTranslations]    = deriveCodec
  implicit val genreCodec: Codec[Genre]                     = deriveCodec
  implicit val movieCodec: Codec[MovieMetadata]             = deriveCodec
  implicit val castCodec: Codec[Cast]                       = deriveCodec
  implicit val movieCastCodec: Codec[MovieCast]             = deriveCodec
  implicit val backdropCodec: Codec[Backdrop]               = deriveCodec
  implicit val imagesCodec: Codec[MovieImages]              = deriveCodec

  val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def load(dir: File) = for {
    movies <- read[List[MovieMetadata]](dir / "tmdb_movies.json.gz")
    cast   <- read[List[MovieCast]](dir / "tmdb_cast.json.gz")
    images <- read[List[MovieImages]](dir / "tmdb_images.json.gz")
    trans  <- read[List[MovieTranslations]](dir / "tmdb_translations.json.gz")
  } yield {
    Tmdb(movies, cast, images, trans)
  }

  def read[T: Decoder](file: File): Either[Throwable, T] = {
    val stream = new GZIPInputStream(file.newFileInputStream)
    val json   = IOUtils.toString(stream, StandardCharsets.UTF_8)
    decode[T](json)
  }

}
