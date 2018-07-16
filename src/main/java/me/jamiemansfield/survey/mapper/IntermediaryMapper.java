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

package me.jamiemansfield.survey.mapper;

import me.jamiemansfield.lorenz.MappingSet;
import me.jamiemansfield.lorenz.model.ClassMapping;
import me.jamiemansfield.lorenz.model.FieldMapping;
import me.jamiemansfield.lorenz.model.MethodMapping;
import me.jamiemansfield.lorenz.model.jar.MethodDescriptor;
import me.jamiemansfield.lorenz.model.jar.signature.MethodSignature;
import me.jamiemansfield.survey.analysis.CascadingInheritanceProvider;
import me.jamiemansfield.survey.analysis.ClassLoaderInheritanceProvider;
import me.jamiemansfield.survey.analysis.InheritanceProvider;
import me.jamiemansfield.survey.analysis.SourceSetInheritanceProvider;
import me.jamiemansfield.survey.jar.SourceSet;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class IntermediaryMapper {

    /**
     * A {@link Comparator} used to alphabetise a collection of {@link ClassNode}s.
     */
    private static final Comparator<ClassNode> ALPHABETISE_CLASSES = (o1, o2) -> o1.name.compareToIgnoreCase(o2.name);

    /**
     * A {@link Comparator} used to alphabetise a collection of {@link MethodSignature}s.
     */
    private static final Comparator<MethodSignature> ALPHABETISE_METHODS = (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName());

    private static final MethodSignature MAIN_METHOD = new MethodSignature("main", MethodDescriptor.compile("([Ljava/lang/String;)V"));

    private static final String FIELD_PREFIX = "field_";
    private static final String METHOD_PREFIX = "func_";

    private final IntermediaryMapperConfig config;
    private final MappingSet mappings;
    private final SourceSet sources;
    private final InheritanceProvider inheritance;

    private final AtomicInteger count;

    public IntermediaryMapper(final IntermediaryMapperConfig config, final MappingSet mappings, final SourceSet sources) {
        this.config = config;
        this.mappings = mappings;
        this.sources = sources;
        this.inheritance = new CascadingInheritanceProvider()
                .install(new SourceSetInheritanceProvider(sources))
                .install(new ClassLoaderInheritanceProvider(IntermediaryMapper.class.getClassLoader()));

        this.count = new AtomicInteger(config.getNextMember());
    }

    public void map() {
        // First pass
        this.sources.getClasses().stream()
                .sorted(ALPHABETISE_CLASSES)
                .forEach(node -> this.map(node, true));
        // Second pass
        sources.getClasses()
                .forEach(node -> this.map(node, false));

        // Set last member in config
        this.config.setNextMember(this.count.get());
    }

    public void map(final ClassNode klass, final boolean firstPass) {
        // Exclude configured packages
        for (final String excludedPackage : this.config.getExcludedPackages()) {
            if (klass.name.startsWith(excludedPackage.replace('.', '/'))) return;
        }

        if (firstPass) this.inheritance.provide(klass.name).ifPresent(info -> this.map0(info, klass));
        else this.inheritance.provide(klass.name).ifPresent(info -> this.map1(info, klass));
    }

    private void map0(final InheritanceProvider.ClassInfo info, final ClassNode klass) {
        final ClassMapping mapping = this.mappings.getOrCreateClassMapping(info.getName());

        // Map field mappings
        info.getFields().stream()
                .sorted(String::compareToIgnoreCase)
                .forEach(field -> this.mapField(mapping, field));

        // Map method mappings
        info.getMethods().stream()
                .sorted(ALPHABETISE_METHODS)
                .forEach(method -> {
                    // methods from parent classes are handled in the second pass
                    if (this.isMethodFromParent(info, method)) return;

                    this.mapMethod(mapping, method, klass);
                });
    }

    private void map1(final InheritanceProvider.ClassInfo info, final ClassNode klass) {
        final ClassMapping mapping = this.mappings.getOrCreateClassMapping(info.getName());

        info.getMethods().forEach(method -> {
            // methods from this class were handled in the first pass
            if (!this.isMethodFromParent(info, method)) return;

            this.getMethodParent(info, method).ifPresent(parentKlass -> {
                this.mappings.getClassMapping(parentKlass.getName()).ifPresent(parentMapping -> {
                    parentMapping.getMethodMapping(method).ifPresent(methodMapping -> {
                        mapping.getOrCreateMethodMapping(method)
                                .setDeobfuscatedName(methodMapping.getDeobfuscatedName());
                    });
                });
            });
        });
    }

    private Optional<InheritanceProvider.ClassInfo> getMethodParent(final InheritanceProvider.ClassInfo klass, final MethodSignature method) {
        final List<InheritanceProvider.ClassInfo> parentClasses = new ArrayList<>();
        if (klass.getSuperName() != null) {
            this.inheritance.provide(klass.getSuperName()).ifPresent(parentClasses::add);
        }
        klass.getInterfaces().stream()
                .map(this.inheritance::provide)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(parentClasses::add);

        for (final InheritanceProvider.ClassInfo parentClass : parentClasses) {
            if (parentClass.getMethods().contains(method)) return Optional.of(parentClass);
            final Optional<InheritanceProvider.ClassInfo> info = this.getMethodParent(parentClass, method);
            if (info.isPresent()) return info;
        }

        return Optional.empty();
    }

    private boolean isMethodFromParent(final InheritanceProvider.ClassInfo klass, final MethodSignature method) {
        return this.getMethodParent(klass, method).isPresent();
    }

    private FieldMapping mapField(final ClassMapping classMapping, final String fieldName) {
        final FieldMapping mapping = classMapping.getOrCreateFieldMapping(fieldName);

        if (fieldName.startsWith(FIELD_PREFIX) ||
                // inner classes, lambdas, etc
                fieldName.startsWith("this$") || fieldName.startsWith("val$") || fieldName.equalsIgnoreCase("$VALUES") ||
                mapping.hasDeobfuscatedName()) return mapping;

        return mapping.setDeobfuscatedName(FIELD_PREFIX + this.count.getAndIncrement() + "_" + fieldName);
    }

    private MethodMapping mapMethod(final ClassMapping classMapping, final MethodSignature signature, final ClassNode klass) {
        final MethodMapping mapping = classMapping.getOrCreateMethodMapping(signature);

        final boolean isEnum = Objects.equals(klass.superName, "java/lang/Enum");
        final MethodSignature enumValueOf = new MethodSignature(
                "valueOf",
                MethodDescriptor.compile("(Ljava/lang/String;)L" + klass.name + ";")
        );
        final MethodSignature enumValues = new MethodSignature(
                "values",
                MethodDescriptor.compile("()[L" + klass.name + ";")
        );

        if (signature.getName().startsWith(METHOD_PREFIX) ||
                // constructors and static inits
                signature.getName().equalsIgnoreCase("<init>") || signature.getName().equalsIgnoreCase("<clinit>") ||
                // inner classes, lambdas, etc
                signature.getName().startsWith("lambda$") || signature.getName().startsWith("access$") ||
                // Enums
                (isEnum && (signature.equals(enumValueOf) || signature.equals(enumValues))) ||
                signature.equals(MAIN_METHOD) ||
                mapping.hasDeobfuscatedName()) return mapping;

        return mapping.setDeobfuscatedName(METHOD_PREFIX + this.count.getAndIncrement() + "_" + signature.getName());
    }

}
