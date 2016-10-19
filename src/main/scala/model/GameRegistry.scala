package model

import java.util.UUID
import javax.inject.Inject

import com.github.devnfun.grenadier.model._

class GameRegistry @Inject()(factory: GameFactory) {
  var registry = Map.empty[String, Game]

  def `new`() = {
    val stage = Stage(
      Map.empty.withDefaultValue(Ground),
      Map(0 -> Agent(Position(0, 0), Direction.Up))
    )
    val initState = GameState(GameState.Active, stage)
    val game = factory.create(initState)
    val id = UUID.randomUUID().toString
    val keyValue = (id, game)
    registry += keyValue
    keyValue
  }

  def get(id: String) = registry apply id
}
