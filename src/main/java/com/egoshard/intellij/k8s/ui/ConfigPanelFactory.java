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

import com.egoshard.intellij.k8s.parser.ConfigParser;
import com.egoshard.intellij.k8s.support.ConfigFileUtil;
import com.intellij.execution.configurations.RunConfigurationBase;

import java.util.Map;

public class ConfigPanelFactory<T extends RunConfigurationBase> {

    public ConfigPanel getPanel(Map<String, ConfigParser> parsers, ConfigFileUtil fileUtil, T config) {
        return new ConfigPanel<>(parsers, fileUtil, config);
    }

}
