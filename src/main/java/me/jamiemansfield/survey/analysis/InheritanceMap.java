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

package me.jamiemansfield.survey.analysis;

import me.jamiemansfield.lorenz.model.jar.MethodDescriptor;
import me.jamiemansfield.survey.jar.SourceSet;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An inheritance map stores inheritance information on classes, which
 * will be obtained upon request (if not present in the cache) as opposed
 * to all in one bulk operation.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public class InheritanceMap {

    private final Map<String, ClassInfo> classes = new HashMap<>();
    private final SourceSet sources;

    public InheritanceMap(final SourceSet sources) {
        this.sources = sources;
    }

    /**
     * Gets the class information for the given class name.
     *
     * @param className The class name
     * @return The class information wrapped in an {@link Optional}
     */
    public Optional<ClassInfo> classInfo(final String className) {
        if (this.classes.containsKey(className)) {
            return Optional.of(this.classes.get(className));
        }
        else {
            return Optional.ofNullable(this.sources.get(className))
                    .flatMap(node -> {
                        final ClassInfo info = new ClassInfo(node);
                        this.classes.put(info.name, info);
                        return Optional.of(info);
                    });
        }
    }

    /**
     * A wrapper used to store inheritance information about classes.
     */
    public class ClassInfo {

        private final String name;
        private final String superName;
        private final List<String> interfaces = new ArrayList<>();
        private final List<String> fields = new ArrayList<>();
        private final List<MethodDescriptor> methods = new ArrayList<>();

        public ClassInfo(final ClassNode node) {
            this.name = node.name;
            this.superName = node.superName;
            this.interfaces.addAll(node.interfaces);
            node.fields.stream()
                    .map(fieldNode -> fieldNode.name)
                    .forEach(this.fields::add);
            node.methods.stream()
                    .map(methodNode -> new MethodDescriptor(methodNode.name, methodNode.desc))
                    .forEach(this.methods::add);
        }

        /**
         * Gets the name of the class.
         *
         * @return The class' name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Gets the name of this class' super class.
         *
         * @return The super name
         */
        public String getSuperName() {
            return this.superName;
        }

        /**
         * Gets an immutable-view of all the interfaces of the class.
         *
         * @return The class' interfaces
         */
        public List<String> getInterfaces() {
            return Collections.unmodifiableList(this.interfaces);
        }

        /**
         * Gets an immutable-view of all the fields of the class.
         *
         * @return The class' fields
         */
        public List<String> getFields() {
            return Collections.unmodifiableList(this.fields);
        }

        /**
         * Gets an immutable-view of all the methods.
         *
         * @return The methods
         */
        public List<MethodDescriptor> getMethods() {
            return Collections.unmodifiableList(this.methods);
        }

    }

}
