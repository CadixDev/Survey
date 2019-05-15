/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.mapper.intermediary.provider;

import org.cadixdev.survey.config.mapper.intermediary.ClassIntermediaryMapperConfigDeserialiser;
import org.cadixdev.survey.config.mapper.provider.SimpleMapperProvider;
import org.cadixdev.survey.context.SurveyContext;
import org.cadixdev.survey.mapper.intermediary.ClassIntermediaryMapper;

/**
 * The mapper provider for the class intermediary mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class ClassIntermediaryMapperProvider
        extends SimpleMapperProvider<ClassIntermediaryMapper, ClassIntermediaryMapper.Config> {

    private static final String ID = "intermediary_classes";

    public ClassIntermediaryMapperProvider() {
        super(ID, ClassIntermediaryMapper.Config.class, ClassIntermediaryMapperConfigDeserialiser.INSTANCE);
    }

    @Override
    public ClassIntermediaryMapper create(final SurveyContext ctx, final ClassIntermediaryMapper.Config config) {
        return new ClassIntermediaryMapper(ctx, config);
    }

}
