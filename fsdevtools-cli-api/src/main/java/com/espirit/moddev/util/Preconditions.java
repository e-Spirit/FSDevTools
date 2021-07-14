/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2021 e-Spirit AG
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

package com.espirit.moddev.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Preconditions {

	@NotNull
	public static <T> T checkNotNull(@Nullable final T object) {
		return checkNotNull(object, null);
	}

	@NotNull
	public static <T> T checkNotNull(@Nullable final T object, @Nullable final String message) {
		if (object == null) {
			throw (message == null ? new NullPointerException() : new NullPointerException(message));
		}
		return object;
	}

	@NotNull
	public static String checkNotEmpty(@Nullable final String text) {
		return checkNotEmpty(text, null);
	}

	@NotNull
	public static String checkNotEmpty(@Nullable final String text, @Nullable final String message) {
		checkNotNull(text, message);
		if (StringUtils.isAnyBlank(text)) {
			throw (message == null ? new IllegalArgumentException() : new IllegalArgumentException(message));
		}
		return text;
	}

	@NotNull
	public static <C extends Collection<?>> C checkNotEmpty(@NotNull final C collection) {
		return checkNotEmpty(collection, null);
	}

	@NotNull
	public static <C extends Collection<?>> C checkNotEmpty(@Nullable final C collection, final String message) {
		checkNotNull(collection, message);
		if (collection.isEmpty()) {
			throw (message == null ? new IllegalArgumentException() : new IllegalArgumentException(message));
		}
		return collection;
	}

	@NotNull
	public static <C> List<C> getNonNullList(@Nullable final List<C> collection) {
		if (collection == null) {
			return Collections.emptyList();
		}
		return collection;
	}

}
