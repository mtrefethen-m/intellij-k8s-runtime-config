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

import com.egoshard.intellij.k8s.ui.ConfigEditor;
import com.egoshard.intellij.k8s.ui.ConfigPanelFactory;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Run configuration extension for converting Kubernetes configuration files into runtime environment variables.
 */
class K8sRunConfiguration extends RunConfigurationExtension {

    private static final Logger logger = Logger.getInstance(K8sRunConfiguration.class);

    @Override
    public boolean isEnabledFor(@NotNull RunConfigurationBase applicableConfiguration, @Nullable RunnerSettings runnerSettings) {
        return true;
    }

    /**
     * Returns the ID used to serialize the settings.
     *
     * @return the serialization ID (must be unique across all run configuration extensions).
     */
    @NotNull
    @Override
    protected String getSerializationId() {
        return ConfigEditor.getSerializationId();
    }

    /**
     * Loads the settings of this extension from the run configuration XML element. In memory, the settings can be placed into the
     * user data of the run configuration.
     *
     * @param runConfiguration the run configuration being deserialized.
     * @param element          the element with persisted settings.
     */
    @Override
    protected void readExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) {
        ConfigEditor.read(runConfiguration, element);
    }

    /**
     * Saves the settings of this extension to the run configuration XML element.
     *
     * @param runConfiguration the run configuration being serialized.
     * @param element          the element into which the settings should be persisted,
     */
    @Override
    protected void writeExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) {
        ConfigEditor.write(runConfiguration, element);
    }

    /**
     * Creates an editor for the settings of this extension. The editor is displayed as an additional tab of the run configuration options
     * in the Run/Debug Configurations dialog.
     *
     * @param configuration the configuration being edited.
     * @return the editor component, or null if this extension doesn't provide any UI for editing the settings.
     */
    @Nullable
    @Override
    protected <P extends RunConfigurationBase<?>> SettingsEditor<P> createEditor(@NotNull P configuration) {
        return new ConfigEditor<>(new ConfigPanelFactory<>(), configuration);
    }

    /**
     * Returns the title of the tab in which the settings editor is displayed.
     *
     * @return the editor tab title, or null if this extension doesn't provide any UI for editing the settings.
     */
    @Nullable
    @Override
    protected String getEditorTitle() {
        return ConfigEditor.getTitle();
    }

    /**
     * Validate extensions after general configuration validation passed
     *
     * @param configuration run configuration implementation
     * @param isExecution   true if the configuration is about to be executed, false if the configuration settings are being edited.
     */
    @Override
    protected void validateConfiguration(@NotNull RunConfigurationBase configuration, boolean isExecution) {
        ConfigEditor.validate(configuration);
    }

    /**
     * Updates environment parameters based on parsed configuration files
     */
    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration, JavaParameters params, RunnerSettings runnerSettings) throws ExecutionException {
        logger.info("Kubernetes configuration injection commencing.");
        params.setEnv(ConfigEditor.parse(configuration, new HashMap<>(params.getEnv())));
        StringBuilder builder = new StringBuilder();
        builder.append("Injected parameters:\n");
        for (Map.Entry<String, String> stringStringEntry : params.getEnv().entrySet()) {
            builder.append(stringStringEntry.getKey()).append("=").append(stringStringEntry.getValue()).append("\n");
        }
        logger.info(builder.toString());
    }

    /**
     * @param configuration Run configuration
     * @return True if extension in general applicable to given run configuration - just to attach settings tab, etc. But extension may be
     * turned off in its settings. E.g. RCov in general available for given run configuration, but may be turned off.
     */
    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        return true;
    }

}
