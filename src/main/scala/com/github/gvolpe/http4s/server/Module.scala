package com.github.gvolpe.http4s.server

import cats.effect.Sync
import com.github.gvolpe.http4s.server.endpoints.{FileHttpEndpoint, HexNameHttpEndpoint}
import com.github.gvolpe.http4s.server.service.FileService
import org.http4s.HttpService
import org.http4s.server.HttpMiddleware
import org.http4s.server.middleware.{AutoSlash, GZip}

class Module[F[_]](implicit F: Sync[F]) {

  private val fileService = new FileService[F]

  def middleware: HttpMiddleware[F] = {
    {(service: HttpService[F]) => GZip(service)(F)} compose
      { service => AutoSlash(service)(F) }
  }

  val fileHttpEndpoint: HttpService[F] =
    new FileHttpEndpoint[F](fileService).service

  private val hexNameHttpEndpoint: HttpService[F] =
    new HexNameHttpEndpoint[F].service

  val compressedHttpEndpoints = middleware(hexNameHttpEndpoint)

}
