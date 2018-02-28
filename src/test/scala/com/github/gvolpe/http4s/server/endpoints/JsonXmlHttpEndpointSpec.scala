package com.github.gvolpe.http4s.server.endpoints

import cats.effect.IO
import com.github.gvolpe.http4s.IOAssertion
import org.http4s.{Header, HttpService, Method, Request, Status, Uri}
import org.scalatest.FunSuite

class JsonXmlHttpEndpointSpec extends FunSuite {

  private val httpService: HttpService[IO] = new JsonXmlHttpEndpoint[IO].service

  private val jsonPerson =
    """
      |{
      |  "name": "gvolpe",
      |  "age": 30
      |}
    """.stripMargin

  private val xmlPerson =
    """
      |<person>
      |  <name>gvolpe</name>
      |  <age>30</age>
      |</person>
    """.stripMargin

  private val request = Request[IO](method = Method.POST, uri = Uri(path = s"/$ApiVersion/media"))

  test("json is decoded") {
    IOAssertion {
      val bodyRequest = request.withBody[String](jsonPerson)

      bodyRequest.flatMap { req =>
        httpService(req.putHeaders(Header("Content-Type", "application/json"))).value.map { maybe =>
          maybe.fold(fail("Empty response")) { response =>
            assert(response.status == Status.Ok)
          }
        }
      }
    }
  }

  test("xml is decoded") {
    IOAssertion {
      val bodyRequest = request.withBody[String](xmlPerson)

      bodyRequest.flatMap { req =>
        httpService(req.putHeaders(Header("Content-Type", "application/xml"))).value.map { maybe =>
          maybe.fold(fail("Empty response")) { response =>
            assert(response.status == Status.Ok)
          }
        }
      }
    }
  }

  test("decoding fails, no Content Type") {
    IOAssertion {
      val bodyRequest = request.withBody[String](jsonPerson)

      bodyRequest.flatMap { req =>
        httpService(req).value.attempt.map {
          case Left(e)  => assert(e.getMessage == "Malformed message body: Invalid XML")
          case Right(_) => fail("Got a response when a failure was expected")
        }
      }

      // Using `req.decode` gives you a response, using `req.as` throws an exception
      // https://gitter.im/http4s/http4s?at=5a964662758c233504cc0fec
//      bodyRequest.flatMap { req =>
//        httpService(req).value.map { maybe =>
//          maybe.fold(fail("Empty response")) { response =>
//            assert(response.status == Status.BadRequest)
//          }
//        }
//      }
    }
  }

}
