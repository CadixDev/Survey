/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey;

import org.cadixdev.bombe.jar.JarClassEntry;
import org.cadixdev.bombe.jar.Jars;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.survey.context.SurveyContext;
import org.cadixdev.survey.context.SurveyContextBuilder;
import org.cadixdev.survey.mapper.AbstractMapper;
import org.cadixdev.survey.mapper.EnumConstantsMapper;
import org.cadixdev.survey.mapper.config.EnumConstantsMapperConfig;
import org.cadixdev.survey.patcher.AbstractPatcher;
import org.cadixdev.survey.patcher.proguard.ProguardSignaturePatcher;
import org.cadixdev.survey.util.Registry;
import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.jar.JarFile;

/**
 * The control centre of Survey.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class Survey implements SurveyContext {

    private final MappingSet mappings;
    private final List<String> blacklist = new ArrayList<>();

    private final Registry<SurveyContext> contexts = new Registry<>();
    private final Registry<AbstractMapper<?>> mappers = new Registry<>();
    private final Registry<AbstractPatcher<?>> patchers = new Registry<>();

    public Survey() {
        this(MappingSet.create());
    }

    public Survey(final MappingSet mappings) {
        this.mappings = mappings;
    }

    @Override
    public MappingSet mappings() {
        return this.mappings;
    }

    @Override
    public boolean blacklisted(final String klass) {
        for (final String blacklisted : this.blacklist) {
            if (klass.startsWith(blacklisted)) return true;
        }
        return false;
    }

    /**
     * Adds the given strings to the global blacklist.
     *
     * @param blacklist The names to blacklist
     * @return {@code this}
     */
    public Survey blacklist(final String... blacklist) {
        this.blacklist.addAll(Arrays.asList(blacklist));
        return this;
    }

    /**
     * Registers the given context.
     *
     * @param id The name of the context instance.
     * @param ctx The context instance
     * @return {@code this}
     */
    public Survey context(final String id, final SurveyContext ctx) {
        this.contexts.register(id, ctx);
        return this;
    }

    /**
     * Creates a context builder/
     *
     * @param id The identifier of the context
     * @return The context builder
     */
    public SurveyContextBuilder context(final String id) {
        return new SurveyContextBuilder(this, id);
    }

    /**
     * Registers the given mapper.
     *
     * @param id The name of the mapper
     * @param mapper The mapper instance
     * @param context The name of the context to use
     * @param config The config for the mapper
     * @param <C> The type of the config
     * @return {@code this}
     */
    public <C> Survey mapper(final String id,
                             final BiFunction<SurveyContext, C, AbstractMapper<C>> mapper,
                             final String context,
                             final C config) {
        this.mappers.register(id, mapper.apply(
                this._getContext(context),
                config
        ));
        return this;
    }

    /**
     * Registers the given patcher.
     *
     * @param id The name of the mapper
     * @param patcher The patcher instance
     * @param context The name of the context to use
     * @param config The config for the patcher
     * @param <C> The type of the config
     * @return {@code this}
     */
    public <C> Survey patcher(final String id,
                          final BiFunction<SurveyContext, C, AbstractPatcher<C>> patcher,
                          final String context,
                          final C config) {
        this.patchers.register(id, patcher.apply(
                this._getContext(context),
                config
        ));
        return this;
    }

    /**
     * Runs the mappers.
     *
     * @return {@code this}
     */
    public Survey map(final JarFile jar) {
        this.mappers.forEach((name, mapper) -> {
            this._runMapper(jar, name, mapper);
        });
        return this;
    }

    // Internal Methods

    public SurveyContext _getContext(final String name) {
        if (name == null) return this;
        if (this.contexts.byId(name) == null) {
            throw new RuntimeException("Unknown context!");
        }
        return this.contexts.byId(name);
    }

    void _runMapper(final JarFile jar, final String name, final AbstractMapper<?> mapper) {
        System.out.println("Running '" + name + "' mapper...");

        Jars.walk(jar)
                .filter(JarClassEntry.class::isInstance)
                .map(JarClassEntry.class::cast)
                .filter(entry -> !mapper.ctx().blacklisted(entry.getName()))
                .sorted(comparingLength(JarClassEntry::getName))
                .forEach(entry -> {
                    final ClassReader klass = new ClassReader(entry.getContents());
                    klass.accept(mapper, 0);
                });
    }

    private static <T> Comparator<T> comparingLength(final Function<? super T, String> keyExtractor) {
        return (c1, c2) -> {
            final String key1 = keyExtractor.apply(c1);
            final String key2 = keyExtractor.apply(c2);
            if (key1.length() != key2.length()) {
                return key1.length() - key2.length();
            }
            return key1.compareTo(key2);
        };
    }

}
