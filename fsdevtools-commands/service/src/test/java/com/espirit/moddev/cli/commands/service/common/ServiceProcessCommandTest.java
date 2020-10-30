/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2020 e-Spirit AG
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.espirit.moddev.cli.commands.service.common.ServiceInfo.ServiceStatus.STOPPED;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;

public class ServiceProcessCommandTest extends ServiceProcessCommandBaseTest<AbstractServiceCommand> {

	@Before
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
		super.setUp(spy(instance));
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

		Assert.assertEquals("Exactly three service results should be found!", 3, result.get().size());
	}

	@Test
	public void oneParam_oneServiceIsReturned() {

		testling.setServiceNames("TestService");
		ServiceProcessResult result = testling.call();

		Assert.assertEquals("Exactly one service result should be found!", 1, result.get().size());
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

		Assert.assertEquals("Exactly two services results should be found!", 2, result.get().size());
	}

	@Test
	public void multipleParams_sameServicesAreReturned() {

		testling.setServiceNames("TestService, TestService3");
		ServiceProcessResult result = testling.call();

		assertThat(result.get().get(0).getServiceName(), is("TestService"));
		assertThat(result.get().get(1).getServiceName(), is("TestService3"));
	}
}

