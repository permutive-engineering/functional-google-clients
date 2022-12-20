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

package com.permutive.google.gcp

import org.typelevel.literally.Literally

package object types:
  extension (inline ctx: StringContext)
    inline def projectId(inline args: Any*): ProjectId =
      ${ProjectIdLiteral('ctx, 'args)}

  object ProjectIdLiteral extends Literally[ProjectId]:
    def validate(s: String)(using Quotes) =
      ProjectId.fromString(s) match
        case None => Left(ProjectId.onError(s))
        case Some(_) => Right('{ProjectId.fromString(${Expr(s)}).get})
