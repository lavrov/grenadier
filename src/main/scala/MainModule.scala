import com.github.devnfun.grenadier.engine.{AgentBombs, AgentMoves, Engine}
import play.api.inject.Module
import play.api.{Configuration, Environment}

class MainModule extends Module {
  def bindings(environment: Environment, configuration: Configuration) =
    bind[Engine].toInstance(Engine fromPhases (AgentMoves, AgentBombs)) ::
      Nil
}
