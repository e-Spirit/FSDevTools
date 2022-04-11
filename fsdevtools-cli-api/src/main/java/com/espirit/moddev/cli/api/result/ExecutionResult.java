/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit GmbH
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
import com.espirit.moddev.cli.api.json.serializer.DefaultExecutionResultSerializer;
import com.espirit.moddev.util.JacksonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface representing a single result when executing a {@link Command}.
 * <p>
 * <br/><br/>
 * This interface can be used if you want to have simple json support for a {@link Command}. Each instance will be serialized
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
@JsonSerialize(using = DefaultExecutionResultSerializer.class)
public interface ExecutionResult {

}
