/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper;

import static org.objectweb.asm.Opcodes.ASM6;

import org.objectweb.asm.ClassVisitor;
import org.cadixdev.lorenz.MappingSet;

/**
 * An object that can generate some de-obfuscation classes.
 *
 * @param <C> The type of the configuration
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public abstract class AbstractMapper<C> extends ClassVisitor {

    protected final MappingSet mappings;
    protected final C configuration;

    public AbstractMapper(final MappingSet mappings, final C configuration) {
        super(ASM6);
        this.mappings = mappings;
        this.configuration = configuration;
    }

    /**
     * Gets the mappings that the mapper is working on.
     *
     * @return The mappings
     */
    public final MappingSet getMappings() {
        return this.mappings;
    }

    /**
     * Gets the configuration of this mapper.
     *
     * @return The configuration
     */
    public final C getConfiguration() {
        return this.configuration;
    }

}
