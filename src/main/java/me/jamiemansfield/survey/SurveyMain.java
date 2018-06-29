/*
 * Copyright (c) 2018, Jamie Mansfield <https://jamiemansfield.me/>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.jamiemansfield.survey;

import static java.util.Arrays.asList;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import me.jamiemansfield.lorenz.MappingSet;
import me.jamiemansfield.lorenz.io.reader.MappingsReader;
import me.jamiemansfield.lorenz.model.Mapping;
import me.jamiemansfield.survey.analysis.InheritanceMap;
import me.jamiemansfield.survey.remapper.LorenzRemapper;
import me.jamiemansfield.survey.util.PathValueConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The Main-Class behind Survey, a simple remapping tool.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public final class SurveyMain {

    public static void main(final String[] args) {
        final OptionParser parser = new OptionParser();

        final OptionSpec<Void> helpSpec = parser.acceptsAll(asList("?", "help"), "Show the help")
                .forHelp();

        final OptionSpec<Path> jarInPathSpec = parser.accepts("jarIn", "The location of the jar to map")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);
        final OptionSpec<Path> mappingsPathSpec = parser.accepts("mappings", "The location of the mappings")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);
        final OptionSpec<MappingFormat> mappingsFormatSpec = parser.accepts("mappingsFormat", "The format of the mappings")
                .withRequiredArg()
                .ofType(MappingFormat.class)
                .defaultsTo(MappingFormat.SRG);
        final OptionSpec<Path> jarOutPathSpec = parser.accepts("jarOut", "Where to save the mapped jar")
                .withRequiredArg()
                .withValuesConvertedBy(PathValueConverter.INSTANCE);

        final OptionSet options;
        try {
            options = parser.parse(args);
        } catch (final OptionException ex) {
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

        final Path jarInPath = options.valueOf(jarInPathSpec);
        final Path mappingsPath = options.valueOf(mappingsPathSpec);
        final MappingFormat mappingFormat = options.valueOf(mappingsFormatSpec);
        final Path jarOutPath = options.valueOf(jarOutPathSpec);

        if (!(Files.exists(jarInPath) && Files.exists(mappingsPath))) {
            throw new RuntimeException("Jar in, mappings, or both do not exist!");
        }

        final MappingSet mappings = MappingSet.create();

        try (final MappingsReader reader = mappingFormat.create(new BufferedReader(new InputStreamReader(Files.newInputStream(mappingsPath))))) {
            reader.parse(mappings);
        }
        catch (final IOException ex) {
            ex.printStackTrace();
        }

        SurveyTool.remapJar(
                jarInPath,
                sources -> {
                    final InheritanceMap inheritanceMap = new InheritanceMap(sources);
                    return new LorenzRemapper(mappings, inheritanceMap);
                },
                klassName -> mappings.getClassMapping(klassName)
                        .map(Mapping::getFullDeobfuscatedName)
                        .orElse(klassName),
                jarOutPath
        );
    }

    private SurveyMain() {
    }

}
