package com.github.gvolpe.http4s.client

import cats.effect.Effect
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.http4s.StreamUtils
import fs2.StreamApp.ExitCode
import fs2.{Scheduler, Stream, StreamApp}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.client.blaze.Http1Client
import org.http4s.headers.`Content-Type`
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{MediaType, Method, Request, Uri}

object MultipartClient extends MultipartHttpClient[Task]

class MultipartHttpClient[F[_]](implicit F: Effect[F], S: StreamUtils[F]) extends StreamApp {

  private val rick = getClass.getResource("/rick.jpg")

  private val multipart = Multipart[F](
    Vector(
      Part.formData("name", "gvolpe"),
      Part.fileData("rick", rick, `Content-Type`(MediaType.`image/png`))
    )
  )

  private val request =
    Request[F](method = Method.POST, uri = Uri.uri("http://localhost:8080/v1/multipart"))
      .withBody(multipart)
      .map(_.replaceAllHeaders(multipart.headers))

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] = {
    Scheduler(corePoolSize = 2).flatMap { implicit scheduler =>
      Stream.eval(
        for {
          client  <- Http1Client[F]()
          req     <- request
          _       <- client.expect[String](req).map(println)
        } yield ()
      )
    }.drain
  }

}
