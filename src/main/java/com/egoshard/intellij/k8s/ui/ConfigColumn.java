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
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ConfigColumn extends ColumnInfo<ConfigEntry, String> {

    private static final String MSG_FILE_NOT_FOUND = "File not found.";
    private static final String MSG_PATH = "Path";

    ConfigColumn() {
        super(MSG_PATH);
    }

    @Override
    public boolean isCellEditable(ConfigEntry entry) {
        return true;
    }

    @Nullable
    @Override
    public TableCellEditor getEditor(ConfigEntry entry) {
        return new DefaultCellEditor(new JBTextField());
    }

    @Override
    public void setValue(ConfigEntry entry, String value) {
        entry.setPath(value == null ? "" : value);
    }

    @Nullable
    @Override
    public String valueOf(ConfigEntry entry) {
        return entry.getPath();
    }

    @Override
    public TableCellRenderer getRenderer(final ConfigEntry entry) {
        return new DefaultTableCellRenderer() {
            @NotNull
            @Override
            public Component getTableCellRendererComponent(@NotNull JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column) {
                final Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(null);
                if (entry.getPath() == null) {
                    setText("<>");
                    setForeground(UIUtil.getLabelDisabledForeground());
                } else {
                    setText(entry.getPath());
                    if (!entry.validate()) {
                        setForeground(JBColor.YELLOW);
                        setToolTipText(MSG_FILE_NOT_FOUND);
                    }
                }
                return renderer;
            }
        };
    }

}
