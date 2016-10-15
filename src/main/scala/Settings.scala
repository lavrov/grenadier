package com.github.devnfun.grenadier

class Settings(
  val agentMovementDelta: Double = 0.1,
  val agentMovementOnIceFactor: Double = 0.5,
  val agentRadius: Double = 0.4,

  val bombMovementDelta: Double = 0.2,
  val bombMovementOnIceFactor: Double = 2,
  val bombRadius: Double = 0.4,
  val bombExplosionDelay: Int = 10,
  val bombPower: Int = 1,
  val explosionVelocity: Double = 0.1
)
