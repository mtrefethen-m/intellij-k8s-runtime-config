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

import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class ConfigMapParser extends AbstractParser {

    /**
     * Parses a source map matching the YAML structure of a Kubernetes configuration file.
     *
     * @param source configuration map
     * @return map key/value pairs
     */
    @Override
    public Map<String, String> parse(Map<String, Object> source) {
        Map<String, String> data = super.getData(source, KEY_DATA);
        return data.keySet().stream()
                .collect(Collectors.toMap(
                        key -> key, data::get, (a, b) -> b)
                );
    }

    /**
     * Indicates the specific kind of Kubernetes configuration the parser is intended to operate on.
     *
     * @return enumerated kind value.
     */
    @Override
    public Kind getSupportedKind() {
        return Kind.CONFIGMAP;
    }

}
