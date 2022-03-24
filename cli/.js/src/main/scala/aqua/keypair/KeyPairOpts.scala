package aqua.keypair

import aqua.io.OutputPrinter
import aqua.js.KeyPair
import aqua.keypair.KeyPairShow.show
import cats.effect.ExitCode
import cats.effect.kernel.Async
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.show.*
import cats.syntax.applicative.*
import cats.syntax.functor.*
import cats.{Applicative, Monad}
import com.monovore.decline.{Command, Opts}
import scribe.Logging

import scala.concurrent.{ExecutionContext, Future}

// Options and commands to work with KeyPairs
object KeyPairOpts extends Logging {

  def command[F[_]: Async]: Command[F[ExitCode]] =
    Command(name = "key", header = "Manage local keys and identity") {
      Opts.subcommands(
        createKeypair
      )
    }

  // KeyPair generation
  def createKeypair[F[_]: Async]: Command[F[ExitCode]] =
    Command(
      name = "create",
      header = "Create a new keypair"
    ) {
      Opts.unit.map(_ =>
        Async[F]
          .fromFuture(
            KeyPair.randomEd25519().toFuture.pure[F]
          )
          .map(keypair =>
            OutputPrinter.print(s"${keypair.show}")
            ExitCode.Success
          )
      )
    }
}
