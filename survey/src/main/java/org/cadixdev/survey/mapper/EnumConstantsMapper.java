/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper;

import static org.objectweb.asm.Opcodes.ASM6;

import org.cadixdev.bombe.type.ArrayType;
import org.cadixdev.bombe.type.ObjectType;
import org.cadixdev.bombe.type.Type;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.survey.mapper.config.EnumConstantsMapperConfig;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Objects;

/**
 * An {@link AbstractMapper} that can produce de-obfuscation mappings for enum
 * constants, based on fragments left behind for {@code "#valueOf(String)"}.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class EnumConstantsMapper extends AbstractMapper<EnumConstantsMapperConfig> {

    private static final MethodSignature STATIC_INIT = MethodSignature.of("<clinit>", "()V");

    private boolean isEnum = false;
    private ObjectType klassType = null;

    public EnumConstantsMapper(final MapperContext ctx, final EnumConstantsMapperConfig config) {
        super(ctx, config);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        this.klassType = new ObjectType(name);
        this.isEnum = Objects.equals("java/lang/Enum", superName);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        this.klassType = null;
        this.isEnum = false;

        super.visitEnd();
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String descriptor, final String signature, final Object value) {
        if (this.configuration.mapSyntheticValues) {
            final boolean isSynthetic = (access & Opcodes.ACC_SYNTHETIC) != 0;
            final Type valuesType = new ArrayType(1, this.klassType);
            if (this.isEnum && isSynthetic && valuesType.equals(Type.of(descriptor))) {
                this.ctx().mappings().getOrCreateClassMapping(this.klassType.getClassName())
                        .getOrCreateFieldMapping(name, descriptor)
                        .setDeobfuscatedName("$VALUES");
            }
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
        final MethodSignature methodSignature = MethodSignature.of(name, descriptor);

        if (this.isEnum && STATIC_INIT.equals(methodSignature)) {
            return new EnumMappingMethodVisitor(
                    super.visitMethod(access, name, descriptor, signature, exceptions),
                    this.klassType,
                    this.ctx().mappings()
            );
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    /**
     * A {@link MethodVisitor} to find de-obfuscation mappings for enums, through
     * non-obfuscated values left for {@code "#valueOf(String)"}.
     */
    public static class EnumMappingMethodVisitor extends MethodVisitor {

        private final MappingSet mappings;
        private final ObjectType klass;

        private boolean expecting = true;
        private String name = null;

        public EnumMappingMethodVisitor(final MethodVisitor mv, final ObjectType klass, final MappingSet mappings) {
            super(ASM6, mv);
            this.klass = klass;
            this.mappings = mappings;
        }

        @Override
        public void visitLdcInsn(final Object cst) {
            if (this.expecting && cst instanceof String) {
                this.expecting = false;
                this.name = (String) cst;
            }
            super.visitLdcInsn(cst);
        }

        @Override
        public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
            // We want to be certain that the field is actually an Enum type
            if (Objects.equals(this.klass.toString(), desc) &&
                    // We also want to be certain that we have a name to map too
                    this.name != null &&
                    // And the opcode is right
                    opcode == Opcodes.PUTSTATIC) {
                // Get the class, get the field, map the field
                this.mappings.getOrCreateClassMapping(owner)
                        .getOrCreateFieldMapping(name, desc)
                        .setDeobfuscatedName(this.name);

                this.name = null;
                this.expecting = true;
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }

    }

}
