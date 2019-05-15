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

package org.cadixdev.survey.config.context;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.cadixdev.survey.Survey;
import org.cadixdev.survey.context.CascadingSurveyContext;
import org.cadixdev.survey.context.SimpleSurveyContext;
import org.cadixdev.survey.context.SurveyContext;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The de-serialiser used for reading Survey contexts.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class SurveyContextDeserialiser implements JsonDeserializer<SurveyContext> {

    private static final String ID = "id";
    private static final String EXTENDS = "extends";
    private static final String BLACKLIST = "blacklist";

    private final Survey survey;

    public SurveyContextDeserialiser(final Survey survey) {
        this.survey = survey;
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
                if (this.survey._getContexts().byId(parentId) == null) throw new JsonParseException("Unknown Survey context!");
                parent = this.survey._getContexts().byId(parentId);
            }
            else {
                parent = null;
            }

            final List<String> blacklist = readBlacklist(object);

            final SurveyContext context = new CascadingSurveyContext(this.survey.mappings())
                    .install(new SimpleSurveyContext(this.survey.mappings(), blacklist))
                    .install(parent);

            if (object.has(ID)) {
                this.survey.context(object.get(ID).getAsString(), context);
            }

            return context;
        }
        else if (element.isJsonPrimitive()) {
            final String id = element.getAsString();
            if (this.survey._getContexts().byId(id) == null) {
                throw new JsonParseException("Unknown Survey context: '" + id + "'!");
            }
            return this.survey._getContexts().byId(element.getAsString());
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
