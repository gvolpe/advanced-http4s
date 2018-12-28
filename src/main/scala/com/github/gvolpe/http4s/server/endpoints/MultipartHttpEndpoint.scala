package com.github.gvolpe.http4s.server.endpoints

import cats.effect.{ContextShift, Sync}
import cats.implicits._
import com.github.gvolpe.http4s.server.service.FileService
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.{Multipart, Part}
import org.slf4j.LoggerFactory

final class MultipartHttpEndpoint[F[_]: ContextShift](fileService: FileService[F])
                                                     (implicit F: Sync[F]) extends Http4sDsl[F] {

  private val logger = LoggerFactory.getLogger(this.getClass)

  final val service: HttpRoutes[F] =
    HttpRoutes
      .of[F] {
        case GET -> Root / ApiVersion / "multipart" =>
          Ok("Send a file (image, sound, etc) via POST Method")

        case req @ POST -> Root / ApiVersion / "multipart" =>
          req.decodeStrict[Multipart[F]] { response =>
            def filterFileTypes(part: Part[F]): Boolean = part.headers.exists(_.value.contains("filename"))

            val stream =
              response
                .parts
                .filter(filterFileTypes)
                .map { v => logger.debug(s"HUUUUUUUUUUUUUGE 11: $v"); v }
                .traverse { v =>
                  fileService.store(v, logger).attempt.map { v => logger.error(s"HUUUUUUUUUUUUUGE 22: $v"); v }
                }

            Ok(stream.map { _ => logger.error("HUUUUUUUUUUUUUGE 33") }.map(_ => s"Multipart file parsed successfully > ${response.parts}"))
          }
      }

}
