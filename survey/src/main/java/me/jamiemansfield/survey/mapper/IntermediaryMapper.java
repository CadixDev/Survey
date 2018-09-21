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

import static org.objectweb.asm.Opcodes.ASM6;

import me.jamiemansfield.bombe.analysis.CascadingInheritanceProvider;
import me.jamiemansfield.bombe.analysis.InheritanceProvider;
import me.jamiemansfield.bombe.asm.analysis.ClassLoaderInheritanceProvider;
import me.jamiemansfield.bombe.asm.analysis.SourceSetInheritanceProvider;
import me.jamiemansfield.bombe.asm.jar.SourceSet;
import me.jamiemansfield.bombe.type.ArrayType;
import me.jamiemansfield.bombe.type.FieldType;
import me.jamiemansfield.bombe.type.MethodDescriptor;
import me.jamiemansfield.bombe.type.ObjectType;
import me.jamiemansfield.bombe.type.Type;
import me.jamiemansfield.bombe.type.signature.FieldSignature;
import me.jamiemansfield.bombe.type.signature.MethodSignature;
import me.jamiemansfield.lorenz.MappingSet;
import me.jamiemansfield.lorenz.model.ClassMapping;
import me.jamiemansfield.lorenz.model.FieldMapping;
import me.jamiemansfield.lorenz.model.MethodMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An intermediary mapper, that can produce version-agnostic mappings for a
 * given jar.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class IntermediaryMapper {

    /**
     * A {@link Comparator} used to alphabetise a collection of {@link ClassNode}s.
     */
    private static final Comparator<ClassNode> ALPHABETISE_CLASSES = (o1, o2) -> o1.name.compareToIgnoreCase(o2.name);

    /**
     * A {@link Comparator} used to alphabetise a collection of {@link MethodSignature}s.
     */
    private static final Comparator<MethodSignature> ALPHABETISE_METHODS = (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName());

    private static final MethodSignature MAIN_METHOD = new MethodSignature("main", MethodDescriptor.of("([Ljava/lang/String;)V"));
    private static final MethodSignature STATIC_INIT = new MethodSignature("<clinit>", MethodDescriptor.of("()V"));

    private final IntermediaryMapperConfig config;
    private final MappingSet mappings;
    private final SourceSet sources;
    private final InheritanceProvider inheritance;

    public IntermediaryMapper(final IntermediaryMapperConfig config, final MappingSet mappings, final SourceSet sources) {
        this.config = config;
        this.mappings = mappings;
        this.sources = sources;
        this.inheritance = new CascadingInheritanceProvider()
                .install(new SourceSetInheritanceProvider(sources))
                .install(new ClassLoaderInheritanceProvider(IntermediaryMapper.class.getClassLoader()));
    }

    public void map() {
        final EnumMappingMethodVisitor enumMapper = new EnumMappingMethodVisitor(null, this.mappings);

        sources.getClasses().stream()
                // Exclude configured packages
                .filter(klass -> this.config.getExcludedPackages().stream()
                        .filter(pkg -> !pkg.isEmpty())
                        .noneMatch(pkg -> klass.name.startsWith(pkg.replace('.', '/'))))
                // Do the classes in order
                .sorted(ALPHABETISE_CLASSES)
                // Complete the passes
                .forEach(klass -> {
                    // Map enum fields
                    if (Objects.equals(klass.superName, "java/lang/Enum")) {
                        this.mapEnumFields(klass, enumMapper);
                    }

                    // Grab inheritance information
                    this.inheritance.provide(klass.name).ifPresent(info -> {
                        // First pass
                        this.map0(info, klass);

                        // Second pass
                        this.map1(info, klass);
                    });
                });
    }

    private void mapEnumFields(final ClassNode klass, final EnumMappingMethodVisitor enumMapper) {
        if (!this.config.shouldMapFields()) return;

        klass.methods.stream()
                .filter(method -> MethodSignature.of(method.name, method.desc).equals(STATIC_INIT))
                .forEach(method -> method.accept(enumMapper));
    }

    private void map0(final InheritanceProvider.ClassInfo info, final ClassNode klass) {
        final ClassMapping mapping = this.mappings.getOrCreateClassMapping(info.getName());

        // Map field mappings
        // TODO: handle different types
        info.getFields().stream()
                .map(FieldSignature::getName)
                .sorted(String::compareToIgnoreCase)
                .forEach(field -> this.mapField(mapping, field, klass));

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

            // Remap the method in child classes
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

    private FieldMapping mapField(final ClassMapping classMapping, final String fieldName, final ClassNode klass) {
        final FieldMapping mapping = classMapping.getOrCreateFieldMapping(fieldName);
        final FieldType klassType = new ObjectType(klass.name);

        final FieldNode field = klass.fields.stream()
                .filter(node -> Objects.equals(node.name, fieldName)).findAny()
                .orElse(null);
        if (field == null || !this.config.shouldMapFields()) return mapping;

        // Some obfuscators will remap synthetic fields, such as $VALUES in enums
        final boolean isEnum = Objects.equals(klass.superName, "java/lang/Enum");
        final boolean isSynthetic = (field.access & Opcodes.ACC_SYNTHETIC) != 0;
        final Type valuesType = new ArrayType(1, klassType);
        if (isEnum && isSynthetic && valuesType.equals(Type.of(field.desc)))
            return mapping.setDeobfuscatedName("$VALUES");

        if (fieldName.startsWith(this.config.getFieldPrefix()) ||
                // inner classes, lambdas, etc
                fieldName.startsWith("this$") || fieldName.startsWith("val$") || fieldName.equalsIgnoreCase("$VALUES") ||
                mapping.hasDeobfuscatedName()) return mapping;

        return mapping.setDeobfuscatedName(this.config.getFieldPrefix() + this.config.getAndIncrementNextMember() + "_" + fieldName);
    }

    private MethodMapping mapMethod(final ClassMapping classMapping, final MethodSignature signature, final ClassNode klass) {
        final MethodMapping mapping = classMapping.getOrCreateMethodMapping(signature);

        final MethodNode method = klass.methods.stream()
                .filter(node -> Objects.equals(node.name, signature.getName()) &&
                        Objects.equals(node.desc, this.mappings.deobfuscate(signature.getDescriptor()))).findAny()
                .orElse(null);
        if (method == null || !this.config.shouldMapMethods()) return mapping;

        final boolean isEnum = Objects.equals(klass.superName, "java/lang/Enum");
        final MethodSignature enumValueOf = new MethodSignature(
                "valueOf",
                MethodDescriptor.of("(Ljava/lang/String;)L" + klass.name + ";")
        );
        final MethodSignature enumValues = new MethodSignature(
                "values",
                MethodDescriptor.of("()[L" + klass.name + ";")
        );

        if (signature.getName().startsWith(this.config.getMethodPrefix()) ||
                // constructors and static inits
                signature.getName().equalsIgnoreCase("<init>") || signature.getName().equalsIgnoreCase("<clinit>") ||
                // inner classes, lambdas, etc
                signature.getName().startsWith("lambda$") || signature.getName().startsWith("access$") ||
                // Enums
                (isEnum && (signature.equals(enumValueOf) || signature.equals(enumValues))) ||
                // Native methods should never be changed!
                Modifier.isNative(method.access) ||
                // Main method
                signature.equals(MAIN_METHOD) ||
                mapping.hasDeobfuscatedName()) return mapping;

        return mapping.setDeobfuscatedName(this.config.getMethodPrefix() + this.config.getAndIncrementNextMember() + "_" + signature.getName());
    }

    /**
     * A {@link MethodVisitor} to find de-obfuscation mappings for enums, through
     * non-obfuscated values left for {@code "#valueOf(String)"}.
     */
    public static class EnumMappingMethodVisitor extends MethodVisitor {

        private final MappingSet mappings;
        private boolean incomingName = false;
        private String name = null;

        public EnumMappingMethodVisitor(final MethodVisitor mv, final MappingSet mappings) {
            super(ASM6, mv);
            this.mappings = mappings;
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (opcode == Opcodes.NEW) {
                this.incomingName = true;
                this.name = null;
            }
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            if (this.incomingName && cst instanceof String) {
                this.incomingName = false;
                this.name = (String) cst;
            }
            super.visitLdcInsn(cst);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            // We want to be certain that the field is actually an Enum type
            if (Objects.equals("L" + owner + ";", desc) &&
                    // We also want to be certain that we have a name to map too
                    this.name != null &&
                    // And the opcode is right
                    opcode == Opcodes.PUTSTATIC) {
                // Get the class, get the field, map the field
                this.mappings.getOrCreateClassMapping(owner)
                        .getOrCreateFieldMapping(name)
                        .setDeobfuscatedName(this.name);
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }

    }

}
