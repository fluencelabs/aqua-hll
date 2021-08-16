package aqua.backend.ts

import aqua.backend.Version
import aqua.model.res.AquaRes

case class TypeScriptFile(res: AquaRes) {

  import TypeScriptFile.Header

  def generate: String =
    s"""${Header}
       |
       |// Services
       |${res.services.map(TypeScriptService(_)).map(_.generate).toList.mkString("\n\n")}
       |
       |// Functions
       |${res.funcs.map(TypeScriptFunc(_)).map(_.generate).toList.mkString("\n\n")}
       |""".stripMargin

}

object TypeScriptFile {

  val Header: String =
    s"""/**
       | *
       | * This file is auto-generated. Do not edit manually: changes may be erased.
       | * Generated by Aqua compiler: https://github.com/fluencelabs/aqua/. 
       | * If you find any bugs, please write an issue on GitHub: https://github.com/fluencelabs/aqua/issues
       | * Aqua version: ${Version.version}
       | *
       | */
       |import { FluenceClient, PeerIdB58 } from '@fluencelabs/fluence';
       |import { RequestFlowBuilder } from '@fluencelabs/fluence/dist/api.unstable';
       |import { RequestFlow } from '@fluencelabs/fluence/dist/internal/RequestFlow';
       |""".stripMargin

}
