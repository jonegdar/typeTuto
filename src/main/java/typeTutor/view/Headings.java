package typeTutor.view;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Static header view with app title and subtitle.
 */
public class Headings extends JPanel implements MainFrame.SupportsAlpha {
    private float alpha = 1f;
    /**
     * Builds centered heading labels.
     */
    public Headings() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(31, 31, 31));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("TypeTuto");
        title.setFont(AppFonts.uiExtraBold(44f));
        title.setForeground(new Color(255, 192, 90));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Start typing, and the game starts");
        subtitle.setFont(AppFonts.uiRegular(22f));
        subtitle.setForeground(new Color(255, 255, 255));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(18));
        add(title);
        add(Box.createVerticalStrut(5));
        add(subtitle);
        add(Box.createVerticalGlue());
    }

    /**
     * Updates panel opacity for distraction-free mode.
     */
    @Override
    public void setAlpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(alpha, 1f));
        repaint();
    }

    /**
     * Paints child labels with configurable alpha.
     */
    @Override
    protected void paintChildren(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setComposite(java.awt.AlphaComposite.SrcOver.derive(alpha));
        super.paintChildren(g2);
        g2.dispose();
    }
}
