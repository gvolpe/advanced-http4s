import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.github.gvolpe",
      scalaVersion := "2.12.8",
      version      := "0.1.0-SNAPSHOT",
      scalacOptions := Seq(
        "-deprecation",
        "-encoding",
        "UTF-8",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-Ypartial-unification"
      )
    )),
    name := "Advanced Http4s",
    libraryDependencies ++= Seq(
      Libraries.catsEffect,
      Libraries.monix,
      Libraries.logs4cats,
      Libraries.console4cats,
      Libraries.catsPar,
      Libraries.fs2Core,
      Libraries.http4sServer,
      Libraries.http4sClient,
      Libraries.http4sDsl,
      Libraries.http4sCirce,
      Libraries.http4sXml,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.typesafeConfig,
      Libraries.logback,
      Libraries.scalaTest,
      Libraries.scalaCheck
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4"),
    addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.9" cross CrossVersion.binary),
    // Decomment if it's too annoying!
    scalacOptions := scalacOptions.value.filter(_ != "-Xfatal-warnings"),
)
