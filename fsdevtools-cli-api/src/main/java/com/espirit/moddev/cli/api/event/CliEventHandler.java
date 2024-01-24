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

package com.espirit.moddev.cli.api.event;

/**
 * Interface for handler implementations that can be used for Cli event handling.
 * Is meant to provide an action in case an exception occurs and when the command
 * execution is finished - whether with or without exception.
 */
public interface CliEventHandler {

	/**
	 * Place to implement logic that should be executed after every execution
	 * a Cli instance performs. Is called whether or not an exception occurred
	 * before.
	 */
	default void afterTermination() {
	}

	/**
	 * Place to implement logic that should be executed when an exception
	 * occurs during command execution.
	 *
	 * @param t the throwable instance to handle
	 */
	default void afterExceptionalTermination(Throwable t) {
	}
}
