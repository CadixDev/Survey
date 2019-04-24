/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.cadixdev.lorenz.util.Registry;
import org.cadixdev.survey.context.SimpleSurveyContext;
import org.cadixdev.survey.context.SurveyContext;
import org.cadixdev.survey.context.SurveyContextDeserialiser;
import org.cadixdev.survey.mapper.provider.MapperProvider;
import org.cadixdev.survey.mapper.provider.MapperProviders;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SurveyDeserialiser implements JsonDeserializer<Survey> {

    private static final String CONTEXTS = "contexts";
    private static final String DEFAULT_CONTEXT = "default_context";
    private static final String MAPPERS = "mappers";
    private static final String MAPPER_ID = "id";
    private static final String MAPPER_TYPE = "type";
    private static final String MAPPER_CONTEXT = "context";
    private static final String MAPPER_CONFIG = "config";

    private final Survey survey;
    private final Registry<MapperProvider<?, ?>> registry;

    public SurveyDeserialiser() {
        this(new Survey(), MapperProviders.REGISTRY);
    }

    public SurveyDeserialiser(final Survey survey, final Registry<MapperProvider<?, ?>> registry) {
        this.survey = survey;
        this.registry = registry;
    }

    @Override
    public Survey deserialize(
            final JsonElement element,
            final Type type,
            final JsonDeserializationContext ctx) throws JsonParseException {
        if (!element.isJsonObject()) throw new JsonParseException("Invalid Survey configuration!");
        final JsonObject object = element.getAsJsonObject();

        // Read the contexts
        final SurveyContextDeserialiser contextDeserialiser = new SurveyContextDeserialiser(this.survey);
        if (object.has(CONTEXTS)) {
            final JsonElement contextsElement = object.get(CONTEXTS);
            if (!contextsElement.isJsonArray()) throw new JsonParseException("Contexts must be an array!");

            for (final JsonElement arrElement : contextsElement.getAsJsonArray()) {
                contextDeserialiser.deserialize(arrElement, SurveyContext.class, ctx);
            }
        }

        final SurveyContext defaultCtx;
        if (object.has(DEFAULT_CONTEXT)) {
            defaultCtx = contextDeserialiser.deserialize(object.get(DEFAULT_CONTEXT), SurveyContext.class, ctx);
        }
        else {
            defaultCtx = new SimpleSurveyContext(this.survey.mappings(), new ArrayList<>());
        }

        // Read mappers
        if (object.has(MAPPERS)) {
            final JsonElement mappersElement = object.get(MAPPERS);
            if (!mappersElement.isJsonArray()) throw new JsonParseException("Mappers must be an array!");

            for (final JsonElement arrElement : mappersElement.getAsJsonArray()) {
                if (!arrElement.isJsonObject()) throw new JsonParseException("Invalid mapper entry!");
                final JsonObject mapper = arrElement.getAsJsonObject();

                final SurveyContext mapperCtx;
                if (mapper.has(MAPPER_CONTEXT)) {
                    mapperCtx = contextDeserialiser.deserialize(mapper.get(MAPPER_CONTEXT), SurveyContext.class, ctx);
                }
                else {
                    mapperCtx = defaultCtx;
                }

                if (!mapper.has(MAPPER_ID)) throw new JsonParseException("Mapper missing id!");
                if (!mapper.has(MAPPER_TYPE)) throw new JsonParseException("Mapper missing type!");
                final String mapperId = mapper.get(MAPPER_ID).getAsString();
                final String mapperType = mapper.get(MAPPER_TYPE).getAsString();

                final MapperProvider<?, ?> provider = this.registry.byId(mapperType);
                if (provider == null) throw new JsonParseException("Unknown mapper type specified!");

                if (!mapper.has(MAPPER_CONFIG)) throw new JsonParseException("Mapper configuration not present!");

                // register the mapper instance :)
                provider._register(this.survey, mapperId, mapperCtx, mapper.get(MAPPER_CONFIG), ctx);
            }
        }

        return this.survey;
    }

}
