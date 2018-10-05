/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.cli;

import static java.util.Arrays.asList;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.cadixdev.survey.SurveyMapper;
import org.cadixdev.survey.cli.util.PathValueConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The Main-Class behind Survey, a simple remapping tool.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public final class SurveyMain {

    public static void main(final String[] args) {
        final OptionParser parser = new OptionParser();

        final OptionSpec<Void> helpSpec = parser.acceptsAll(asList("?", "help"), "Show the help")
                .forHelp();

        // Modes
        final OptionSpec<Void> remapSpec = parser.accepts("remap", "Remap a jar");

        // Options
        final OptionSpec<Path> jarInPathSpec = parser.accepts("jar-in", "The location of the jar to map")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);
        final OptionSpec<Path> mappingsPathSpec = parser.accepts("mappings", "The location of the mappings")
                .requiredIf(remapSpec)
                .withOptionalArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);
        final OptionSpec<MappingFormat> mappingsFormatSpec = parser.accepts("mappings-format", "The format of the mappings")
                .withRequiredArg()
                .ofType(MappingFormat.class)
                .defaultsTo(MappingFormat.SRG);
        final OptionSpec<Path> jarOutPathSpec = parser.accepts("jar-out", "Where to save the mapped jar")
                .requiredIf(remapSpec)
                .withOptionalArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);

        final OptionSet options;
        try {
            options = parser.parse(args);
        }
        catch (final OptionException ex) {
            System.err.println("Failed to parse OptionSet! Exiting...");
            ex.printStackTrace(System.err);
            System.exit(-1);
            return;
        }

        if (options == null || options.has(helpSpec)) {
            try {
                parser.printHelpOn(System.err);
            } catch (final IOException ex) {
                System.err.println("Failed to print help information!");
                ex.printStackTrace(System.err);
            }
            System.exit(-1);
            return;
        }

        if (options.has(remapSpec)) {
            final Path jarInPath = options.valueOf(jarInPathSpec);
            final MappingFormat mappingFormat = options.valueOf(mappingsFormatSpec);

            if (Files.notExists(jarInPath)) {
                throw new RuntimeException("Input jar does not exist!");
            }

            final Path mappingsPath = options.valueOf(mappingsPathSpec);
            final Path jarOutPath = options.valueOf(jarOutPathSpec);

            if (Files.notExists(mappingsPath)) {
                throw new RuntimeException("Input mappings does not exist!");
            }

            new SurveyMapper()
                    .loadMappings(mappingsPath, mappingFormat.get())
                    .remap(jarInPath, jarOutPath);
        }
        else {
            try {
                parser.printHelpOn(System.err);
            }
            catch (final IOException ex) {
                System.err.println("Failed to print help information!");
                ex.printStackTrace(System.err);
            }
            System.exit(-1);
        }
    }

    private SurveyMain() {
    }

}
