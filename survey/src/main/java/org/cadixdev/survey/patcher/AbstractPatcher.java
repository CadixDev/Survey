/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.patcher;

import org.objectweb.asm.ClassVisitor;
import org.cadixdev.survey.context.SurveyContext;

/**
 * An object that can patch a target.
 *
 * @param <C> The type of the configuration
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public abstract class AbstractPatcher<C> {

    protected final SurveyContext ctx;
    protected final C configuration;

    public AbstractPatcher(final SurveyContext ctx, final C configuration) {
        this.ctx = ctx;
        this.configuration = configuration;
    }

    /**
     * Gets the context the patcher is running within.
     *
     * @return The context
     */
    public final SurveyContext ctx() {
        return this.ctx;
    }

    /**
     * Gets the configuration of this patcher.
     *
     * @return The configuration
     */
    public final C getConfiguration() {
        return this.configuration;
    }

    /**
     * Creates a {@link ClassVisitor} to modify the class.
     *
     * @param parent The parent class visitor
     * @return The class visitor
     */
    public abstract ClassVisitor createVisitor(final ClassVisitor parent);

}
