package com.github.devnfun.grenadier.engine

import com.github.devnfun.grenadier.model._
import model.ClientSignal

object AgentMoves extends Engine.Phase {
  def apply(state: GameState, signals: Seq[ClientSignal]) =
    signals.flatMap {
      case ClientSignal(agentId, signal) =>
        val agent = state.stage.agents(agentId)
        val newPosition = {
          val delta = state.stage.map(agent.position) match {
            case Ice => 2
            case _ => 1
          }
          signal.move.toList.foldLeft(agent.position) { case (position, direction) =>
            direction match {
              case Direction.Up => agent.position.up(delta)
              case Direction.Down => agent.position.down(delta)
              case Direction.Left => agent.position.left(delta)
              case Direction.Right => agent.position.right(delta)
            }
          }
        }
        val cell = state.stage.map(newPosition)
        cell match {
          case Ground =>
            AgentMoved(agentId, newPosition) :: Nil
          case _ => Nil
        }
    }
}

object AgentBombs extends Engine.Phase {
  def apply(state: GameState, signals: Seq[ClientSignal]) = signals.collect {
    case signal if signal.signal.bomb =>
      val agentId = signal.clientId
      val agent = state.stage.agents(agentId)
      val cell = state.stage.map(agent.position)
      PartialFunction.condOpt(cell){
        case Ground =>
          AgentDroppedBomb(agentId, agent.position)
      }.toList
  }.flatten
}

object BombCountDown extends Engine.Phase {
  def apply(state: GameState, signals: Seq[ClientSignal]) = BombsTimerDown :: Nil
}

object BombExplosions extends Engine.Phase {
  def apply(state: GameState, signals: Seq[ClientSignal]) = {
    state.stage.bombs.filter(_.countDown <= 0)
      .flatMap { bomb =>
        val onFire =
          bomb.position.up(1) ::
          bomb.position.down(1) ::
          bomb.position.left(1) ::
          bomb.position.right(1) :: Nil

        BombExploded(bomb.position) ::
        onFire.map(p => (p, state.stage.map(p))).collect {
          case (position, Box) =>
            BoxDestroyed(position)
        }
      }
  }
}

case class Engine(run: (GameState, Seq[ClientSignal]) => (GameState, Seq[GameEvent]))

object Engine {
  val Unit = Engine((stage, signals) => (stage, Nil))
  type Phase = (GameState, Seq[ClientSignal]) => Seq[GameEvent]

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
