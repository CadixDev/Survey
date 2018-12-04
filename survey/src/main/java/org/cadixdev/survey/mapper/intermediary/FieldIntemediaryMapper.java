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
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.survey.mapper.MapperContext;
import org.objectweb.asm.FieldVisitor;

import java.lang.reflect.Type;

/**
 * The field intermediary mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class FieldIntemediaryMapper extends AbstractIntermediaryMapper<FieldIntemediaryMapper.Config> {

    private int count = 0;
    private ClassMapping<?, ?> klass;

    public FieldIntemediaryMapper(final MapperContext ctx, final Config configuration) {
        super(ctx, configuration);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        this.klass = this.ctx().mappings().getOrCreateClassMapping(name);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String descriptor, final String signature, final Object value) {
        final FieldMapping fieldMapping = this.klass.getOrCreateFieldMapping(name, descriptor);
        if (!fieldMapping.hasDeobfuscatedName()) {
            fieldMapping.setDeobfuscatedName(this.getConfiguration().getMemberName(++this.count, name));
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visitEnd() {
        this.klass = null;

        super.visitEnd();
    }

    /**
     * The field intermediary mapper configuration.
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

        public static class Deserialiser implements JsonDeserializer<FieldIntemediaryMapper.Config> {

            public static final Deserialiser INSTANCE = new Deserialiser();

            private static final String FORMAT = "format";
            private static final String FORMAT_DEFAULT = "field_{id}_{obf}";

            @Override
            public FieldIntemediaryMapper.Config deserialize(
                    final JsonElement element,
                    final Type type,
                    final JsonDeserializationContext ctx) throws JsonParseException {
                if (!element.isJsonObject()) throw new JsonParseException("field intermediary config must be an object!");
                final JsonObject object = element.getAsJsonObject();

                final String format = object.has(FORMAT) ?
                        object.get(FORMAT).getAsString() :
                        FORMAT_DEFAULT;

                return new FieldIntemediaryMapper.Config(format);
            }

        }

    }

}
