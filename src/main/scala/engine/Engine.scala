package com.github.devnfun.grenadier.engine

import com.github.devnfun.grenadier.model._

class AgentMoves extends Engine.Phase {
  def apply(stage: Stage, signals: Seq[Signal]) =
    signals.collect {
      case Move(agentId, direction) =>
        val agent = stage.agents(agentId)
        val newPosition = {
          val delta = stage.map(agent.position) match {
            case Ice => 2
            case _ => 1
          }
          direction match {
            case Direction.Up => agent.position.up(delta)
            case Direction.Down => agent.position.down(delta)
            case Direction.Left => agent.position.left(delta)
            case Direction.Right => agent.position.right(delta)
          }
        }
      AgentMoved(agentId, newPosition)
    }
}

object Engine {
  type Phase = (Stage, Seq[Signal]) => Seq[StageEvent]
  type Engine = (Stage, Seq[Signal]) => (Stage, Seq[StageEvent])

  def apply(phase: Phase*): Engine = {
    phase.foldLeft[Engine]((stage, signals) => (stage, Nil)){
      case (engine, phase) => (stage, signals) => {
        val (e1Stage, e1Events) = engine(stage, signals)
        val e2Events = phase(e1Stage, signals)
        val e2Stage = StageMutation(e1Stage, e2Events)
        (e2Stage, e1Events ++ e2Events)
      }
    }
  }
}
