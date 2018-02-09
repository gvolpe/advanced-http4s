package com.github.gvolpe.http4s.server

import cats.effect.Effect
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.server.blaze.BlazeBuilder

object Server extends HttpServer[Task]

class HttpServer[F[_]](implicit F: Effect[F]) extends StreamApp[F] {

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    Scheduler(corePoolSize = 2).flatMap { implicit scheduler =>
      for {
        ctx      <- Stream.emit(new Module[F])
        exitCode <- BlazeBuilder[F]
                      .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
                      .mountService(ctx.fileHttpEndpoint)
                      .serve
      } yield exitCode
    }

}
