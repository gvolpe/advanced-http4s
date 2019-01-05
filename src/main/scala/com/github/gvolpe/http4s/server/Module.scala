package com.github.gvolpe.http4s.server

import cats.data.OptionT
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import com.github.gvolpe.http4s.server.endpoints._
import com.github.gvolpe.http4s.server.endpoints.auth.{BasicAuthHttpEndpoint, GitHubHttpEndpoint}
import com.github.gvolpe.http4s.server.service.{FileService, GitHubService}
import org.http4s.HttpRoutes
import org.http4s.client.Client
import org.http4s.server.HttpMiddleware
import org.http4s.server.middleware.{AutoSlash, ChunkAggregator, GZip, Timeout}
import io.chrisdavenport.log4cats.Logger

import scala.concurrent.duration._

class Module[F[_]: ConcurrentEffect: ContextShift: Timer](client: Client[F], logger: Logger[F]) {

  private val fileService = new FileService[F]

  private val gitHubService = new GitHubService[F](client)

  def middleware: HttpMiddleware[F] =
    { service: HttpRoutes[F] => GZip(service) } compose { service => AutoSlash(service) }

  val fileHttpEndpoint: HttpRoutes[F] =
    new FileHttpEndpoint[F](fileService).service

  val nonStreamFileHttpEndpoint: HttpRoutes[F] = ChunkAggregator(OptionT.liftK[F])(fileHttpEndpoint)

  private val hexNameHttpEndpoint: HttpRoutes[F] =
    new HexNameHttpEndpoint[F].service

  private val compressedEndpoints: HttpRoutes[F] =
    middleware(hexNameHttpEndpoint)

  private val timeoutHttpEndpoint: HttpRoutes[F] =
    new TimeoutHttpEndpoint[F].service

  private val timeoutEndpoints: HttpRoutes[F] =
    Timeout(1.second)(timeoutHttpEndpoint)

  private val mediaHttpEndpoint: HttpRoutes[F] =
    new JsonXmlHttpEndpoint[F].service

  private val multipartHttpEndpoint: HttpRoutes[F] =
      new MultipartHttpEndpoint[F](fileService, logger).service

  private val gitHubHttpEndpoint: HttpRoutes[F] =
    new GitHubHttpEndpoint[F](gitHubService).service

  val basicAuthHttpEndpoint: HttpRoutes[F] =
    new BasicAuthHttpEndpoint[F].service

  // NOTE: If you mix services wrapped in `AuthMiddleware[F, ?]` the entire namespace will be protected.
  // You'll get 401 (Unauthorized) instead of 404 (Not found). Mount it separately as done in Server.
  val httpServices: HttpRoutes[F] = {
    import cats.syntax.semigroupk._ // ⚠️ IntelliJ doesn't understand the need for this import

    compressedEndpoints <+>
      timeoutEndpoints <+>
      mediaHttpEndpoint <+>
      multipartHttpEndpoint <+>
      gitHubHttpEndpoint
  }

}
