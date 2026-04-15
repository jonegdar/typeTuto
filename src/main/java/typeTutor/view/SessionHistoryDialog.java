package typeTutor.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import typeTutor.model.SessionHistoryTracker.Entry;

/**
 * Shows a table of previously completed typing sessions.
 */
public class SessionHistoryDialog extends JDialog {
    private static final int CORNER_RADIUS = 40;

    /**
     * Creates the history dialog shell.
     */
    public SessionHistoryDialog(Frame owner) {
        super(owner, "TypeTuto - Session History", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyRoundedShape();
            }
        });
    }

    /**
     * Shows the dialog for the supplied history entries.
     */
    public void showHistory(List<Entry> entries) {
        JPanel content = buildContent(entries);
        int ownerWidth = getOwner() != null ? getOwner().getWidth() : 1200;
        content.setPreferredSize(new Dimension(Math.round(ownerWidth * 0.80f), content.getPreferredSize().height));
        setContentPane(content);
        pack();
        applyRoundedShape();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    /**
     * Builds the history table UI.
     */
    private JPanel buildContent(List<Entry> entries) {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(new Color(22, 22, 30));
        content.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JButton close = new JButton("X");
        close.setFocusPainted(false);
        close.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        close.setBackground(new Color(170, 55, 55));
        close.setForeground(Color.WHITE);
        close.addActionListener(e -> dispose());

        JLabel title = new JLabel("Session History", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(AppFonts.uiBold(21f));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.add(title, BorderLayout.CENTER);
        titleBar.add(close, BorderLayout.EAST);

        String[] columns = {"When", "WPM", "Accuracy", "Rank", "Time", "Language", "Word Type"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Entry entry : entries) {
            model.addRow(new Object[] {
                    entry.getTimestamp(),
                    String.format("%.0f", entry.getWpm()),
                    String.format("%.1f%%", entry.getAccuracy()),
                    entry.getRank(),
                    entry.getTimeMode(),
                    entry.getLanguage(),
                    entry.getWordMode()
            });
        }

        if (entries.isEmpty()) {
            model.addRow(new Object[] {"No sessions yet", "-", "-", "-", "-", "-", "-"});
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(32, 32, 46));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(70, 70, 85));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setBackground(new Color(42, 42, 58));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(AppFonts.uiBold(12f));
        table.setDefaultRenderer(Object.class, new RankAwareRenderer());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        content.add(titleBar, BorderLayout.NORTH);
        content.add(tablePanel, BorderLayout.CENTER);
        return content;
    }

    /**
     * Updates the rounded clipping for the dialog window.
     */
    private void applyRoundedShape() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS));
    }

    /**
     * Renderer that colors the rank column.
     */
    private static class RankAwareRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 3 && value != null) {
                String text = value.toString();
                if (text.startsWith("No") || text.equals("-")) {
                    c.setForeground(Color.WHITE);
                } else {
                    Color color = switch (text.charAt(0)) {
                        case 'S' -> new Color(110, 231, 183);
                        case 'A' -> new Color(139, 233, 253);
                        case 'B' -> new Color(255, 192, 90);
                        case 'C' -> new Color(255, 184, 108);
                        case 'D' -> new Color(255, 122, 89);
                        default -> new Color(255, 85, 85);
                    };
                    c.setForeground(color);
                }
            } else {
                c.setForeground(Color.WHITE);
            }
            c.setBackground(new Color(32, 32, 46));
            return c;
        }
    }
}
