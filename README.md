# RankLens dataset

This dataset is a MovieLens-based crowd-sourced training data for ranking models. It has:
* Movie metadata from TMDB: actors, genres, tags, dates.
* Rankings for 2100 visitors for random movie groups.
* Each visitor interacted with ~5 groups, and marked >1 movies as liked.

Dataset is built using [toloka.ai](https://toloka.ai) platform with real humans and contains no synthetic data.

## Data and tools

* the actual dataset is located in the [dataset](https://github.com/metarank/ranklens/tree/master/dataset) directory.
* code used to generate the dataset is available in [converter](https://github.com/metarank/ranklens/tree/master/converter) subproject.
* source data is in [raw](https://github.com/metarank/ranklens/tree/master/raw) directory.

Source data is anonymized and contains only abstract random user identifiers.

## Movie metadata

Movie metadata is stored in `dataset/metadata.jsonl.gz` file in a JSONL format.

Example record for a movie:
```json
{
  "budget": 90000000,
  "director": {
    "gender": 2,
    "id": 7879,
    "name": "John Lasseter",
    "popularity": 4.32
  },
  "genres": [
    {
      "id": 16,
      "name": "Animation"
    },
    {
      "id": 35,
      "name": "Comedy"
    },
    {
      "id": 10751,
      "name": "Family"
    }
  ],
  "id": 3114,
  "overview": "Andy heads off to Cowboy Camp, leaving his toys to their own devices. Things shift into high gear when an obsessive toy collector named Al McWhiggen, owner of Al's Toy Barn kidnaps Woody. Andy's toys mount a daring rescue mission, Buzz Lightyear meets his match and Woody has to decide where he and his heart truly belong.",
  "poster": "https://image.tmdb.org/t/p/original/xVhEI1WCgNCCa5I86AqiwuZoog3.jpg",
  "releaseDate": "1999-10-30",
  "revenue": 497366869,
  "runtime": 92,
  "tags": [
    "pixar",
    "disney",
    "animation",
    "computer animation",
    "sequel",
    "tom hanks",
    "funny"
  ],
  "title": "Toy Story 2",
  "tmdbId": 863,
  "tmdbPopularity": 112.767,
  "tmdbVoteAverage": 7.6,
  "tmdbVoteCount": 11025,
  "topActors": [
    {
      "gender": 2,
      "id": 31,
      "name": "Tom Hanks",
      "popularity": 29.979
    },
    {
      "gender": 1,
      "id": 3234,
      "name": "Joan Cusack",
      "popularity": 11.631
    },
    {
      "gender": 2,
      "id": 15831,
      "name": "Frank Welker",
      "popularity": 10.184
    }
  ],
  "writer": {
    "gender": 2,
    "id": 7,
    "name": "Andrew Stanton",
    "popularity": 3.097
  }
}
```

## Ranking event example

Ranking events are stored in `dataset/ranking.jsonl.gz` file in a JSONL format.

```json
{
  "id": 6259,
  "liked": [
    4002,
    21,
    46970
  ],
  "shown": [
    1805,
    2387,
    105197,
    62374,
    47423,
    4002,
    114662,
    21,
    89904,
    94478,
    8528,
    6322,
    3418,
    5294,
    103372,
    46970,
    67408,
    1726,
    999,
    62081,
    762,
    80219,
    2374,
    104211
  ],
  "ts": 1636993834,
  "user": "90df34e521cc3d53af5f42f5c16ecb60"
}
```

## License

* the dataset is shared under the [CC-BY-SA 4.0 license](LICENSE.md)
* code is under the [Apache 2.0 license](converter/LICENSE)