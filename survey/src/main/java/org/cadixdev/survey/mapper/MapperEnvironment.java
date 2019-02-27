/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.cadixdev.bombe.jar.JarClassEntry;
import org.cadixdev.bombe.jar.Jars;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.util.Registry;
import org.cadixdev.survey.context.SurveyContext;
import org.cadixdev.survey.mapper.provider.MapperProvider;
import org.objectweb.asm.ClassReader;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.jar.JarFile;

/**
 * The environment mappers run within.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class MapperEnvironment {

    private static <T> Comparator<T> comparingLength(final Function<? super T, String> keyExtractor) {
        return (c1, c2) -> {
            final String key1 = keyExtractor.apply(c1);
            final String key2 = keyExtractor.apply(c2);
            if (key1.length() != key2.length()) {
                return key1.length() - key2.length();
            }
            return key1.compareTo(key2);
        };
    }

    private final MappingSet mappings;
    private final List<Instance<?, ?>> mappers = new ArrayList<>();

    public MapperEnvironment(final MappingSet mappings) {
        this.mappings = mappings;
    }

    /**
     * Gets the mapping set, that the environment is working on.
     *
     * @return The mappings
     */
    public MappingSet getMappings() {
        return this.mappings;
    }

    public void run(final JarFile jar) {
        for (final Instance<?, ?> mapper : this.mappers) {
            System.out.println("Running '" + mapper.getId() + "' mapper...");
            mapper.run(jar);
        }
    }

    /**
     * The container for mapper instances.
     *
     * @param <C> The type of the mapper's configuration
     * @param <M> The type of the mapper
     */
    public static class Instance<C, M extends AbstractMapper<C>> {

        private final String id;
        private final M mapper;

        Instance(final String id, final M mapper) {
            this.id = id;
            this.mapper = mapper;
        }

        /**
         * Gets the identifier assigned to the mapper instance.
         *
         * @return The identifier
         */
        public String getId() {
            return this.id;
        }

        /**
         * Gets the mapper.
         *
         * @return The mapper
         */
        public M getMapper() {
            return this.mapper;
        }

        public void run(final JarFile jar) {
            Jars.walk(jar)
                    .filter(JarClassEntry.class::isInstance)
                    .map(JarClassEntry.class::cast)
                    .filter(entry -> !this.mapper.ctx().blacklisted(entry.getName()))
                    .sorted(comparingLength(JarClassEntry::getName))
                    .forEach(entry -> {
                        final ClassReader klass = new ClassReader(entry.getContents());
                        klass.accept(this.mapper, 0);
                    });
        }

    }

    /**
     * The de-serialiser used for reading mapper configuration files.
     */
    public static class Deserialiser implements JsonDeserializer<MapperEnvironment> {

        private static final String CONTEXTS = "contexts";
        private static final String DEFAULT_CONTEXT = "default_context";
        private static final String MAPPERS = "mappers";
        private static final String MAPPER_ID = "id";
        private static final String MAPPER_TYPE = "type";
        private static final String MAPPER_CONTEXT = "context";
        private static final String MAPPER_CONFIG = "config";

        private final MappingSet mappings;
        private final Registry<MapperProvider<?, ?>> registry;

        public Deserialiser(final MappingSet mappings, final Registry<MapperProvider<?, ?>> registry) {
            this.mappings = mappings;
            this.registry = registry;
        }

        @Override
        public MapperEnvironment deserialize(
                final JsonElement element,
                final Type type,
                final JsonDeserializationContext ctx) throws JsonParseException {
            if (!element.isJsonObject()) throw new JsonParseException("Invalid environment configuration!");
            final JsonObject object = element.getAsJsonObject();

            final SurveyContext.Deserialiser contextDeserialiser = new SurveyContext.Deserialiser(this.mappings);

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
                defaultCtx = new SurveyContext(this.mappings, null) {};
            }

            final MapperEnvironment env = new MapperEnvironment(this.mappings);

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
                    env.mappers.add(new Instance<>(mapperId, provider.deserialise(mapperCtx, ctx, mapper.get(MAPPER_CONFIG))));
                }
            }

            return env;
        }

    }

}
