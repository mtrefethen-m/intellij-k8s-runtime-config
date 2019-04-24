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

import com.egoshard.intellij.k8s.ConfigTestDataUtils;
import com.egoshard.intellij.k8s.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests Kubernetes ConfigMap parsing")
class ConfigMapParserTest {

    private static final String TEST_KEY = TestUtils.getFinalStaticString(ConfigTestDataUtils.class, "TEST_KEY");
    private static final String TEST_VALUE = TestUtils.getFinalStaticString(ConfigTestDataUtils.class, "TEST_VALUE");

    private ConfigParser parser;

    @BeforeEach
    void setUp() {
        parser = new ConfigMapParser();
    }

    @DisplayName("Tests that the correct kind is supported")
    @Test
    void testKind() {
        assertEquals(ConfigParser.Kind.CONFIGMAP, parser.getSupportedKind());
    }

    @DisplayName("Tests that config map data is parsed correctly")
    @Test
    void testParser() {

        Map<String, String> result = parser.parse(ConfigTestDataUtils.getConfigMapData());

        assertTrue(result.containsKey(TEST_KEY));
        assertEquals(TEST_VALUE, result.get(TEST_KEY));

    }

}