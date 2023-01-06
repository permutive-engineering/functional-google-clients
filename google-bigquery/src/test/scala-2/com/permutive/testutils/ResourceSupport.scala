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

package com.permutive.testutils

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import io.circe.Json
import io.circe.parser.parse
import munit.FunSuite

import scala.jdk.CollectionConverters._
import scala.util.Try

/** This trait and matcher can be used to store and test expected JSON result in files rather than coded as strings.
  *
  * Files are stored in the `test/resources` directory under the package that the test is located in. To use mix the
  * trait ResourceSupport in with your test (and Matchers). A test can then look like the following:
  *
  * ```
  * val myCaseClass: MyCaseClass = ???
  * myCaseClass.asJson should equalJsonResource("some-file-name.json")
  * ```
  *
  * Assuming this was in the package `com.permutive.foo` this would check against `some-file-name.json` in
  * `test/resources/com/permutive/foo`.
  *
  * In order to generate these files (for a new test or if the API has deliberately changed) set the
  * `overwriteResources` flag in the companion object to `true`. Make sure to not commit this though!
  */
trait ResourceSupport { self: FunSuite =>

  // Needed for multi-module builds
  def packageName: Option[String]

  private lazy val resources: Path =
    ResourceSupport.testResourceFile(packageName)(
      this.getClass.getPackage.getName
        .split("\\.")
        .toList
        .mkString(File.separator)
    )

  def equalStringResource(
      name: String,
      fileDescription: Option[String]
  )(string: String): Unit =
    equalTo(name)(string, StringResource(name, fileDescription, resources))

  def equalStringResource(name: String)(string: String): Unit =
    equalStringResource(name, None)(string)
  def equalStringResource(
      name: String,
      fileDescription: String
  )(string: String): Unit =
    equalStringResource(name, Some(fileDescription))(string)

  def readStringResource(
      name: String,
      fileDescription: Option[String]
  ): String =
    StringResource(name, fileDescription, resources).fileContents
  def readStringResource(name: String): String = readStringResource(name, None)
  def readStringResource(name: String, fileDescription: String): String =
    readStringResource(name, Some(fileDescription))

  def equalJsonResource(
      name: String,
      fileDescription: Option[String]
  )(json: Json): Unit =
    equalTo(name)(json, JsonResource(name, fileDescription, resources))
  def equalJsonResource(name: String)(json: Json): Unit =
    equalJsonResource(name, None)(json)
  def equalJsonResource(
      name: String,
      fileDescription: String
  )(json: Json): Unit =
    equalJsonResource(name, Some(fileDescription))(json)

  def readJsonResource(name: String, fileDescription: Option[String]): Json =
    JsonResource(name, fileDescription, resources).fileContents
  def readJsonResource(name: String): Json = readJsonResource(name, None)
  def readJsonResource(name: String, fileDescription: String): Json =
    readJsonResource(name, Some(fileDescription))

  private def equalTo[T, R <: Resource[T]](
      name: String
  )(left: T, resource: R): Unit =
    if (ResourceSupport.overwriteResources) {
      resource.write(left)
      assert(cond = true)
    } else {
      assertEquals(
        left,
        resource.fileContents,
        s"${resource.serialise(left)} should equal `$name`'s contents: ${resource.fileContentsAsString}"
      )
    }
}

object ResourceSupport {

  private def testResourceFile(
      packageName: Option[String]
  )(relativePath: String): Path = {
    val commonDirs: List[Path] = List(
      Paths.get("src", "test", "resources"),
      Paths.get("test", "resources"),
      Paths.get("modules", "src", "test", "resources"),
      Paths.get("modules", "test", "resources")
    )

    val searchDirs: List[Path] = packageName.fold(commonDirs) { packageDir =>
      List(
        Paths.get(packageDir, "src", "test", "resources")
      ) ++ commonDirs
    }

    val baseDir: Path = searchDirs
      .find(Files.exists(_))
      .getOrElse(
        sys.error(s"Unable to locate resources directory under `$relativePath`")
      )

    baseDir.resolve(relativePath)
  }

  // Change this to true to regenerate files instead of matching
  final def overwriteResources: Boolean = false

}

trait ResourceSupportSpec { self: FunSuite =>

  // Just in case someone (definitely not Ben...) ever accidentally commits with overwriting
  test("Resource.support.overwriteResources should be false") {
    assertEquals(ResourceSupport.overwriteResources, false)
  }

}

sealed trait Resource[T] {
  def name: String
  def serialise(t: T): String
  def description: String
  def fileDescription: Option[String]

  private[this] val charset = StandardCharsets.UTF_8

  // Description lines are prefixed with this
  private[this] val commentMarker = "#"

  // Expected to throw
  def deserialise(s: String): T

  def resources: Path

  def file: Path = resources.resolve(name)

  def read: Try[T] =
    Try(
      deserialise(
        Files
          .readAllLines(file, charset)
          .asScala
          .dropWhile(_.startsWith(commentMarker))
          .mkString
      )
    )

  def fileContents: T =
    read.getOrElse(
      sys.error(
        s"File `$name` does not exist or the contents are invalid for $description"
      )
    )

  def fileContentsAsString: String =
    serialise(fileContents)

  def write(left: T): Unit = {
    val contents: String =
      Try(serialise(left))
        .getOrElse(
          sys.error(
            s"Trying to store invalid $description content in `$name` in resource matching test: $left"
          )
        )

    Files.createDirectories(file.getParent)
    Files.write(
      file,
      fileDescriptionToWrite.getOrElse(List.empty).asJava,
      charset
    )
    Files.write(file, contents.getBytes(charset), StandardOpenOption.APPEND)

    ()
  }

  private def fileDescriptionToWrite: Option[List[String]] =
    fileDescription.map {
      _.split("\n").toList.map(s"$commentMarker " + _)
    }
}

final case class StringResource(
    name: String,
    fileDescription: Option[String],
    resources: Path
) extends Resource[String] {
  override def description: String = "string"
  override def serialise(s: String): String = s
  override def deserialise(s: String): String = s
}

final case class JsonResource(
    name: String,
    fileDescription: Option[String],
    resources: Path
) extends Resource[Json] {
  override def description: String = "JSON"
  override def serialise(json: Json): String = json.spaces2
  override def deserialise(s: String): Json = parse(s).fold(throw _, identity)
}
