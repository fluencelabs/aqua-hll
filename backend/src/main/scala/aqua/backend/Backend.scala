package aqua.backend

import aqua.model.AquaContext
import aqua.res.AquaRes
import cats.data.ValidatedNec

/**
 * Compiler backend generates output based on the processed model
 */
trait Backend {

  /**
   * Generate the result based on the given [[AquaRes]]
   *
   * @param aqua Source file context, processed, transformed
   * @return Zero or more [[Generated]] objects, based on arguments
   */
  def generate(aqua: AquaRes, airChecker: String => ValidatedNec[String, Unit]): ValidatedNec[String, Seq[Generated]]
}

object Backend {

  trait Transform extends Backend {
    def transform(ex: AquaContext): AquaRes
  }
}
