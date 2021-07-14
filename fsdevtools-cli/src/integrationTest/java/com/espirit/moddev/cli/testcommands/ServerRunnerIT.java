/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *********************************************************************
 *
 */

package com.espirit.moddev.cli.testcommands;

import com.espirit.moddev.cli.api.result.Result;
import com.espirit.moddev.cli.commands.help.HelpCommand;
import com.espirit.moddev.cli.commands.help.HelpResult;
import com.espirit.moddev.cli.commands.module.installCommand.InstallModuleCommand;
import com.espirit.moddev.cli.commands.module.installCommand.InstallModuleResult;
import com.espirit.moddev.cli.commands.project.importCommand.ImportProjectCommand;
import com.espirit.moddev.cli.commands.server.startCommand.ServerStartCommand;
import com.espirit.moddev.cli.commands.server.stopCommand.ServerStopCommand;
import com.espirit.moddev.cli.commands.test.connectionCommand.TestConnectionCommand;
import com.espirit.moddev.cli.configuration.GlobalConfig;
import com.espirit.moddev.cli.results.SimpleResult;
import com.espirit.moddev.cli.commands.test.common.TestResult;
import com.espirit.moddev.server.ServerConfigurator;
import com.espirit.moddev.server.ServerInstaller;
import com.espirit.moddev.util.FsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ServerRunnerIT {

	@NotNull
	private static TemporaryFolder _temp = new TemporaryFolder();
	@Nullable
	private static Path _workingDir;

	///////////////////////////////////////////////////////////////////////////////////
	//
	// TEST SETUP
	//
	///////////////////////////////////////////////////////////////////////////////////

	@BeforeClass
	public static void before() throws IOException {
		_temp.create();
		final Path workingDir = _temp.getRoot().toPath().resolve("workingDir");

		// install
		final ServerInstaller serverInstaller = new ServerInstaller(workingDir);
		serverInstaller.setInstallerTarGz(Paths.get(System.getProperty("testInstallerTar")));
		serverInstaller.setServerJar(Paths.get(System.getProperty("testServerJar")));
		serverInstaller.execute();

		// configure
		final ServerConfigurator serverConfigurator = new ServerConfigurator(workingDir);
		serverConfigurator.setLicenseFile(Paths.get(System.getProperty("testLicenseFile")));
		serverConfigurator.addServerConfValue("HTTP_PORT", System.getProperty("testHttpPort"));
		serverConfigurator.addServerConfValue("SOCKET_PORT", System.getProperty("testSocketPort"));
		serverConfigurator.addLoggingConfValue("log4j.rootCategory", "DEBUG, fs");
		serverConfigurator.addLoggingConfValue("log4j.appender.fs.consoleLogging", "true");
		serverConfigurator.execute();

		// start the server
		final ServerStartCommand serverStartCommand = new ServerStartCommand();
		serverStartCommand.setServerDir(workingDir.toAbsolutePath().toString());
		final SimpleResult<String> startResult = serverStartCommand.call();
		if (startResult.isError()) {
			throw new IllegalStateException("Server did not start", startResult.getError());
		}
		_workingDir = workingDir;
	}

	@AfterClass
	public static void after() {
		try {
			if (_workingDir != null) {
				// stop the server
				final ServerStopCommand serverStopCommand = new ServerStopCommand();
				serverStopCommand.setServerDir(_workingDir.toAbsolutePath().toString());
				final SimpleResult<Boolean> stopResult = serverStopCommand.call();
				if (stopResult.isError()) {
					throw new IllegalStateException("Server did not stop", stopResult.getError());
				}
			}
		} finally {
			try {
				_temp.delete();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void prepareCommand(@NotNull final GlobalConfig command) {
		command.setHost(FsUtil.VALUE_DEFAULT_HOST);
		command.setPort(Integer.valueOf(System.getProperty("testHttpPort")));
	}

	///////////////////////////////////////////////////////////////////////////////////
	//
	// TEST METHODS
	//
	///////////////////////////////////////////////////////////////////////////////////

	@Test
	public void TestConnectionCommand() {
		final TestConnectionCommand command = new TestConnectionCommand();
		command.setHost(FsUtil.VALUE_DEFAULT_HOST);
		command.setPort(Integer.valueOf(System.getProperty("testHttpPort")));
		final TestResult call = command.call();
		assertFalse("TestConnectionCommand failed - command error", call.isError());
	}

	@Test
	public void HelpCommand() {
		final HelpCommand command = new HelpCommand();
		final HelpResult call = command.call();
		assertFalse("HelpCommand failed - error during command execution", call.isError());
		assertNotNull("HelpCommand failed - result is null", call.get());
	}

	@Test
	public void ImportProjectCommand() throws URISyntaxException {
		final ImportProjectCommand command = new ImportProjectCommand();
		prepareCommand(command);
		command.setProjectFile(new File(getClass().getResource("/minimal_project.tar.gz").toURI()).toPath().toAbsolutePath().toString());
		command.setProjectName("imported project");
		command.setProjectDescription("imported project description");
		final Result<Boolean> call = command.call();
		assertFalse("ImportProjectCommand failed - error during command execution", call.isError());
		assertTrue("ImportProjectCommand failed - result mismatch", call.get());
	}

	@Test
	public void InstallModuleCommand() throws URISyntaxException {
		final InstallModuleCommand command = new InstallModuleCommand();
		prepareCommand(command);
		command.setFsm(new File(getClass().getResource("/testmodule-1.0.0-SNAPSHOT.fsm").toURI()).toPath().toAbsolutePath().toString());
		final InstallModuleResult call = command.call();
		assertFalse("InstallModuleCommand failed - error during command execution", call.isError());
		assertNotNull("InstallModuleCommand failed - module name is null", call.getModuleName());
	}

}
