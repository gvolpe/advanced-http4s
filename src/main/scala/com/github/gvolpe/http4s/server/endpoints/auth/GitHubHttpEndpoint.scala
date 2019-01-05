package com.github.gvolpe.http4s.server.endpoints.auth

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.http4s.server.endpoints.ApiVersion
import com.github.gvolpe.http4s.server.service.GitHubService
import org.http4s._
import org.http4s.dsl.Http4sDsl

class GitHubHttpEndpoint[F[_]](gitHubService: GitHubService[F])
                              (implicit F: Sync[F]) extends Http4sDsl[F] {

  object CodeQuery extends QueryParamDecoderMatcher[String]("code")
  object StateQuery extends QueryParamDecoderMatcher[String]("state")

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / ApiVersion / "github" =>
      Ok(gitHubService.authorize)

    // OAuth2 Callback URI
    case GET -> Root / ApiVersion / "login" / "github" :? CodeQuery(code) :? StateQuery(state) =>
      Ok(gitHubService.accessToken(code, state).flatMap(gitHubService.userData))
        .map(_.putHeaders(Header("Content-Type", "application/json")))
  }

}
