package com.github.gvolpe.http4s.server.service

import java.io.File

import cats.effect.Sync
import com.github.gvolpe.http4s.StreamUtils
import fs2.Stream

class FileService[F[_]](implicit F: Sync[F], S: StreamUtils[F]) {

  def homeDirectories(depth: Option[Int]): Stream[F, String] =
    S.env("HOME").flatMap { maybePath =>
      val ifEmpty = S.error("HOME environment variable not found!")
      maybePath.fold(ifEmpty)(directories(_, depth.getOrElse(1)))
    }

  def directories(path: String, depth: Int): Stream[F, String] = {

    // Maybe the iterator approach is faster
    def dir(f: File, d: Int): Stream[F, File] = {
      val dirs = Stream.emits(f.listFiles().toSeq).filter(_.isDirectory)

      if (d <= 0) Stream.empty
      else if (d == 1) dirs
      else dirs ++ dirs.flatMap(x => dir(x, d - 1))
    }

    S.evalF(new File(path)).flatMap { file =>
      dir(file, depth)
        .map(_.getName)
        .filter(!_.startsWith("."))
        .intersperse("\n")
    }

//    val it = file.listFiles().iterator
//    def loop(pred: Boolean): Stream[F, String] = {
//      if (pred)
//        Stream.eval(F.delay(it.next()))
//          .filter(_.isDirectory)
//          .map(_.getCanonicalPath) ++ loop(it.hasNext)
//      else Stream.empty
//    }
//    loop(it.hasNext)
  }

//  def streamingFiles: Stream[F, String] =
//    io.file.readAll[F](Paths.get(""), 4096)
//      .through(text.utf8Decode)
//      .through(text.lines)

}
