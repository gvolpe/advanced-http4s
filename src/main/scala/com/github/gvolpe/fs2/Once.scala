package com.github.gvolpe.fs2

import cats.effect.{Effect, IO}
import fs2.StreamApp.ExitCode
import fs2.async.Promise
import fs2.{Scheduler, Stream, StreamApp, async}

import scala.concurrent.ExecutionContext.Implicits.global

object OnceApp extends Once[IO]

/**
  * Demonstrate the use of [[fs2.async.Promise]]
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
class Once[F[_]: Effect] extends StreamApp[F] {

  override def stream(args: List[String], requestShutdown: F[Unit]): fs2.Stream[F, ExitCode] =
    Scheduler(corePoolSize = 4).flatMap { implicit scheduler =>
      for {
        p <- Stream.eval(async.promise[F, Int])
        e <- new ConcurrentCompletion[F](p).start
      } yield e
    }

}

class ConcurrentCompletion[F[_]](p: Promise[F, Int])(implicit F: Effect[F]) {

  private def attemptPromiseCompletion(n: Int): Stream[F, Unit] =
    Stream.eval(p.complete(n)).attempt.drain

  def start: Stream[F, ExitCode] =
    Stream(
      attemptPromiseCompletion(1),
      attemptPromiseCompletion(2),
      Stream.eval(p.get).evalMap(n => F.delay(println(s"Result: $n")))
    ).join(3).drain ++ Stream.emit(ExitCode.Success)

}
