package com.github.gvolpe.http4s.server.endpoints

import cats.Monad
import com.github.gvolpe.http4s.server.service.FileService
import org.http4s._
import org.http4s.dsl.Http4sDsl

class FileStreamHttpEndpoint[F[_] : Monad](fileService: FileService[F]) extends Http4sDsl[F] {

  // TODO: Add the depth as a query parameter

  val service: HttpService[F] = HttpService {
    case GET -> Root / ApiVersion / "files" =>
      Ok(fileService.homeDirectories)
  }

}