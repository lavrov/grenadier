import org.specs2.mutable._

class MapFactorySpec extends Specification {
  "MapFactory" should {
    "create map form string" in {
      val str =
        """■    i
          |■    #
          |■→→→□□
          |■    t
        """.stripMargin

      val map = MapFactory.simple(str)
      map(Position(0, 0)) must be (Wall)
      map(Position(5, 0)) must be (Ice)
      map(Position(5, 1)) must be (Box)
      map(Position(0, 3)) must be (Wall)
      map(Position(5, 3)) must be (EmptyTrap)
    }
  }
}
