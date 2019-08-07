/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.config.patcher.provider;

import org.cadixdev.survey.context.SurveyContext;
import org.cadixdev.survey.patcher.MappingsPopulator;

public class MappingsPopulatorProvider extends SimplePatcherProvider<MappingsPopulator, Void> {

    private static final String ID = "mappings_populator";

    public MappingsPopulatorProvider() {
        super(ID, Void.class, null);
    }

    @Override
    public MappingsPopulator create(final SurveyContext ctx, final Void config) {
        return new MappingsPopulator(ctx);
    }

}
