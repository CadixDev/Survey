/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.util.Registry;
import org.cadixdev.survey.mapper.MapperEnvironment;

import java.util.ServiceLoader;

/**
 * The global list of {@link MapperProvider}s.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public final class MapperProviders {

    /**
     * A {@link Registry} of {@link MapperProvider}s, populated by service loaders.
     */
    public static final Registry<MapperProvider<?, ?>> REGISTRY = new Registry<>();

    static {
        for (final MapperProvider<?, ?> provider : ServiceLoader.load(MapperProvider.class)) {
            REGISTRY.register(provider.getId(), provider);
        }
    }

    /**
     * Creates a gson instance, that can be used to de-serialise a mapper environment.
     *
     * @param providerRegistry The mapper provider registry
     * @param mappings The mapping set
     * @return The gson instance
     */
    public static Gson createGson(final Registry<MapperProvider<?, ?>> providerRegistry,
                                  final MappingSet mappings) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(MapperEnvironment.class, new MapperEnvironment.Deserialiser(mappings, providerRegistry));
        REGISTRY.values().forEach(provider ->
                builder.registerTypeAdapter(provider.getConfigurationClass(), provider.getConfigurationDeserialiser())
        );
        return builder.create();
    }

    private MapperProviders() {
    }

}
