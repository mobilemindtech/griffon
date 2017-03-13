/*
 * Copyright 2008-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.util;

import griffon.core.resources.ResourceHandler;
import griffon.util.ResourceBundleLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.URL;
import java.util.List;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 2.11.0
 */
public abstract class AbstractResourceBundleLoader implements ResourceBundleLoader {
    protected static final String ERROR_FILENAME_BLANK = "Argument 'fileName' must not be blank";
    protected static final String ERROR_SUFFIX_BLANK = "Argument 'suffix' must not be blank";
    protected static final String ERROR_RESOURCE_HANDLER_NULL = "Argument 'resourceHandler' must not be null";

    protected final ResourceHandler resourceHandler;

    @Inject
    public AbstractResourceBundleLoader(@Nonnull ResourceHandler resourceHandler) {
        this.resourceHandler = requireNonNull(resourceHandler, ERROR_RESOURCE_HANDLER_NULL);
    }

    @Nonnull
    protected ResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    @Nullable
    protected URL getResourceAsURL(@Nonnull String fileName, @Nonnull String suffix) {
        requireNonBlank(fileName, ERROR_FILENAME_BLANK);
        requireNonBlank(suffix, ERROR_SUFFIX_BLANK);
        return resourceHandler.getResourceAsURL(fileName + suffix);
    }

    @Nullable
    protected List<URL> getResources(@Nonnull String fileName, @Nonnull String suffix) {
        requireNonBlank(fileName, ERROR_FILENAME_BLANK);
        requireNonBlank(suffix, ERROR_SUFFIX_BLANK);
        return resourceHandler.getResources(fileName + suffix);
    }
}
