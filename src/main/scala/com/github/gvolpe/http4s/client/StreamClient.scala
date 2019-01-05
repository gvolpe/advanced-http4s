package com.github.gvolpe.http4s.client

import cats.effect._
import cats.implicits._
import fs2.Stream
import io.circe.Json
import jawn.RawFacade
import monix.execution.Scheduler
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Request, Uri}

object StreamClient extends IOApp {

  implicit final val jsonFacade: RawFacade[Json] =
    io.circe.jawn.CirceSupportParser.facade

  def stream[F[_]: ConcurrentEffect: ConsoleOut]: Stream[F, Unit] =
    BlazeClientBuilder[F](Scheduler.global).stream.flatMap { client =>
      val request = Request[F](uri = Uri.uri("http://localhost:8080/v1/dirs?depth=3"))
      for {
        response <- client.stream(request).flatMap(_.body.chunks.through(fs2.text.utf8DecodeC))
        _        <- Stream.eval(ConsoleOut[F].putStr(response))
      } yield ()
    }

  override def run(args: List[String]): IO[ExitCode] = {
    // TODO: When this PR is merged: https://github.com/gvolpe/console4cats/pull/22, prefer `import cats.effect.Console.implicits._`
    implicit val console: Console[IO] = cats.effect.Console.io

    stream[IO].compile.drain.as(ExitCode.Success)
  }

}
