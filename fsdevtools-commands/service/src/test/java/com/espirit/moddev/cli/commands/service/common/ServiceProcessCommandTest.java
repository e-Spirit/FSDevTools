/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2022 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli.commands.service.common;

import de.espirit.firstspirit.agency.ModuleAdminAgent;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus.STOPPED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceProcessCommandTest extends ServiceProcessCommandBaseTest<AbstractServiceCommand> {

	@BeforeEach
	public void setUp() {
		final AbstractServiceCommand instance = new AbstractServiceCommand() {
			@NotNull
			@Override
			protected String getResultLoggingHeaderString(@NotNull final List<ServiceInfo> serviceInfos) {
				return "Processing " + serviceInfos.size() + " services:";
			}

			@NotNull
			@Override
			protected ServiceInfo processService(@NotNull ModuleAdminAgent moduleAdminAgent, @NotNull String serviceName) {
				return new ServiceInfo(serviceName, STOPPED, STOPPED);
			}
		};
		super.setUp(instance);
	}

	@Test
	public void defaultConstructorDoesntFail() {
		assertThat("Expect not null", testling, is(notNullValue()));
	}

	@Test
	public void noParams_allServicesAreReturned() {

		ServiceProcessResult result = testling.call();

		assertThat(result.get().get(0).getServiceName(), is("RunningTestService"));
		assertThat(result.get().get(1).getServiceName(), is("StoppedTestService"));
		assertThat(result.get().get(2).getServiceName(), is("StoppedTestService2"));
	}

	@Test
	public void noParams_sameAmountIsReturned() {

		ServiceProcessResult result = testling.call();

		assertEquals(3, result.get().size(), "Exactly three service results should be found!");
	}

	@Test
	public void oneParam_oneServiceIsReturned() {

		testling.setServiceNames("TestService");
		ServiceProcessResult result = testling.call();

		assertEquals(1, result.get().size(), "Exactly one service result should be found!");
	}

	@Test
	public void oneParam_sameServiceIsReturned() {

		testling.setServiceNames("TestService");
		ServiceProcessResult result = testling.call();

		assertThat(result.get().get(0).getServiceName(), is("TestService"));
	}

	@Test
	public void multipleParams_sameAmountIsReturned() {

		testling.setServiceNames("TestService, TestService3");
		ServiceProcessResult result = testling.call();

		assertEquals(2, result.get().size(), "Exactly two services results should be found!");
	}

	@Test
	public void multipleParams_sameServicesAreReturned() {

		testling.setServiceNames("TestService, TestService3");
		ServiceProcessResult result = testling.call();

		assertThat(result.get().get(0).getServiceName(), is("TestService"));
		assertThat(result.get().get(1).getServiceName(), is("TestService3"));
	}
}

