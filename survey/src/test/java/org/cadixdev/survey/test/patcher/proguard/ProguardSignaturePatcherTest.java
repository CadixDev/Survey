/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.survey.test.patcher.proguard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cadixdev.survey.patcher.proguard.ProguardSignaturePatcher;
import org.junit.jupiter.api.Test;

final class ProguardSignaturePatcherTest {

    private static final String GOOD = "(TK;)Lzt<TK;TT;TR;>.a;";
    private static final String BAD = "(TK;)Lzt<TK;TT;TR;>.zt$a;";

    @Test
    void patches() {
        assertEquals(GOOD, ProguardSignaturePatcher.Patcher.patch(BAD));
        assertEquals(GOOD, ProguardSignaturePatcher.Patcher.patch(GOOD));
    }

}
