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
package com.egoshard.intellij.k8s;

import com.egoshard.intellij.k8s.parser.AbstractParser;
import com.egoshard.intellij.k8s.parser.ConfigParser;
import com.egoshard.intellij.k8s.parser.SecretParser;

import java.util.HashMap;
import java.util.Map;

public class ConfigTestDataUtils {

    public static final String TEST_KEY = "test_data_key";
    public static final String TEST_VALUE = "test_data_value";
    public static final String TEST_STRING_DATA_KEY = "test_string_data_key";
    public static final String TEST_STRING_DATA_VALUE = "test_string_data_value";
    private static final String KEY_DATA = (String) TestUtils.getFinalStatic(AbstractParser.class, "KEY_DATA");
    private static final String KEY_STRING_DATA = (String) TestUtils.getFinalStatic(SecretParser.class, "KEY_STRING_DATA");
    private static final String KEY_KIND = (String) TestUtils.getFinalStatic(AbstractParser.class, "KEY_KIND");
    private static final String TEST_VALUE_ENCODED = "dGVzdF9kYXRhX3ZhbHVl";

    public static Map<String, Object> getInvalidData() {
        Map<String, Object> data = new HashMap<>();
        data.put(KEY_KIND, "invalidKind");
        return data;
    }

    public static Map<String, Object> getConfigMapData() {
        Map<String, Object> data = new HashMap<>();
        data.put(TEST_KEY, TEST_VALUE);
        return getSource(data, null, ConfigParser.Kind.CONFIGMAP);
    }

    public static Map<String, Object> getSecretData() {
        Map<String, Object> data = new HashMap<>();
        data.put(TEST_KEY, TEST_VALUE_ENCODED);
        return getSource(data, null, ConfigParser.Kind.SECRET);
    }

    public static Map<String, Object> getSecretStringData() {
        Map<String, Object> stringData = new HashMap<>();
        stringData.put(TEST_STRING_DATA_KEY, TEST_STRING_DATA_VALUE);
        return getSource(null, stringData, ConfigParser.Kind.SECRET);
    }

    public static Map<String, Object> getSecretDataAndStringData() {
        Map<String, Object> data = new HashMap<>();
        data.put(TEST_KEY, TEST_VALUE_ENCODED);
        Map<String, Object> stringData = new HashMap<>();
        stringData.put(TEST_STRING_DATA_KEY, TEST_STRING_DATA_VALUE);
        return getSource(data, stringData, ConfigParser.Kind.SECRET);
    }

    private static Map<String, Object> getSource(Map<String, Object> data, Map<String, Object> stringData, ConfigParser.Kind kind) {
        Map<String, Object> source = new HashMap<>();
        source.put(KEY_KIND, kind.getKey());
        if (data != null) {
            source.put(KEY_DATA, data);
        }
        if (stringData != null) {
            source.put(KEY_STRING_DATA, stringData);
        }
        return source;
    }

}
