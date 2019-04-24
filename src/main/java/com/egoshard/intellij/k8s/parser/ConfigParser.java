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

import java.util.Map;

/**
 * Common interface for parsing configuration files into discrete environment variable key/value pairs.
 */
public interface ConfigParser {

    /**
     * Parses a source map matching the YAML structure of a Kubernetes configuration file.
     *
     * @param source configuration map
     * @return map key/value pairs
     */
    Map<String, String> parse(Map<String, Object> source);

    /**
     * Indicates the specific kind of Kubernetes configuration the parser is intended to operate on.
     *
     * @return enumerated kind value.
     */
    Kind getSupportedKind();

    /**
     * Kind enumeration
     */
    enum Kind {

        CONFIGMAP("ConfigMap"),
        SECRET("Secret");

        private String key;

        Kind(String key) {
            this.key = key;
        }

        /**
         * Used to match kind values in Kubernetes configuration files to kind enumerations.
         *
         * @return the matching Kubernetes configuration kind key.
         */
        public String getKey() {
            return key;
        }

    }

}
