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
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * A basic configuration for the intermediary mapper, allowing for
 * additional configuration that is likely to be required in many
 * scenarios.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class IntermediaryMapperConfig {

    private static final String NEXT_MEMBER = "next-member";
    private static final String EXCLUDED_PACKAGES = "excluded-packages";
    private static final String FIELD_PREFIX = "field-prefix";
    private static final String METHOD_PREFIX = "method-prefix";

    private static Properties DEFAULTS = new Properties();

    static {
        DEFAULTS.setProperty(NEXT_MEMBER, "0");
        DEFAULTS.setProperty(EXCLUDED_PACKAGES, "");
        DEFAULTS.setProperty(FIELD_PREFIX, "field_");
        DEFAULTS.setProperty(METHOD_PREFIX, "func_");
    }

    /**
     * Reads a configuration from a given {@link Path}, falling back to default
     * values if unable to read.
     *
     * @param configPath The path to the configuration
     * @return The configuration
     */
    public static IntermediaryMapperConfig read(final Path configPath) {
        final Properties props = new Properties(DEFAULTS);
        try (final InputStream in = Files.newInputStream(configPath)) {
            props.load(in);
        }
        catch (final IOException ignored) {
        }
        return new IntermediaryMapperConfig(props);
    }

    /**
     * Creates a configuration of default values.
     *
     * @return The configuration
     */
    public static IntermediaryMapperConfig create() {
        return new IntermediaryMapperConfig(DEFAULTS);
    }

    private int nextMember;
    private final List<String> excludedPackages = new ArrayList<>();
    private final String fieldPrefix;
    private final String methodPrefix;

    public IntermediaryMapperConfig(final Properties props) {
        this.nextMember = Integer.valueOf(props.getProperty(NEXT_MEMBER));
        this.excludedPackages.addAll(Arrays.asList(props.getProperty(EXCLUDED_PACKAGES).split(",")));
        this.fieldPrefix = props.getProperty(FIELD_PREFIX);
        this.methodPrefix = props.getProperty(METHOD_PREFIX);
    }

    /**
     * Gets the integer identifier of the next member.
     *
     * @return The next member
     */
    public int getNextMember() {
        return this.nextMember;
    }

    /**
     * Gets the integer identifier of the next member, and increments
     * the value for the next.
     *
     * @return The next member
     */
    public int getAndIncrementNextMember() {
        final int currentValue = this.nextMember;
        ++this.nextMember;
        return currentValue;
    }

    /**
     * Sets the integer identifier of the next member.
     *
     * @param nextMember The next member
     */
    public void setNextMember(final int nextMember) {
        this.nextMember = nextMember;
    }

    /**
     * Gets an immutable view of all the excluded packages.
     *
     * @return The excluded packages
     */
    public List<String> getExcludedPackages() {
        return Collections.unmodifiableList(this.excludedPackages);
    }

    /**
     * Gets the prefix used for fields.
     *
     * @return The field prefix
     */
    public String getFieldPrefix() {
        return this.fieldPrefix;
    }

    /**
     * Gets the prefix used for methods.
     *
     * @return The method prefix
     */
    public String getMethodPrefix() {
        return this.methodPrefix;
    }

    /**
     * Saves the configuration to the given {@link Path}.
     *
     * @param configPath The path to save the configuration to
     */
    public void save(final Path configPath) {
        try (final OutputStream out = Files.newOutputStream(configPath)) {
            final Properties props = new Properties(DEFAULTS);
            props.setProperty(NEXT_MEMBER, "" + this.nextMember);
            props.setProperty(EXCLUDED_PACKAGES, this.excludedPackages.stream().collect(Collectors.joining(",")));
            props.setProperty(FIELD_PREFIX, this.fieldPrefix);
            props.setProperty(METHOD_PREFIX, this.methodPrefix);
            props.store(out, "Configuration options for Survey's intermediary mapper");
        }
        catch (final IOException ignored) {
        }
    }

}
