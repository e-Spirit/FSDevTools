/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
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

package com.espirit.moddev;

import com.espirit.moddev.test.rules.firstspirit.FirstSpiritConnectionRule;
import com.espirit.moddev.test.rules.logging.InitLog4jLoggingRule;
import org.apache.log4j.Level;

/**
 * This is a marker class, that can be used to categorize integration
 * tests. Constants and other test helpers could be implemented here.
 */
public class IntegrationTest {
    public static final String PROJECT_NAME = "fs-cli Project";

    public static final InitLog4jLoggingRule LOGGING_RULE = new InitLog4jLoggingRule(Level.INFO);
    public static final FirstSpiritConnectionRule FIRST_SPIRIT_CONNECTION_RULE = new FirstSpiritConnectionRule();

}
