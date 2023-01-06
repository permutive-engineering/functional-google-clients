/*
 * Copyright 2022 Permutive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
      prefetchPages: Int
  ): F[(ResultMeta, Stream[F, Result])] =
    fetch(None).map { init =>
      val resultStream: Stream[F, Result] =
        for {
          rawResult <-
            Stream.emit(init._1) ++
              Stream
                .unfoldEval[F, Option[PageToken], RawFetch](init._2)(
                  _.coflatMap(fetch).sequence
                )
                .prefetchN(prefetchPages)
          result <- readRawResults(rawResult)
            .traverse(parseRawResult)
            .fold(Stream.raiseError[F], Stream.emits[F, Result])
        } yield result

      (readMeta(init._1), resultStream)
    }

  def flattenUnrolled[F[_], A, B](results: F[(A, Stream[F, B])]): Stream[F, B] =
    Stream.eval(results).flatMap(_._2)

}
