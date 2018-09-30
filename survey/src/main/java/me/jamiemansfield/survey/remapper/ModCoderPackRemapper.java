/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
 *
 * @author Jamie Mansfield
 * @since 0.1.0
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
