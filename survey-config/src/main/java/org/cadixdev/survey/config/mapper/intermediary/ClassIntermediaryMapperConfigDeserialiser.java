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
import org.cadixdev.survey.mapper.intermediary.ClassIntermediaryMapper;

import java.lang.reflect.Type;

public class ClassIntermediaryMapperConfigDeserialiser implements JsonDeserializer<ClassIntermediaryMapper.Config> {

    public static final ClassIntermediaryMapperConfigDeserialiser INSTANCE = new ClassIntermediaryMapperConfigDeserialiser();

    private static final String PACKAGE = "package";
    private static final String FORMAT = "format";
    private static final String FORMAT_DEFAULT = "Class{id}_{obf}";

    @Override
    public ClassIntermediaryMapper.Config deserialize(
            final JsonElement element,
            final Type type,
            final JsonDeserializationContext ctx) throws JsonParseException {
        if (!element.isJsonObject()) throw new JsonParseException("class intermediary config must be an object!");
        final JsonObject object = element.getAsJsonObject();

        final String format = object.has(FORMAT) ?
                object.get(FORMAT).getAsString() :
                FORMAT_DEFAULT;

        final String packageName = object.has(PACKAGE) ?
                object.get(PACKAGE).getAsString() :
                "";

        return new ClassIntermediaryMapper.Config(format, packageName);
    }

}
