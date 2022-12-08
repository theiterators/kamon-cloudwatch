/* =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

name := "kamon-cloudwatch"
description := "Kamon extension to publish metrics into AWS CloudWatch"
licenses += (("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")))
organization := "pl.iterators"
organizationName := "Iterators"
homepage := Some(url("https://github.com/theiterators/kamon-cloudwatch"))

developers := List(
    Developer(
      id = "kpalcowski",
      name = "Krzysztof Palcowski",
      email = "kpalcowski@iteratorshq.com",
      url = url("https://github.com/kristerr")
    )
  )
scmInfo := Some(
  ScmInfo(
    url("https://github.com/theiterators/kamon-cloudwatch"),
    "scm:git:git@github.com:theiterators/kamon-cloudwatch.git"
  )
)
sonatypeCredentialHost := "oss.sonatype.org"
scalaVersion := "2.13.10"

scalacOptions ++= Seq(
//  "-release:17",
  "-Ymacro-annotations",
  "-Ywarn-macros:after",
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)


resolvers += Resolver.bintrayRepo("kamon-io", "releases")


libraryDependencies ++= {
  object Versions {
    val awsCloudwatch = "2.18.28"
    val log4j         = "2.19.0"
    val kamon         = "2.5.11"
    val scalatest     = "3.2.14"
    val slf4j         = "2.0.5"
    val wiremock      = "2.25.1"
  }

  Seq(
    "io.kamon"                 %% "kamon-core"             % Versions.kamon,
    "io.kamon"                 %% "kamon-testkit"          % Versions.kamon % Test,
    "org.slf4j"                % "slf4j-api"               % Versions.slf4j,
    "software.amazon.awssdk"   % "cloudwatch"              % Versions.awsCloudwatch,
    "org.scalatest"            %% "scalatest"              % Versions.scalatest % Test,
    "com.github.tomakehurst"   % "wiremock"                % Versions.wiremock % Test,
    "org.apache.logging.log4j" % "log4j-core"              % Versions.log4j % Test,
    "org.apache.logging.log4j" % "log4j-api"               % Versions.log4j % Test,
    "org.apache.logging.log4j" % "log4j-slf4j-impl"        % Versions.log4j % Test
  )
}

