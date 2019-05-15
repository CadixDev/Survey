/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.patcher.provider;

import org.cadixdev.lorenz.util.Registry;
import org.cadixdev.survey.config.mapper.provider.MapperProvider;

import java.util.ServiceLoader;

/**
 * The global list of {@link MapperProvider}s.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public final class PatcherProviders {

    /**
     * A {@link Registry} of {@link PatcherProvider}s, populated by service loaders.
     */
    public static final Registry<PatcherProvider<?, ?>> REGISTRY = new Registry<>();

    static {
        for (final PatcherProvider<?, ?> provider : ServiceLoader.load(PatcherProvider.class)) {
            REGISTRY.register(provider.getId(), provider);
        }
    }

    private PatcherProviders() {
    }

}
