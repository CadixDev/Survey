/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.intermediary.provider;

import org.cadixdev.survey.mapper.MapperContext;
import org.cadixdev.survey.mapper.intermediary.FieldIntemediaryMapper;
import org.cadixdev.survey.mapper.provider.SimpleMapperProvider;

/**
 * The mapper provider for the field intermediary mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class FieldIntermediaryMapperProvider
        extends SimpleMapperProvider<FieldIntemediaryMapper, FieldIntemediaryMapper.Config> {

    private static final String ID = "intermediary-fields";

    public FieldIntermediaryMapperProvider() {
        super(ID, FieldIntemediaryMapper.Config.class, FieldIntemediaryMapper.Config.Deserialiser.INSTANCE);
    }

    @Override
    public FieldIntemediaryMapper create(final MapperContext ctx, final FieldIntemediaryMapper.Config config) {
        return new FieldIntemediaryMapper(ctx, config);
    }

}
