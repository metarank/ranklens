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

Example record for a movie:
```json
{
  "budget": 0,
  "director": {
    "gender": 2,
    "id": 56106,
    "name": "Charles Shyer",
    "popularity": 0.751
  },
  "genres": [
    {
      "id": 35,
      "name": "Comedy"
    },
    {
      "id": 10751,
      "name": "Family"
    }
  ],
  "id": 5,
  "overview": "Just when George Banks has recovered from his daughter's wedding, he receives the news that she's pregnant ... and that George's wife is expecting too. He was planning on selling their home, but that's a plan that—like George—will have to change with the arrival of both a grandchild and a kid of his own.",
  "releaseDate": 9472,
  "revenue": 76594107,
  "runtime": 106,
  "tags": [
    "steve martin",
    "wedding",
    "pregnancy",
    "family",
    "daughter",
    "touching",
    "midlife crisis",
    "gynecologist",
    "contraception",
    "confidence"
  ],
  "title": "Father of the Bride Part II",
  "tmdbId": 11862,
  "tmdbPopularity": 11.633,
  "tmdbVoteAverage": 6.3,
  "tmdbVoteCount": 529,
  "topActors": [
    {
      "gender": 2,
      "id": 18793,
      "name": "Kieran Culkin",
      "popularity": 9.054
    },
    {
      "gender": 1,
      "id": 3092,
      "name": "Diane Keaton",
      "popularity": 8.421
    },
    {
      "gender": 2,
      "id": 519,
      "name": "Martin Short",
      "popularity": 6.453
    }
  ],
  "writer": {
    "gender": 2,
    "id": 26160,
    "name": "Albert Hackett",
    "popularity": 1.628
  }
}
```

## Ranking event example
```json
{
  "id": 6259,
  "liked": [
    78,
    80,
    89
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