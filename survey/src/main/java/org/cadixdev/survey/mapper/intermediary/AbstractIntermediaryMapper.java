/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.intermediary;

import org.cadixdev.survey.mapper.AbstractMapper;
import org.cadixdev.survey.context.SurveyContext;

/**
 * An intermediary mapper, that produces version-agnostic mappings.
 *
 * @param <C> The type of the configuration
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public abstract class AbstractIntermediaryMapper<C extends AbstractIntermediaryMapper.Config>
        extends AbstractMapper<C> {

    public AbstractIntermediaryMapper(final SurveyContext ctx, final C configuration) {
        super(ctx, configuration);
    }

    /**
     * The intermediary mapper configuration.
     */
    public static abstract class Config {

        /**
         * Gets the format used for the member names.
         *
         * @return The format
         */
        public abstract String getFormat();

        /**
         * Creates the member name, from the configured format, with the id
         * and obfuscated name.
         *
         * @param id The id
         * @param obf The obfuscated name
         * @return The member name
         */
        public String getMemberName(final int id, final String obf) {
            return this.getFormat()
                    .replace("{id}", Integer.toString(id))
                    .replace("{obf}", obf);
        }

    }

}
