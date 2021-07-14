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

package com.espirit.moddev.cli.api.json.common;

public class AttributeNames {

	// common
	public static final String ATTR_COMMAND = "command";
	public static final String ATTR_RESULT = "result";
	public static final String ATTR_COMPONENT_NAME = "componentName";
	public static final String ATTR_FILES = "files";
	public static final String ATTR_PROJECT_NAME = "projectName";

	// result & exceptions
	public static final String ATTR_CAUSE = "cause";
	public static final String ATTR_CLASS = "class";
	public static final String ATTR_EXCEPTION = "exception";
	public static final String ATTR_ERROR = "error";
	public static final String ATTR_LOCALIZED_MESSAGE = "localizedMessage";
	public static final String ATTR_MESSAGE = "message";
	public static final String ATTR_RESULTS = "results";

	// related to module
	public static final String ATTR_COMPONENTS = "components";
	public static final String ATTR_MODULE_NAME = "moduleName";

	// related to components
	public static final String ATTR_PROJECT_COMPONENTS = "projectComponents";
	public static final String ATTR_SERVICES = "services";
	public static final String ATTR_WEB_COMPONENTS = "webComponents";

	// related to web apps
	public static final String ATTR_DEPLOY = "deploy";
	public static final String ATTR_WEB_APPS = "webApps";
	public static final String ATTR_WEB_APP_NAME = "webAppName";

	// related to project apps
	public static final String ATTR_PROJECT_APPS = "projectApps";

	// related to services
	public static final String ATTR_AUTO_START = "autoStart";
	public static final String ATTR_RESTART = "restart";
	public static final String ATTR_SERVICE_NAME = "serviceName";

}
