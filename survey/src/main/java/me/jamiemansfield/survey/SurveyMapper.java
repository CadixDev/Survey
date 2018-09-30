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

import me.jamiemansfield.bombe.analysis.InheritanceProvider;
import me.jamiemansfield.bombe.asm.analysis.ClassProviderInheritanceProvider;
import me.jamiemansfield.bombe.asm.jar.JarEntryRemappingTransformer;
import me.jamiemansfield.bombe.asm.jar.JarFileClassProvider;
import me.jamiemansfield.bombe.jar.Jars;
import me.jamiemansfield.lorenz.MappingSet;
import me.jamiemansfield.lorenz.io.MappingFormat;
import me.jamiemansfield.survey.remapper.SurveyRemapper;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * A fluent interface for using Survey's {@link SurveyRemapper}.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class SurveyMapper {

    private final MappingSet mappings;

    public SurveyMapper(final MappingSet mappings) {
        this.mappings = mappings;
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
            format.read(this.mappings, mappingsPath);
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
        try (final JarFile jarFile = new JarFile(input.toFile());
             final JarOutputStream jos = new JarOutputStream(Files.newOutputStream(output))) {
            final InheritanceProvider inheritance = new ClassProviderInheritanceProvider(new JarFileClassProvider(jarFile));
            Jars.transform(jarFile, jos,
                    new JarEntryRemappingTransformer(
                            new SurveyRemapper(this.mappings, inheritance),
                            SurveyClassRemapper::new
                    )
            );
        }
        catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    // TODO: Use the new ASM7 stuff (that I introduced \o/)
    private static class SurveyClassRemapper extends ClassRemapper {

        public SurveyClassRemapper(final ClassVisitor classVisitor, final Remapper remapper) {
            super(classVisitor, remapper);
        }

        @Override
        public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
            // We need to change the inner name as ASM doesn't
            final String mappedName = this.remapper.map(name);
            super.visitInnerClass(
                    name,
                    outerName,
                    // This check is for anonymous classes
                    innerName == null ? null : mappedName.substring(mappedName.lastIndexOf('$') + 1),
                    access
            );
        }

    }

}
