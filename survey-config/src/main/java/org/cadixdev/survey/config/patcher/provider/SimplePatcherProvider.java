/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.patcher.provider;

import com.google.gson.JsonDeserializer;
import org.cadixdev.survey.patcher.AbstractPatcher;

/**
 * A simple implementation of {@link PatcherProvider}, making implementation
 * vastly quicker.
 *
 * @param <M> The patcher type
 * @param <C> The patcher configuration type
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public abstract class SimplePatcherProvider<M extends AbstractPatcher<C>, C> implements PatcherProvider<M, C> {

    private final String id;
    private final Class<C> configType;
    private final JsonDeserializer<C> configDeserialiser;

    public SimplePatcherProvider(final String id,
                                final Class<C> configType,
                                final JsonDeserializer<C> configDeserialiser) {
        this.id = id;
        this.configType = configType;
        this.configDeserialiser = configDeserialiser;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Class<C> getConfigurationClass() {
        return this.configType;
    }

    @Override
    public JsonDeserializer<C> getConfigurationDeserialiser() {
        return this.configDeserialiser;
    }

}

