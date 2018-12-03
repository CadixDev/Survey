/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.intermediary;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.survey.mapper.MapperContext;

import java.lang.reflect.Type;

/**
 * The class intermediary mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class ClassIntermediaryMapper extends AbstractIntermediaryMapper<ClassIntermediaryMapper.Config> {

    private int count = 0;

    public ClassIntermediaryMapper(final MapperContext ctx, final ClassIntermediaryMapper.Config configuration) {
        super(ctx, configuration);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        final ClassMapping<?, ?> klass = this.ctx().mappings().getOrCreateClassMapping(name);
        if (!klass.hasDeobfuscatedName()) {
            klass.setDeobfuscatedName(this.getConfiguration().getMemberName(this.count++, name));
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * The class intermediary mapper configuration.
     */
    public static class Config extends AbstractIntermediaryMapper.Config {

        private final String format;

        public Config(final String format) {
            this.format = format;
        }

        @Override
        public String getFormat() {
            return this.format;
        }

        public static class Deserialiser implements JsonDeserializer<ClassIntermediaryMapper.Config> {

            public static final Deserialiser INSTANCE = new Deserialiser();

            private static final String FORMAT = "format";
            private static final String FORMAT_DEFAULT = "Class{id}_{obf}";

            @Override
            public ClassIntermediaryMapper.Config deserialize(
                    final JsonElement element,
                    final Type type,
                    final JsonDeserializationContext ctx) throws JsonParseException {
                if (!element.isJsonObject()) throw new JsonParseException("class intermediary config must be an object!");
                final JsonObject object = element.getAsJsonObject();

                final String format = object.has(FORMAT) ?
                        object.get(FORMAT).getAsString() :
                        FORMAT_DEFAULT;

                return new ClassIntermediaryMapper.Config(format);
            }

        }

    }

}
