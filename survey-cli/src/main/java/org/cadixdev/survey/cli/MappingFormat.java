/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.cli;

import org.cadixdev.lorenz.io.MappingFormats;

/**
 * The many mapping formats that are supported by Survey's CLI, all provided
 * by Lorenz.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public enum MappingFormat {

    /**
     * @see MappingFormats#SRG
     */
    SRG(MappingFormats.SRG),

    /**
     * @see MappingFormats#CSRG
     */
    CSRG(MappingFormats.CSRG),

    /**
     * @see MappingFormats#TSRG
     */
    TSRG(MappingFormats.TSRG),

    /**
     * @see MappingFormats#KIN
     */
    KIN(MappingFormats.KIN),

    /**
     * @see MappingFormats#JAM
     */
    JAM(MappingFormats.JAM),

    /**
     * @see MappingFormats#ENIGMA
     */
    ENIGMA(MappingFormats.ENIGMA),

    ;

    private final org.cadixdev.lorenz.io.MappingFormat format;

    MappingFormat(final org.cadixdev.lorenz.io.MappingFormat format) {
        this.format = format;
    }

    /**
     * Gets the wrapped mapping format.
     *
     * @return The mapping format
     */
    public org.cadixdev.lorenz.io.MappingFormat get() {
        return this.format;
    }

}
