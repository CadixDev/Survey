/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.context;

import org.cadixdev.lorenz.MappingSet;

import java.util.List;

/**
 * A simple implementation of {@link SurveyContext}.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class SimpleSurveyContext implements SurveyContext {

    private final MappingSet mappings;
    private final List<String> blacklist;

    public SimpleSurveyContext(final MappingSet mappings, final List<String> blacklist) {
        this.mappings = mappings;
        this.blacklist = blacklist;
    }

    @Override
    public MappingSet mappings() {
        return this.mappings;
    }

    @Override
    public boolean blacklisted(final String klass) {
        for (final String blacklisted : this.blacklist) {
            if (klass.startsWith(blacklisted)) return true;
        }
        return false;
    }

}
