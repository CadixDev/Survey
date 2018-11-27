/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.cadixdev.survey.mapper.EnumConstantsMapper;

import java.lang.reflect.Type;

/**
 * Configuration for {@link EnumConstantsMapper}.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class EnumConstantsMapperConfig {

    /**
     * Toggle for mapping {@code "$VALUES"}.
     */
    public boolean mapSyntheticValues = true;

    public static class Deserialiser implements JsonDeserializer<EnumConstantsMapperConfig> {

        public static final Deserialiser INSTANCE = new Deserialiser();

        private static final String MAP_SYNTHETIC_VALUES = "map_synthetic_values";

        @Override
        public EnumConstantsMapperConfig deserialize(
                final JsonElement element,
                final Type type,
                final JsonDeserializationContext ctx) throws JsonParseException {
            if (!element.isJsonObject()) throw new JsonParseException("enum-constants config must be an object!");
            final JsonObject object = element.getAsJsonObject();

            final EnumConstantsMapperConfig config = new EnumConstantsMapperConfig();

            if (object.has(MAP_SYNTHETIC_VALUES)) {
                config.mapSyntheticValues = object.get(MAP_SYNTHETIC_VALUES).getAsBoolean();
            }

            return config;
        }

    }

}
