package com.github.gvolpe.cats_effects

import cats.Monad
import cats.effect._
import cats.effect.IO
import cats.effect.concurrent.Ref

/**
  * Concurrent counter that demonstrates the use of [[cats.effect.concurrent.Ref]].
  *
  * The workers will concurrently run and modify the value of the Ref so this is one
  * possible outcome showing "#worker >> currentCount":
  *
  * #1 >> 0
  * #3 >> 0
  * #2 >> 0
  * #1 >> 2
  * #2 >> 3
  * #3 >> 3
  * */
object CounterApp extends IOApp {
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  def example[F[_]: ConsoleOut](implicit F: Concurrent[F]): F[Unit] =
    for {
      ref <- Ref.of[F, Int](0)
      w1 = Worker.exec(1, ref)
      w2 = Worker.exec(2, ref)
      w3 = Worker.exec(3, ref)
      f1 <- F.start(w1)
      f2 <- F.start(w2)
      f3 <- F.start(w3)
      _ <- f1.join
      _ <- f2.join
      _ <- f3.join
    } yield ()

  override def run(args: List[String]): IO[ExitCode] = {
    // TODO: When this PR is merged: https://github.com/gvolpe/console4cats/pull/22, prefer `import cats.effect.Console.implicits._`
    implicit val console: Console[IO] = cats.effect.Console.io

    example[IO].as(ExitCode.Success)
  }
}

object Worker {
  import cats.syntax.flatMap._
  import cats.syntax.apply._

  private def printRefContent[F[_]: Monad: ConsoleOut](number: Int, ref: Ref[F, Int]): F[Unit] =
    ref.get.flatMap(n => ConsoleOut[F].putStrLn(s"#$number >> $n"))

  final def exec[F[_]: Monad: ConsoleOut](number: Int, ref: Ref[F, Int]): F[Unit] =
    printRefContent(number, ref) *> ref.update(_ + 1) *> printRefContent(number, ref)

}
