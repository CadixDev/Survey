/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.patcher;

import org.cadixdev.bombe.jar.JarClassEntry;
import org.cadixdev.bombe.jar.JarEntryTransformer;
import org.cadixdev.survey.Survey;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.Arrays;
import java.util.Collection;

/**
 * An implementation of {@link JarEntryTransformer} for patching classes,
 * using the patchers from a {@link Survey} instance.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class JarEntryPatcherTransformer implements JarEntryTransformer {

    public static JarEntryPatcherTransformer from(final AbstractPatcher<?>... patchers) {
        return new JarEntryPatcherTransformer(Arrays.asList(patchers));
    }

    private final Collection<AbstractPatcher<?>> patchers;

    public JarEntryPatcherTransformer(final Collection<AbstractPatcher<?>> patchers) {
        this.patchers = patchers;
    }

    @Override
    public JarClassEntry transform(final JarClassEntry entry) {
        final ClassReader reader = new ClassReader(entry.getContents());
        final ClassWriter writer = new ClassWriter(reader, 0);

        ClassVisitor lastVisitor = writer;
        for (final AbstractPatcher<?> patcher : this.patchers) {
            lastVisitor = patcher.createVisitor(lastVisitor);
        }
        reader.accept(lastVisitor, 0);

        return new JarClassEntry(entry.getName(), entry.getTime(), writer.toByteArray());
    }

}
