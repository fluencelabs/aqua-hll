/*
 * Copyright (C) 2024  Fluence DAO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package aqua

import cats.effect.kernel.Async
import fs2.io.file.Path
import cats.syntax.applicative.*

object PlatformPackagePath {
  def getPackagePath[F[_]: Async](path: String): F[Path] = Path("").pure[F]
  def getGlobalNodeModulePath: List[Path] = Nil
}
