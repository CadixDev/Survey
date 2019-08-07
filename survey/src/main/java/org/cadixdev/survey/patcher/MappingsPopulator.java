/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.patcher;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.ExtensionKey;
import org.cadixdev.survey.context.SurveyContext;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class MappingsPopulator extends AbstractPatcher<Void> {

    public static final ExtensionKey<Integer> ACCESS = new ExtensionKey<>(Integer.class, "access");

    public static MappingsPopulator create(final SurveyContext ctx, final Void config) {
        return new MappingsPopulator(ctx);
    }

    public MappingsPopulator(final SurveyContext ctx) {
        super(ctx, null);
    }

    @Override
    public ClassVisitor createVisitor(final ClassVisitor parent) {
        return new Visitor(parent, this.ctx().mappings());
    }

    private final class Visitor extends ClassVisitor {

        private final MappingSet mappings;

        Visitor(final ClassVisitor cv, final MappingSet mappings) {
            super(Opcodes.ASM6, cv);
            this.mappings = mappings;
        }

        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            final ClassMapping<?, ?> klass = this.mappings.getOrCreateClassMapping(name);
            klass.set(ACCESS, access);

            super.visit(version, access, name, signature, superName, interfaces);
        }

    }

}
