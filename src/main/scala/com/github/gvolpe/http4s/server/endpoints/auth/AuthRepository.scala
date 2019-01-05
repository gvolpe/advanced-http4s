package com.github.gvolpe.http4s.server.endpoints.auth

import cats.effect.Sync
import cats.implicits._
import org.http4s.BasicCredentials

trait AuthRepository[F[_], A] {
  def persist(entity: A): F[Unit]
  def find(entity: A): F[Option[A]]
}

object AuthRepository {

  implicit def authUserRepo[F[_]](implicit F: Sync[F]): AuthRepository[F, BasicCredentials] =
    new AuthRepository[F, BasicCredentials] {
      private val storage = scala.collection.mutable.Set[BasicCredentials](
        BasicCredentials("gvolpe", "123456")
      )
      override def persist(entity: BasicCredentials): F[Unit] = F.delay(storage.add(entity)).void
      override def find(entity: BasicCredentials): F[Option[BasicCredentials]] = F.delay(storage.find(_ == entity))
    }

}
