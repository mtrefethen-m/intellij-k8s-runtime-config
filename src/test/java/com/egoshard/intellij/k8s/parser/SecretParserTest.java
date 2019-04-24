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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests Kubernetes Secret parsing")
class SecretParserTest {

    private SecretParser parser;

    @BeforeEach
    void setUp() {
        parser = new SecretParser();
    }

    @DisplayName("Tests that the correct kind is supported")
    @Test
    void testKind() {
        assertEquals(ConfigParser.Kind.SECRET, parser.getSupportedKind());
    }

    @DisplayName("Tests that secret data is decoded and parsed correctly")
    @Test
    void testSecretDataParser() {

        Map<String, String> result = parser.parse(ConfigTestDataUtils.getSecretData());

        assertTrue(result.containsKey(ConfigTestDataUtils.TEST_KEY));
        assertEquals(ConfigTestDataUtils.TEST_VALUE, result.get(ConfigTestDataUtils.TEST_KEY));

    }

    @DisplayName("Tests that secret string data is parsed correctly")
    @Test
    void testSecretStringDataParser() {

        Map<String, String> result = parser.parse(ConfigTestDataUtils.getSecretStringData());

        assertTrue(result.containsKey(ConfigTestDataUtils.TEST_STRING_DATA_KEY));
        assertEquals(ConfigTestDataUtils.TEST_STRING_DATA_VALUE, result.get(ConfigTestDataUtils.TEST_STRING_DATA_KEY));

    }

    @DisplayName("Tests that secret data and string data is parsed correctly")
    @Test
    void testSecretDataStringDataParser() {

        Map<String, String> result = parser.parse(ConfigTestDataUtils.getSecretDataAndStringData());

        assertTrue(result.containsKey(ConfigTestDataUtils.TEST_KEY));
        assertEquals(ConfigTestDataUtils.TEST_VALUE, result.get(ConfigTestDataUtils.TEST_KEY));
        assertTrue(result.containsKey(ConfigTestDataUtils.TEST_STRING_DATA_KEY));
        assertEquals(ConfigTestDataUtils.TEST_STRING_DATA_VALUE, result.get(ConfigTestDataUtils.TEST_STRING_DATA_KEY));

    }

}