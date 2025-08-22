/*
 By Samuel Cousins, adapted from https://stackoverflow.com/a/8012857
 For SmartWithIt pilot, August 2025
 All rights to SmartWithIt, Inc.
 */

package me.cousinss.graphic;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;

public class GraphicDemo {

    public static Color getGradientColor(Color endColor, float factor) {
        if (factor < 0.0f) factor = 0.0f;
        if (factor > 1.0f) factor = 1.0f;

        // Start color is white (RGB: 255, 255, 255)
        int startR = 255;
        int startG = 255;
        int startB = 255;

        // End color's RGB components
        int endR = endColor.getRed();
        int endG = endColor.getGreen();
        int endB = endColor.getBlue();

        // Linearly interpolate each RGB component
        int interpolatedR = (int) (startR + (endR - startR) * factor);
        int interpolatedG = (int) (startG + (endG - startG) * factor);
        int interpolatedB = (int) (startB + (endB - startB) * factor);

        // Ensure values are within the valid 0-255 range
        interpolatedR = Math.max(0, Math.min(255, interpolatedR));
        interpolatedG = Math.max(0, Math.min(255, interpolatedG));
        interpolatedB = Math.max(0, Math.min(255, interpolatedB));

        return new Color(interpolatedR, interpolatedG, interpolatedB);
    }

    private final JTable table;

    public GraphicDemo(float[][] v1, float[][] v2, float feedProp, int maxDay, int endDate) {
        NumberFormat percentFormatter = NumberFormat.getPercentInstance();
        percentFormatter.setMaximumFractionDigits(0);
        JFrame frame = new JFrame("("+endDate + " day trial) Average days late, with avg. " + String.format("%.2f",feedProp*maxDay) + " questions/lesson of " + maxDay + "/day");
        String[] colHeaders = new String[v1[0].length-1];
        for(int i = 0; i < v1[0].length-1; i++) {
            colHeaders[i] = (i == 0 ? "F: " : "") + percentFormatter.format(v1[0][i + 1]);
        }
        table = new JTable(new DefaultTableModel(colHeaders, v1.length - 1));
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                table.setValueAt(String.format("%.2f", v1[i + 1][j + 1]) + "d, " + percentFormatter.format(1f-v2[i + 1][j + 1]), i, j);
            }
        }
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, column);

                c.setBackground(table.getBackground());
                // You can add more complex logic here based on row, column, or cell value
                // For instance, coloring based on cell value:
                if (value != null) {
                    float vF = 0;
                    try {
                        vF = Float.parseFloat(value.toString().split("d")[0]);
                    } catch (NumberFormatException ignored) {}
                    c.setBackground(getGradientColor(Color.RED, vF/20f));
                }
                return c;
            }
        });
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public int getColumnCount() {
                return 1;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
            @Override
            public int getRowCount() {
                return table.getRowCount();
            }
            @Override
            public Class<?> getColumnClass(int colNum) {
                if (colNum == 0) {
                    return String.class;
                }
                return super.getColumnClass(colNum);
            }
        };
        JTable headerTable = new JTable(model);
        for (int i = 0; i < table.getRowCount(); i++) {
            headerTable.setValueAt((i == 0 ? "S: " : "") + percentFormatter.format(v1[i+1][0]), i, 0);
        }
        headerTable.setShowGrid(false);
        headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        headerTable.setPreferredScrollableViewportSize(new Dimension(50, 0));
        headerTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        headerTable.getColumnModel().getColumn(0).setCellRenderer((x, value, isSelected, hasFocus, row, column) -> {
            Component component = table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, value, false, false, -1, -2);
            ((JLabel) component).setHorizontalAlignment(SwingConstants.CENTER);
            return component;
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setRowHeaderView(headerTable);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(scrollPane);
        frame.pack();
        frame.setLocation(150, 150);
        frame.setVisible(true);
    }
}