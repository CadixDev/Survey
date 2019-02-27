/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A registry of identifiable objects.
 *
 * @param <T> The type of the registered objects
 * @author Jamie Mansfield
 * @since 0.2.0
 */
// TODO(0.2.0): merge changes into Lorenz
public class Registry<T> {

    private final Map<String, T> map = new HashMap<>();

    /**
     * Registers the given value, with the given identifier.
     *
     * @param id The identifier of the value
     * @param value The value
     * @return {@code this}, for chaining
     */
    public Registry<T> register(final String id, final T value) {
        this.map.put(id.toLowerCase(), value);
        return this;
    }

    /**
     * Gets the value of the given identifier.
     *
     * @param id The identifier of the value
     * @return The value
     */
    public T byId(final String id) {
        return this.map.get(id.toLowerCase());
    }

    /**
     * Gets all of the values within the registry.
     *
     * @return The values
     */
    public Collection<T> values() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    /**
     * @see Map#forEach(BiConsumer)
     */
    public void forEach(final BiConsumer<String, T> consumer) {
        this.map.forEach(consumer);
    }

}