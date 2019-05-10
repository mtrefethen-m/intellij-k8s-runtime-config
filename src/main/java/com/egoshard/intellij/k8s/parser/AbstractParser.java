/*
 * Copyright (c) 2019. Matt Trefethen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.egoshard.intellij.k8s.parser;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Base parsing functionality
 */
public abstract class AbstractParser implements ConfigParser {

    static final String KEY_DATA = "data";
    private static final String MSG_SOURCE_MISSING = "The source parameter is required.";
    private static final String MSG_KEY_MISSING = "The provided configuration cannot be parsed. The key, [%s], does not exist in the source object.";
    private static final String MSG_INCORRECT_KIND = "The provided source data is invalid or not the correct Kubernetes kind for this parser.";

    /**
     * Validates that the source collection contains a 'Kind' key and that it's value matches the supported kind of the parser.
     *
     * @param source configuration map
     */
    void validate(@NotNull(MSG_SOURCE_MISSING) Map<String, Object> source) {
        if (!source.getOrDefault(ConfigParser.KEY_KIND, "INVALID").equals(getSupportedKind().getKey())) {
            throw new IllegalArgumentException(MSG_INCORRECT_KIND);
        }
    }

    /**
     * Extracts a map of configuration values from a source map using a provided key.
     * <p>
     * This functionality is specifically intended to extract an inner Map<String, String> from a source map based on a YAML file.
     * </p>
     *
     * @param source configuration map
     * @param key    key to extract the value of
     * @return map key/value pairs or an empty map if the key does not exist in the source map.
     */
    @SuppressWarnings("unchecked")
    Map<String, Object> getData(@NotNull(MSG_SOURCE_MISSING) Map<String, Object> source, @NotNull(MSG_KEY_MISSING) String key) {
        return (Map<String, Object>) source.getOrDefault(key, new HashMap<>());
    }

}
