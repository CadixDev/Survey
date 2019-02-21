/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.patcher.proguard;

import static org.objectweb.asm.Opcodes.ASM6;

import org.cadixdev.survey.SurveyContext;
import org.cadixdev.survey.patcher.AbstractPatcher;
import org.cadixdev.survey.util.SimpleSignatureVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A patcher to correct broken signature produced by Proguard.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class ProguardSignaturePatcher extends AbstractPatcher<Void> {

    public ProguardSignaturePatcher(final SurveyContext ctx) {
        super(ctx, null);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
        return super.visitMethod(access, name, descriptor, Patcher.patch(signature), exceptions);
    }

    /**
     * Proguard has a problem where it will sometimes incorrectly output a method signature.
     * It will put the fully qualified obf name for the inner instead of the inner name.
     * So here we try and detect and fix that.
     * Example:
     *   Bad:  (TK;)Lzt<TK;TT;TR;>.zt$a;
     *   Good: (TK;)Lzt<TK;TT;TR;>.a;
     */
    static class Patcher extends SimpleSignatureVisitor {

        /**
         * Patches the signature.
         *
         * @param signature The signature
         * @return The corrected signature
         */
        static String patch(final String signature) {
            final SignatureWriter writer = new SignatureWriter();
            new SignatureReader(signature).accept(new Patcher(writer));
            return writer.toString();
        }

        private final Deque<String> classNames = new ArrayDeque<>();

        private Patcher(final SignatureVisitor sv) {
            super(ASM6, sv);
        }

        @Override
        public void visitClassType(final String name) {
            this.classNames.push(name);
            super.visitClassType(name);
        }

        @Override
        public void visitInnerClassType(String name) {
            final String outerClassName = this.classNames.pop();

            if (name.startsWith(outerClassName + '$')) {
                name = name.substring(outerClassName.length() + 1);
            }

            final String className = outerClassName + '$' + name;
            this.classNames.push(className);
            super.visitInnerClassType(name);
        }

        @Override
        public void visitEnd() {
            this.classNames.pop();
            super.visitEnd();
        }

    }

}
