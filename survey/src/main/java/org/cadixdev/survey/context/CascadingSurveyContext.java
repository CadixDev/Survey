/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.context;

import org.cadixdev.lorenz.MappingSet;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link SurveyContext} that is backed by
 * many - allowing multiples sources to be used.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class CascadingSurveyContext implements SurveyContext {

    private final MappingSet mappings;
    private final List<SurveyContext> contexts = new ArrayList<>();

    public CascadingSurveyContext(final MappingSet mappings) {
        this.mappings = mappings;
    }

    /**
     * Installs a {@link SurveyContext} to the context.
     *
     * @param ctx The context
     * @return {@code this}, for chaining
     */
    public CascadingSurveyContext install(final SurveyContext ctx) {
        if (ctx == null) return this;
        this.contexts.add(ctx);
        return this;
    }

    @Override
    public MappingSet mappings() {
        return this.mappings;
    }

    @Override
    public boolean blacklisted(final String klass) {
        for (final SurveyContext ctx : this.contexts) {
            if (ctx.blacklisted(klass)) return true;
        }
        return false;
    }

}
