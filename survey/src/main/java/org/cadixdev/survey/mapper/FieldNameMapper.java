/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper;

import org.cadixdev.bombe.type.ObjectType;
import org.cadixdev.survey.context.SurveyContext;
import org.cadixdev.survey.mapper.config.FieldNameMapperConfig;
import org.cadixdev.survey.mapper.config.ModifierRequirement;
import org.objectweb.asm.FieldVisitor;

import java.util.Map;
import java.util.Objects;

/**
 * An {@link AbstractMapper} that maps fields based on basic configurations.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class FieldNameMapper extends AbstractMapper<FieldNameMapperConfig> {

    private ObjectType klassType;

    public FieldNameMapper(final SurveyContext ctx, final FieldNameMapperConfig config) {
        super(ctx, config);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        this.klassType = new ObjectType(name);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String descriptor, final String signature, final Object value) {
        if (Objects.equals(descriptor, this.getConfiguration().desc)) {
            boolean map = true;
            for (final Map.Entry<ModifierRequirement, Boolean> entry : this.getConfiguration().requirements.entrySet()) {
                if (entry.getKey().test(access) != entry.getValue()) {
                    map = false;
                    break;
                }
            }
            if (map) {
                this.ctx().mappings().getOrCreateClassMapping(this.klassType.getClassName())
                        .getOrCreateFieldMapping(name, descriptor)
                        .setDeobfuscatedName(this.getConfiguration().name);
            }
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visitEnd() {
        this.klassType = null;

        super.visitEnd();
    }

}
