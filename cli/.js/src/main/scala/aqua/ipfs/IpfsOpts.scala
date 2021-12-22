package aqua.ipfs

import aqua.{AppOpts, LogFormatter, LogLevelTransformer, OptUtils}
import aqua.io.OutputPrinter
import aqua.js.{Fluence, PeerConfig}
import aqua.keypair.KeyPairShow.show
import cats.data.{NonEmptyChain, NonEmptyList, Validated, ValidatedNec, ValidatedNel}
import Validated.{invalid, invalidNec, valid, validNec, validNel}
import aqua.ipfs.js.IpfsApi
import aqua.run.RunCommand.createKeyPair
import aqua.run.RunOpts
import cats.effect.{Concurrent, ExitCode, Resource, Sync}
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.applicative.*
import cats.syntax.apply.*
import cats.effect.kernel.Async
import cats.syntax.show.*
import cats.{Applicative, Monad}
import com.monovore.decline.{Command, Opts}
import fs2.io.file.Files
import scribe.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

// Options and commands to work with IPFS
object IpfsOpts extends Logging {

  val multiaddrOpt: Opts[String] =
    Opts
      .option[String]("addr", "Relay multiaddress", "a")

  def pathOpt[F[_]: Files: Concurrent]: Opts[String] =
    Opts
      .option[String]("path", "Path to file", "p")

  // Uploads a file to IPFS
  def upload[F[_]: Async](implicit ec: ExecutionContext): Command[F[ExitCode]] =
    Command(
      name = "upload",
      header = "Upload a file to IPFS"
    ) {
      (
        pathOpt,
        multiaddrOpt,
        AppOpts.logLevelOpt,
        AppOpts.wrapWithOption(RunOpts.secretKeyOpt),
        RunOpts.timeoutOpt
      ).mapN { (path, multiaddr, logLevel, secretKey, timeout) =>
        LogFormatter.initLogger(Some(logLevel))
        val resource = Resource.make(Fluence.getPeer().pure[F]) { peer =>
          Async[F].fromFuture(Sync[F].delay(peer.stop().toFuture))
        }
        resource.use { peer =>
          Async[F].fromFuture {
            (for {
              keyPair <- createKeyPair(secretKey)
              _ <- Fluence
                .start(
                  PeerConfig(
                    multiaddr,
                    timeout,
                    LogLevelTransformer.logLevelToAvm(logLevel),
                    keyPair.orNull
                  )
                )
                .toFuture
              cid <- IpfsApi
                .uploadFile(
                  path,
                  peer,
                  logger.info: String => Unit,
                  logger.error: String => Unit
                )
                .toFuture
            } yield {
              ExitCode.Success
            }).pure[F]
          }
        }

      }
    }
}
