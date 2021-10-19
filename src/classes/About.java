package classes;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.font.NumericShaper;

class About extends JWindow
{
	About(final Quran quran)
	{
		super(quran);

		final String splashString = Quran.StreamConverter("setting/version.txt")[0];
        final NumericShaper shaper = NumericShaper.getShaper(NumericShaper.ARABIC);
        final char [] version = splashString.toCharArray();
        shaper.shape(version, 0, 3);

        final JLabel splash = new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("images/quran.png")))
		{
			// paint() is heavier
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
                g.drawChars(version, 0, 3, 410, 303);
				g.drawString(splashString, 68, 287);
			}
		};
		splash.setFont(splash.getFont().deriveFont(11f));
		splash.setForeground(new Color(206, 205, 146));
		getContentPane().add(splash);

		pack();
		Quran.centerInScreen(this);
		setVisible(true);
		addMouseListener(new MouseAdapter(){public void mousePressed(MouseEvent m){dispose();}});
	}
}