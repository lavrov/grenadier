package com.github.devnfun.grenadier.model

object StageMutation {
  def apply(stage: Stage, events: Seq[StageEvent]): Stage = events.foldLeft(stage) {
    (stage, event) =>
      event match {
        case AgentMoved(agentId, position) =>
          val agent = stage.agents(agentId).copy(position = position)
          stage.copy(agents = stage.agents.updated(agentId, agent))
        case _ => stage
      }
  }
}
