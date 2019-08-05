/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.MappingFormat;
import org.cadixdev.survey.patcher.proguard.ProguardSignaturePatcher;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A fluent interface for mapping and re-mapping with Survey.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class SurveyMapper {

    private final Survey survey;

    public SurveyMapper(final MappingSet mappings) {
        this.survey = new Survey(mappings)
                // Setup Survey with the legacy defaults.
                .patcher("Proguard Signature", ProguardSignaturePatcher::create, null);
    }

    public SurveyMapper() {
        this(MappingSet.create());
    }

    /**
     * Loads mappings from the given path, using the given reader.
     *
     * @param mappingsPath The path to the mappings file
     * @param format The mapping format to use for reading the mappings file
     * @return {@code this}, for chaining
     */
    public SurveyMapper loadMappings(final Path mappingsPath, final MappingFormat format) {
        try {
            format.read(this.survey.mappings(), mappingsPath);
        }
        catch (final IOException ignored) {
        }
        return this;
    }

    public SurveyMapper saveMappings(final Path mappingsPath, final MappingFormat format) {
        try {
            format.write(this.survey.mappings(), mappingsPath);
        }
        catch (final IOException ignored) {
        }
        return this;
    }

    /**
     * Remaps the given input jar, with the loaded mappings, and saves it to
     * the given output path.
     *
     * @param input The input jar
     * @param output The output jar
     */
    public void remap(final Path input, final Path output) {
        this.survey.run(input, output, false);
    }

}