/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.provider;

import org.cadixdev.survey.mapper.EnumConstantsMapper;
import org.cadixdev.survey.mapper.MapperContext;
import org.cadixdev.survey.mapper.config.EnumConstantsMapperConfig;

/**
 * The mapper provider for the enum constants mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class EnumConstantsMapperProvider extends SimpleMapperProvider<EnumConstantsMapper, EnumConstantsMapperConfig> {

    private static final String ID = "enum-constants";

    public EnumConstantsMapperProvider() {
        super(ID, EnumConstantsMapperConfig.class, EnumConstantsMapperConfig.Deserialiser.INSTANCE);
    }

    @Override
    public EnumConstantsMapper create(final MapperContext ctx, final EnumConstantsMapperConfig config) {
        return new EnumConstantsMapper(ctx, config);
    }

}
