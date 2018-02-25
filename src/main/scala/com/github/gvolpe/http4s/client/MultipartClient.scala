package com.github.gvolpe.http4s.client

import cats.effect.Effect
import cats.syntax.functor._
import com.github.gvolpe.http4s.StreamUtils
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.Method._
import org.http4s.client.blaze.Http1Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.`Content-Type`
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{MediaType, Uri}

object MultipartClient extends MultipartHttpClient[Task]

class MultipartHttpClient[F[_]](implicit F: Effect[F], S: StreamUtils[F]) extends StreamApp with Http4sClientDsl[F] {

  private val rick = getClass.getResource("/rick.jpg")

  private val multipart = Multipart[F](
    Vector(
      Part.formData("name", "gvolpe"),
      Part.fileData("rick", rick, `Content-Type`(MediaType.`image/png`))
    )
  )

  private val request =
    POST(Uri.uri("http://localhost:8080/v1/multipart"), multipart)
      .map(_.replaceAllHeaders(multipart.headers))

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] = {
    Scheduler(corePoolSize = 2).flatMap { implicit scheduler =>
      for {
        client <- Http1Client.stream[F]()
        req    <- Stream.eval(request)
        value  <- Stream.eval(client.expect[String](req))
        _      <- S.evalF(println(value))
      } yield ()
    }.drain
  }

}
