package com.github.devnfun.grenadier.model

object GameMutation {
  def apply(game: GameState, events: Seq[GameEvent]): GameState = events.foldLeft(game) {
    (game, event) =>
      event match {
        case AgentMoved(agentId, position) =>
          val stage = game.stage
          val agent = stage.agents(agentId).copy(position = position)
          game.copy(stage =
            stage.copy(agents = stage.agents.updated(agentId, agent)))
        case AgentDroppedBomb(agentId, position) =>
          game.copy(stage =
            game.stage.copy(bombs =
              Bomb(position, 10) :: game.stage.bombs
            )
          )
        case BombsTimerDown =>
          game.copy(stage =
            game.stage.copy(bombs =
              game.stage.bombs.map(b => b.copy(countDown = b.countDown - 1))
            )
          )
        case BombExploded(position) =>
          game.copy(stage =
            game.stage.copy(bombs =
              game.stage.bombs.filterNot(_.position == position)
            )
          )
        case _ => game
      }
  }
}
