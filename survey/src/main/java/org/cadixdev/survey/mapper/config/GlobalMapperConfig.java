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
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.survey.SurveyMapper;
import org.cadixdev.survey.mapper.AbstractMapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The global mapper configuration.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class GlobalMapperConfig {

    public final List<String> blacklist = new ArrayList<>();
    public final Map<SurveyMapper.MapperRegistration<?, ?>, List<?>> mapperConfigs = new HashMap<>();

    public List<AbstractMapper<?>> createMappers(final MappingSet mappings) {
        final List<AbstractMapper<?>> mappers = new ArrayList<>();
        this.mapperConfigs.forEach((regis, configs) -> configs.stream()
                .map(config -> regis.createMapper(mappings, configs))
                .forEach(mappers::add));
        return mappers;
    }

    public boolean isBlacklisted(final String name) {
        for (final String blacklisted : this.blacklist) {
            if (name.startsWith(blacklisted)) return true;
        }
        return false;
    }

    public List<?> getConfigs(final SurveyMapper.MapperRegistration<?, ?> registration) {
        return this.mapperConfigs.computeIfAbsent(registration, regis -> new ArrayList<>());
    }

    public static class Deserialiser implements JsonDeserializer<GlobalMapperConfig> {

        private static final String BLACKLIST = "blacklist";
        private static final String MAPPERS = "mappers";
        private static final String MAPPER_TYPE = "type";
        private static final String MAPPER_CONFIG = "config";

        private final Map<String, SurveyMapper.MapperRegistration<?, ?>> mappers;

        public Deserialiser(final Map<String, SurveyMapper.MapperRegistration<?, ?>> mappers) {
            this.mappers = mappers;
        }

        @Override
        public GlobalMapperConfig deserialize(
                final JsonElement element,
                final Type type,
                final JsonDeserializationContext ctx) throws JsonParseException {
            if (!element.isJsonObject()) throw new JsonParseException("global config must be an object!");
            final JsonObject object = element.getAsJsonObject();

            final GlobalMapperConfig config = new GlobalMapperConfig();

            if (object.has(BLACKLIST)) {
                final JsonElement blacklistElement = object.get(BLACKLIST);
                if (!blacklistElement.isJsonArray()) throw new JsonParseException("invalid blacklist!");

                for (final JsonElement arrElement : blacklistElement.getAsJsonArray()) {
                    if (!arrElement.isJsonPrimitive()) throw new JsonParseException("invalid blacklist entry!");
                    config.blacklist.add(arrElement.getAsString());
                }
            }

            if (object.has(MAPPERS)) {
                final JsonElement mappersElement = object.get(MAPPERS);
                if (!mappersElement.isJsonArray()) throw new JsonParseException("invalid mappers!");

                for (final JsonElement arrElement : mappersElement.getAsJsonArray()) {
                    if (!arrElement.isJsonObject()) throw new JsonParseException("invalid mapper!");
                    final JsonObject mapper = arrElement.getAsJsonObject();

                    if (!mapper.has(MAPPER_TYPE)) throw new JsonParseException("mapper missing type!");
                    final String mapperType = mapper.get(MAPPER_TYPE).getAsString();
                    if (!this.mappers.containsKey(mapperType)) throw new JsonParseException("invalid mapper type!");
                    final SurveyMapper.MapperRegistration<?, ?> registration = this.mappers.get(mapperType);

                    if (!mapper.has(MAPPER_CONFIG)) throw new JsonParseException("mapper missing config!");
                    config.getConfigs(registration).add(ctx.deserialize(mapper.get(MAPPER_CONFIG), registration.getConfigType()));
                }
            }

            return config;
        }

    }

}
