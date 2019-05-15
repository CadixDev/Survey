/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.mapper.provider;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import org.cadixdev.survey.Survey;
import org.cadixdev.survey.context.SurveyContext;
import org.cadixdev.survey.mapper.AbstractMapper;

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

    default void _register(final Survey survey, final String id, final SurveyContext ctx,
                          final JsonElement jsonElement, final JsonDeserializationContext jsonCtx) {
        survey.mapper(
                id,
                this::create,
                ctx,
                this.getConfigurationDeserialiser().deserialize(jsonElement, this.getConfigurationClass(), jsonCtx)
        );
    }

}
