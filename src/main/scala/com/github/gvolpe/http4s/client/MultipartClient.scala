package com.github.gvolpe.http4s.client

import java.net.URL

import cats.effect._
import com.github.gvolpe.http4s.StreamUtils
import fs2.Stream
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.`Content-Type`
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{MediaType, Method, Request, Uri}

object MultipartClient extends MultipartHttpClient[Task] with IOApp

class MultipartHttpClient[F[_]: ContextShift](implicit F: ConcurrentEffect[F], S: StreamUtils[F]) extends Http4sClientDsl[F] {

  import cats.implicits._

  private val image: F[URL] = F.delay(getClass.getResource("/rick.jpg"))
  private def request(body: Multipart[F]): F[Request[F]] =
    F.delay {
      org.http4s.Request[F](
        method = Method.POST,
        uri = Uri.uri("http://localhost:8080/v1/multipart"),
        body = body.parts.traverse(_.body).reduce(_ combine _).flatMap(Stream.apply) // TODO: There's maybe a better solution ?
      )
    }

  private def multipart(url: URL) = Multipart[F](
    Vector(
      Part.formData("name", "gvolpe"),
      Part.fileData("rick", url, global, `Content-Type`(MediaType.image.png))
    )
  )

  private val request: F[Request[F]] =
    for {
      body <- image.map(multipart)
      req  <- request(body)
    } yield req.withHeaders(body.headers)

  def stream: Stream[F, Unit] = {
      for {
        client <- BlazeClientBuilder[F](global).stream
        req    <- Stream.eval(request)
        value  <- Stream.eval(client.expect[String](req))
        _      <- S.evalF(println(value))
      } yield ()
    }

  def run(args: List[String]): F[ExitCode] = stream.compile.drain.as(ExitCode.Success)

}
