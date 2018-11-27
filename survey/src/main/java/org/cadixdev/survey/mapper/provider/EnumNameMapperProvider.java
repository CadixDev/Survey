/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.provider;

import org.cadixdev.survey.mapper.EnumNameMapper;
import org.cadixdev.survey.mapper.MapperContext;
import org.cadixdev.survey.mapper.config.EnumNameMapperConfig;

/**
 * The mapper provider for the enum name mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class EnumNameMapperProvider extends SimpleMapperProvider<EnumNameMapper, EnumNameMapperConfig> {

    private static final String ID = "enum";

    public EnumNameMapperProvider() {
        super(ID, EnumNameMapperConfig.class, EnumNameMapperConfig.Deserialiser.INSTANCE);
    }

    @Override
    public EnumNameMapper create(final MapperContext ctx, final EnumNameMapperConfig config) {
        return new EnumNameMapper(ctx, config);
    }

}
