/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.util.Registry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The context of which Survey runs within.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public abstract class SurveyContext {

    private final MappingSet mappings;
    private final SurveyContext parent;

    public SurveyContext(final MappingSet mappings, final SurveyContext parent) {
        this.mappings = mappings;
        this.parent = parent;
    }

    /**
     * Gets the mapping set, in use by Survey.
     *
     * @return The mappings
     */
    public MappingSet mappings() {
        return this.mappings;
    }

    /**
     * Established whether the given class is blacklisted either
     * globally, or specific to context.
     *
     * @param klass The class to check
     * @return {@code true} if the class is blacklisted;
     *         {@code false} otherwise
     */
    public boolean blacklisted(final String klass) {
        return this.parent != null && this.parent.blacklisted(klass);
    }

    /**
     * The de-serialiser used for reading Survey contexts.
     */
    public static class Deserialiser implements JsonDeserializer<SurveyContext> {

        private static final String ID = "id";
        private static final String EXTENDS = "extends";
        private static final String BLACKLIST = "blacklist";

        private final MappingSet mappings;
        private final Registry<SurveyContext> registry = new Registry<>();

        public Deserialiser(final MappingSet mappings) {
            this.mappings = mappings;
        }

        @Override
        public SurveyContext deserialize(
                final JsonElement element,
                final Type type,
                final JsonDeserializationContext ctx) throws JsonParseException {
            if (element.isJsonObject()) {
                final JsonObject object = element.getAsJsonObject();

                final SurveyContext parent;
                if (object.has(EXTENDS)) {
                    final String parentId = object.get(EXTENDS).getAsString();
                    if (this.registry.byId(parentId) == null) throw new JsonParseException("Unknown Survey context!");
                    parent = this.registry.byId(parentId);
                }
                else {
                    parent = null;
                }

                final List<String> blacklist = readBlacklist(object);

                final SurveyContext context = new SurveyContext(this.mappings, parent) {
                    @Override
                    public boolean blacklisted(final String klass) {
                        for (final String blacklisted : blacklist) {
                            if (klass.startsWith(blacklisted)) return true;
                        }
                        return super.blacklisted(klass);
                    }
                };

                if (object.has(ID)) {
                    this.registry.register(object.get(ID).getAsString(), context);
                }

                return context;
            }
            else if (element.isJsonPrimitive()) {
                final String id = element.getAsString();
                if (this.registry.byId(id) == null){
                    throw new JsonParseException("Unknown Survey context: '" + id + "'!");
                }
                return this.registry.byId(element.getAsString());
            }
            throw new JsonParseException("Don't know how to produce a context!");
        }

        private static List<String> readBlacklist(final JsonObject parent) throws JsonParseException {
            final List<String> blacklist = new ArrayList<>();

            if (parent.has(BLACKLIST)) {
                final JsonElement blacklistElement = parent.get(BLACKLIST);
                if (!blacklistElement.isJsonArray()) throw new JsonParseException("Blacklist must be an array!");

                for (final JsonElement arrElement : blacklistElement.getAsJsonArray()) {
                    if (!arrElement.isJsonPrimitive()) throw new JsonParseException("Invalid blacklist entry!");
                    blacklist.add(arrElement.getAsString());
                }
            }

            return blacklist;
        }

    }

}
