package aqua.lsp

import aqua.parser.Ast
import aqua.parser.head.{ImportExpr, ImportFromExpr, UseExpr, UseFromExpr}
import aqua.parser.lexer.{LiteralToken, Token}
import aqua.raw.ConstantRaw
import aqua.semantics.header.Picker.*
import aqua.semantics.rules.locations.LocationsState
import aqua.semantics.{CompilerState, RawSemantics, SemanticError, SemanticWarning, Semantics}

import cats.data.Validated.{Invalid, Valid}
import cats.data.{EitherT, NonEmptyChain, ValidatedNec, Writer}
import cats.syntax.applicative.*
import cats.syntax.apply.*
import cats.syntax.either.*
import cats.syntax.flatMap.*
import cats.syntax.foldable.*
import cats.syntax.functor.*
import cats.syntax.reducible.*
import monocle.Lens
import monocle.macros.GenLens

class LspSemantics[S[_]](
  constants: List[ConstantRaw] = Nil
) extends Semantics[S, LspContext[S]] {

  private def getImportTokens(ast: Ast[S]): List[LiteralToken[S]] =
    ast.head.collect {
      case ImportExpr(fn) => fn
      case ImportFromExpr(_, fn) => fn
      case UseExpr(fn, _) => fn
      case UseFromExpr(_, fn, _) => fn
    }.toList

  /**
   * Process the AST and return the semantics result.
   * NOTE: LspSemantics never return errors or warnings,
   * they are collected in LspContext.
   */
  def process(
    ast: Ast[S],
    init: LspContext[S]
  ): ProcessResult = {

    val withConstants = init.addFreeParts(constants)
    val rawState = CompilerState.init[S](withConstants.raw)

    val initState = rawState.copy(
      locations = rawState.locations.copy(
        variables = rawState.locations.variables ++ withConstants.variables
      )
    )

    val importTokens = getImportTokens(ast)

    given Lens[CompilerState[S], LocationsState[S]] =
      GenLens[CompilerState[S]](_.locations)

    given LocationsInterpreter[S, CompilerState[S]] =
      new LocationsInterpreter[S, CompilerState[S]]()

    RawSemantics
      .interpret(ast, withConstants.raw)
      .run(initState)
      .map { case (state, ctx) =>
        EitherT(
          Writer
            .tell(state.warnings)
            .as(
              NonEmptyChain
                .fromChain(state.errors)
                .toLeft(
                  LspContext(
                    raw = ctx,
                    rootArrows = state.names.rootArrows,
                    constants = state.names.constants,
                    abDefinitions = state.abilities.definitions,
                    importTokens = importTokens,
                    variables = state.locations.variables,
                    errors = state.errors.toList,
                    warnings = state.warnings.toList
                  )
                )
            )
        )
      }
      .value
  }
}
