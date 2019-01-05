package com.github.gvolpe.http4s.server.endpoints

import cats.effect.Effect
import cats.syntax.flatMap._
import com.github.gvolpe.http4s.server.Person
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, _}

// Docs: http://http4s.org/v0.20/entity/
class JsonXmlHttpEndpoint[F[_]: Effect] extends Http4sDsl[F] {

  implicit def jsonXmlDecoder: EntityDecoder[F, Person] = jsonOf[F, Person] orElse personXmlDecoder[F]

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / ApiVersion / "media" =>
      Ok(
        "Send either json or xml via POST method. Eg: \n{\n  \"name\": \"gvolpe\",\n  \"age\": 30\n}\n or \n <person>\n  <name>gvolpe</name>\n  <age>30</age>\n</person>")

    case req @ POST -> Root / ApiVersion / "media" =>
      req.as[Person].flatMap { person =>
        Ok(s"Successfully decoded person: ${person.name}")
      }
  }

}
