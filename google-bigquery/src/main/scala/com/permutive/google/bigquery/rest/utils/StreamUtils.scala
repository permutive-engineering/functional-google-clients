package com.permutive.google.bigquery.rest.utils

import cats.effect.Concurrent
import cats.syntax.all._
import com.permutive.google.bigquery.models.Exceptions.BigQueryException
import com.permutive.google.bigquery.rest.models.job.results.NewTypes.PageToken
import fs2.Stream

object StreamUtils {

  def unrollResults[F[_]: Concurrent, RawFetch, RawResult, ResultMeta, Result](
    fetch: Option[PageToken] => F[(RawFetch, Option[PageToken])],
    readMeta: RawFetch => ResultMeta,
    readRawResults: RawFetch => List[RawResult],
    parseRawResult: RawResult => Either[BigQueryException, Result],
    prefetchPages: Int,
  ): F[(ResultMeta, Stream[F, Result])] =
    fetch(None).map { init =>
      val resultStream: Stream[F, Result] =
        for {
          rawResult <-
            Stream.emit(init._1) ++
              Stream
                .unfoldEval[F, Option[PageToken], RawFetch](init._2)(_.coflatMap(fetch).sequence)
                .prefetchN(prefetchPages)
          result <- readRawResults(rawResult)
            .traverse(parseRawResult)
            .fold(Stream.raiseError[F], Stream.emits[F, Result])
        } yield result

      (readMeta(init._1), resultStream)
    }

  // This use of `forSome` is to avoid exposing the `ANY` type to the method caller directly as a third type parameter
  // Without it they signature would be `[F[_], T, ANY](results: F[(ANY, Stream[F, T])])`
  // Inference seems to break down if the `ANY` is removed and tuple is `(_, Stream[F, T])`
  def flattenUnrolled[F[_], T](results: F[(ANY, Stream[F, T])] forSome { type ANY }): Stream[F, T] =
    Stream.eval(results).flatMap(_._2)

}
