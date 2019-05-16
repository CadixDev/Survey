/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.patcher;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

import org.cadixdev.survey.context.SurveyContext;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InnerClassInitPatcher extends AbstractPatcher<Void> {

    public InnerClassInitPatcher(final SurveyContext ctx) {
        super(ctx, null);
    }

    @Override
    public ClassVisitor createVisitor(final ClassVisitor parent) {
        return new Visitor(parent);
    }

    /**
     * @author LexManos
     */
    private final class Visitor extends ClassVisitor {

        private static final int FIELD_ACCESS = ACC_FINAL | ACC_SYNTHETIC;

        private String className;
        private String parentName;
        private String parentField;
        private boolean hasInit = false;
        private boolean isStatic = false;

        Visitor(final ClassVisitor cv) {
            super(Opcodes.ASM6, cv);
        }

        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            this.isStatic = (access & ACC_STATIC) != 0;
            this.className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        // The reader *should* read this before any fields/methods, so we can set the parent name to find the field
        public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
            if (this.className.equals(name)) {
                this.parentName = "L" + outerName + ";";
            }
            super.visitInnerClass(name, outerName, innerName, access);
        }

        @Override
        public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
            if ((access & FIELD_ACCESS) == FIELD_ACCESS && desc.equals(this.parentName)) {
                this.parentField = name;
            }
            return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            if ("<init>".equals(name)) {
                this.hasInit = true;
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            if (!this.hasInit && !this.isStatic && this.parentName != null && this.parentField != null) {
                System.out.println(" - Adding synthetic <init> " + this.parentName + " " + this.parentField);
                MethodVisitor mv = this.visitMethod(FIELD_ACCESS, "<init>", "(" + this.parentName + ")V", null, null);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, this.className, this.parentField, this.parentName);
                mv.visitInsn(RETURN);
            }
            super.visitEnd();
        }

    }

}
