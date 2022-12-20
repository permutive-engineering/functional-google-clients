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
