package ai.metarank.ranklens

import ai.metarank.ranklens.TolokaDatasetGenerator.{FILM_POPULARITY_THRESHOLD, NOT_OLDER_THAN_YEAR, TAGS_PER_MOVIE}
import ai.metarank.ranklens.dataset.{MovieLens, RankLens, Tmdb, TolokaRanking}
import better.files.File
import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = args match {
    case mlDir :: tdDir :: tolokaFile :: outDir :: Nil =>
      for {
        toloka    <- IO(TolokaRanking(File(tolokaFile)))
        _         <- IO(println(s"toloka tasks loaded: count=${toloka.tasks.size}"))
        movieLens <- IO(MovieLens(File(mlDir), NOT_OLDER_THAN_YEAR, TAGS_PER_MOVIE, FILM_POPULARITY_THRESHOLD))
        _         <- IO(println(s"movielens dataset loaded: count=${movieLens.movies.size}"))
        tmdb      <- IO.fromEither(Tmdb.load(File(tdDir)))
        _         <- IO(println(s"tmdb dataset loaded: count=${tmdb.movies.size}"))
        rl        <- IO(RankLens(movieLens, tmdb, toloka))
        _         <- IO(rl.write(File(outDir)))
        _         <- IO(println(s"dataset written to $outDir, movies=${rl.movies.size} rankings=${rl.actions.size}"))
      } yield {
        println("done")
        ExitCode.Success
      }
    case _ =>
      IO.raiseError(
        new IllegalArgumentException("expected: <movielens data dir> <tmdb data dir> <toloka file> <out file>")
      )
  }
}
