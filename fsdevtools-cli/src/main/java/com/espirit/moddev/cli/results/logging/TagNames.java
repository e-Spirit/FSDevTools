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

package com.espirit.moddev.cli.results.logging;

//        TODO: CORE-9421: Remove string comparison and use TagNames-API
public enum TagNames {
	TEMPLATE("TEMPLATE"),
	SECTION("SECTION"),
	PAGE("PAGE"),
	PAGEREF("PAGEREF"),
	SCHEMA("SCHEMA"),
	PAGETEMPLATES("PAGETEMPLATES"),
	MEDIUM("MEDIUM"),
	LINKTEMPLATE("LINKTEMPLATE"),
	FORMATTEMPLATE("FORMATTEMPLATE"),
	MEDIANODE("MEDIANODE"),
	WORKFLOW("WORKFLOW");

	private String name;

	public String getName() {
		return name;
	}

	TagNames(String name) {
		this.name = name;
	}
}
