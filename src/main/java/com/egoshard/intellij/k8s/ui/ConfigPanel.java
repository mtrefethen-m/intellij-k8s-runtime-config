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
import com.egoshard.intellij.k8s.ConfigSettings;
import com.egoshard.intellij.k8s.parser.ConfigParser;
import com.egoshard.intellij.k8s.support.ConfigFileUtil;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AnActionButtonUpdater;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

class ConfigPanel<T extends RunConfigurationBase> extends JPanel {

    private static final String MSG_NO_FILE_SELECTED = "No file selected";
    private static final String MSG_SELECT_K8S_FILE = "Select Kubernetes ConfigMap or Secret File";
    private static final String MSG_ENABLE = "Enable";
    private final Map<String, ConfigParser> parsers;
    private final ConfigFileUtil fileUtil;
    private final RunConfigurationBase config;
    private final JCheckBox checkBox;
    private final ListTableModel<ConfigEntry> files;
    private final TableView<ConfigEntry> table;

    ConfigPanel(Map<String, ConfigParser> parsers, ConfigFileUtil fileUtil, T config) {

        this.parsers = parsers;
        this.fileUtil = fileUtil;
        this.config = config;

        // data model
        ColumnInfo<ConfigEntry, String> file = new ConfigColumn();

        // generate table
        files = new ListTableModel<>(file);
        table = new TableView<>(files);
        table.getEmptyText().setText(MSG_NO_FILE_SELECTED);

        table.setColumnSelectionAllowed(false);
        table.setShowGrid(false);
        table.setDragEnabled(true);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // enable checkbox
        checkBox = new JCheckBox(MSG_ENABLE);
        checkBox.addActionListener(e -> table.setEnabled(checkBox.isSelected()));

        // action bar
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        final AnActionButtonUpdater updater = event -> checkBox.isSelected();

        decorator.setAddAction(button -> doAddAction(table, files))
                .setAddActionUpdater(updater)
                .setRemoveActionUpdater(event -> updater.isEnabled(event) && table.getSelectedRowCount() >= 1);

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, JBUI.scale(5), JBUI.scale(5)));
        checkboxPanel.add(checkBox);

        JPanel decoratorPanel = decorator.createPanel();
        Dimension size = new Dimension(-1, 150);
        decoratorPanel.setMinimumSize(size);
        decoratorPanel.setPreferredSize(size);

        setLayout(new BorderLayout());
        add(checkboxPanel, BorderLayout.NORTH);
        add(decoratorPanel, BorderLayout.CENTER);

    }

    private void doAddAction(final TableView<ConfigEntry> table, final ListTableModel<ConfigEntry> model) {

        final FileChooserDescriptor chooserDescriptor = FileChooserDescriptorFactory
                .createSingleFileNoJarsDescriptor()
                .withTitle(MSG_SELECT_K8S_FILE);

        VirtualFile path = FileChooser.chooseFile(chooserDescriptor, config.getProject(), null);
        if (path != null) {
            String selectedPath = path.getPath();
            String baseDir = config.getProject().getBaseDir().getPath();
            if (selectedPath.startsWith(baseDir)) {
                selectedPath = selectedPath.substring(baseDir.length() + 1);
            }
            // Generate new data
            ArrayList<ConfigEntry> newModelData = new ArrayList<>(model.getItems());
            newModelData.add(new ConfigEntry(parsers, fileUtil, config, selectedPath));
            model.setItems(newModelData);
            int index = model.getRowCount() - 1;
            // Fire data update events
            model.fireTableRowsInserted(index, index);
            table.setRowSelectionInterval(index, index);

        }
    }

    ConfigSettings getSettings() {
        return new ConfigSettings(checkBox.isSelected(), files.getItems());
    }

    void setSettings(ConfigSettings settings) {
        this.checkBox.setSelected(settings.isEnabled());
        this.table.setEnabled(settings.isEnabled());
        files.setItems(settings.getEntries());
    }

}