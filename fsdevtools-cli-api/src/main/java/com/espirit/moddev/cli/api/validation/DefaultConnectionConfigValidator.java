package com.espirit.moddev.cli.api.validation;

/*
 *
 * *********************************************************************
 * fsdevtools
 * %%
 * Copyright (C) 2016 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License"),
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

import com.espirit.moddev.cli.api.configuration.Config;
import com.espirit.moddev.shared.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;


/**
 * The type DefaultConnectionConfigValidator is used to check connection settings.
 */
public class DefaultConnectionConfigValidator implements Validator<Config> {

    private static final String MESSAGE_NULL_BLANK = "is null or blank";
    private static final String MESSAGE_NULL = "is null";

    @Override
    public Set<Violation> validate(@NotNull final Config config) {
        final Set<Violation> violations = new HashSet<>();

        if(StringUtils.isNullOrEmpty(config.getHost())){
            violations.add(new Violation("host", MESSAGE_NULL_BLANK));
        }
        if(StringUtils.isNullOrEmpty(config.getUser())){
            violations.add(new Violation("user", MESSAGE_NULL_BLANK));
        }
        if(StringUtils.isNullOrEmpty(config.getPassword())){
            violations.add(new Violation("password", MESSAGE_NULL_BLANK));
        }
        if(config.getConnectionMode() == null){
            violations.add(new Violation("connectionMode", MESSAGE_NULL));
        }
        if(config.getPort() == null){
            violations.add(new Violation("port", MESSAGE_NULL));
        }

        return violations;
    }
}
