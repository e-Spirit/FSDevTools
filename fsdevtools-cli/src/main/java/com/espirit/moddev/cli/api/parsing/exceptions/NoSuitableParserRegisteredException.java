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

package com.espirit.moddev.cli.api.parsing.exceptions;

/**
 * This exception indicates that no parser is available for a given input string.
 */
public class NoSuitableParserRegisteredException extends RuntimeException {

	/**
	 * Create a new instance of this exception with the given message.
	 *
	 * @param message the message of this exception
	 * @see java.lang.Exception#Exception(String)
	 */
	public NoSuitableParserRegisteredException(String message) {
		super(message);
	}
}
