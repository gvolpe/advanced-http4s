package com.github.gvolpe.http4s.client

import cats.effect.{ConcurrentEffect, ExitCode, IO}
import com.github.gvolpe.fs2.PubSubApp.stream
import com.github.gvolpe.http4s.StreamUtils
import fs2.Stream
import io.circe.Json
import jawn.RawFacade
import monix.eval.TaskApp
import monix.execution.Scheduler.Implicits.global
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Request, Uri}

object StreamClient extends TaskApp {

  implicit final val jsonFacade: RawFacade[Json] =
    io.circe.jawn.CirceSupportParser.facade

  def stream[F[_]: ConcurrentEffect](implicit S: StreamUtils[F]): Stream[F, Unit] =
    BlazeClientBuilder[F](global).stream.flatMap { client =>
      val request = Request[F](uri = Uri.uri("http://localhost:8080/v1/dirs?depth=3"))
      for {
        response <- client.streaming(request)(
          _.body.chunks.through(fs2.text.utf8DecodeC))
        _ <- S.putStr(response)
      } yield ()
    }

  override def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

}
