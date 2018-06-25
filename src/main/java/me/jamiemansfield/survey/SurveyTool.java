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

import com.google.common.io.ByteStreams;
import me.jamiemansfield.survey.jar.JarWalker;
import me.jamiemansfield.survey.jar.SourceSet;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

/**
 * A utility class for remapping jar files.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public final class SurveyTool {

    /**
     * Remaps the given jar file, with the provided remapper, to the given
     * location.
     *
     * @param jarInPath The input jar file
     * @param remapperConstructor The remapper to use
     * @param classNameResolver The resolver to use to get class names
     * @param jarOutPath The output jar file
     */
    public static void remapJar(
            final Path jarInPath,
            final Function<SourceSet, Remapper> remapperConstructor,
            final Function<String, String> classNameResolver,
            final Path jarOutPath) {
        try (final JarFile jarFile = new JarFile(jarInPath.toFile())) {
            final SourceSet sources = new SourceSet();
            JarWalker.walk(jarFile, sources);
            final Remapper remapper = remapperConstructor.apply(sources);

            try (final JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarOutPath))) {
                for (final JarEntry entry : jarFile.stream().collect(Collectors.toSet())) {
                    try (final InputStream is = jarFile.getInputStream(entry)) {
                        if (!entry.getName().endsWith(".class") && !entry.isDirectory()) {
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ByteStreams.copy(is, baos);

                            jos.putNextEntry(new JarEntry(entry.getName()));
                            jos.write(baos.toByteArray());
                        }
                    }
                }

                for (final ClassNode node : sources.getClasses()) {
                    final ClassNode newNode = new ClassNode();
                    node.accept(new ClassRemapper(newNode, remapper));

                    final ClassWriter writer = new ClassWriter(0);
                    newNode.accept(writer);

                    final String name = classNameResolver.apply(newNode.name);

                    jos.putNextEntry(new JarEntry(name + ".class"));
                    jos.write(writer.toByteArray());
                }
            }
            catch (final IOException ex) {
                ex.printStackTrace();
            }
        } catch (final IOException ex) {
            System.err.println("Failed to read the jar file!");
            ex.printStackTrace(System.err);
        }
    }

    private SurveyTool() {
    }

}
