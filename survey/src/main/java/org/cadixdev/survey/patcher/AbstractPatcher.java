/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.patcher;

import static org.objectweb.asm.Opcodes.ASM6;

import org.objectweb.asm.ClassVisitor;

/**
 * An object that can patch a target.
 *
 * @param <C> The type of the configuration
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public abstract class AbstractPatcher<C> extends ClassVisitor {

    protected final C configuration;

    public AbstractPatcher(final C configuration) {
        super(ASM6);
        this.configuration = configuration;
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
