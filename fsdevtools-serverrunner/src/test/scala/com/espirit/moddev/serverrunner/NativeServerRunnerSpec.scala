package com.espirit.moddev.serverrunner

import java.io.{File, IOException}
import java.nio.file.{Files, Path}
import java.time.Duration
import java.util.concurrent.Executors
import java.util.function.Supplier

import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}
import spec.IntegrationTest
import util.Timer

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.io.Source
import scala.language.{implicitConversions, postfixOps}

class NativeServerRunnerSpec extends WordSpec with Matchers with Eventually {
  def fixture = new {
    lazy val propsWithVersionBuilder        = ServerProperties.builder().serverRoot(new File(System.getProperty("fsServerRoot")).toPath).firstSpiritJars(ServerProperties.getFirstSpiritJarsFromClasspath)
    lazy val minimalServerPropertiesBuilder = propsWithVersionBuilder
    lazy val minimalServerProperties        = minimalServerPropertiesBuilder.build()
    lazy val propsWithVersion               = propsWithVersionBuilder.build()
  }

  def assertDirExists(path: Path) = {
    assert(Files.exists(path))
    assert(Files.isDirectory(path))
  }

  def assertFileExists(path: Path) = {
    assert(Files.exists(path))
    assert(!Files.isDirectory(path))
  }

  "NativeServerRunner.waitForCondition" should {
    def executeAndClock(block: => Unit): Duration = {
      val startTime = System.currentTimeMillis()
      block
      Duration.ofMillis(System.currentTimeMillis() - startTime)
    }

    "execute at max 'triesLeft' times" in {
      //pending
      val waitTime  = 100.millis
      var callCount = 0
      val condition: Supplier[java.lang.Boolean] = () => {
        callCount += 1
        false
      }
      for { i <- 1 to 3 } {
        callCount = 0
        lazy val result = NativeServerRunner.waitForCondition(condition, i * waitTime, 2)
        val duration    = executeAndClock(result)
        assert(!result)
        assert(duration >= i * waitTime)
        assert(callCount == 2)
      }
    }
    "wait approximately `waitTime` between calls" in {
      lazy val result = NativeServerRunner.waitForCondition(() => false, 100 milliseconds, 2)
      val duration    = executeAndClock(result)
      assert(!result)
      assert(duration >= 100.milliseconds)
    }
    "return immediately if 'condition' is true" in {
      lazy val result = NativeServerRunner.waitForCondition(() => true, 100 milliseconds, 1)
      val duration    = executeAndClock(result)
      assert(result)
      assert(duration < 50.milliseconds)
    }
  }

  "NativeServerRunner.prepareFilesystem" when {
    "we have any version" should {
      "create the 'server' directory" in {
        val props = fixture.propsWithVersion
        NativeServerRunner.prepareFilesystem(props)
        assertDirExists(props.getServerRoot.resolve("server"))
      }
      "create the 'conf' directory" in {
        val props = fixture.propsWithVersion
        NativeServerRunner.prepareFilesystem(props)
        assertDirExists(props.getServerRoot.resolve("conf"))
      }

      "create 'fs-server.policy" in {
        val props = fixture.propsWithVersion
        NativeServerRunner.prepareFilesystem(props)
        assertFileExists(props.getServerRoot.resolve("conf").resolve("fs-server.policy"))
      }
      "create 'fs-license.conf' if a supplier for it is available" in {
        val props = fixture.propsWithVersion
        NativeServerRunner.prepareFilesystem(props)
        if (props.getLicenseFileSupplier.get().isPresent) { //not the case if fs-license.jar is not on the class path
          assertFileExists(props.getServerRoot.resolve("conf").resolve("fs-license.conf"))
        }
      }
      "create 'fs-server.conf' with the correct server port" in {
        val props = fixture.propsWithVersionBuilder.serverPort(9000).build()
        NativeServerRunner.prepareFilesystem(props)
        val confFile = props.getServerRoot.resolve("conf").resolve("fs-server.conf")
        assertFileExists(confFile)
        assert(Source.fromFile(confFile.toFile).getLines().contains("HTTP_PORT=9000"))
      }
    }
    "we install the server" should {
        "create the 'fs-init' file" in {
          val props = fixture.propsWithVersion
          NativeServerRunner.prepareFilesystem(props)
          assertFileExists(props.getServerRoot.resolve("server").resolve("fs-init"))
        }
      }
  }

  "NativeServerRunner.prepareStartup" should {
    "call 'java'" in {
      val args = NativeServerRunner.prepareStartup(fixture.minimalServerProperties).asScala
      assert(args.head == "java")
    }
    "call the FirstSpirit server main class as last argument" in {
      val args = NativeServerRunner.prepareStartup(fixture.minimalServerProperties).asScala
      assert(args.last == "de.espirit.common.bootstrap.Bootstrap")
    }
    "define a java security policy" in {
      val props      = fixture.minimalServerProperties
      val args       = NativeServerRunner.prepareStartup(props).asScala
      val policyFile = props.getServerRoot.resolve("conf").resolve("fs-server.policy")
      assert(args contains s"-Djava.security.policy=$policyFile")
    }
    "pass the 'cmsroot'" in {
      val props    = fixture.minimalServerProperties
      val commands = NativeServerRunner.prepareStartup(props)

      assert(commands contains s"-Dcmsroot=${props.getServerRoot}")
    }
    "only contain '-Xloggc' when it is explicitly configured" in {
      val argsNoGcLog = NativeServerRunner.prepareStartup(fixture.minimalServerProperties).asScala
      assert(!argsNoGcLog.exists(str => str startsWith "-Xloggc"))
      val argsGcLog =
        NativeServerRunner
          .prepareStartup(
            ServerProperties.builder().serverInstall(false).serverGcLog(true).firstSpiritJars(ServerProperties.getFirstSpiritJarsFromClasspath).build())
          .asScala
      assert(argsGcLog.count(str => str startsWith "-Xloggc") == 1)
    }
    "add the server ops" in {
      val commands = NativeServerRunner
        .prepareStartup(
          ServerProperties.builder().serverOp("-Dabc=123").serverOp("-Dcde=234").firstSpiritJars(ServerProperties.getFirstSpiritJarsFromClasspath).build())
      commands should contain inOrder ("-Dabc=123", "-Dcde=234")
    }
    "add the server policy file" in {
      val props = fixture.minimalServerProperties
      NativeServerRunner.prepareStartup(props)

      val policyFileRegex   = """(?m).*grant\s*\{\s*permission java.security.AllPermission;\s*\};\s*""".r
      val policyFileContent = Source.fromFile(props.getServerRoot.resolve("conf").resolve("fs-server.policy").toFile).mkString

      assert(policyFileRegex.findFirstIn(policyFileContent).isDefined)
    }
  }

  "NativeServerRunner.testConnection" should {
    "return true" when {
      "the connection can be made correctly" in pending //complex to implement because URL class is final and PowerMock did not work
    }
    "return false" when {
      "an exception gets thrown" in {
        val badConnection = mock(classOf[ServerProperties])
        when(badConnection.getServerUrl).thenThrow(new RuntimeException)

        assert(!NativeServerRunner.testConnection(badConnection))
      }
      "no 200 OK is returned" in pending //complex to implement because URL class is final and PowerMock did not work
    }
  }

  def assertNoServerRunning(props: ServerProperties): Unit = {
    assert(new NativeServerRunner(props).stop())
  }

  "NativeServerRunner.startFirstSpiritServer" should {
    "boot a server when given minimal server properties" taggedAs IntegrationTest in {
      val props = fixture.minimalServerPropertiesBuilder.clearFirstSpiritJars().build()
      assertNoServerRunning(props)
      val testTask = NativeServerRunner.startFirstSpiritServer(props, Executors.newCachedThreadPool())
      assert(!testTask.isDone)
      eventually(timeout(60 seconds), interval(2 seconds)) {
        assert(NativeServerRunner.testConnection(props))
      }
      assert(new NativeServerRunner(props).stop())
    }
  }

  "NativeServerRunner.isRunning" should {
    "return false when a server has not been started" taggedAs IntegrationTest in {
      val props = fixture.minimalServerPropertiesBuilder.clearFirstSpiritJars().build()
      assertNoServerRunning(props)
      assert(!new NativeServerRunner(props).isRunning)
    }
  }

  "NativeServerRunner" should {
    "start a FirstSpirit server, see that it's running, and shut it down afterwards" taggedAs IntegrationTest in {
      val props = fixture.minimalServerPropertiesBuilder.clearFirstSpiritJars().build()
      assertNoServerRunning(props)
      val runner = new NativeServerRunner(props)
      val timer  = Timer()
      assert(!runner.isRunning)
      assert(runner.start())
      info(s"starting took $timer.")
      assert(runner.isRunning)
      info("shutting down now")
      assert(runner.stop())
      assert(!runner.isRunning)
      info(s"shutdown succeeded after $timer")
    }
    "return true when no server has been started by the runner itself and already a server was running (from another service)" taggedAs IntegrationTest in {
      val props = fixture.minimalServerPropertiesBuilder.clearFirstSpiritJars().build()
      assertNoServerRunning(props)
      val runner1 = new NativeServerRunner(props)
      val runner2 = new NativeServerRunner(props)
      assert(!runner1.isRunning)
      assert(!runner2.isRunning)

      info("runner1 will start the server itself, runner2 will jump on the already-existing server")
      assert(runner1.start())
      assert(runner2.start())

      assert(runner1.isRunning)
      assert(runner2.isRunning)

      assert(runner2.stop())
      assert(!runner1.isRunning)
      assert(!runner2.isRunning)
    }
  }
}
