package aqua.files

import fs2.io.file.Path
import org.scalacheck.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scala.math.sqrt

class ImportsSpec extends AnyFlatSpec with ScalaCheckPropertyChecks with Matchers {

  implicit override val generatorDrivenConfig =
    // Tests here are lightweight, so we can afford to run more of them
    PropertyCheckConfiguration(minSuccessful = 10000)

  val shortAlphaNumStr = for {
    length <- Gen.choose(1, 10)
    chars <- Gen.listOfN(5, Gen.alphaNumChar)
  } yield chars.mkString

  val fileNameWithExt = Gen
    .zip(
      shortAlphaNumStr,
      shortAlphaNumStr
    )
    .map((name, ext) => s"$name.$ext")

  given Arbitrary[Path] = Arbitrary(
    Gen.sized(size =>
      for {
        segments <- Gen.listOfN(
          size / 5,
          Gen.oneOf(
            shortAlphaNumStr,
            Gen.oneOf(".", "..")
          )
        )
        prefix <- Gen.oneOf("", "/", "~/")
        suffix <- Gen.oneOf(
          Gen.oneOf("", "/"),
          fileNameWithExt
        )
        str = (prefix +: segments :+ suffix).mkString("/")
      } yield Path(str)
    )
  )

  // Paths without "..", ".", "~" and absolute paths
  val simplePath: Gen[Path] = Gen.sized(size =>
    for {
      segments <- Gen.listOfN(
        size / 5,
        shortAlphaNumStr
      )
      suffix <- Gen.option(
        fileNameWithExt
      )
      path = segments.appendedAll(suffix).mkString("/")
    } yield Path(path)
  )

  val simpleNonEmptyPath: Gen[Path] =
    for {
      prefix <- shortAlphaNumStr.map(Path.apply)
      suffix <- simplePath
    } yield prefix / suffix

  given Arbitrary[Imports] = Arbitrary(
    Gen.sized { size =>
      val N = sqrt(size).toInt
      val pathResized = Gen.resize(N, Arbitrary.arbitrary[Path])
      Gen
        .mapOfN(
          N,
          Gen.zip(
            pathResized,
            Gen
              .mapOfN(
                N,
                Gen.zip(
                  Gen.asciiPrintableStr,
                  Gen.listOfN(N, pathResized)
                )
              )
              .map(Imports.PathSettings.apply)
          )
        )
    }.map(Imports.apply)
  )

  val nonEmptyAsciiPrintableStr: Gen[String] =
    Gen.nonEmptyListOf(Gen.asciiPrintableChar).map(_.mkString)

  "Imports" should "resolve relative import first" in {
    forAll(
      Arbitrary.arbitrary[Imports],
      Arbitrary.arbitrary[Path].filter(_.parent.isDefined),
      Arbitrary.arbitrary[Path]
    ) { (imports, path, imported) =>
      val resolved = imports.resolutions(path, imported.toString)
      val parent = path.parent.get
      resolved.headOption should be(Some(parent.resolve(imported)))
    }
  }

  it should "take the longest path prefix" in {
    forAll(
      Arbitrary.arbitrary[Imports],
      Arbitrary.arbitrary[Path],
      simpleNonEmptyPath,
      simpleNonEmptyPath,
      Gen.asciiPrintableStr
    ) { (imports, prefix, middle, suffix, imported) =>
      val shortPrefix = prefix
      val longPrefix = prefix / middle
      val path = prefix / middle / suffix
      val shortLocation = Path("short/path")
      val longLocation = Path("long/path")
      val importsPrepared = imports.copy(
        settings = imports.settings
          .filterKeys(p => !p.startsWith(prefix))
          .toMap
          .updated(shortPrefix, Imports.PathSettings(Map(imported -> List(shortLocation))))
          .updated(longPrefix, Imports.PathSettings(Map(imported -> List(longLocation))))
      )
      val resolved = importsPrepared.resolutions(path, imported)
      resolved should not contain (shortLocation)
      resolved should contain(longLocation)
    }
  }

  it should "rewrite the longest import prefix" in {
    forAll(
      Arbitrary.arbitrary[Imports],
      simpleNonEmptyPath,
      simplePath,
      nonEmptyAsciiPrintableStr,
      nonEmptyAsciiPrintableStr,
      nonEmptyAsciiPrintableStr
    ) { (imports, pathPrefix, pathSuffix, prefix, middle, suffix) =>
      val path = pathPrefix / pathSuffix
      val shortPrefix = prefix
      val longPrefix = prefix + middle
      val imported = prefix + middle + suffix
      val shortLocation = Path("short/path")
      val longLocation = Path("long/path")
      val importsPrepared = imports.copy(
        settings = imports.settings
          .filterKeys(p => !p.startsWith(pathPrefix))
          .toMap
          .updated(
            pathPrefix,
            Imports.PathSettings(
              Map(
                shortPrefix -> List(shortLocation),
                longPrefix -> List(longLocation)
              )
            )
          )
      )
      val resolved = importsPrepared.resolutions(path, imported)
      resolved should contain(longLocation / suffix)
    }
  }
}
