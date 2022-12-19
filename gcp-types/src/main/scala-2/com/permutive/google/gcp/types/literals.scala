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

package com.permutive.google.gcp.types

import org.typelevel.literally.Literally

object literals {
  implicit class short(val sc: StringContext) extends AnyVal {
    def projectId(args: Any*): ProjectId = macro ProjectIdLiteral.make
  }

  object ProjectIdLiteral extends Literally[ProjectId] {
    def validate(c: Context)(s: String): Either[String, c.Expr[ProjectId]] = {
      import c.universe.{Try => _, _}
      ProjectId.fromString(s) match {
        case None    => Left(ProjectId.onError(s))
        case Some(_) => Right(c.Expr(q"ProjectId.fromString($s).get"))
      }
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[ProjectId] =
      apply(c)(args: _*)
  }
}
