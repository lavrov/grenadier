package com.github.devnfun.grenadier.model

import io.circe.generic.JsonCodec

@JsonCodec
sealed trait GameEvent
case class AgentMoved(agentId: Int, position: Position) extends GameEvent
case class AgentDroppedBomb(agentId: Int, position: Position) extends GameEvent
case object GameCompleted extends GameEvent
case object BombsTimerDown extends GameEvent
case class BombExploded(position: Position) extends GameEvent
object GameEvent
