package com.github.gvolpe.http4s.server.endpoints

import cats.effect.Effect
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

// Docs: http://http4s.org/v0.18/entity/
class JsonXmlHttpEndpoint[F[_]: Effect] extends Http4sDsl[F] {

  implicit def jsonXmlDecoder: EntityDecoder[F, Person] = jsonOf[F, Person] orElse personXmlDecoder[F]

  val service: HttpService[F] = HttpService {
    case GET -> Root / ApiVersion / "media" =>
      Ok("Send either json or xml via POST method. Eg: \n{\n  \"name\": \"gvolpe\",\n  \"age\": 30\n}\n or \n <person>\n  <name>gvolpe</name>\n  <age>30</age>\n</person>")

    case req @ POST -> Root / ApiVersion / "media" =>
      req.decode[Person] { person =>
        Ok(s"Successfully decoded person: ${person.name}")
      }
  }

}
