/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.mapper.provider;

import com.google.gson.JsonDeserializer;
import org.cadixdev.survey.mapper.AbstractMapper;

/**
 * A simple implementation of {@link MapperProvider}, making implementation
 * vastly quicker.
 *
 * @param <M> The mapper type
 * @param <C> The mapper configuration type
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public abstract class SimpleMapperProvider<M extends AbstractMapper<C>, C> implements MapperProvider<M, C> {

    private final String id;
    private final Class<C> configType;
    private final JsonDeserializer<C> configDeserialiser;

    public SimpleMapperProvider(final String id,
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
