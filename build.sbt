// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.0" // your current series x.y

ThisBuild / organization := "com.permutive"
ThisBuild / organizationName := "Permutive"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("janstenpickle", "Chris Jansen")
)

// Jsoniter is only built against Java 11
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("11"))

// publish to s01.oss.sonatype.org (set to true to publish to oss.sonatype.org instead)
ThisBuild / tlSonatypeUseLegacyHost := true

val CatsEffect = "3.4.2"

val Cats = "2.9.0"

val Http4s = "0.23.16"

val Munit = "0.7.29"

val MunitCE3 = "1.0.7"

val Refined = "0.10.1"

val Scala213 = "2.13.10"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.2.1")
ThisBuild / scalaVersion := Scala213 // the default Scala

lazy val root =
  tlCrossRootProject.aggregate(gcpTypes, googleAuth, googleProjectId)

lazy val gcpTypes = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("gcp-types"))
  .settings(
    name := "gcp-types",
    libraryDependencies ++= Seq(
      "eu.timepit" %%% "refined" % Refined
    )
  )

lazy val googleAuth = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("google-auth"))
  .settings(
    name := "google-auth",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % Cats,
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffect,
      "io.scalaland" %%% "chimney" % "0.6.2",
      "io.circe" %%% "circe-generic" % "0.14.3",
      "io.circe" %%% "circe-fs2" % "0.14.0",
      "org.http4s" %%% "http4s-client" % Http4s,
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core" % "2.18.1",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-macros" % "2.18.1" % Provided,
      "org.typelevel" %% "log4cats-slf4j" % "2.5.0",
      "com.auth0" % "java-jwt" % "4.2.1",
      "com.permutive" %%% "refreshable" % "1.1.0",
      "org.scala-lang.modules" %%% "scala-java8-compat" % "1.0.2",
      "org.typelevel" %%% "cats-effect" % CatsEffect % Test,
      "org.scalameta" %%% "munit" % Munit % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % MunitCE3 % Test
    )
  )

lazy val googleProjectId = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("google-project-id"))
  .settings(
    name := "google-project-id",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % Cats,
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffect,
      "org.http4s" %%% "http4s-client" % Http4s
    )
  )
  .dependsOn(gcpTypes)

lazy val googleProjectIdPureconfig = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("google-project-id-pureconfig"))
  .settings(
    name := "google-project-pureconfig",
    libraryDependencies ++= Seq(
      "eu.timepit" %%% "refined-pureconfig" % Refined,
      "com.github.pureconfig" %%% "pureconfig-generic" % "0.17.2",
      "org.scalameta" %%% "munit" % Munit % Test
    )
  )
  .dependsOn(googleProjectId)
