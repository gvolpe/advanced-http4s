package com.github.gvolpe.http4s.server

import cats.data.Kleisli
import cats.effect._
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler.Implicits.global
import org.http4s.{Request, Response}
import org.http4s.client.blaze.{BlazeClientBuilder, Http1Client}
import org.http4s.server.Router
import org.http4s.server.blaze.{BlazeBuilder, BlazeServerBuilder}

object Server extends TaskApp {
  import org.http4s.implicits._


  private def app[F[_]: Sync](ctx: Module[F]): Kleisli[F, Request[F], Response[F]] =
    Router(
      "/" -> ctx.httpServices,
      s"/${endpoints.ApiVersion}" -> ctx.fileHttpEndpoint,
      s"/${endpoints.ApiVersion}/nonstream" -> ctx.nonStreamFileHttpEndpoint,
      s"/${endpoints.ApiVersion}/protected" -> ctx.basicAuthHttpEndpoint
    ).orNotFound

  def stream[F[_]: ConcurrentEffect: Timer]: Stream[F, Unit] =
    for {
      client <- BlazeClientBuilder[F](global).stream
      ctx <- Stream(new Module[F](client))
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(app(ctx))
        .serve
    } yield exitCode

  override def run(args: List[String]): Task[ExitCode] = stream.compile.drain.
}
