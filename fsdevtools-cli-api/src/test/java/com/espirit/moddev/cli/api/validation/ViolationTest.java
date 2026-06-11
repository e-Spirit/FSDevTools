package com.espirit.moddev.cli.api.validation;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License"),
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ViolationTest {

	private Violation _testling;

	@BeforeEach
	public void setUp() throws Exception {
		_testling = new Violation("field", "is wrong!");
	}

	@Test
	public void testEquals() throws Exception {
		Violation copy = new Violation("field", "is wrong!");
		Violation newOne = new Violation("field", "is blank!");

		assertThat(_testling).as("Expect identity").isEqualTo(_testling);
		assertThat(_testling).as("Expect identity").isEqualTo(copy);
		assertThat(_testling).as("Expect non-identity").isNotEqualTo(newOne);
	}

	@Test
	public void testToString() throws Exception {
		assertThat(_testling.toString()).isEqualTo("field is wrong!");
	}

}
