package com.github.gvolpe.cats_effects

import cats.effect._
import cats.effect.concurrent.Deferred
import cats.implicits._
import cats.temp.par._

/**
  * Demonstrate the use of [[cats.effect.concurrent.Deferred]]
  *
  * Two processes will try to complete the promise at the same time but only one will succeed,
  * completing the promise exactly once.
  * The loser one will raise an error when trying to complete a promise already completed,
  * that's why we call `attempt` on the evaluation.
  *
  * Notice that the loser process will remain running in the background and the program will
  * end on completion of all of the inner streams.
  *
  * So it's a "race" in the sense that both processes will try to complete the promise at the
  * same time but conceptually is different from "race". So for example, if you schedule one
  * of the processes to run in 10 seconds from now, then the entire program will finish after
  * 10 seconds and you can know for sure that the process completing the promise is going to
  * be the first one.
  * */
object OnceApp extends IOApp {

  def stream[F[_]: Concurrent: Par: ConsoleOut]: F[Unit] =
    for {
      p <- Deferred[F, Int]
      _ <- new ConcurrentCompletion[F](p).exec
    } yield ()

  override def run(args: List[String]): IO[ExitCode] = {
    // TODO: When this PR is merged: https://github.com/gvolpe/console4cats/pull/22, prefer `import cats.effect.Console.implicits._`
    implicit val console: Console[IO] = cats.effect.Console.io

    stream[IO].as(ExitCode.Success)
  }

}


class ConcurrentCompletion[F[_]: Sync: Par](p: Deferred[F, Int])(implicit F: ConsoleOut[F]) {

  val exec: F[Unit] =
    List(
      p.complete(1),
      p.complete(2),
      p.get.flatMap(n => F.putStrLn(s"Result: $n"))
    ).parTraverse(_.attempt).void

}
