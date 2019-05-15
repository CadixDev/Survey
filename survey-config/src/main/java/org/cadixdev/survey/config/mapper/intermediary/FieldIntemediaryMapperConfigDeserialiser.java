/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.mapper.intermediary;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.cadixdev.survey.mapper.intermediary.FieldIntemediaryMapper;

import java.lang.reflect.Type;

public class FieldIntemediaryMapperConfigDeserialiser implements JsonDeserializer<FieldIntemediaryMapper.Config> {

    public static final FieldIntemediaryMapperConfigDeserialiser INSTANCE = new FieldIntemediaryMapperConfigDeserialiser();

    private static final String FORMAT = "format";
    private static final String FORMAT_DEFAULT = "field_{id}_{obf}";

    @Override
    public FieldIntemediaryMapper.Config deserialize(
            final JsonElement element,
            final Type type,
            final JsonDeserializationContext ctx) throws JsonParseException {
        if (!element.isJsonObject()) throw new JsonParseException("field intermediary config must be an object!");
        final JsonObject object = element.getAsJsonObject();

        final String format = object.has(FORMAT) ?
                object.get(FORMAT).getAsString() :
                FORMAT_DEFAULT;

        return new FieldIntemediaryMapper.Config(format);
    }

}
