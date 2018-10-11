package com.espirit.moddev.serverrunner

import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.util

import com.espirit.moddev.serverrunner.ServerProperties.ConnectionMode.HTTP_MODE
import de.espirit.common.base.Logger.LogLevel
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Gen, Shrink}
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import spec.UnitSpec

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class ServerPropertiesSpec extends UnitSpec with GeneratorDrivenPropertyChecks with Matchers {

  private val fakeJars = Seq(new File("foobar")).asJava

  "ServerProperties constructor, parameter serverRoot" should "use a default parameter if given null" in {
    assert(
      new ServerProperties(null, null, 1, 2, HTTP_MODE, true, true, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null, LogLevel.DEBUG).getServerRoot !=
      null)
  }
  "ServerProperties constructor, parameter serverInstall" should "use a default parameter if given null" in {
    noException should be thrownBy
      new ServerProperties(Paths.get(""), null, 1, 2, HTTP_MODE, true, null, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null, LogLevel.DEBUG).isServerInstall
  }
  "ServerProperties constructor, parameter connectionMode" should "use a default parameter if given null" in {
    assert(
      new ServerProperties(Paths.get(""), null, 1, 2, null, true, true, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null, LogLevel.DEBUG).getMode !=
      null
    )
  }

  def objWithPort(port: Int) =
    new ServerProperties(Paths.get(""), null, port, 2, HTTP_MODE, true, true, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null, LogLevel.DEBUG)

  "ServerProperties constructor, parameter httpPort" should "accept ports that are strictly positive and smaller than 65536" in {
    forAll(Gen.choose(1, 65536)) { port =>
      noException should be thrownBy objWithPort(port)
      assert(objWithPort(port).getHttpPort == port)
    }
  }
  it should "reject negative ports" in {
    forAll(arbitrary[Int] suchThat (_ < 0)) { port =>
      an[IllegalArgumentException] should be thrownBy objWithPort(port)
    }
  }
  it should "reject ports > 65536" in {
    forAll(arbitrary[Int] suchThat (_ > 65536)) { port =>
      an[IllegalArgumentException] should be thrownBy objWithPort(port)
    }
  }
  it should "be set to 8000 for connection mode HTTP_MODE if no port is given explicitly" in {
    assert(ServerProperties.builder().firstSpiritJar(new File("foobar")).build().getHttpPort == 8000)
  }

  def objWithRetryWait(retryWait: Duration) =
    new ServerProperties(Paths.get(""), null, 1, 2, HTTP_MODE, true, true, new util.ArrayList(), retryWait, "", 0, fakeJars, null, LogLevel.DEBUG)

  "ServerProperties constructor, parameter retryWait" should "accept positive values" in {
    forAll(arbitrary[Long] suchThat (_ >= 0)) { retryWait =>
      objWithRetryWait(Duration.ofMillis(retryWait))
    }
  }
  it should "reject a negative duration" in {
    forAll(arbitrary[Long] suchThat (_ < 0)) { retryWait =>
      an[IllegalArgumentException] should be thrownBy objWithRetryWait(Duration.ofMillis(retryWait))
    }
  }
  it should "use a default parameter if given null" in {
    assert(objWithRetryWait(null).getRetryWait > 0.milliseconds)
  }
  "ServerProperties constructor, parameter serverAdminPw" should "use a default parameter if given null" in {
    assert(
      new ServerProperties(Paths.get(""), null, 1, 2, HTTP_MODE, true, true, new util.ArrayList(), Duration.ofMillis(0), null, 0, fakeJars, null, LogLevel.DEBUG).getServerAdminPw != null)
  }
  "ServerProperties constructor, parameter serverHost" should "use a default parameter if given null" in {
    assert(
      new ServerProperties(Paths.get(""), null, 1, 2, HTTP_MODE, true, true, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null, LogLevel.DEBUG).getServerHost != null)
  }

  def objWithRetryCount(retryCount: Int) =
    new ServerProperties(Paths.get(""), null, 1, 2, HTTP_MODE, true, true, new util.ArrayList(), Duration.ofMillis(0), "", retryCount, fakeJars, null, LogLevel.DEBUG)

  "ServerProperties constructor, parameter retryCount" should "accept positive values" in {
    forAll(arbitrary[Int] suchThat (_ >= 0)) { retryCount =>
      assert(objWithRetryCount(retryCount).getRetryCount >= 0)
    }
  }
  it should "reject negative values" in {
    forAll(arbitrary[Int] suchThat (_ < 0)) { retryCount =>
      an[IllegalArgumentException] should be thrownBy objWithRetryCount(retryCount)
    }
  }
  "ServerProperties constructor, parameter serverOps" should "use a default parameter if given null" in {
    noException should be thrownBy new ServerProperties(Paths.get(""), null, 1, 2, HTTP_MODE, true, true, null, Duration.ofMillis(0), "", 0, fakeJars, null, LogLevel.DEBUG)
  }
  it should "not contain null" in {
    val listWithNulls = Seq("list", null, "null")
    val listFromServerProps =
      new ServerProperties(Paths.get(""), null, 1, 2, HTTP_MODE, true, true, listWithNulls.asJava, Duration.ofMillis(0), "", 0, fakeJars, null, LogLevel.DEBUG).getServerOps.asScala
    listFromServerProps should contain inOrderElementsOf listWithNulls.filter(_ != null)
  }

  "ServerProperties constructor, parameter logLevel" should "use DEBUG log level by default" in {
    val props = new ServerProperties(Paths.get(""), null, 1234, 2, HTTP_MODE, true, true, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null, null)
    assert(props.getLogLevel == LogLevel.DEBUG)
  }

  def noShrink[T] = Shrink[T](_ => Stream.empty)

  implicit val myTypeNoShrink = noShrink[String]
}
