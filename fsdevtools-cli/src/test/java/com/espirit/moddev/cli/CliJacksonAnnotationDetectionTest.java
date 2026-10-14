/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2026 Crownpeak Technology GmbH
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

package com.espirit.moddev.cli;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliJacksonAnnotationDetectionTest {

	@Test
	public void classWithJacksonAnnotation_isDetected() {
		assertThat(Cli.referencesJacksonAnnotations(Annotated.class)).isTrue();
	}

	@Test
	public void classWithoutJacksonAnnotation_isNotDetected() {
		assertThat(Cli.referencesJacksonAnnotations(Plain.class)).isFalse();
	}

	static class Annotated {

		@JsonProperty("renamed")
		String _value;

	}

	static class Plain {

		String _value;

	}

}