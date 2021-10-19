package classes;

import javax.swing.*;
import java.awt.*;

public class Test extends JFrame
{
    Test()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JPanel quranPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        quranPanel.add(new JTextField("Test layout in RTL"));


        setBounds(0, 0, screenSize.width, screenSize.height - 40);
        setExtendedState(MAXIMIZED_BOTH);

        getContentPane().applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        add(new JScrollPane(quranPanel), BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(Test::new);
    }
}
