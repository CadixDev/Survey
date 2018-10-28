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
import org.cadixdev.lorenz.io.MappingFormat;
import org.cadixdev.lorenz.io.MappingFormats;
import org.cadixdev.survey.SurveyMapper;
import org.cadixdev.survey.cli.util.MappingFormatValueConverter;
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

    // This is replaced by blossum at build-time
    private static final String VERSION = "${VERSION}";

    public static void main(final String[] args) {
        final OptionParser parser = new OptionParser();

        // Modes
        final OptionSpec<Void> helpSpec = parser.acceptsAll(asList("?", "help"), "Show the help").forHelp();
        final OptionSpec<Void> versionSpec = parser.accepts("version", "Shows the version");
        final OptionSpec<Void> remapSpec = parser.accepts("remap", "Remap a jar");
        // TODO: mapSpec

        // Options

        // - Common
        final OptionSpec<MappingFormat> mappingFormatSpec = parser.acceptsAll(asList("mapping-format", "f"), "The mapping format")
                .withRequiredArg()
                .withValuesConvertedBy(MappingFormatValueConverter.INSTANCE)
                .defaultsTo(MappingFormats.SRG);
        final OptionSpec<Path> jarInSpec = parser.acceptsAll(asList("jar-in", "i"), "The jar to remap/map")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);
        final OptionSpec<Path> outputSpec = parser.acceptsAll(asList("output", "o"), "The output jar/mappings")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);

        // - Remap
        final OptionSpec<Path> mappingsInSpec = parser.acceptsAll(asList("mappings-in", "m"), "The mappings to remap with")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);

        // - Map
        // TODO:

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

        if (options.has(helpSpec)) {
            try {
                parser.printHelpOn(System.out);
            } catch (final IOException ex) {
                System.err.println("Failed to print help information!");
                ex.printStackTrace(System.err);
                System.exit(-1);
            }
        }
        // see https://www.gnu.org/prep/standards/standards.html#g_t_002d_002dversion
        else if (options.has(versionSpec)) {
            asList(
                    "Survey " + VERSION,
                    "Copyright (c) 2018 Jamie Mansfield <https://www.jamiemansfield.me/>",
                    // The following is adapted from a similar statement Mozilla make for Firefox
                    // See about:rights
                    "Survey is made available under the terms of the Mozilla Public License, giving",
                    "you the freedom to use, copy, and distribute Survey to others, in addition to",
                    "the right to distribute modified versions."
            ).forEach(System.out::println);
        }
        else if (options.has(remapSpec)) {
            final Path jarInPath = options.valueOf(jarInSpec);
            final MappingFormat mappingFormat = options.valueOf(mappingFormatSpec);

            if (Files.notExists(jarInPath)) {
                throw new RuntimeException("Input jar does not exist!");
            }

            final Path mappingsPath = options.valueOf(mappingsInSpec);
            final Path jarOutPath = options.valueOf(outputSpec);

            if (Files.notExists(mappingsPath)) {
                throw new RuntimeException("Input mappings does not exist!");
            }

            new SurveyMapper()
                    .loadMappings(mappingsPath, mappingFormat)
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
