/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.intermediary;

import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.survey.context.SurveyContext;
import org.objectweb.asm.FieldVisitor;

/**
 * The field intermediary mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class FieldIntemediaryMapper extends AbstractIntermediaryMapper<FieldIntemediaryMapper.Config> {

    private int count = 0;
    private ClassMapping<?, ?> klass;

    public FieldIntemediaryMapper(final SurveyContext ctx, final Config configuration) {
        super(ctx, configuration);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        this.klass = this.ctx().mappings().getOrCreateClassMapping(name);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String descriptor, final String signature, final Object value) {
        final FieldMapping fieldMapping = this.klass.getOrCreateFieldMapping(name, descriptor);
        if (!fieldMapping.hasDeobfuscatedName()) {
            fieldMapping.setDeobfuscatedName(this.getConfiguration().getMemberName(++this.count, name));
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visitEnd() {
        this.klass = null;

        super.visitEnd();
    }

    /**
     * The field intermediary mapper configuration.
     */
    public static class Config extends AbstractIntermediaryMapper.Config {

        private final String format;

        public Config(final String format) {
            this.format = format;
        }

        @Override
        public String getFormat() {
            return this.format;
        }

    }

}
