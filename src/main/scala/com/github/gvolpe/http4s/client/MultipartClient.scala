package com.github.gvolpe.http4s.client

import java.net.URL

import cats.effect._
import com.github.gvolpe.http4s.StreamUtils
import fs2.Stream
import monix.execution.Scheduler
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.`Content-Type`
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{MediaType, Method, Request, Uri}

object MultipartClient extends IOApp with MultipartHttpClient[IO] {
  import cats.implicits._

  def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

  // @guizmaii remark: I'm not proud of the following code but I didn't find a better solution.
  // TODO: Is there a better solution ?
  private def sumoner(implicit F: ConcurrentEffect[IO],
                      S: StreamUtils[IO],
                      CS: ContextShift[IO]) = (F, S, CS)

  override implicit val F: ConcurrentEffect[IO] = sumoner._1
  override implicit val S: StreamUtils[IO] = sumoner._2
  override implicit val CS: ContextShift[IO] = sumoner._3
}

trait MultipartHttpClient[F[_]] extends Http4sClientDsl[F] {

  import cats.implicits._

  implicit val F: ConcurrentEffect[F]
  implicit val S: StreamUtils[F]
  implicit val CS: ContextShift[F]

  private def image: F[URL] = F.delay(getClass.getResource("/rick.jpg"))
  private def request(body: Multipart[F]): F[Request[F]] =
    F.delay {
      Request[F](
        method = Method.POST,
        uri = Uri.uri("http://localhost:8080/v1/multipart"),
        body = body.parts
          .traverse(_.body)
          .reduce(_ combine _)
          .flatMap(Stream.apply) // TODO: There's maybe a better solution ?
      )
    }

  private def multipart(url: URL) = Multipart[F](
    Vector(
      Part.formData("name", "gvolpe"),
      Part.fileData("rick",
                    url,
                    Scheduler.global,
                    `Content-Type`(MediaType.image.png))
    )
  )

  private val request: F[Request[F]] =
    for {
      body <- image.map(multipart)
      req <- request(body)
    } yield req.withHeaders(body.headers)

  def stream: Stream[F, Unit] =
    for {
      client <- BlazeClientBuilder[F](Scheduler.global).stream
      req <- Stream.eval(request)
      value <- Stream.eval(client.expect[String](req))
      _ <- S.evalF(println(value))
    } yield ()

}
