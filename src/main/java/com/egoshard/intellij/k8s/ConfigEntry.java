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

import com.egoshard.intellij.k8s.parser.ConfigParser;
import com.egoshard.intellij.k8s.support.ConfigFileUtil;
import com.intellij.execution.configurations.RunConfigurationBase;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.egoshard.intellij.k8s.parser.ConfigParser.KEY_KIND;

public class ConfigEntry {

    private static final String MSG_SOURCE_NULL = "The provided configuration file has no data elements in it.";
    private static final String MSG_SOURCE_WRONGKIND = "The provided configuration cannot be parsed. No parser exists for kind: [%s].";
    private static final String MSG_SOURCE_INVALID = "Unable to read YAML file, [%s], invalid file";
    private static final String MSG_SOURCE_NOT_EXIST = "Unable to read YAML file, [%s], file not found.";

    private final Map<String, ConfigParser> parsers;
    private final ConfigFileUtil fileUtil;
    private final RunConfigurationBase config;
    private String path;

    public ConfigEntry(Map<String, ConfigParser> parsers, ConfigFileUtil fileUtil, RunConfigurationBase config, String path) {
        this.parsers = parsers;
        this.fileUtil = fileUtil;
        this.config = config;
        this.path = path;
    }

    public Map<String, String> parse() throws IOException, ConfigFileException {
        Map<String, Object> source = getYaml();
        if (source == null || source.size() == 0) {
            throw new IllegalArgumentException(MSG_SOURCE_NULL);
        }
        String kind = (String) source.get(KEY_KIND);
        if (parsers.containsKey(kind)) {
            return parsers.get(kind).parse(source);
        } else {
            throw new ConfigFileException(String.format(MSG_SOURCE_WRONGKIND, kind));
        }
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean validate() {
        if (fileUtil == null) {
            throw new IllegalStateException("A required configuration is not configured, File Utility.");
        }
        return fileUtil.exists(config, path);
    }

    private Map<String, Object> getYaml() throws IOException, ConfigFileException {
        try (InputStream input = fileUtil.getStream(config, path)) {
            return new Yaml().load(input);
        } catch (ClassCastException e) {
            throw new ConfigFileException(String.format(MSG_SOURCE_INVALID, path), e);
        } catch (FileNotFoundException ex) {
            throw new ConfigFileException(String.format(MSG_SOURCE_NOT_EXIST, path), ex);
        }
    }

}
