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

package me.jamiemansfield.survey.remapper;

import me.jamiemansfield.lorenz.MappingSet;
import me.jamiemansfield.lorenz.model.Mapping;
import me.jamiemansfield.lorenz.model.jar.signature.MethodSignature;
import me.jamiemansfield.survey.analysis.InheritanceProvider;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SignatureRemapper;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

/**
 * A simple implementation of {@link Remapper} to remap based
 * on a {@link MappingSet}.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class LorenzRemapper extends Remapper {

    private final MappingSet mappings;
    private final InheritanceProvider inheritance;

    public LorenzRemapper(final MappingSet mappings, final InheritanceProvider inheritance) {
        this.mappings = mappings;
        this.inheritance = inheritance;
    }

    @Override
    public String map(final String typeName) {
        return this.mappings.getClassMapping(typeName)
                .map(Mapping::getFullDeobfuscatedName)
                .orElse(typeName);
    }

    /**
     * Gets a de-obfuscated field name, wrapped in an {@link Optional}, with respect to inheritance.
     *
     * @param owner The owner of the field
     * @param name The name of the field
     * @return The de-obfuscated field name, wrapped in an {@link Optional}
     */
    private Optional<String> getFieldMapping(final String owner, final String name) {
        // First, check the current class
        final Optional<String> fieldName = this.mappings.getClassMapping(owner)
                .flatMap(mapping -> mapping.getFieldMapping(name)
                        .map(Mapping::getDeobfuscatedName));
        if (fieldName.isPresent()) return fieldName;

        // Now, check parent classes and interfaces
        final Optional<InheritanceProvider.ClassInfo> info = this.inheritance.provide(owner);
        if (info.isPresent()) {
            final List<String> parents = new ArrayList<String>() {
                {
                    if (info.get().getSuperName() != null) this.add(info.get().getSuperName());
                    this.addAll(info.get().getInterfaces());
                }
            };

            for (final String parent : parents) {
                final Optional<String> mapping = this.getFieldMapping(parent, name);
                if (mapping.isPresent()) return mapping;
            }
        }

        // The field seemingly has no mapping
        return Optional.empty();
    }

    @Override
    public String mapFieldName(final String owner, final String name, final String desc) {
        return this.getFieldMapping(owner, name).orElse(name);
    }

    /**
     * Gets a de-obfuscated method name, wrapped in an {@link Optional}, with respect to inheritance.
     *
     * @param owner The owner of the method
     * @param signature The signature of the method
     * @return The de-obfuscated method name, wrapped in an {@link Optional}
     */
    private Optional<String> getMethodMapping(final String owner, final MethodSignature signature) {
        // First, check the current class
        final Optional<String> methodName = this.mappings.getClassMapping(owner)
                .flatMap(mapping -> mapping.getMethodMapping(signature)
                        .map(Mapping::getDeobfuscatedName));
        if (methodName.isPresent()) return methodName;

        // Now, check the parent classes
        final Optional<InheritanceProvider.ClassInfo> info = this.inheritance.provide(owner);
        if (info.isPresent()) {
            final List<String> parents = new ArrayList<String>() {
                {
                    if (info.get().getSuperName() != null) this.add(info.get().getSuperName());
                    this.addAll(info.get().getInterfaces());
                }
            };

            for (final String parent : parents) {
                final Optional<String> name = this.getMethodMapping(parent, signature);
                if (name.isPresent()) return name;
            }
        }

        // The method seemingly has no mapping
        return Optional.empty();
    }

    @Override
    public String mapMethodName(final String owner, final String name, final String desc) {
        return this.getMethodMapping(owner, new MethodSignature(name, desc)).orElse(name);
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

        private Stack<String> classNames = new Stack<>();

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
