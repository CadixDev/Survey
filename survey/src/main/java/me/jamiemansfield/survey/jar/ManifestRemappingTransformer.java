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

package me.jamiemansfield.survey.jar;

import me.jamiemansfield.bombe.jar.JarEntryTransformer;
import me.jamiemansfield.bombe.jar.JarResourceEntry;
import me.jamiemansfield.lorenz.MappingSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.jar.Manifest;

/**
 * A jar entry transformer, for remapping manifest entries.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class ManifestRemappingTransformer implements JarEntryTransformer {

    private final MappingSet mappings;

    public ManifestRemappingTransformer(final MappingSet mappings) {
        this.mappings = mappings;
    }

    // TODO: Introduce a specific JarManifestEntry class?
    @Override
    public JarResourceEntry transform(final JarResourceEntry entry) {
        if (Objects.equals("META-INF/MANIFEST.MF", entry.getName())) {
            final Manifest manifest = new Manifest();

            // Read original manifest
            try (final InputStream is = new ByteArrayInputStream(entry.getContents())) {
                manifest.read(is);
            }
            catch (final IOException ignored) {
            }

            // Remap the Main-Class attribute
            final String mainClassObf = manifest.getMainAttributes().getValue("Main-Class");
            this.mappings.getClassMapping(mainClassObf).ifPresent(mapping -> {
                final String mainClassDeobf = mapping.getFullDeobfuscatedName();
                manifest.getMainAttributes().putValue("Main-Class", mainClassDeobf);
            });

            // Return the new jar entry
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                manifest.write(baos);
                return new JarResourceEntry(entry.getName(), baos.toByteArray());
            }
            catch (final IOException ignored) {
            }
        }
        return entry;
    }

}
