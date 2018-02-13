import sbt._

object Dependencies {

  object Versions {
    val CatsEffect  = "0.8"
    val Monix       = "3.0.0-M3"
    val Fs2         = "0.10.1"
    val Http4s      = "0.18.0"
    val Tsec        = "0.0.1-M7"
    val Circe       = "0.9.1"
    val ScalaTest   = "3.0.4"
    val ScalaCheck  = "1.13.4"
    val Logback     = "1.2.1"
    val TypesafeCfg = "1.3.1"
  }

  object Libraries {
    lazy val catsEffect     = "org.typelevel"       %% "cats-effect"                  % Versions.CatsEffect
    lazy val monix          = "io.monix"            %% "monix"                        % Versions.Monix

    lazy val fs2Core        = "co.fs2"              %% "fs2-core"                     % Versions.Fs2
    lazy val fs2IO          = "co.fs2"              %% "fs2-io"                       % Versions.Fs2

    lazy val http4sServer   = "org.http4s"          %% "http4s-blaze-server"          % Versions.Http4s
    lazy val http4sClient   = "org.http4s"          %% "http4s-blaze-client"          % Versions.Http4s
    lazy val http4sDsl      = "org.http4s"          %% "http4s-dsl"                   % Versions.Http4s
    lazy val http4sCirce    = "org.http4s"          %% "http4s-circe"                 % Versions.Http4s
    lazy val http4sXml      = "org.http4s"          %% "http4s-scala-xml"             % Versions.Http4s

    lazy val tsecJwtMac     = "io.github.jmcardon"  %% "tsec-jwt-mac"                 % Versions.Tsec

    lazy val circeCore      = "io.circe"            %% "circe-core"                   % Versions.Circe
    lazy val circeGeneric   = "io.circe"            %% "circe-generic"                % Versions.Circe

    lazy val typesafeConfig = "com.typesafe"        %  "config"                       % Versions.TypesafeCfg
    lazy val logback        = "ch.qos.logback"      %  "logback-classic"              % Versions.Logback

    lazy val scalaTest      = "org.scalatest"       %% "scalatest"                    % Versions.ScalaTest   % Test
    lazy val scalaCheck     = "org.scalacheck"      %% "scalacheck"                   % Versions.ScalaCheck  % Test
  }

}
