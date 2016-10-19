package model

import java.util.UUID
import javax.inject.Inject

import com.github.devnfun.grenadier.model._
import com.github.devnfun.grenadier.utils.MapFactory

class GameRegistry @Inject()(factory: GameFactory) {
  var registry = Map.empty[String, Game]

  def `new`() = {
    val stage = Stage(
      MapFactory.simple(
        """■■■■■■■■■■
          |■        ■
          |■        ■
          |■        ■
          |■        ■
          |■        ■
          |■        ■
          |■        ■
          |■        ■
          |■■■■■■■■■■""".stripMargin
      ).withDefaultValue(Abyss),
      Map(0 -> Agent(Position(1, 1), Direction.Up)),
      Nil
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
