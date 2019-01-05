package com.github.gvolpe.cats_effect

import cats.Monad
import cats.effect._
import cats.effect.concurrent.Semaphore
import cats.implicits._
import cats.temp.par._

import scala.concurrent.duration._

/**
  * It demonstrates one of the possible uses of [[cats.effect.concurrent.Semaphore]]
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
object ResourcesApp extends IOApp {

  def stream[F[_]: Concurrent: NonEmptyPar: Timer: ConsoleOut]: F[Unit] =
      for {
        s   <- Semaphore[F](1)
        r1  = new PreciousResource[F]("R1", s)
        r2  = new PreciousResource[F]("R2", s)
        r3  = new PreciousResource[F]("R3", s)
        _  <- (r1.use, r2.use, r3.use).parTupled.void
      } yield ()

  override def run(args: List[String]): IO[ExitCode] = {
    // TODO: When this PR is merged: https://github.com/gvolpe/console4cats/pull/22, prefer `import cats.effect.Console.implicits._`
    implicit val console: Console[IO] = cats.effect.Console.io

    stream[IO].as(ExitCode.Success)
  }

}

class PreciousResource[F[_]: Monad: Timer](name: String, s: Semaphore[F])(implicit F: ConsoleOut[F]) {

  val use: F[Unit] =
    for {
      _ <- s.available.flatMap(a => F.putStrLn(s"$name >> Availability: $a"))
      _ <- s.acquire
      _ <- s.available.flatMap(a => F.putStrLn(s"$name >> Started | Availability: $a"))
      _ <- Timer[F].sleep(3.seconds)
      _ <- s.release
      _ <- s.available.flatMap(a => F.putStrLn(s"$name >> Done | Availability: $a"))
    } yield ()

}