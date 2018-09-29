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

package me.jamiemansfield.survey.jar;

import me.jamiemansfield.bombe.jar.JarEntryTransformer;
import me.jamiemansfield.bombe.jar.JarServiceProviderConfigurationEntry;
import me.jamiemansfield.bombe.jar.ServiceProviderConfiguration;
import me.jamiemansfield.lorenz.MappingSet;
import me.jamiemansfield.lorenz.model.ClassMapping;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * A jar entry transformer, for remapping service provider configurations.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class ServiceProviderConfigurationRemappingTransformer implements JarEntryTransformer {

    private final MappingSet mappings;

    public ServiceProviderConfigurationRemappingTransformer(final MappingSet mappings) {
        this.mappings = mappings;
    }

    @Override
    public JarServiceProviderConfigurationEntry transform(final JarServiceProviderConfigurationEntry entry) {
        final ServiceProviderConfiguration obfService = entry.getConfig();

        final String deobfServiceName = this.mappings.getClassMapping(obfService.getService().replace('.', '/'))
                .map(ClassMapping::getFullDeobfuscatedName)
                .orElse(obfService.getService())
                .replace('/', '.');
        final AtomicReference<String> currentProvider = new AtomicReference<>();
        final List<String> deobfProviders = obfService.getProviders().stream()
                .peek(currentProvider::set)
                .map(this.mappings::getClassMapping)
                .map(mapping -> mapping.map(ClassMapping::getFullDeobfuscatedName))
                .map(mapping -> mapping.orElse(currentProvider.get()))
                .map(mapping -> mapping.replace('/', '.'))
                .collect(Collectors.toList());
        final ServiceProviderConfiguration deobfService = new ServiceProviderConfiguration(
                deobfServiceName,
                deobfProviders
        );
        return new JarServiceProviderConfigurationEntry(deobfService);
    }
}
