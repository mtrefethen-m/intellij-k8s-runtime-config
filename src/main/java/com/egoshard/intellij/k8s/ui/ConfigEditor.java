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
import com.egoshard.intellij.k8s.parser.ConfigMapParser;
import com.egoshard.intellij.k8s.parser.ConfigParser;
import com.egoshard.intellij.k8s.parser.SecretParser;
import com.egoshard.intellij.k8s.support.ConfigFileUtil;
import com.google.common.collect.ImmutableMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.Key;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

/**
 * User interface editor provider.
 */
public class ConfigEditor<T extends RunConfigurationBase> extends SettingsEditor<T> {

    private static final Logger logger = Logger.getInstance(ConfigEditor.class);

    private static final String TITLE = "K8s Config";
    private static final String SERIALIZATION_ID = "com.egoshard.intellij.k8s-runtime-config-plugin";
    private static final String KEY_NAME = TITLE + " Settings";
    private static final Key<ConfigSettings> SETTING_KEY = new Key<>(KEY_NAME);

    private static final String ELEMENT_ENTRIES = "entries";
    private static final String ELEMENT_ENTRY = "entry";
    private static final String FIELD_ENABLED = "ENABLED";
    private static final String FIELD_PATH = "path";
    private static final Map<String, ConfigParser> PARSERS = ImmutableMap.of(
            ConfigParser.Kind.CONFIGMAP.getKey(), new ConfigMapParser(),
            ConfigParser.Kind.SECRET.getKey(), new SecretParser()
    );
    private static final String MSG_PATH_INVALID = "Kubernetes configuration has an invalid path, [%s]. This may have been caused by using shared configuration files in conjunction with a missing Yaml configuration file.";
    private static final String MSG_PARSE_FAIL = "Unable to parse configuration file, [%s]. %s";

    private static ConfigFileUtil fileUtil;
    private final ConfigPanel panel;

    public ConfigEditor(ConfigPanelFactory<T> factory, T config) {
        this.panel = factory.getPanel(PARSERS, getFileUtil(), config);
    }

    static ConfigFileUtil getFileUtil() {
        if (fileUtil == null) {
            fileUtil = new ConfigFileUtil();
        }
        return fileUtil;
    }

    static void setFileUtil(ConfigFileUtil fileUtil) {
        ConfigEditor.fileUtil = fileUtil;
    }

    public static String getTitle() {
        return TITLE;
    }

    public static String getSerializationId() {
        return SERIALIZATION_ID;
    }

    /**
     * Saves the settings of this extension to the run configuration XML element.
     *
     * @param config  the run configuration being serialized.
     * @param element the element into which the settings should be persisted,
     */
    public static void write(RunConfigurationBase config, Element element) {
        Optional.ofNullable(config.getUserData(SETTING_KEY)).ifPresent(settings -> {
            JDOMExternalizerUtil.writeField(element, FIELD_ENABLED, Boolean.toString(settings.isEnabled()));
            final Element entriesElement = new Element(ELEMENT_ENTRIES);
            for (ConfigEntry entry : settings.getEntries()) {
                final Element entryElement = new Element(ELEMENT_ENTRY);
                String path = entry.getPath();
                if (path != null) {
                    entryElement.setAttribute(FIELD_PATH, entry.getPath());
                }
                entriesElement.addContent(entryElement);
            }
            element.addContent(entriesElement);
        });
    }

    /**
     * Loads the settings of this extension from the run configuration XML
     * <p>
     * element. In memory, the settings can be placed into the
     * userdata of the run configuration.
     *
     * @param config  the run configuration being deserialized.
     * @param element the element with persisted settings.
     */
    public static void read(RunConfigurationBase config, Element element) {
        List<ConfigEntry> entries = new ArrayList<>();
        Optional.ofNullable(element.getChild(ELEMENT_ENTRIES))
                .ifPresent(entry -> entry.getChildren(ELEMENT_ENTRY).stream()
                        .map(child -> new ConfigEntry(PARSERS, getFileUtil(), config, child.getAttributeValue(FIELD_PATH)))
                        .forEach(entries::add));
        config.putUserData(
                SETTING_KEY,
                new ConfigSettings(
                        Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, FIELD_ENABLED)),
                        entries));
    }


    /**
     * Validate extensions after general configuration validation passed.
     *
     * @param config the run configuration being validated.
     */
    public static void validate(RunConfigurationBase config) {
        Optional.ofNullable(config.getUserData(SETTING_KEY)).ifPresent(settings -> {
            if (settings.isEnabled()) {
                settings.getEntries().stream().filter(entry -> !entry.validate()).findFirst()
                        .ifPresent(entry -> logger.error(String.format(MSG_PATH_INVALID, entry.getPath())));
            }
        });
    }


    /**
     * Parses all configuration entries in settings and builds a variable map.
     *
     * @param config run configuration
     * @param params parameter map returned if settings are disable or null
     * @return configuration map
     */
    public static Map<String, String> parse(RunConfigurationBase config, Map<String, String> params) throws ExecutionException {
        ConfigSettings settings = config.getUserData(SETTING_KEY);
        if (settings != null && settings.isEnabled()) {
            Map<String, String> result = new HashMap<>();
            for (ConfigEntry entry : settings.getEntries()) {
                try {
                    result.putAll(entry.parse());
                } catch (IllegalArgumentException | IOException | ConfigFileException ex) {
                    throw new ExecutionException(String.format(MSG_PARSE_FAIL, entry.getPath(), ex.getMessage()), ex);
                }
            }
            return result;
        }
        return params;
    }

    @Override
    protected void resetEditorFrom(@NotNull T configuration) {
        Optional.ofNullable(configuration.getUserData(SETTING_KEY)).ifPresent(this.panel::setSettings);
    }

    @Override
    protected void applyEditorTo(@NotNull T configuration) {
        configuration.putUserData(SETTING_KEY, this.panel.getSettings());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return this.panel;
    }

}
