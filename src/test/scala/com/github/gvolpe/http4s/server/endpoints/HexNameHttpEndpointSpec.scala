package com.github.gvolpe.http4s.server.endpoints

import cats.effect.IO
import com.github.gvolpe.http4s.IOAssertion
import org.http4s.server.middleware.GZip
import org.http4s.{Header, HttpService, Query, Request, Uri}
import org.scalatest.FunSuite

// Docs: http://http4s.org/v0.18/gzip/
class HexNameHttpEndpointSpec extends FunSuite {

  private val httpService: HttpService[IO] = GZip(new HexNameHttpEndpoint[IO].service)

  private val CompressedLength  = 74
  private val NormalLength      = 88

  private val request = Request[IO](uri =
    Uri(
      path = s"/$ApiVersion/hex",
      query = Query("name" -> Some("Scala is a really cool programming language!"))
    )
  )

  test("Compressed Response") {
    IOAssertion {
      val gzipHeader  = Header("Accept-Encoding", "gzip")
      val gzipRequest = request.putHeaders(gzipHeader)

      httpService(gzipRequest).value.flatMap { maybe =>
        maybe.fold(IO[Unit](fail("Empty response"))) { response =>
          response.as[String].map(r => assert(r.length == CompressedLength))
        }
      }
    }
  }

  test("Uncompressed Response") {
    IOAssertion {
      httpService(request).value.flatMap { maybe =>
        maybe.fold(IO[Unit](fail("Empty response"))) { response =>
          response.as[String].map(r => assert(r.length == NormalLength))
        }
      }
    }
  }

}
