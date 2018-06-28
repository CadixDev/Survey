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

package me.jamiemansfield.survey.remapper;

import me.jamiemansfield.csv.CsvRow;
import org.objectweb.asm.commons.Remapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A remapper for named MCP mappings (fields.csv + methods.csv).
 */
public class ModCoderPackRemapper extends Remapper {

    private final Map<String, String> fields = new HashMap<>();
    private final Map<String, String> methods = new HashMap<>();

    public ModCoderPackRemapper(final List<CsvRow> fieldsCsv, final List<CsvRow> methodsCsv) {
        fieldsCsv.forEach(row -> {
            final Optional<String> seargeName = row.getValue("searge");
            final Optional<String> name = row.getValue("name");
            if (seargeName.isPresent() && name.isPresent()) {
                this.fields.put(seargeName.get(), name.get());
            }
        });
        methodsCsv.forEach(row -> {
            final Optional<String> seargeName = row.getValue("searge");
            final Optional<String> name = row.getValue("name");
            if (seargeName.isPresent() && name.isPresent()) {
                this.methods.put(seargeName.get(), name.get());
            }
        });
    }

    @Override
    public String mapFieldName(final String owner, final String name, final String desc) {
        if (this.fields.containsKey(name)) {
            return this.fields.get(name);
        }
        return name;
    }

    @Override
    public String mapMethodName(final String owner, final String name, final String desc) {
        if (this.methods.containsKey(name)) {
            return this.methods.get(name);
        }
        return name;
    }

}
