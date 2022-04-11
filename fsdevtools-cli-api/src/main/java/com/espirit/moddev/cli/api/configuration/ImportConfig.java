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

package com.espirit.moddev.cli.api.configuration;

/**
 * {@link com.espirit.moddev.cli.api.configuration.Config} specialization that defines additional means to access the configuration for an import operation.
 *
 * @author e-Spirit GmbH
 */
public interface ImportConfig extends Config {

    /**
     * Get the revision comment used for the revision created by the import operation.
     *
     * @see de.espirit.firstspirit.store.access.nexport.operations.ImportOperation#setRevisionComment(String)
     * @return the revision comment used for the revision created by the import operation
     */
    String getImportComment();

    /**
     * Overrides the default implementation given through {@link Config#isCreatingProjectIfMissing()} and
     * returns true, because project import often imports a complete project as a new one.
     *
     * @return true
     */
    @Override
    default boolean isCreatingProjectIfMissing() { return true; }

}
