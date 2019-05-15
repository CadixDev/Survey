/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.config;

import org.cadixdev.survey.mapper.EnumConstantsMapper;

/**
 * Configuration for {@link EnumConstantsMapper}.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class EnumConstantsMapperConfig {

    /**
     * Toggle for mapping {@code "$VALUES"}.
     */
    public boolean mapSyntheticValues = true;

}
