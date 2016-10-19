package com.github.devnfun.grenadier.model

object GameMutation {
  def apply(game: GameState, events: Seq[GameEvent]): GameState = events.foldLeft(game) {
    (stage, event) =>
      event match {
        case AgentMoved(agentId, position) =>
          val stage = game.stage
          val agent = stage.agents(agentId).copy(position = position)
          game.copy(stage =
            stage.copy(agents = stage.agents.updated(agentId, agent)))
        case _ => stage
      }
  }
}
