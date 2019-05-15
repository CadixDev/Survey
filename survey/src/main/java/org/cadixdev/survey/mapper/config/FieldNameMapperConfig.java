/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.mapper.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The configuration for the field name mapper.
 *
 * @author Jamie Mansfield
 * @since 0.2.0
 */
public class FieldNameMapperConfig {

    public final Map<ModifierRequirement, Boolean> requirements = new HashMap<>();
    public String desc;
    public String name;

}
