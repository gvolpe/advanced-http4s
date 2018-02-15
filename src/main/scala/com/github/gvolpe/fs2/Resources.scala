package com.github.gvolpe.fs2

import cats.effect.{Effect, IO}
import cats.syntax.functor._
import fs2.StreamApp.ExitCode
import fs2.async.mutable.Semaphore
import fs2.{Scheduler, Stream, StreamApp, async}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object ResourcesApp extends Resources[IO]

/**
  * It demonstrates one of the possible uses of [[fs2.async.mutable.Semaphore]]
  *
  * Three processes are trying to access a shared resource at the same time but only one at
  * a time will be granted access and the next process have to wait until the resource gets
  * available again (availability is one as indicated by the semaphore counter).
  *
  * R1, R2 & R3 will request access of the precious resource concurrently so this could be
  * one possible outcome:
  *
  * R1 >> Availability: 1
  * R2 >> Availability: 1
  * R2 >> Started | Availability: 0
  * R3 >> Availability: 0
  * --------------------------------
  * R1 >> Started | Availability: 0
  * R2 >> Done | Availability: 0
  * --------------------------------
  * R3 >> Started | Availability: 0
  * R1 >> Done | Availability: 0
  * --------------------------------
  * R3 >> Done | Availability: 1
  *
  * This means when R1 and R2 requested the availability it was one and R2 was faster in
  * getting access to the resource so it started processing. R3 was the slowest and saw
  * that there was no availability from the beginning.
  *
  * Once R2 was done R1 started processing immediately showing no availability.
  *
  * Once R1 was done R3 started processing immediately showing no availability.
  *
  * Finally, R3 was done showing an availability of one once again.
  * */
class Resources[F[_]: Effect] extends StreamApp[F] {

  override def stream(args: List[String], requestShutdown: F[Unit]): fs2.Stream[F, ExitCode] =
    Scheduler(corePoolSize = 4).flatMap { implicit scheduler =>
      for {
        s   <- Stream.eval(async.semaphore[F](1))
        r1  = new PreciousResource[F]("R1", s)
        r2  = new PreciousResource[F]("R2", s)
        r3  = new PreciousResource[F]("R3", s)
        ec  <- Stream(r1.use, r2.use, r3.use).join(3).drain ++ Stream.emit(ExitCode.Success)
      } yield ec
    }

}

class PreciousResource[F[_]: Effect](name: String, s: Semaphore[F])
                                    (implicit S: Scheduler) {

  def use: Stream[F, Unit] =
    for {
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Availability: $a")))
      _ <- Stream.eval(s.decrement)
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Started | Availability: $a")))
      _ <- S.sleep(3.seconds)
      _ <- Stream.eval(s.increment)
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Done | Availability: $a")))
    } yield ()

}