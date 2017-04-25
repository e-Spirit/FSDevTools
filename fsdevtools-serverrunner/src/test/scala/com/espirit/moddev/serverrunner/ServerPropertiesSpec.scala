package com.espirit.moddev.serverrunner

import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.util

import com.espirit.moddev.serverrunner.ServerProperties.ConnectionMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Gen, Shrink}
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import spec.UnitSpec

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.Try

class ServerPropertiesSpec extends UnitSpec with GeneratorDrivenPropertyChecks with Matchers {

  private val fakeJars = Seq(new File("foobar")).asJava

  "ServerProperties constructor, parameter serverRoot" should "use a default parameter if given null" in {
    assert(
      new ServerProperties(null, null, 1, true, true, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null).getServerRoot !=
        null)
  }
  "ServerProperties constructor, parameter serverInstall" should "use a default parameter if given null" in {
    noException should be thrownBy
      new ServerProperties(Paths.get(""), null, 1, true, true, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null).isServerInstall
  }

  def objWithPort(port: Int) =
    new ServerProperties(Paths.get(""), null, port, true, true, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null)

  "ServerProperties constructor, parameter serverPort" should "accept ports that are strictly positive and smaller than 65536" in {
    forAll(Gen.choose(1, 65536)) { port =>
      noException should be thrownBy objWithPort(port)
      assert(objWithPort(port).getServerPort == port)
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
    assert(ServerProperties.builder().firstSpiritJar(new File("foobar")).build().getServerPort == 8000)
  }

  def objWithThreadWait(threadWait: Duration) =
    new ServerProperties(Paths.get(""), null, 1, true, true, new util.ArrayList(), threadWait, "", 0, fakeJars, null)

  "ServerProperties constructor, parameter threadWait" should "accept positive values" in {
    forAll(arbitrary[Long] suchThat (_ >= 0)) { threadWait =>
      objWithThreadWait(Duration.ofMillis(threadWait))
    }
  }
  it should "reject a negative duration" in {
    forAll(arbitrary[Long] suchThat (_ < 0)) { threadWait =>
      an[IllegalArgumentException] should be thrownBy objWithThreadWait(Duration.ofMillis(threadWait))
    }
  }
  it should "use a default parameter if given null" in {
    assert(objWithThreadWait(null).getThreadWait > 0.milliseconds)
  }
  "ServerProperties constructor, parameter serverAdminPw" should "use a default parameter if given null" in {
    assert(
      new ServerProperties(Paths.get(""), null, 1, true, true, new util.ArrayList(), Duration.ofMillis(0), null, 0, fakeJars, null).getServerAdminPw != null)
  }
  "ServerProperties constructor, parameter serverHost" should "use a default parameter if given null" in {
    assert(
      new ServerProperties(Paths.get(""), null, 1, true, true, new util.ArrayList(), Duration.ofMillis(0), "", 0, fakeJars, null).getServerHost != null)
  }

  def objWithConnectionRetryCount(connectionRetryCount: Int) =
    new ServerProperties(Paths.get(""), null, 1, true, true, new util.ArrayList(), Duration.ofMillis(0), "", connectionRetryCount, fakeJars, null)

  "ServerProperties constructor, parameter connectionRetryCount" should "accept positive values" in {
    forAll(arbitrary[Int] suchThat (_ >= 0)) { connectionRetryCount =>
      assert(objWithConnectionRetryCount(connectionRetryCount).getConnectionRetryCount >= 0)
    }
  }
  it should "reject negative values" in {
    forAll(arbitrary[Int] suchThat (_ < 0)) { connectionRetryCount =>
      an[IllegalArgumentException] should be thrownBy objWithConnectionRetryCount(connectionRetryCount)
    }
  }
  "ServerProperties constructor, parameter serverOps" should "use a default parameter if given null" in {
    noException should be thrownBy new ServerProperties(Paths.get(""), null, 1, true, true, null, Duration.ofMillis(0), "", 0, fakeJars, null)
  }
  it should "not contain null" in {
    val listWithNulls = Seq("list", null, "null")
    val listFromServerProps =
      new ServerProperties(Paths.get(""), null, 1, true, true, listWithNulls.asJava, Duration.ofMillis(0), "", 0, fakeJars, null).getServerOps.asScala
    listFromServerProps should contain inOrderElementsOf listWithNulls.filter(_ != null)
  }

  def noShrink[T] = Shrink[T](_ => Stream.empty)

  implicit val myTypeNoShrink = noShrink[String]

  def filenameGen(pathSeperator: String) =
    for { filename <- Gen.alphaNumStr suchThat (_.nonEmpty) } yield
      s"""de${pathSeperator}espirit${pathSeperator}firstspirit$pathSeperator$filename.jar"""

  "FS_SERVER_JAR_PATTERN" should "match unix-like paths" in {
    forAll(filenameGen("/")) { filename =>
      assert(ServerProperties.FS_SERVER_JAR_PATTERN.matcher(filename).matches)
    }
  }
  it should "match windows-like paths" in {
    forAll(filenameGen("""\""")) { filename =>
      assert(ServerProperties.FS_SERVER_JAR_PATTERN.matcher(filename).matches)
    }
  }
}
