import sbt._

object Dependencies {

  object Versions {
    val CatsEffect   = "1.1.0"
    val Monix        = "3.0.0-RC2"
    val Console4Cats = "0.5"
    val CatsPar      = "0.2.0"
    val Fs2          = "1.0.2"
    val Http4s       = "0.20.0-M4"
    val Circe        = "0.11.0"
    val ScalaTest    = "3.0.5"
    val ScalaCheck   = "1.14.0"
    val Logback      = "1.2.3"
    val TypesafeCfg  = "1.3.3"

  }

  object Libraries {
    lazy val catsEffect     = "org.typelevel"       %% "cats-effect"                  % Versions.CatsEffect
    lazy val monix          = "io.monix"            %% "monix"                        % Versions.Monix
    lazy val console4cats   = "com.github.gvolpe"   %% "console4cats"                 % Versions.Console4Cats
    lazy val catsPar        = "io.chrisdavenport"   %% "cats-par"                     % Versions.CatsPar
    
    lazy val fs2Core        = "co.fs2"              %% "fs2-core"                     % Versions.Fs2
    lazy val fs2IO          = "co.fs2"              %% "fs2-io"                       % Versions.Fs2

    lazy val http4sServer   = "org.http4s"          %% "http4s-blaze-server"          % Versions.Http4s
    lazy val http4sClient   = "org.http4s"          %% "http4s-blaze-client"          % Versions.Http4s
    lazy val http4sDsl      = "org.http4s"          %% "http4s-dsl"                   % Versions.Http4s
    lazy val http4sCirce    = "org.http4s"          %% "http4s-circe"                 % Versions.Http4s
    lazy val http4sXml      = "org.http4s"          %% "http4s-scala-xml"             % Versions.Http4s

    lazy val circeCore      = "io.circe"            %% "circe-core"                   % Versions.Circe
    lazy val circeGeneric   = "io.circe"            %% "circe-generic"                % Versions.Circe

    lazy val typesafeConfig = "com.typesafe"        %  "config"                       % Versions.TypesafeCfg
    lazy val logback        = "ch.qos.logback"      %  "logback-classic"              % Versions.Logback

    lazy val scalaTest      = "org.scalatest"       %% "scalatest"                    % Versions.ScalaTest   % Test
    lazy val scalaCheck     = "org.scalacheck"      %% "scalacheck"                   % Versions.ScalaCheck  % Test
  }

}
