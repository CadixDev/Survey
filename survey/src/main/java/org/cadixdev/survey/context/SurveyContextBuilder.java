/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.context;

import org.cadixdev.survey.Survey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A builder for creating {@link SurveyContext}s for {@link Survey} instances.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class SurveyContextBuilder {

    private final Survey survey;
    private final String id;

    private String parent = null;
    private final List<String> blacklist = new ArrayList<>();

    public SurveyContextBuilder(final Survey survey, final String id) {
        this.survey = survey;
        this.id = id;
    }

    /**
     * Sets the parent context.
     *
     * @param parent The identifier of the parent context
     * @return {@code this}
     */
    public SurveyContextBuilder parent(final String parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Adds the given strings to the blacklist.
     *
     * @param blacklist The names to blacklist
     * @return {@code this}
     */
    public SurveyContextBuilder blacklist(final String... blacklist) {
        this.blacklist.addAll(Arrays.asList(blacklist));
        return this;
    }

    /**
     * Creates a {@link SurveyContext} with the input, and registers it.
     *
     * @return The Survey instance
     */
    public Survey build() {
        return this.survey.context(this.id, new CascadingSurveyContext(this.survey.mappings())
                .install(this.survey._getContext(this.parent))
                .install(new SimpleSurveyContext(this.survey.mappings(), this.blacklist))
        );
    }

}
