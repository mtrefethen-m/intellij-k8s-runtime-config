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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.egoshard.intellij.k8s.parser.ConfigParser.Kind;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests common parsing")
class AbstractParserTest {

    private static final String KEY_DATA = TestUtils.getFinalStaticString(AbstractParser.class, "KEY_DATA");
    private static final String MSG_INCORRECT_KIND = TestUtils.getFinalStaticString(AbstractParser.class, "MSG_INCORRECT_KIND");

    @DisplayName("Test parser invalid kind validation")
    @Test
    void testValidateInvalid() {
        TestParser parser = new TestParser(Kind.CONFIGMAP);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.validate(ConfigTestDataUtils.getInvalidData()));
        assertEquals(MSG_INCORRECT_KIND, exception.getMessage());
    }

    @DisplayName("Test parser configmap kind validation")
    @Test
    void testValidateConfigMap() {
        TestParser parser = new TestParser(Kind.CONFIGMAP);
        parser.validate(ConfigTestDataUtils.getConfigMapData());
    }

    @DisplayName("Test parser secret kind validation")
    @Test
    void testValidateSecret() {
        TestParser parser = new TestParser(Kind.SECRET);
        parser.validate(ConfigTestDataUtils.getSecretData());
    }

    @DisplayName("Test parser secret kind validation")
    @Test
    void testValidateSecretString() {
        TestParser parser = new TestParser(Kind.SECRET);
        parser.validate(ConfigTestDataUtils.getSecretStringData());
    }

    @DisplayName("Test key data retrieval")
    @Test
    void testGetData() {
        TestParser parser = new TestParser(Kind.CONFIGMAP);
        Map<String, Object> result = parser.getData(ConfigTestDataUtils.getConfigMapData(), KEY_DATA);

        assertTrue(result.containsKey(ConfigTestDataUtils.TEST_KEY));
        assertEquals(ConfigTestDataUtils.TEST_VALUE, result.get(ConfigTestDataUtils.TEST_KEY));
    }

    @DisplayName("Test key no data retrieval returns empty map")
    @Test
    void testGetNoData() {
        Map<String, Object> source = new HashMap<>();

        TestParser parser = new TestParser(Kind.CONFIGMAP);
        Map<String, Object> result = parser.getData(source, KEY_DATA);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("ConstantConditions")
    @DisplayName("Tests that passing null parameters throws an exception")
    @Test
    void testGetDataNull() {
        TestParser parser = new TestParser(Kind.CONFIGMAP);
        assertThrows(IllegalArgumentException.class, () -> parser.getData(null, KEY_DATA));
        assertThrows(IllegalArgumentException.class, () -> parser.getData(new HashMap<>(), null));
    }

    class TestParser extends AbstractParser {

        private final Kind kind;

        TestParser(Kind kind) {
            this.kind = kind;
        }

        @Override
        public Map<String, String> parse(Map<String, Object> source) {
            return new HashMap<>();
        }

        @Override
        public Kind getSupportedKind() {
            return kind;
        }

        @Override
        Map<String, Object> getData(@NotNull Map<String, Object> source, @NotNull String key) {
            return super.getData(source, key);
        }

    }

}