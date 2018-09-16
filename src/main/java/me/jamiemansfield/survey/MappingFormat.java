/*
 * Copyright (c) 2018, Jamie Mansfield <https://jamiemansfield.me/>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.jamiemansfield.survey;

import me.jamiemansfield.lorenz.io.MappingFormats;

/**
 * The many mapping formats that are supported by Survey's CLI, all provided
 * by Lorenz.
 *
 * @author Jamie Mansfield
 * @since 0.1.0
 */
public enum MappingFormat {

    /**
     * The standard SRG mapping format.
     */
    SRG(MappingFormats.SRG),

    /**
     * The compact SRG mapping format, used by Spigot.
     */
    CSRG(MappingFormats.CSRG),

    /**
     * The tabbed SRG mapping format, used by MCPConfig.
     */
    TSRG(MappingFormats.TSRG),

    ;

    private final me.jamiemansfield.lorenz.io.MappingFormat format;

    MappingFormat(final me.jamiemansfield.lorenz.io.MappingFormat format) {
        this.format = format;
    }

    /**
     * Gets the wrapped mapping format.
     *
     * @return The mapping format
     * @since 0.2.0
     */
    public me.jamiemansfield.lorenz.io.MappingFormat get() {
        return this.format;
    }

}
