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

project.afterEvaluate {
    disablePublishing();
}

dependencies {
    api(project(":fsdevtools-commands:module"))
    api(project(":fsdevtools-commands:project"))
    api(project(":fsdevtools-commands:schedule"))
    api(project(":fsdevtools-commands:script"))
    api(project(":fsdevtools-commands:server"))
    api(project(":fsdevtools-commands:service"))
    api(project(":fsdevtools-commands:test"))
    api(project(":fsdevtools-commands:feature"))
}
