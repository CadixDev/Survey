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
import me.jamiemansfield.survey.analysis.CascadingInheritanceProvider;
import me.jamiemansfield.survey.analysis.ClassLoaderInheritanceProvider;
import me.jamiemansfield.survey.analysis.InheritanceProvider;
import me.jamiemansfield.survey.analysis.SourceSetInheritanceProvider;
import me.jamiemansfield.survey.jar.JarWalker;
import me.jamiemansfield.survey.jar.SourceSet;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class IntermediaryMapper {

    public static void main(final String[] args) {
        final SourceSet sources = new SourceSet();
        final MappingSet mappings = MappingSet.create();
        new JarWalker(Paths.get("joined.jar"));

        final IntermediaryMapper mapper = new IntermediaryMapper(mappings, sources);
        sources.getClasses().forEach(klass -> mapper.map(klass.name));
    }

    private final MappingSet mappings;
    private final InheritanceProvider inheritance;

    private final AtomicInteger count = new AtomicInteger();

    private IntermediaryMapper(final MappingSet mappings, final SourceSet sources) {
        this.mappings = mappings;
        this.inheritance = new CascadingInheritanceProvider()
                .install(new SourceSetInheritanceProvider(sources))
                .install(new ClassLoaderInheritanceProvider(IntermediaryMapper.class.getClassLoader()));
    }

    public void map(final String klass) {
        this.inheritance.provide(klass).ifPresent(this::map0);
    }

    private void map0(final InheritanceProvider.ClassInfo info) {
    }

    private FieldMapping mapField(final ClassMapping classMapping, final String fieldName) {
        return classMapping.createFieldMapping(
                fieldName,
                "field_" + this.count.getAndIncrement() + "_" + fieldName
        );
    }

    private MethodMapping mapMethod(final ClassMapping classMapping, final MethodDescriptor descriptor) {
        return classMapping.createMethodMapping(
                descriptor,
                "func_" + this.count.getAndIncrement() + "_" + descriptor.getName()
        );
    }

}
