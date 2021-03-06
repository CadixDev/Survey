/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.mapper.provider;

import org.cadixdev.survey.config.mapper.FieldNameMapperConfigDeserialiser;
import org.cadixdev.survey.context.SurveyContext;
import org.cadixdev.survey.mapper.FieldNameMapper;
import org.cadixdev.survey.mapper.config.FieldNameMapperConfig;

/**
 * The mapper provider for the field name mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class FieldNameMapperProvider extends SimpleMapperProvider<FieldNameMapper, FieldNameMapperConfig> {

    private static final String ID = "field_name";

    public FieldNameMapperProvider() {
        super(ID, FieldNameMapperConfig.class, FieldNameMapperConfigDeserialiser.INSTANCE);
    }

    @Override
    public FieldNameMapper create(final SurveyContext ctx, final FieldNameMapperConfig config) {
        return new FieldNameMapper(ctx, config);
    }

}
