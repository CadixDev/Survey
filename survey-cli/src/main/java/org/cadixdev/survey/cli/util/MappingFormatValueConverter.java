/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.cli.util;

import joptsimple.ValueConverter;
import org.cadixdev.lorenz.io.MappingFormat;
import org.cadixdev.lorenz.io.MappingFormats;

/**
 * An implementation of {@link ValueConverter} for handling {@link MappingFormat}s.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public final class MappingFormatValueConverter implements ValueConverter<MappingFormat> {

    public static final MappingFormatValueConverter INSTANCE = new MappingFormatValueConverter();

    @Override
    public MappingFormat convert(final String value) {
        switch (value.toLowerCase()) {
            case "srg": return MappingFormats.SRG;
            case "csrg": return MappingFormats.CSRG;
            case "tsrg": return MappingFormats.TSRG;
            case "kin": return MappingFormats.KIN;
            case "jam": return MappingFormats.JAM;
            case "enigma": return MappingFormats.ENIGMA;
            default: return null;
        }
    }

    @Override
    public Class<MappingFormat> valueType() {
        return MappingFormat.class;
    }

    @Override
    public String valuePattern() {
        return null;
    }

}
