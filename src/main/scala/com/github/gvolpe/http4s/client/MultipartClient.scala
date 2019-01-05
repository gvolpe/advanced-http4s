package com.github.gvolpe.http4s.client

import java.net.URL

import cats.effect._
import fs2.Stream
import monix.execution.Scheduler
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.headers.`Content-Type`
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{MediaType, Method, Request, Uri}
import cats.implicits._

object MultipartClient extends IOApp {

  private def image[F[_]: Sync]: F[URL] =
    Sync[F].delay(getClass.getResource("/rick.jpg"))

  private def multipart[F[_]: Sync: ContextShift](url: URL) =
    Multipart[F](
      Vector(
        Part.formData("name", "gvolpe"),
        Part.fileData(
          "rick",
          url,
          Scheduler.global,
          `Content-Type`(MediaType.image.png)
        )
      )
    )

  private def request[F[_]: Sync: ContextShift]: F[Request[F]] =
    for {
      body <- image.map(multipart[F])
      req  <- Sync[F].delay {
        Request[F](
          method = Method.POST,
          uri = Uri.uri("http://localhost:8080/v1/multipart"),
          body = body.parts
            .traverse(_.body)
            .flatMap(Stream.emits)
            .reduce(_ |+| _)
        )
      }
    } yield req.withHeaders(body.headers)

  def stream[F[_]: ContextShift](implicit F: ConcurrentEffect[F], C: ConsoleOut[F]): F[Unit] =
    for {
      client <- F.delay(BlazeClientBuilder[F](Scheduler.global).resource)
      req    <- request
      value  <- client.use(_.expect[String](req))
      _      <- C.putStrLn(value)
    } yield ()

  def run(args: List[String]): IO[ExitCode] = {
    // TODO: When this PR is merged: https://github.com/gvolpe/console4cats/pull/22, prefer `import cats.effect.Console.implicits._`
    implicit val console: Console[IO] = cats.effect.Console.io

    stream[IO].as(ExitCode.Success)
  }

}
