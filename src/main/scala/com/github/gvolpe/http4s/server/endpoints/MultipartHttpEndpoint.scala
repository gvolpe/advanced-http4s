package com.github.gvolpe.http4s.server.endpoints

import cats.effect.{ContextShift, Sync}
import cats.implicits._
import com.github.gvolpe.http4s.server.service.FileService
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.{Multipart, Part}

final class MultipartHttpEndpoint[F[_]: ContextShift](fileService: FileService[F])
                                                     (implicit F: Sync[F]) extends Http4sDsl[F] {

  final val service: HttpRoutes[F] =
    HttpRoutes
      .of[F] {
        case GET -> Root / ApiVersion / "multipart" =>
          Ok("Send a file (image, sound, etc) via POST Method")

        case req @ POST -> Root / ApiVersion / "multipart" =>
          req.decodeStrict[Multipart[F]] { response =>
            def filterFileTypes(part: Part[F]): Boolean = part.headers.exists(_.value.contains("filename"))

            val stream = response.parts.filter(filterFileTypes).traverse(fileService.store)

            Ok(stream.map(_ => s"Multipart file parsed successfully > ${response.parts}"))
          }
      }

}
