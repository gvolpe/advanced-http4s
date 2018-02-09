package com.github.gvolpe.http4s.client

import cats.effect.Effect
import com.github.gvolpe.http4s.StreamUtils
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import io.circe.Json
import jawn.Facade
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.client.blaze.Http1Client
import org.http4s.{Request, Uri}

object Client extends HttpClient[Task]

class HttpClient[F[_]](implicit F: Effect[F], S: StreamUtils[F]) extends StreamApp {

  implicit val jsonFacade: Facade[Json] = io.circe.jawn.CirceSupportParser.facade

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] = {
    Http1Client.stream[F]().flatMap { client =>
      val request = Request[F](uri = Uri.uri("http://localhost:8080/v1/dirs?depth=3"))
      for {
        response <- client.streaming(request)(_.body.chunks.through(fs2.text.utf8DecodeC))
        _        <- S.putStr(response)
      } yield ()
    }.drain
  }

}
