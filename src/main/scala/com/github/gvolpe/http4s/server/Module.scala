package com.github.gvolpe.http4s.server

import cats.effect.Sync
import com.github.gvolpe.http4s.server.endpoints.FileStreamHttpEndpoint
import com.github.gvolpe.http4s.server.service.FileService
import org.http4s.HttpService

class Module[F[_]: Sync] {

  private val fileService = new FileService[F]

  val fileStreamHttpEndpoint: HttpService[F] =
    new FileStreamHttpEndpoint[F](fileService).service

}
