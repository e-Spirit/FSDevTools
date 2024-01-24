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

package com.espirit.moddev.cli.api.result;

import com.espirit.moddev.cli.api.command.Command;
import com.espirit.moddev.cli.api.json.serializer.DefaultExecutionErrorResultSerializer;
import com.espirit.moddev.util.JacksonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing a single result if an error during a {@link Command} occurs.
 * <br/><br/>
 * This interface can be used if you want to have simple json support. Each instance will be serialized
 * (using a mapper from {@link JacksonUtil#createOutputMapper()}) with the information gathered from the {@link #toString()} method.
 * <p>
 * If complex json objects are needed, you can use default JACKSON mechanics like the {@link JsonProperty}-annotation or
 * custom {@link JsonSerializer serializers} (in combination with {@link JsonSerialize}-annotation).
 *
 * @see JacksonUtil#createOutputMapper()
 * @see JsonProperty
 * @see JsonSerialize
 * @see JsonSerializer
 */
@JsonSerialize(using = DefaultExecutionErrorResultSerializer.class)
public interface ExecutionErrorResult<E extends Throwable> extends ExecutionResult {

	@NotNull
	E getThrowable();

}
