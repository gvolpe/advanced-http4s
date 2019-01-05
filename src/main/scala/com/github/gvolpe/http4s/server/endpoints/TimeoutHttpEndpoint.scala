package com.github.gvolpe.http4s.server.endpoints

import java.util.concurrent.TimeUnit

import cats.effect.{Concurrent, Timer}
import org.http4s._
import org.http4s.dsl.Http4sDsl

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class TimeoutHttpEndpoint[F[_]](implicit F: Concurrent[F], T: Timer[F]) extends Http4sDsl[F] {

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / ApiVersion / "timeout" =>
      val randomDuration = FiniteDuration(Random.nextInt(3) * 1000L, TimeUnit.MILLISECONDS)
      val response = Ok("delayed response")
      Concurrent.timeoutTo(response, randomDuration, GatewayTimeout())
  }

}
