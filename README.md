## IntelliJ Kubernetes RunConfiguration
[IntelliJ Plugin Repository](https://plugins.jetbrains.com/plugin/12394-kubernetes-runtime-configuration)


This plugin loads Kubernetes ConfigMap, Secret and String Data values as environment variables in an IntelliJ run configuration. This allows for development of Kubernetes deployable modules without the need to run Kubernetes on local development environments which may not have enough resources.

### Version Compatibility
| Branch | IntelliJ |
|:---:|:---:|
| 1.0.x   | 2018+    |
| 1.1.x   | 2019+    |
| 1.2.x   | 2019.2+  |

### Plugin Installation

- Using intelliJ plugin management on Windows:
  - <kbd>File</kbd> -> <kbd>Settings</kbd> -> <kbd>Plugins</kbd> -> <kbd>Marketplace</kbd> -> <kbd>Search for "Kubernetes" or "Runtime Configuration"</kbd> -> <kbd>Install Plugin</kbd>
- Using intelliJ plugin management on MacOS:
  - <kbd>Preferences</kbd> -> <kbd>Settings</kbd> -> <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> -> <kbd>Search for "Kubernetes" or "Runtime Configuration"</kbd> -> <kbd>Install Plugin</kbd>

Restart intelliJ.

### Usage

- <kbd>Run</kbd> -> <kbd>Edit Configurations</kbd>
- Select or create a new configuration
- <kbd>K8s Config</kbd> tab
- <kbd>Enable</kbd> enables or disables variable injection for a config 
- Add Kubernetes ConfigMap or Secret configuration files
- <kbd>Apply</kbd> or <kbd>Ok</kbd>

During run configuration execution, configuration files will be parsed in list order and their values injected as environment variables to the executing module. Configuration files should be ordered in the order they are consumed within deployments from generic to specific. Configuration keys with matching names in specific files will override previous values exactly as the would during a Kubernetes deployment
