package typeTutor.view;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Headings extends JPanel {

    public Headings() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(31, 31, 31));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("TypeTuto");
        title.setFont(AppFonts.ui(40f, Font.BOLD));
        title.setForeground(new Color(255, 192, 90));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Start typing, and the game starts");
        subtitle.setFont(AppFonts.ui(20f, Font.PLAIN));
        subtitle.setForeground(new Color(255, 255, 255));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(title);
        add(Box.createVerticalStrut(5));
        add(subtitle);
        add(Box.createVerticalGlue());
    }
}
