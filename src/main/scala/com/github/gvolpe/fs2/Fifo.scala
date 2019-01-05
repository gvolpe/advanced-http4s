package com.github.gvolpe.fs2

import cats.effect._
import fs2.Stream
import fs2.concurrent.Queue
import cats.implicits._

/**
  * Represents a FIFO (First IN First OUT) system built on top of two [[fs2.concurrent.Queue]].
  *
  * q1 has a buffer size of 1 while q2 has a buffer size of 100 so you will notice the buffering when
  * pulling elements out of the q2.
  * */
object FifoApp extends IOApp {

  def stream[F[_]: ConcurrentEffect: ConsoleOut]: F[Unit] =
    for {
      q1 <- Queue.bounded[F, Int](1)
      q2 <- Queue.bounded[F, Int](100)
      _  <- Buffering.exec(q1, q2).compile.drain
    } yield ()

  override def run(args: List[String]): IO[ExitCode] = {
    // TODO: When this PR is merged: https://github.com/gvolpe/console4cats/pull/22, prefer `import cats.effect.Console.implicits._`
    implicit val console: Console[IO] = cats.effect.Console.io

    stream[IO].as(ExitCode.Success)
  }

}

object Buffering {

  def exec[F[_]](q1: Queue[F, Int], q2: Queue[F, Int])(implicit F: Concurrent[F], C: ConsoleOut[F]): Stream[F, Unit] =
    Stream(
      Stream.range(0, 1000).covary[F] to q1.enqueue,
      q1.dequeue to q2.enqueue,
      //.map won't work here as you're trying to map a pure value with a side effect. Use `evalMap` instead.
      q2.dequeue.evalMap(n => C.putStrLn(s"Pulling out $n from Queue #2"))
    ).parJoin(3)

}
