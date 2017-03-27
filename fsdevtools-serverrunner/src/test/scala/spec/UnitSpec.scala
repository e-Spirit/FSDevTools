package spec

import org.apache.log4j.{BasicConfigurator, Level, Logger}
import org.scalatest.FlatSpec

trait UnitSpec extends FlatSpec {
  BasicConfigurator.configure()
  Logger.getRootLogger().setLevel(Level.INFO)
}
