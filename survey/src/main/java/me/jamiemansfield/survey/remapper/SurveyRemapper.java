/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package me.jamiemansfield.survey.remapper;

import me.jamiemansfield.bombe.analysis.InheritanceProvider;
import me.jamiemansfield.lorenz.MappingSet;
import me.jamiemansfield.lorenz.asm.LorenzRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SignatureRemapper;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A simple implementation of {@link Remapper} to remap based
 * on a {@link MappingSet}.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class SurveyRemapper extends LorenzRemapper {

    public SurveyRemapper(final MappingSet mappings, final InheritanceProvider inheritance) {
        super(mappings, inheritance);
    }

    @Override
    protected SignatureVisitor createSignatureRemapper(final SignatureVisitor v) {
        return new ProguardSignatureFixer(v, this);
    }

    /**
     * Proguard has a problem where it will sometimes incorrectly output a method signature.
     * It will put the fully qualified obf name for the inner instead of the inner name.
     * So here we try and detect and fix that.
     * Example:
     *   Bad:  (TK;)Lzt<TK;TT;TR;>.zt$a;
     *   Good: (TK;)Lzt<TK;TT;TR;>.a;
     */
    static class ProguardSignatureFixer extends SignatureRemapper {

        private final Deque<String> classNames = new ArrayDeque<>();

        private ProguardSignatureFixer(final SignatureVisitor sv, final Remapper m) {
            super(sv, m);
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
