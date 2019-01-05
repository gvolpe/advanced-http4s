package com.github.gvolpe.http4s.server.service

import java.io.File
import java.nio.file.Paths

import cats.effect.{ContextShift, Effect}
import fs2.Stream
import org.http4s.multipart.Part
import cats.implicits._
import com.github.gvolpe.http4s.server.Server
import io.chrisdavenport.log4cats.Logger

class FileService[F[_]](implicit F: Effect[F]) {

  private def env(key: String): F[String] =
    F
      .delay(sys.env.get(key))
      .flatMap(F.fromOption(_, new Exception(s"$key environment variable not found!")))

  private def file(path: String): F[File] = F.delay(new File(path))

  def homeDirectories(depth: Option[Int]): Stream[F, String] =
    Stream.eval(env("HOME")).flatMap(path => directories(path, depth.getOrElse(1)))

  def directories(path: String, depth: Int): Stream[F, String] = {

    def dir(f: File, d: Int): Stream[F, File] = {
      val dirs = Stream.emits(f.listFiles().toSeq).filter(_.isDirectory).covary[F]

      if (d <= 0) Stream.empty
      else if (d == 1) dirs
      else dirs ++ dirs.flatMap(x => dir(x, d - 1))
    }

    Stream.eval(file(path)).flatMap { file =>
      dir(file, depth)
        .map(_.getName)
        .filter(!_.startsWith("."))
        .intersperse("\n")
    }
  }

  def store(logger: Logger[F])(part: Part[F])(implicit cs: ContextShift[F]): Stream[F, Part[F]] =
    part.body
      .to(fs2.io.file.writeAll(Paths.get("/tmp/sample"), Server.ioScheduler)) // TODO: BUG ?
      .flatMap(_ => fs2.Stream(part))
      .evalTap((p: Part[F]) => logger.debug(s"CONTINUE ?????????????????????: $p"))

}
