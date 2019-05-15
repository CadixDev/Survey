/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.mapper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.cadixdev.survey.mapper.config.FieldNameMapperConfig;
import org.cadixdev.survey.mapper.config.ModifierRequirement;

import java.lang.reflect.Type;

public class FieldNameMapperConfigDeserialiser implements JsonDeserializer<FieldNameMapperConfig> {

    public static final FieldNameMapperConfigDeserialiser INSTANCE = new FieldNameMapperConfigDeserialiser();

    private static final String REQUIREMENTS = "requirements";
    private static final String DESC = "desc";
    private static final String NAME = "name";

    @Override
    public FieldNameMapperConfig deserialize(
            final JsonElement element,
            final Type type,
            final JsonDeserializationContext ctx) throws JsonParseException {
        if (!element.isJsonObject()) throw new JsonParseException("field config must be an object!");
        final JsonObject object = element.getAsJsonObject();

        final FieldNameMapperConfig config = new FieldNameMapperConfig();

        if (!object.has(DESC) || !object.has(NAME)) throw  new JsonParseException("Missing essential fields!");
        config.desc = object.get(DESC).getAsString();
        config.name = object.get(NAME).getAsString();

        if (object.has(REQUIREMENTS)) {
            if (!object.get(REQUIREMENTS).isJsonObject()) {
                throw new JsonParseException("Requirements block must be an object!");
            }
            final JsonObject reqs = object.get(REQUIREMENTS).getAsJsonObject();

            for (final String requirement : reqs.keySet()) {
                final ModifierRequirement modifierRequirement = ModifierRequirement.byId(requirement);
                if (modifierRequirement == null) {
                    throw new JsonParseException("Unknown modifier requirement: '" + requirement + "'!");
                }
                config.requirements.put(modifierRequirement, reqs.get(requirement).getAsBoolean());
            }
        }

        return config;
    }

}
