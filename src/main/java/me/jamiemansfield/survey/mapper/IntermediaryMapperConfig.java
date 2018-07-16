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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class IntermediaryMapperConfig {

    private static Properties DEFAULTS = new Properties();

    static {
        DEFAULTS.setProperty("next-member", "0");
        DEFAULTS.setProperty("excluded-packages", "");
    }

    public static IntermediaryMapperConfig read(final Path configPath) {
        final Properties props = new Properties(DEFAULTS);
        try (final InputStream in = Files.newInputStream(configPath)) {
            props.load(in);
        }
        catch (final IOException ignored) {
        }
        return new IntermediaryMapperConfig(props);
    }

    public static IntermediaryMapperConfig create() {
        return new IntermediaryMapperConfig(DEFAULTS);
    }

    private int nextMember;
    private final List<String> excludedPackages = new ArrayList<>();

    public IntermediaryMapperConfig(final Properties props) {
        this.nextMember = Integer.valueOf(props.getProperty("next-member"));
        this.excludedPackages.addAll(Arrays.asList(props.getProperty("excluded-packages").split(",")));
    }

    public int getNextMember() {
        return this.nextMember;
    }

    public void setNextMember(final int nextMember) {
        this.nextMember = nextMember;
    }

    public List<String> getExcludedPackages() {
        return this.excludedPackages;
    }

    public void save(final Path configPath) {
        try (final OutputStream out = Files.newOutputStream(configPath)) {
            final Properties props = new Properties();
            props.setProperty("next-member", "" + this.nextMember);
            props.setProperty("excluded-packages", this.excludedPackages.stream().collect(Collectors.joining(",")));
            props.store(out, "Configuration options for Survey's intermediary mapper");
        }
        catch (final IOException ignored) {
        }
    }

}
