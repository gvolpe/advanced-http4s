package com.github.gvolpe.http4s.server

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import fs2.Stream
import monix.execution.Scheduler
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{Request, Response}

object Server extends IOApp {

  private def app[F[_]: Sync](
      ctx: Module[F]): Kleisli[F, Request[F], Response[F]] =
    Router(
      "/" -> ctx.httpServices,
      s"/${endpoints.ApiVersion}" -> ctx.fileHttpEndpoint,
      s"/${endpoints.ApiVersion}/nonstream" -> ctx.nonStreamFileHttpEndpoint,
      s"/${endpoints.ApiVersion}/protected" -> ctx.basicAuthHttpEndpoint
    ).orNotFound

  def stream[F[_]: ConcurrentEffect: Timer: ContextShift]: Stream[F, ExitCode] =
    for {
      client    <- BlazeClientBuilder[F](Scheduler.global).stream
      ctx       <- Stream(new Module[F](client))
      exitCode  <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(app(ctx))
        .serve
    } yield exitCode

  override def run(args: List[String]): IO[ExitCode] =
    stream[IO].compile.drain.as(ExitCode.Success)
}
