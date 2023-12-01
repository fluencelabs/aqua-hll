package aqua.semantics.rules.locations

import aqua.parser.lexer.Token
import aqua.semantics.rules.StackInterpreter
import aqua.semantics.rules.types.TypesState

import cats.data.{NonEmptyList, NonEmptyMap, State}
import monocle.Lens
import monocle.macros.GenLens

class DummyLocationsInterpreter[S[_], X] extends LocationsAlgebra[S, State[X, *]] {

  def addToken(name: String, tokenInfo: TokenInfo[S]): State[X, Unit] = State.pure(())

  def addTokenWithFields(
    name: String,
    token: TokenInfo[S],
    fields: List[(String, TokenInfo[S])]
  ): State[X, Unit] = State.pure(())

  def pointFieldLocation(typeName: String, fieldName: String, token: Token[S]): State[X, Unit] =
    State.pure(())

  def pointTokenWithFieldLocation(
    typeName: String,
    typeToken: Token[S],
    fieldName: String,
    token: Token[S]
  ): State[X, Unit] = State.pure(())
  override def pointLocation(name: String, token: Token[S]): State[X, Unit] = State.pure(())
  override def pointLocations(locations: List[(String, Token[S])]): State[X, Unit] = State.pure(())

  def beginScope(): State[X, Unit] = State.pure(())

  def endScope(): State[X, Unit] = State.pure(())
}
