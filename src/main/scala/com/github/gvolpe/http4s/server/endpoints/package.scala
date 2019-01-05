package com.github.gvolpe.http4s.server

import cats.effect.Sync
import org.http4s.EntityDecoder

import scala.xml._

final case class Person(name: String, age: Int)

/**
  * XML Example for Person:
  *
  * <person>
  *   <name>gvolpe</name>
  *   <age>30</age>
  * </person>
  * */
object Person {
  def fromXml(elem: Elem): Person = {
    val name = (elem \\ "name").text
    val age = (elem \\ "age").text
    Person(name, age.toInt)
  }
}

package object endpoints {
  val ApiVersion = "v1"

  def personXmlDecoder[F[_]: Sync]: EntityDecoder[F, Person] =
    org.http4s.scalaxml.xml[F].map(Person.fromXml)

}
