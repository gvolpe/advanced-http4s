package com.github.gvolpe.fs2

import cats.effect._
import cats.effect.concurrent.Ref
import fs2.{Sink, Stream}

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
  import cats.syntax.all._

  def stream[F[_]: ConcurrentEffect]: Stream[F, Unit] =
    for {
      ref <- Stream.eval(Ref.of[F, Int](0))
      w1 = new Worker[F](1, ref)
      w2 = new Worker[F](2, ref)
      w3 = new Worker[F](3, ref)
      _ <- Stream(w1.start, w2.start, w3.start).parJoin(3)
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    stream[IO].compile.drain.as(ExitCode.Success)
}

class Worker[F[_]](number: Int, ref: Ref[F, Int])(implicit F: Sync[F]) {

  private val sink: Sink[F, Int] =
    _.evalMap(n => F.delay(println(s"#$number >> $n")))

  def start: Stream[F, Unit] =
    for {
      _ <- Stream.eval(ref.get) to sink
      _ <- Stream.eval(ref.update(_ + 1))
      _ <- Stream.eval(ref.get) to sink
    } yield ()

}
