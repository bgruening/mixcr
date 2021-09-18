/*
 * Copyright (c) 2014-2021, MiLaboratories Inc. All Rights Reserved
 *
 * BEFORE DOWNLOADING AND/OR USING THE SOFTWARE, WE STRONGLY ADVISE
 * AND ASK YOU TO READ CAREFULLY LICENSE AGREEMENT AT:
 *
 * https://github.com/milaboratory/mixcr/blob/develop/LICENSE
 */
package com.milaboratory.mixcr.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.milaboratory.util.GlobalObjectMappers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CloneAssemblerParametersPresets {
    private CloneAssemblerParametersPresets() {
    }

    private static Map<String, CloneAssemblerParameters> knownParameters;

    private static void ensureInitialized() {
        if (knownParameters == null)
            synchronized (CloneAssemblerParametersPresets.class) {
                if (knownParameters == null) {
                    Map<String, CloneAssemblerParameters> map;
                    try {
                        InputStream is = CloneAssemblerParameters.class.getClassLoader().getResourceAsStream("parameters/assembler_parameters.json");
                        TypeReference<HashMap<String, CloneAssemblerParameters>> typeRef
                                = new TypeReference<HashMap<String, CloneAssemblerParameters>>() {
                        };
                        map = GlobalObjectMappers.ONE_LINE.readValue(is, typeRef);
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                    knownParameters = map;
                }
            }
    }

    public static Set<String> getAvailableParameterNames() {
        ensureInitialized();
        return knownParameters.keySet();
    }

    public static CloneAssemblerParameters getByName(String name) {
        ensureInitialized();
        CloneAssemblerParameters params = knownParameters.get(name);
        if (params == null)
            return null;
        return params.clone();
    }
}
