package classes;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;

class DefaultLanguage extends JDialog
{
	private boolean arabicLanguage = true;
	private final String lineSeparator = System.getProperty("line.separator");
	DefaultLanguage(final Quran quran)
	{
		super(quran, "اللغة المستخدمة (Default Language)", true);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);

		final JPanel languageChoicePanel = new JPanel();
		languageChoicePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"اختر لغة البرنامج (Choose the program language)",0,0,null,Color.red));
		languageChoicePanel.setPreferredSize(new Dimension(400, 65));
		add(languageChoicePanel, BorderLayout.CENTER);

		final JRadioButton arabicLanguageRadioButton = new JRadioButton("العربية", true);
		final JRadioButton englishLanguageRadioButton = new JRadioButton("English");
		final ActionListener languageGroupListener = (ae)->
		{
            if(ae.getSource() == arabicLanguageRadioButton){arabicLanguage = true;}
            if(ae.getSource() == englishLanguageRadioButton){arabicLanguage = false;}
		};

		arabicLanguageRadioButton.addActionListener(languageGroupListener);
		englishLanguageRadioButton.addActionListener(languageGroupListener);

		final ButtonGroup languageGroup = new ButtonGroup();
		languageGroup.add(arabicLanguageRadioButton);
		languageGroup.add(englishLanguageRadioButton);

		languageChoicePanel.add(arabicLanguageRadioButton);
		languageChoicePanel.add(englishLanguageRadioButton);

		final JPanel closePanel = new JPanel();
		add(closePanel, BorderLayout.SOUTH);

		final JButton closeButton = new JButton ("التالي (Next)");
		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Quran.language = arabicLanguage;

				try
				{
					final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("setting/setting.txt"), StandardCharsets.UTF_8);
					out.write(Quran.language + lineSeparator);
					out.write(quran.internetStreaming + lineSeparator);
					out.write(quran.audioLocation);
					out.close();
				}
				catch(Exception ex){ex.printStackTrace();}
				dispose();
			}
		});

		closePanel.add(closeButton);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){closeButton.doClick();}});

		pack();
		Quran.centerInScreen(this);
        
		getContentPane().applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		englishLanguageRadioButton.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		setVisible(true);
	}
}