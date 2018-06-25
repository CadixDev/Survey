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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import me.jamiemansfield.lorenz.model.jar.MethodDescriptor;
import me.jamiemansfield.survey.jar.SourceSet;
import org.objectweb.asm.tree.ClassNode;

public class SourceSetInheritanceProvider implements InheritanceProvider {

    private final SourceSet sources;

    private final LoadingCache<String, ClassInfo> cache;

    public SourceSetInheritanceProvider(final SourceSet sources) {
        this.sources = sources;
        this.cache = Caffeine.newBuilder()
                .build(key -> {
                    if (this.sources.has(key)) {
                        return new SourceSetClassInfo(this.sources.get(key));
                    }
                    else {
                        return null;
                    }
                });
    }

    @Override
    public ClassInfo provide(final String klass) {
        return this.cache.get(klass);
    }

}

class SourceSetClassInfo extends InheritanceProvider.ClassInfo.Impl {

    SourceSetClassInfo(final ClassNode klass) {
        super(
                klass.name,
                klass.superName
        );
        this.interfaces.addAll(klass.interfaces);

        klass.fields.stream()
                .map(fieldNode -> fieldNode.name)
                .forEach(this.fields::add);
        klass.methods.stream()
                .map(methodNode -> new MethodDescriptor(methodNode.name, methodNode.desc))
                .forEach(this.methods::add);
    }

}
