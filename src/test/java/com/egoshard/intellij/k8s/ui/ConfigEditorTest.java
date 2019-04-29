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
package com.egoshard.intellij.k8s.ui;

import com.egoshard.intellij.k8s.ConfigEntry;
import com.egoshard.intellij.k8s.ConfigFileException;
import com.egoshard.intellij.k8s.ConfigSettings;
import com.egoshard.intellij.k8s.TestUtils;
import com.egoshard.intellij.k8s.parser.ConfigMapParser;
import com.egoshard.intellij.k8s.parser.ConfigParser;
import com.egoshard.intellij.k8s.parser.SecretParser;
import com.egoshard.intellij.k8s.support.ConfigFileUtil;
import com.google.common.collect.ImmutableMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.Key;
import org.jdom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.MockUtil;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Test configuration editor")
class ConfigEditorTest extends TestUtils {

    private static final String PATH = "path";
    private static final String ELEMENT_ENTRIES = getFinalStaticString(ConfigEditor.class, "ELEMENT_ENTRIES");
    private static final String ELEMENT_ENTRY = getFinalStaticString(ConfigEditor.class, "ELEMENT_ENTRY");
    private static final String FIELD_ENABLED = getFinalStaticString(ConfigEditor.class, "FIELD_ENABLED");
    private static final String FIELD_PATH = getFinalStaticString(ConfigEditor.class, "FIELD_PATH");
    private static final String SERIALIZATION_ID = getFinalStaticString(ConfigEditor.class, "SERIALIZATION_ID");
    private static final String TITLE = getFinalStaticString(ConfigEditor.class, "TITLE");
    private static final String MSG_PATH_INVALID = getFinalStaticString(ConfigEditor.class, "MSG_PATH_INVALID");
    private static final String YAML_VALUE = "{\"kind\":\"ConfigMap\",\"data\":{\"test_key\":\"test_data\"}}";

    private static final String TEST_CONFIG_MAP = "TestConfigMap.yml";
    private static final String TEST_SECRET = "TestSecretData.yml";

    private ConfigEditor<RunConfigurationBase> editor;

    @Mock
    private Map<String, ConfigParser> mockConfigParsers;

    @Mock
    private ConfigFileUtil mockFileUtil;

    @Mock
    private RunConfigurationBase mockConfig;

    @Mock
    private ConfigPanelFactory<RunConfigurationBase> mockPanelFactory;

    @Mock
    private ConfigPanel mockPanel;

    @Captor
    private ArgumentCaptor<ConfigSettings> settingsCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        editor = new ConfigEditor<>(mockPanelFactory, mockConfig);
    }

    @DisplayName("Tests set and retrieve of serialization ID")
    @Test
    void testSerialization() {
        assertEquals(SERIALIZATION_ID, ConfigEditor.getSerializationId());
    }

    @DisplayName("Tests set and retrieve of editor title")
    @Test
    void testTitle() {
        assertEquals(TITLE, ConfigEditor.getTitle());
    }

    @DisplayName("Tests static file utility setting")
    @Test
    void testFileUtil() {
        assertFalse(MockUtil.isMock(ConfigEditor.getFileUtil()));
        ConfigEditor.setFileUtil(mockFileUtil);
        assertTrue(MockUtil.isMock(ConfigEditor.getFileUtil()));
    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration writing")
    @Test
    void testWrite() {

        Element element = new Element("test");
        ConfigSettings settings = new ConfigSettings(true, Arrays.asList(
                new ConfigEntry(mockConfigParsers, mockFileUtil, mockConfig, PATH + "1"),
                new ConfigEntry(mockConfigParsers, mockFileUtil, mockConfig, PATH + "2")
        ));
        doReturn(settings).when(mockConfig).getUserData(any(Key.class));

        ConfigEditor.write(mockConfig, element);

        assertTrue(element.getChildren().stream().filter((child) -> child.getName().equals(ELEMENT_ENTRIES)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No entries found."))
                .getChildren().stream().allMatch((entry) -> entry.getName().equals(ELEMENT_ENTRY)));

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration reading")
    @Test
    void testRead() {

        Element element = new Element("test");

        JDOMExternalizerUtil.writeField(element, FIELD_ENABLED, "true");

        Element entryElement = new Element(ELEMENT_ENTRY);
        entryElement.setAttribute(FIELD_PATH, PATH);

        Element entriesElement = new Element(ELEMENT_ENTRIES);
        entriesElement.addContent(entryElement);

        element.addContent(entriesElement);

        ConfigEditor.read(mockConfig, element);
        verify(mockConfig).putUserData(any(Key.class), settingsCaptor.capture());

        ConfigSettings settings = settingsCaptor.getValue();
        assertTrue(settings.isEnabled());
        assertEquals(1, settings.getEntries().size());
        assertEquals(PATH, settings.getEntries().get(0).getPath());

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration validation")
    @Test
    void testValidate() {

        ConfigEntry mockEntry = mock(ConfigEntry.class);
        doReturn(true).when(mockEntry).validate();
        ConfigSettings settings = new ConfigSettings(true, Collections.singletonList(
                mockEntry
        ));
        doReturn(settings).when(mockConfig).getUserData(any(Key.class));
        ConfigEditor.validate(mockConfig);

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration validation failure")
    @Test
    void testValidateFail() {

        String badPath = "badpath";
        ConfigEntry mockEntry = mock(ConfigEntry.class);
        doReturn(false).when(mockEntry).validate();
        doReturn(badPath).when(mockEntry).getPath();
        ConfigSettings settings = new ConfigSettings(true, Collections.singletonList(
                mockEntry
        ));
        doReturn(settings).when(mockConfig).getUserData(any(Key.class));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ConfigEditor.validate(mockConfig));
        assertEquals(String.format(MSG_PATH_INVALID, badPath), exception.getMessage());

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration parsing")
    @Test
    void testParse() throws ExecutionException, FileNotFoundException {

        String key = "key";
        String value = "value";

        ConfigParser mockParser = mock(ConfigParser.class);
        Map<String, ConfigParser> parsers = ImmutableMap.of(
                ConfigParser.Kind.CONFIGMAP.getKey(), mockParser,
                ConfigParser.Kind.SECRET.getKey(), mockParser
        );

        Map<String, String> parsedConfig = new HashMap<>();
        parsedConfig.put(key, value);
        when(mockParser.parse(anyMap()))
                .thenReturn(parsedConfig)
                .thenReturn(parsedConfig);

        ConfigSettings settings = new ConfigSettings(true, Arrays.asList(
                new ConfigEntry(parsers, mockFileUtil, mockConfig, PATH + "1"),
                new ConfigEntry(parsers, mockFileUtil, mockConfig, PATH + "2")
        ));
        doReturn(settings).when(mockConfig).getUserData(any(Key.class));
        when(mockFileUtil.getStream(any(RunConfigurationBase.class), anyString()))
                .thenReturn(new ByteArrayInputStream(YAML_VALUE.getBytes()))
                .thenReturn(new ByteArrayInputStream(YAML_VALUE.getBytes()));

        Map<String, String> params = ConfigEditor.parse(mockConfig, new HashMap<>());
        assertEquals(value, params.get(key));

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests parsing of config map file")
    @Test
    void testParseConfigMap() throws IOException, ExecutionException {

        Map<String, ConfigParser> parsers = ImmutableMap.of(
                ConfigParser.Kind.CONFIGMAP.getKey(), new ConfigMapParser()
        );

        when(mockFileUtil.getStream(any(RunConfigurationBase.class), anyString()))
                .thenReturn(ClassLoader.getSystemResourceAsStream(TEST_CONFIG_MAP));

        ConfigSettings settings = new ConfigSettings(true, Collections.singletonList(
                new ConfigEntry(parsers, mockFileUtil, mockConfig, PATH + "1")
        ));
        doReturn(settings).when(mockConfig).getUserData(any(Key.class));

        Map<String, String> params = new HashMap<>();
        Map<String, String> result = ConfigEditor.parse(mockConfig, params);
        assertEquals("value", result.get("STRING_TEST_DATA_VALUE_QUOTED"));
        assertEquals("value", result.get("STRING_TEST_DATA_VALUE_NONQUOTED"));
        assertEquals("2", result.get("STRING_TEST_DATA_VALUE_2"));

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests parsing of secret file")
    @Test
    void testParseSecret() throws IOException, ExecutionException {

        Map<String, ConfigParser> parsers = ImmutableMap.of(
                ConfigParser.Kind.SECRET.getKey(), new SecretParser()
        );

        when(mockFileUtil.getStream(any(RunConfigurationBase.class), anyString()))
                .thenReturn(ClassLoader.getSystemResourceAsStream(TEST_SECRET));

        ConfigSettings settings = new ConfigSettings(true, Collections.singletonList(
                new ConfigEntry(parsers, mockFileUtil, mockConfig, PATH + "1")
        ));
        doReturn(settings).when(mockConfig).getUserData(any(Key.class));

        Map<String, String> params = new HashMap<>();
        Map<String, String> result = ConfigEditor.parse(mockConfig, params);
        assertEquals("value", result.get("STRING_TEST_DATA_VALUE_NONQUOTED"));
        assertEquals("2", result.get("STRING_TEST_DATA_VALUE_2"));

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration parse throws exception")
    @Test
    void testParseConfigFileException() throws IOException, ConfigFileException {

        editor = new ConfigEditor<>(new TestPanelFactory(), mockConfig);
        ConfigEntry mockEntry = mock(ConfigEntry.class);
        doThrow(new ConfigFileException()).when(mockEntry).parse();
        ConfigSettings settings = new ConfigSettings(true, Collections.singletonList(
                mockEntry
        ));
        doReturn(settings).when(mockConfig).getUserData(any(Key.class));

        ExecutionException execution = assertThrows(ExecutionException.class, () -> ConfigEditor.parse(mockConfig, new HashMap<>()));
        assertEquals(ConfigFileException.class, execution.getCause().getClass());

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration disabled returns staring config")
    @Test
    void testParseConfigDisabled() throws ExecutionException, IOException, ConfigFileException {

        ConfigEntry mockEntry = mock(ConfigEntry.class);
        doThrow(new ConfigFileException("Exception")).when(mockEntry).parse();
        ConfigSettings settings = new ConfigSettings(false, Collections.singletonList(
                mockEntry
        ));
        doReturn(settings).when(mockConfig).getUserData(any(Key.class));

        Map<String, String> params = new HashMap<>();
        params.put("key", "value");

        Map<String, String> returned = ConfigEditor.parse(mockConfig, params);
        assertEquals(1, returned.size());
        assertEquals("value", returned.get("key"));

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration reset from panel")
    @Test
    void testReset() {

        editor = new ConfigEditor<>(new TestPanelFactory(), mockConfig);
        ConfigSettings settings = new ConfigSettings(true, Arrays.asList(
                new ConfigEntry(mockConfigParsers, mockFileUtil, mockConfig, PATH + "1"),
                new ConfigEntry(mockConfigParsers, mockFileUtil, mockConfig, PATH + "2")
        ));
        doReturn(settings).when(mockConfig).getUserData(any(Key.class));

        ConfigPanel panel = (ConfigPanel) editor.createEditor();
        assertNull(panel.getSettings());
        editor.resetEditorFrom(mockConfig);

        verify(mockPanel).setSettings(settingsCaptor.capture());
        ConfigSettings actualSettings = settingsCaptor.getValue();
        assertTrue(actualSettings.isEnabled());
        assertEquals(2, actualSettings.getEntries().size());
        assertEquals(PATH + "1", actualSettings.getEntries().get(0).getPath());
        assertEquals(PATH + "2", actualSettings.getEntries().get(1).getPath());

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration reset from panel")
    @Test
    void testResetNull() {

        editor = new ConfigEditor<>(new TestPanelFactory(), mockConfig);
        doReturn(null).when(mockConfig).getUserData(any(Key.class));
        ConfigPanel panel = (ConfigPanel) editor.createEditor();
        assertNull(panel.getSettings());
        editor.resetEditorFrom(mockConfig);
        assertNull(panel.getSettings());

    }

    @SuppressWarnings("unchecked")
    @DisplayName("Tests configuration apply from panel")
    @Test
    void testApply() {

        editor = new ConfigEditor<>(new TestPanelFactory(), mockConfig);
        ConfigSettings settings = new ConfigSettings(false, Arrays.asList(
                new ConfigEntry(mockConfigParsers, mockFileUtil, mockConfig, PATH + "1"),
                new ConfigEntry(mockConfigParsers, mockFileUtil, mockConfig, PATH + "2")
        ));

        doReturn(settings).when(mockPanel).getSettings();
        editor.applyEditorTo(mockConfig);

        verify(mockConfig).putUserData(any(Key.class), settingsCaptor.capture());
        ConfigSettings actualSettings = settingsCaptor.getValue();
        assertFalse(actualSettings.isEnabled());
        assertEquals(2, actualSettings.getEntries().size());
        assertEquals(PATH + "1", actualSettings.getEntries().get(0).getPath());
        assertEquals(PATH + "2", actualSettings.getEntries().get(1).getPath());

    }

    class TestPanelFactory extends ConfigPanelFactory {
        @Override
        public ConfigPanel getPanel(Map parsers, ConfigFileUtil fileUtil, RunConfigurationBase config) {
            return mockPanel;
        }
    }

}
