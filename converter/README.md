# RankLens dataset generator

See the dataset [description](../README.md) for details about the dataset itself. This repo has a set of tools to
process the original input data into the final dataset.

## Required data

This set of tools depends on:
* MovieLens 25M dataset available [here](https://grouplens.org/datasets/movielens/25m/)
* [TMDB api key](https://developers.themoviedb.org/3/getting-started/introduction) to pull the movie metadata

## Running the script

Download the TMDB metadata for movies (it may take 20-30 minutes as it's done single-thread to reduce stress for the API):
```shell
sbt "runMain ai.metarank.ranklens.TmdbTool <movielens 25m dir> <5000> <tmdb api> <out dir>"
```

It will emit a set of `tmdb_<something>.json` files for each type of content in the `<out dir>` directory on disk.

Then run the building script:
```shell
sbt "runMain ai.metarank.ranklens.Main <movielens 25m dir> <tmdb dir> <out file>"
```

It will dump the final dataset into a JSONL-formatted file.