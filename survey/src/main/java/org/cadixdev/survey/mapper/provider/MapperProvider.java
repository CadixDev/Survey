/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.provider;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import org.cadixdev.survey.mapper.AbstractMapper;
import org.cadixdev.survey.SurveyContext;

/**
 * Used to describe a mapper in complete.
 *
 * @param <M> The mapper type
 * @param <C> The mapper configuration type
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public interface MapperProvider<M extends AbstractMapper<C>, C> {

    /**
     * Gets the identifier of the mapper type.
     *
     * @return The id
     */
    String getId();

    /**
     * Creates an instance of the mapper type, given a context and
     * configuration.
     *
     * @param ctx The context
     * @param config The configuration
     * @return The mapper instance
     */
    M create(final SurveyContext ctx, final C config);

    /**
     * Gets the class of the configuration type.
     *
     * @return The configuration class
     */
    Class<C> getConfigurationClass();

    /**
     * Gets the {@link JsonDeserializer} for the configuration.
     *
     * @return The configuration de-serialiser
     */
    JsonDeserializer<C> getConfigurationDeserialiser();

    /**
     * De-serialises a mapper from the given JSON context and mapper
     * context.
     *
     * @param mapperCtx The mapper context
     * @param jsonCtx The JSON context
     * @param jsonElement The JSON element to de-serialise
     * @return The mapper instance
     */
    default M deserialise(final SurveyContext mapperCtx,
                          final JsonDeserializationContext jsonCtx, final JsonElement jsonElement) {
        // De-serialise the configuration
        final C config = this.getConfigurationDeserialiser()
                .deserialize(jsonElement, this.getConfigurationClass(), jsonCtx);

        // Create the mapper instance
        return this.create(mapperCtx, config);
    }

}
