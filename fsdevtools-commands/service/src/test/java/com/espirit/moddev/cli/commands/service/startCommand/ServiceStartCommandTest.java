/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2024 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.commands.service.startCommand;

import com.espirit.moddev.cli.commands.service.common.ServiceInfo;
import com.espirit.moddev.cli.commands.service.common.ServiceProcessCommandBaseTest;
import com.espirit.moddev.cli.commands.service.common.ServiceProcessResult;
import de.espirit.firstspirit.access.ServiceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus.RUNNING;
import static com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus.STOPPED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceStartCommandTest extends ServiceProcessCommandBaseTest<ServiceStartCommand> {

	private ServiceProcessResult result;

	@BeforeEach
	public void setUp() {
		super.setUp(new ServiceStartCommand());
		result = testling.call();
	}

	@Test
	public void noParams_allStoppedServices_callStartOnce() {
		verify(mockModuleAdminAgent, times(1)).startService("StoppedTestService");
		verify(mockModuleAdminAgent, times(1)).startService("StoppedTestService2");
	}

	@Test
	public void allStoppedServicesAreReturned() {
		assertThat(result.get(), hasItem(new ServiceInfo("StoppedTestService", STOPPED, RUNNING)));
		assertThat(result.get(), hasItem(new ServiceInfo("StoppedTestService2", STOPPED, RUNNING)));
	}

	@Test
	public void noParams_allRunningServices_neverCallStart() {
		verify(mockModuleAdminAgent, never()).startService("RunningTestService");
	}

	@Test
	public void allRunningServicesAreReturned() {
		assertThat(result.get(), hasItem(new ServiceInfo("RunningTestService", RUNNING, RUNNING)));
	}

	@Test
	public void noParams_totalNumberOfResults_equalsNumberOfServices() {
		assertEquals(3, result.get().size(), "Exactly three service results should be found!");
	}

	@Test
	public void multipleParams_nonExistingServices_resultIsException() {
		when(mockModuleAdminAgent.isRunning("NonExistentService")).thenReturn(false);
		doThrow(new ServiceNotFoundException("unidentified service")).when(mockModuleAdminAgent).startService("NonExistentService");

		testling.setServiceNames("NonExistentService");
		result = testling.call();

		//if there is a better way to test this, be sure to tell me izgoel@e-spirit.com
		assert (result.isError());
		assertEquals(result.getError().getClass(), ServiceNotFoundException.class);
	}

}

