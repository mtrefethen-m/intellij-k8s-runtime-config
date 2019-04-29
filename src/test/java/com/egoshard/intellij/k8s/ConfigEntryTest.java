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
import com.egoshard.intellij.k8s.parser.ConfigMapParser;
import com.egoshard.intellij.k8s.parser.ConfigParser;
import com.egoshard.intellij.k8s.parser.SecretParser;
import com.egoshard.intellij.k8s.support.ConfigFileUtil;
import com.google.common.collect.ImmutableMap;
import com.intellij.execution.configurations.RunConfigurationBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

@DisplayName("Tests ConfigEntry functionality")
class ConfigEntryTest extends TestUtils {

    private static final String KEY_KIND = getFinalStaticString(ConfigParser.class, "KEY_KIND");
    private static final String KEY_DATA = getFinalStaticString(AbstractParser.class, "KEY_DATA");
    private static final String MSG_SOURCE_NOT_EXIST = getFinalStaticString(ConfigEntry.class, "MSG_SOURCE_NOT_EXIST");
    private static final String MSG_SOURCE_INVALID = getFinalStaticString(ConfigEntry.class, "MSG_SOURCE_INVALID");
    private static final String MSG_SOURCE_WRONGKIND = getFinalStaticString(ConfigEntry.class, "MSG_SOURCE_WRONGKIND");
    private static final String MSG_SOURCE_NULL = getFinalStaticString(ConfigEntry.class, "MSG_SOURCE_NULL");
    private static final String PATH = "path";
    private static final String YAML_VALUE = "{\"%s\":\"%s\",\"%s\":{\"test_key\":\"test_data\"}}";
    private static final String YAML_EMPTY = "{}";
    private static final String YAML_INVALID = "test";
    private ConfigEntry entry;

    @Mock
    private Map<String, ConfigParser> mockConfigParsers;

    @Mock
    private ConfigFileUtil mockFileUtil;

    @Mock
    private RunConfigurationBase mockRunconfiguration;

    @Mock
    private ConfigMapParser mockConfigMapParser;

    @Mock
    private SecretParser mockSecretParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @DisplayName("Tests entry validation")
    @Test
    void testValidate() {
        doReturn(true).when(mockFileUtil).exists(any(RunConfigurationBase.class), anyString());
        entry = new ConfigEntry(mockConfigParsers, mockFileUtil, mockRunconfiguration, PATH);
        entry.validate();
    }

    @DisplayName("Tests entry validation with null file utility")
    @Test
    void testValidateNullFileUtil() {
        doReturn(true).when(mockFileUtil).exists(any(RunConfigurationBase.class), anyString());
        entry = new ConfigEntry(mockConfigParsers, null, mockRunconfiguration, PATH);
        assertThrows(IllegalStateException.class, () -> entry.validate());
    }

    @DisplayName("Tests path setting validation")
    @Test
    void testValidatePathSet() {
        String newPath = "new path";
        doReturn(true).when(mockFileUtil).exists(any(RunConfigurationBase.class), anyString());
        entry = new ConfigEntry(mockConfigParsers, mockFileUtil, mockRunconfiguration, PATH);
        entry.setPath(newPath);
        entry.validate();
        assertEquals(newPath, entry.getPath());
        verify(mockFileUtil).exists(any(RunConfigurationBase.class), eq(newPath));
    }

    @DisplayName("Tests parse, file not found")
    @Test
    void testParseFileNotFound() {
        entry = new ConfigEntry(mockConfigParsers, new TestFileNotFoundUtil(), mockRunconfiguration, PATH);
        ConfigFileException exception = assertThrows(ConfigFileException.class, () -> entry.parse());
        assertEquals(String.format(MSG_SOURCE_NOT_EXIST, PATH), exception.getMessage());
    }

    @DisplayName("Tests parse, class cast")
    @Test
    void testParseClassCast() throws FileNotFoundException {
        doReturn(new ByteArrayInputStream(YAML_INVALID.getBytes()))
                .when(mockFileUtil).getStream(any(RunConfigurationBase.class), anyString());
        entry = new ConfigEntry(mockConfigParsers, mockFileUtil, mockRunconfiguration, PATH);
        ConfigFileException exception = assertThrows(ConfigFileException.class, () -> entry.parse());
        assertEquals(String.format(MSG_SOURCE_INVALID, PATH), exception.getMessage());
    }

    @DisplayName("Tests parse, null")
    @Test
    void testParseNull() throws IOException {
        doReturn(new ByteArrayInputStream(YAML_EMPTY.getBytes())).when(mockFileUtil).getStream(any(RunConfigurationBase.class), anyString());
        entry = new ConfigEntry(mockConfigParsers, mockFileUtil, mockRunconfiguration, PATH);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> entry.parse());
        assertEquals(String.format(MSG_SOURCE_NULL, PATH), exception.getMessage());
    }

    @DisplayName("Tests parse, missing parser")
    @Test
    void testParseInvalidKind() throws FileNotFoundException {
        doReturn(getTestYamlStream(KEY_KIND, YAML_INVALID, KEY_DATA))
                .when(mockFileUtil).getStream(any(RunConfigurationBase.class), anyString());
        entry = new ConfigEntry(mockConfigParsers, mockFileUtil, mockRunconfiguration, PATH);
        ConfigFileException exception = assertThrows(ConfigFileException.class, () -> entry.parse());
        assertEquals(String.format(MSG_SOURCE_WRONGKIND, YAML_INVALID), exception.getMessage());
    }

    @DisplayName("Tests parse, ConfigMap")
    @Test
    void testParseConfigMapKind() throws IOException, ConfigFileException {
        Map<String, ConfigParser> parsers = ImmutableMap.of(
                ConfigParser.Kind.CONFIGMAP.getKey(), mockConfigMapParser,
                ConfigParser.Kind.SECRET.getKey(), mockSecretParser
        );
        doReturn(getTestYamlStream(KEY_KIND, ConfigParser.Kind.CONFIGMAP.getKey(), KEY_DATA))
                .when(mockFileUtil).getStream(any(RunConfigurationBase.class), anyString());
        entry = new ConfigEntry(parsers, mockFileUtil, mockRunconfiguration, PATH);
        entry.parse();
        verify(mockConfigMapParser).parse(anyMap());
        verify(mockSecretParser, never()).parse(anyMap());
    }

    @DisplayName("Tests parse, Secret")
    @Test
    void testParseSecretKind() throws IOException, ConfigFileException {
        Map<String, ConfigParser> parsers = ImmutableMap.of(
                ConfigParser.Kind.CONFIGMAP.getKey(), mockConfigMapParser,
                ConfigParser.Kind.SECRET.getKey(), mockSecretParser
        );
        doReturn(getTestYamlStream(KEY_KIND, ConfigParser.Kind.SECRET.getKey(), KEY_DATA))
                .when(mockFileUtil).getStream(any(RunConfigurationBase.class), anyString());
        entry = new ConfigEntry(parsers, mockFileUtil, mockRunconfiguration, PATH);
        entry.parse();
        verify(mockConfigMapParser, never()).parse(anyMap());
        verify(mockSecretParser).parse(anyMap());
    }

    private InputStream getTestYamlStream(String kindKey, String kindValue, String dataKey) {
        return new ByteArrayInputStream(String.format(YAML_VALUE, kindKey, kindValue, dataKey).getBytes());
    }

    class TestFileNotFoundUtil extends ConfigFileUtil {
        @Override
        public InputStream getStream(RunConfigurationBase config, String path) throws FileNotFoundException {
            throw new FileNotFoundException("");
        }
    }

}
