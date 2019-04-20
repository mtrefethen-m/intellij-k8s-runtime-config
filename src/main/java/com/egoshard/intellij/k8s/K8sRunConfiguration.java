package com.egoshard.intellij.k8s;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class K8sRunConfiguration extends RunConfigurationExtension {

    private static final Logger logger = Logger.getInstance(K8sRunConfiguration.class);

    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration, JavaParameters params, RunnerSettings runnerSettings) throws ExecutionException {

    }

    @Override
    public boolean isEnabledFor(@NotNull RunConfigurationBase applicableConfiguration, @Nullable RunnerSettings runnerSettings) {
        return true;
    }

    /**
     * @param configuration Run configuration
     * @return True if extension in general applicable to given run configuration - just to attach settings tab, etc. But extension may be
     * turned off in its settings. E.g. RCov in general available for given run configuration, but may be turned off.
     */
    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase<?> configuration) {
        return true;
    }

}
