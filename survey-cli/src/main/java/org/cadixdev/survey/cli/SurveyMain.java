/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.cli;

import static java.util.Arrays.asList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.cadixdev.lorenz.io.MappingFormat;
import org.cadixdev.lorenz.io.MappingFormats;
import org.cadixdev.survey.Survey;
import org.cadixdev.survey.cli.util.MappingFormatValueConverter;
import org.cadixdev.survey.cli.util.PathValueConverter;
import org.cadixdev.survey.config.SurveyDeserialiser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

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
        final OptionSpec<Void> noMapSpec = parser.accepts("no-map", "Do not map the jar");

        // Options
        final OptionSpec<MappingFormat> mappingFormatSpec = parser.acceptsAll(asList("mapping-format", "f"), "The mapping format")
                .withRequiredArg()
                .withValuesConvertedBy(MappingFormatValueConverter.INSTANCE)
                .defaultsTo(MappingFormats.SRG);
        final OptionSpec<Path> jarInSpec = parser.acceptsAll(asList("jar-in", "j"), "The jar to remap/map")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);
        final OptionSpec<Path> jarOutSpec = parser.acceptsAll(asList("jar-out", "r"), "The output jar")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);
        final OptionSpec<Path> mappingsInSpec = parser.acceptsAll(asList("mappings-in", "m"), "The mappings to remap with")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);
        final OptionSpec<Path> mappingsOutSpec = parser.acceptsAll(asList("mappings-out", "s"), "The output mappings")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);
        final OptionSpec<Path> configSpec = parser.acceptsAll(asList("config", "c"), "The Survey configuration")
                .withRequiredArg()
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
        else if (options.has(jarInSpec)) {
            final Path jarInPath = options.valueOf(jarInSpec);
            final Path jarOutPath = options.valueOf(jarOutSpec);
            if (Files.notExists(jarInPath)) {
                throw new RuntimeException("Input jar does not exist!");
            }

            final MappingFormat mappingFormat = options.valueOf(mappingFormatSpec);
            final Path mappingsInPath = options.valueOf(mappingsInSpec);
            final Path mappingsOutPath = options.valueOf(mappingsOutSpec);

            final Path configPath = options.valueOf(configSpec);

            final Survey survey = new Survey();
            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Survey.class, new SurveyDeserialiser(survey))
                    .create();

            if (mappingsInPath != null) {
                if (Files.notExists(mappingsInPath)) {
                    throw new RuntimeException("Input mappings do not exist!");
                }

                try {
                    mappingFormat.read(survey.mappings(), mappingsInPath);
                }
                catch (final IOException ex) {
                    System.err.println("Failed to read input mappings!");
                    ex.printStackTrace(System.err);
                    System.exit(-1);
                }
            }

            if (configPath != null) {
                if (Files.notExists(configPath)) {
                    throw new RuntimeException("Configuration does not exist!");
                }

                try (final BufferedReader reader = Files.newBufferedReader(configPath)) {
                    gson.fromJson(reader, Survey.class);
                }
                catch (final IOException ex) {
                    System.err.println("Failed to read configuration!");
                    ex.printStackTrace(System.err);
                    System.exit(-1);
                }
            }

            try (final JarFile jar = new JarFile(jarInPath.toFile())) {
                // Map the jar, if required
                if (!options.has(noMapSpec)) survey.map(jar);

                // Remap and patch, if required
                if (jarOutPath != null) {
                    try (final JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarOutPath))) {
                        survey.run(jar, jos, false);
                    }
                    catch (final IOException ex) {
                        System.err.println("Failed to write output jar!");
                        ex.printStackTrace(System.err);
                        System.exit(-1);
                    }
                }
            }
            catch (final IOException ex) {
                System.err.println("Failed to read input jar!");
                ex.printStackTrace(System.err);
                System.exit(-1);
            }

            if (mappingsOutPath != null) {
                try {
                    mappingFormat.write(survey.mappings(), mappingsOutPath);
                }
                catch (final IOException ex) {
                    System.err.println("Failed to write output mappings!");
                    ex.printStackTrace(System.err);
                    System.exit(-1);
                }
            }
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
