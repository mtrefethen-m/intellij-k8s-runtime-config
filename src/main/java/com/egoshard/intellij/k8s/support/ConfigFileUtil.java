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
package com.egoshard.intellij.k8s.support;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Utility for file handling
 */
public class ConfigFileUtil {

    private static final Logger logger = Logger.getInstance(ConfigFileUtil.class);

    private File getFile(RunConfigurationBase config, String path) {

        logger.debug("Retrieving K8s config file, {}", path);
        if (!FileUtil.isAbsolute(path)) {
            VirtualFile virtual = config.getProject().getBaseDir().findFileByRelativePath(path);
            if (virtual != null) {
                return new File(virtual.getPath());
            }
        }
        return new File(path);
    }

    public InputStream getStream(RunConfigurationBase config, String path) throws FileNotFoundException {
        return new FileInputStream(getFile(config, path));
    }

    public boolean exists(RunConfigurationBase config, String path) {
        return getFile(config, path).exists();
    }

}
