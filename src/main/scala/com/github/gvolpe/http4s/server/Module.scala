package com.github.gvolpe.http4s.server

import cats.effect.Effect
import cats.syntax.semigroupk._ // For <+>
import com.github.gvolpe.http4s.server.endpoints._
import com.github.gvolpe.http4s.server.endpoints.auth.BasicAuthHttpEndpoint
import com.github.gvolpe.http4s.server.service.FileService
import fs2.Scheduler
import org.http4s.HttpService
import org.http4s.server.HttpMiddleware
import org.http4s.server.middleware.{AutoSlash, ChunkAggregator, GZip, Timeout}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class Module[F[_]](implicit F: Effect[F], S: Scheduler) {

  private val fileService = new FileService[F]

  def middleware: HttpMiddleware[F] = {
    {(service: HttpService[F]) => GZip(service)(F)} compose
      { service => AutoSlash(service)(F) }
  }

  val fileHttpEndpoint: HttpService[F] =
    new FileHttpEndpoint[F](fileService).service

  val nonStreamFileHttpEndpoint = ChunkAggregator(fileHttpEndpoint)

  private val hexNameHttpEndpoint: HttpService[F] =
    new HexNameHttpEndpoint[F].service

  private val compressedEndpoints: HttpService[F] =
    middleware(hexNameHttpEndpoint)

  private val timeoutHttpEndpoint: HttpService[F] =
    new TimeoutHttpEndpoint[F].service

  private val timeoutEndpoints: HttpService[F] =
    Timeout(1.second)(timeoutHttpEndpoint)

  private val mediaHttpEndpoint: HttpService[F] =
    new JsonXmlHttpEndpoint[F].service

  private val multipartHttpEndpoint: HttpService[F] =
    new MultipartHttpEndpoint[F](fileService).service

  private val basicAuthHttpEndpoint: HttpService[F] =
    new BasicAuthHttpEndpoint[F].service

  val httpServices: HttpService[F] = (
    compressedEndpoints <+> timeoutEndpoints
    <+> mediaHttpEndpoint <+> multipartHttpEndpoint
    <+> basicAuthHttpEndpoint
  )

}
