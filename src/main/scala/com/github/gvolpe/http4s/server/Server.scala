package com.github.gvolpe.http4s.server

import cats.effect.Effect
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.client.blaze.Http1Client
import org.http4s.server.blaze.BlazeBuilder

object Server extends HttpServer[Task]

class HttpServer[F[_]](implicit F: Effect[F]) extends StreamApp[F] {

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    Scheduler(corePoolSize = 2).flatMap { implicit scheduler =>
      for {
        client   <- Stream.eval(Http1Client[F]())
        ctx      <- Stream(new Module[F](client))
        exitCode <- BlazeBuilder[F]
                      .bindHttp(8080, "0.0.0.0")
                      .mountService(ctx.fileHttpEndpoint, s"/${endpoints.ApiVersion}")
                      .mountService(ctx.nonStreamFileHttpEndpoint, s"/${endpoints.ApiVersion}/nonstream")
                      .mountService(ctx.httpServices)
                      .mountService(ctx.basicAuthHttpEndpoint, s"/${endpoints.ApiVersion}/protected")
                      .serve
      } yield exitCode
    }

}
