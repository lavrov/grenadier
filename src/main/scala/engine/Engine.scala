package com.github.devnfun.grenadier.engine

import com.github.devnfun.grenadier.model._

object AgentMoves extends Engine.Phase {
  def apply(state: GameState, signals: Seq[Signal]) =
    signals.collect {
      case Move(agentId, direction) =>
        val agent = state.stage.agents(agentId)
        val newPosition = {
          val delta = state.stage.map(agent.position) match {
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
        val cell = state.stage.map(newPosition)
        cell match {
          case Ground =>
            AgentMoved(agentId, newPosition) :: Nil
          case _ => Nil
        }
    }.flatten
}

object AgentBombs extends Engine.Phase {
  def apply(state: GameState, signals: Seq[Signal]) = signals.collect {
    case DropBomb(agentId) =>
      val agent = state.stage.agents(agentId)
      val cell = state.stage.map(agent.position)
      PartialFunction.condOpt(cell){
        case Ground =>
          AgentDroppedBomb(agentId, agent.position)
      }.toList
  }.flatten
}

case class Engine(run: (GameState, Seq[Signal]) => (GameState, Seq[GameEvent]))

object Engine {
  val Unit = Engine((stage, signals) => (stage, Nil))
  type Phase = (GameState, Seq[Signal]) => Seq[GameEvent]

  def fromPhases(phases: Phase*) = {
    phases.foldLeft(Unit) {
      case (engine, phase) => Engine {
        (stage, signals) => {
          val (e1Stage, e1Events) = engine.run(stage, signals)
          val e2Events = phase(e1Stage, signals)
          val e2Stage = GameMutation(e1Stage, e2Events)
          (e2Stage, e1Events ++ e2Events)
        }
      }
    }
  }
}
