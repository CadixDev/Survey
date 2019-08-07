/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.patcher;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.InnerClassMapping;
import org.cadixdev.survey.context.SurveyContext;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class InnerClassPatcher extends AbstractPatcher<Void> {

    public static InnerClassPatcher create(final SurveyContext ctx, final Void config) {
        return new InnerClassPatcher(ctx);
    }

    public InnerClassPatcher(final SurveyContext ctx) {
        super(ctx, null);
    }

    @Override
    public ClassVisitor createVisitor(final ClassVisitor parent) {
        return new Visitor(parent, this.ctx().mappings());
    }

    private final class Visitor extends ClassVisitor {

        private final MappingSet mappings;

        private ClassMapping<?, ?> klass;
        private List<String> inners;
        private boolean outer = false;

        Visitor(final ClassVisitor cv, final MappingSet mappings) {
            super(Opcodes.ASM6, cv);
            this.mappings = mappings;
        }

        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            this.klass = this.mappings.getOrCreateClassMapping(name);
            this.inners = new ArrayList<>();

            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
            this.inners.add(innerName);
            super.visitInnerClass(name, outerName, innerName, access);
        }

        @Override
        public void visitOuterClass(final String owner, final String name, final String descriptor) {
            this.outer = true;
            super.visitOuterClass(owner, name, descriptor);
        }

        @Override
        public void visitEnd() {
            // Add entries, if not already present
            this.klass.getInnerClassMappings().stream()
                    .filter(inner -> !this.inners.contains(inner.getObfuscatedName()))
                    .forEach(inner -> {
                        final int access = inner.get(MappingsPopulator.ACCESS).orElse(0);

                        this.visitInnerClass(
                                inner.getFullObfuscatedName(),
                                inner.getParent().getFullObfuscatedName(),
                                inner.getObfuscatedName(),
                                access
                        );
                    });
            if (!this.outer && this.klass instanceof InnerClassMapping) {
                final InnerClassMapping inner = (InnerClassMapping) this.klass;
                // TODO: implement
            }

            // Reset state
            this.klass = null;
            this.inners = null;
            this.outer = false;

            super.visitEnd();
        }

    }

}
