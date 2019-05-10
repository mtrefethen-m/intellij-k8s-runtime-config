## intelliJ Kubernetes RunConfiguration

This plugin loads Kubernetes ConfigMap, Secret and String Data values as environment variables in an IntelliJ run configuration. This allows for development of Kubernetes deployable modules without the need to run Kubernetes on local development environments which may not have enough resources.

### Plugin Installation

- Using intelliJ plugin management on Windows:
  - <kbd>File</kbd> -> <kbd>Settings</kbd> -> <kbd>Plugins</kbd> -> <kbd>Marketplace</kbd> -> <kbd>Search for "Kubernetes" or "Runtime Configuration"</kbd> -> <kbd>Install Plugin</kbd>
- Using intelliJ plugin management on MacOS:
  - <kbd>Preferences</kbd> -> <kbd>Settings</kbd> -> <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> -> <kbd>Search for "Kubernetes" or "Runtime Configuration"</kbd> -> <kbd>Install Plugin</kbd>

Restart intelliJ.

### Usage

- Run -> Edit Configurations
- Select or create a new configuration
- "K8s Config" tab
- Select "Enable"
- Add Kubernetes configuration files
- Run configuration



During run configuration execution, configuration files will be parsed in list order and their values injected as environment variables to the executing module. Configuration files should be ordered in the order they are consumed within deployments from generic to specific. Configuration keys with matching names in specific files will override previous values exactly as the would during a Kubernetes deployment