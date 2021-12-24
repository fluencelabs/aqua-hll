package aqua

import cats.data.{NonEmptyList, Validated}
import com.monovore.decline.Opts
import scribe.Level

import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.applicative.*
import cats.syntax.apply.*

import java.util.Base64

case class Common(
  timeout: Int,
  logLevel: Level,
  multiaddr: String,
  printAir: Boolean,
  secretKey: Option[Array[Byte]]
)

object FluenceOpts {

  val timeoutOpt: Opts[Int] =
    Opts
      .option[Int]("timeout", "Request timeout in milliseconds", "t")
      .withDefault(7000)

  val multiaddrOpt: Opts[String] =
    Opts
      .option[String]("addr", "Relay multiaddress", "a")

  val secretKeyOpt: Opts[Array[Byte]] =
    Opts
      .option[String]("sk", "Ed25519 32-byte secret key in base64", "s")
      .mapValidated { s =>
        val decoder = Base64.getDecoder
        Validated.catchNonFatal {
          decoder.decode(s)
        }.leftMap(t => NonEmptyList.one("secret key isn't a valid base64 string: " + t.getMessage))
      }

  val printAir: Opts[Boolean] =
    Opts
      .flag("print-air", "Prints generated AIR code before function execution")
      .map(_ => true)
      .withDefault(false)

  val logLevelOpt: Opts[Level] =
    Opts.option[String]("log-level", help = "Set log level").withDefault("info").mapValidated {
      str =>
        Validated.fromEither(toLogLevel(str))
    }

  def toLogLevel(logLevel: String): Either[NonEmptyList[String], Level] = {
    LogLevel.stringToLogLevel
      .get(logLevel.toLowerCase)
      .toRight(
        NonEmptyList(
          "log-level could be only 'all', 'trace', 'debug', 'info', 'warn', 'error', 'off'",
          Nil
        )
      )
  }

  val commonOpt: Opts[Common] =
    (timeoutOpt, logLevelOpt, multiaddrOpt, printAir, AppOpts.wrapWithOption(secretKeyOpt))
      .mapN(Common.apply)
}
