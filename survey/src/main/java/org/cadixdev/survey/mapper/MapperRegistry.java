/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.survey.mapper.config.EnumNameMapperConfig;
import org.cadixdev.survey.mapper.config.FieldNameMapperConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A registry of {@link AbstractMapper}s.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class MapperRegistry {

    /**
     * Populates a mapper registry, with the Survey-provided mappers.
     *
     * @return The registry
     */
    public static MapperRegistry createDefault() {
        return new MapperRegistry()
                .register(
                        "enum",
                        EnumNameMapper::new,
                        EnumNameMapperConfig.class,
                        EnumNameMapperConfig.Deserialiser.INSTANCE
                )
                .register(
                        "field-name",
                        FieldNameMapper::new,
                        FieldNameMapperConfig.class,
                        FieldNameMapperConfig.Deserialiser.INSTANCE
                )
                ;
    }

    private final Map<String, Registration<?, ?>> mappers = new HashMap<>();

    /**
     * Registers a mapper type to the registry.
     *
     * @param id The <strong>unique</strong> identifier of the mapper
     * @param constructor The constructor for the mapper
     * @param configType The type of the mapper's configuration
     * @param configDeserialiser The de-serialiser for the mapper's configuration
     * @param <C> The type of the mapper's configuration
     * @param <M> The type of the mapper
     * @return {@code this}, allowing chaining
     */
    public <C, M extends AbstractMapper<C>> MapperRegistry register(
            final String id,
            final BiFunction<MapperContext, C, M> constructor,
            final Class<C> configType,
            final JsonDeserializer<C> configDeserialiser) {
        if (this.mappers.containsKey(id)) {
            throw new UnsupportedOperationException("Mappers must use unique identifiers (id: '" + id + "')!");
        }

        // Register the mapper
        this.mappers.put(id, new Registration<>(
                constructor,
                configType,
                configDeserialiser
        ));

        return this;
    }

    /**
     * Gets the mapper registration, for the given identifier.
     *
     * @param id The identifier of the mapper
     * @return The mapper, wrapped in an {@link Optional}
     */
    public Optional<Registration<?, ?>> get(final String id) {
        return Optional.ofNullable(this.mappers.get(id));
    }

    /**
     * Creates a {@link Gson} instance, that can be used to de-serialise a
     * mapper configuration.
     *
     * @return The gson instance
     */
    public Gson createGson(final MappingSet mappings) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(MapperEnvironment.class, new MapperEnvironment.Deserialiser(mappings, this));
        this.mappers.forEach((id, regis) -> builder.registerTypeAdapter(regis.getConfigType(), regis.getConfigDeserialiser()));
        return builder.create();
    }

    /**
     * A representation of a mapper type, including a de-serialiser.
     *
     * @param <C> The type of the mapper's configuration
     * @param <M> The type of the mapper
     */
    public static class Registration<C, M extends AbstractMapper<C>> {

        private final BiFunction<MapperContext, C, M> constructor;
        private final Class<C> configType;
        private final JsonDeserializer<C> configDeserialiser;

        Registration(final BiFunction<MapperContext, C, M> constructor,
                           final Class<C> configType,
                           final JsonDeserializer<C> configDeserialiser) {
            this.configType = configType;
            this.constructor = constructor;
            this.configDeserialiser = configDeserialiser;
        }

        /**
         * Creates a {@link AbstractMapper} given a mapping set and configuration.
         *
         * @param ctx The mapper context
         * @param config The mapper configuration
         * @return The mapper
         */
        public M create(final MapperContext ctx, final Object config) {
            return this.constructor.apply(ctx, this.configType.cast(config));
        }

        /**
         * Gets the class of the configuration.
         *
         * @return The configuration class
         */
        public Class<C> getConfigType() {
            return this.configType;
        }

        /**
         * Gets the {@link JsonDeserializer} for the configuration.
         *
         * @return The json de-serialiser
         */
        public JsonDeserializer<C> getConfigDeserialiser() {
            return this.configDeserialiser;
        }

    }

}
