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

package com.espirit.moddev.cli.exception;

/**
 * The Class LayerMappingException.
 */
public class LayerMappingException extends RuntimeException {

	private static final long serialVersionUID = 8164220948231148970L;

	/**
	 * Instantiates a new layer mapping exception.
	 */
	public LayerMappingException() {
		super();
	}

	/**
	 * Instantiates a new layer mapping exception.
	 *
	 * @param message the message
	 * @param cause   the cause
	 */
	public LayerMappingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new layer mapping exception.
	 *
	 * @param message the message
	 */
	public LayerMappingException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new layer mapping exception.
	 *
	 * @param cause the cause
	 */
	public LayerMappingException(Throwable cause) {
		super(cause);
	}

}
