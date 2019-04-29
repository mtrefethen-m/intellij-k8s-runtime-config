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

import static java.util.Base64.Decoder;
import static java.util.Base64.getDecoder;

/**
 * Parsers secret configuration files into discrete environment variable key/value pairs.
 */
public class SecretParser extends AbstractParser {

    private static final String KEY_STRING_DATA = "stringData";

    /**
     * Parses a source map matching the YAML structure of a Kubernetes secret configuration file.
     * <p>
     * This parser supports both secret encoded data and stringData models.
     * </p>
     *
     * @param source configuration map
     * @return map key/value pairs
     */
    @Override
    public Map<String, String> parse(Map<String, Object> source) {
        Map<String, String> result;
        Map<String, String> data = super.getData(source, KEY_DATA);
        Map<String, String> stringData = super.getData(source, KEY_STRING_DATA);

        Decoder decoder = getDecoder();
        result = data.keySet().stream().collect(
                Collectors.toMap(
                        s -> s,
                        s -> new String(decoder.decode(String.valueOf(data.get(s)))), (a, b) -> b));
        stringData.keySet().forEach(key -> result.put(key, stringData.get(key)));

        return result;
    }

    /**
     * Indicates the specific kind of Kubernetes configuration the parser is intended to operate on.
     *
     * @return enumerated kind value.
     */
    @Override
    public Kind getSupportedKind() {
        return Kind.SECRET;
    }

}
