/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper;

import org.cadixdev.lorenz.MappingSet;

/**
 * The context of which a mapper runs within.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public interface MapperContext {

    /**
     * Gets the mapping set, in use by the mapper.
     *
     * @return The mappings
     */
    MappingSet mappings();

    /**
     * Established whether the given class is blacklisted either
     * globally, or specific to the mapper.
     *
     * @param klass The class to check
     * @return {@code true} if the class is blacklisted;
     *         {@code false} otherwise
     */
    boolean blacklisted(final String klass);

}
