package com.github.gvolpe.fs2

import cats.effect._
import com.github.gvolpe.fs2.CounterApp.stream
import fs2.Stream
import fs2.concurrent.Queue

/**
  * Represents a FIFO (First IN First OUT) system built on top of two [[fs2.concurrent.Queue]].
  *
  * q1 has a buffer size of 1 while q2 has a buffer size of 100 so you will notice the buffering when
  * pulling elements out of the q2.
  * */
object FifoApp extends IOApp {

  def stream[F[_]: ConcurrentEffect]: fs2.Stream[F, Unit] =
    for {
      q1 <- Stream.eval(Queue.bounded[F, Int](1))
      q2 <- Stream.eval(Queue.bounded[F, Int](100))
      _ <- new Buffering[F](q1, q2).start
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

}

class Buffering[F[_]](q1: Queue[F, Int], q2: Queue[F, Int])(
    implicit F: Concurrent[F]) {

  def start: Stream[F, Unit] =
    Stream(
      Stream.range(0, 1000).covary[F] to q1.enqueue,
      q1.dequeue to q2.enqueue,
      //.map won't work here as you're trying to map a pure value with a side effect. Use `evalMap` instead.
      q2.dequeue.evalMap(n => F.delay(println(s"Pulling out $n from Queue #2")))
    ).parJoin(3)

}
