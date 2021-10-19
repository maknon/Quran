package classes;

/*
	setting.txt format:

	Line 1: language (true -> arabic, false -> english)
	Line 2: internetStreaming (true, false)
	Line 3: audioLocation (Path)
*/

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import javax.swing.border.TitledBorder;

class Setting extends JDialog
{
	private final String lineSeparator = System.getProperty("line.separator");

	Setting(final Quran quran)
	{
		super(quran, true);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);

		final String[] translations = Quran.StreamConverter("language/" + ((Quran.language) ? "SettingArabic.txt" : "SettingEnglish.txt"));

		setTitle(translations[0]);

		final JPanel languageChoicePanel = new JPanel();
		languageChoicePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), translations[1], TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_JUSTIFICATION, null, Color.red));
		languageChoicePanel.setPreferredSize(new Dimension(400, 65));
		add(languageChoicePanel, BorderLayout.NORTH);

		final JRadioButton arabicLanguageRadioButton = new JRadioButton(translations[2], Quran.language);
		final JRadioButton englishLanguageRadioButton = new JRadioButton(translations[3], !Quran.language);

		final ButtonGroup languageGroup = new ButtonGroup();
		languageGroup.add(arabicLanguageRadioButton);
		languageGroup.add(englishLanguageRadioButton);

		languageChoicePanel.add(arabicLanguageRadioButton);
		languageChoicePanel.add(englishLanguageRadioButton);

		final JPanel locationPanel = new JPanel(new BorderLayout());
		locationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), translations[8], TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_JUSTIFICATION, null, Color.red));
		add(locationPanel, BorderLayout.CENTER);

		final JPanel directryPanel = new JPanel(new BorderLayout());
		final JTextField directryTextField = new JTextField(quran.audioLocation);
		locationPanel.add(directryPanel, BorderLayout.SOUTH);

		final JButton browseButton = new JButton(translations[10]);
		browseButton.addActionListener((e) ->
		{
			final JFileChooser fc = new JFileChooser();
			if (Quran.language) fc.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			fc.setDialogTitle(translations[11]);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showOpenDialog(Setting.this) == JFileChooser.APPROVE_OPTION)
				directryTextField.setText(fc.getSelectedFile().toString());
		});

		directryPanel.add(directryTextField, BorderLayout.CENTER);
		directryPanel.add(browseButton, BorderLayout.EAST);

		final JPanel internetPanel = new JPanel();
		locationPanel.add(internetPanel, BorderLayout.CENTER);
		final JRadioButton directryRadioButton = new JRadioButton(translations[12]);
		final JRadioButton internetRadioButton = new JRadioButton(translations[13]);
		final ActionListener locationListener = (ae) ->
		{
			if (ae.getSource() == directryRadioButton)
			{
				directryTextField.setEnabled(true);
				browseButton.setEnabled(true);
			}

			if (ae.getSource() == internetRadioButton)
			{
				directryTextField.setEnabled(false);
				browseButton.setEnabled(false);
			}
		};
		directryRadioButton.addActionListener(locationListener);
		internetRadioButton.addActionListener(locationListener);

		final ButtonGroup locationGroup = new ButtonGroup();
		locationGroup.add(directryRadioButton);
		locationGroup.add(internetRadioButton);

		internetPanel.add(internetRadioButton);
		internetPanel.add(directryRadioButton);

		if (quran.internetStreaming)
		{
			directryTextField.setEnabled(false);
			browseButton.setEnabled(false);
			internetRadioButton.setSelected(true);
		}
		else
			directryRadioButton.setSelected(true);

		final JPanel closePanel = new JPanel();
		add(closePanel, BorderLayout.SOUTH);

		final JButton cancelButton = new JButton(translations[6]);
		cancelButton.addActionListener((e) -> dispose());

		final JButton closeButton = new JButton(translations[7]);
		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (internetRadioButton.isSelected())
					quran.internetStreaming = true;
				else
				{
					quran.internetStreaming = false;
					quran.audioLocation = directryTextField.getText().trim();
				}

				try
				{
					final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("setting/setting.txt"), StandardCharsets.UTF_8);
					out.write(arabicLanguageRadioButton.isSelected() + lineSeparator);
					out.write(quran.internetStreaming + lineSeparator);
					out.write(quran.audioLocation);
					out.close();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				dispose();

				// if there is a change in the setting
				if (Quran.language != arabicLanguageRadioButton.isSelected())
				{
					JOptionPane.showOptionDialog(getContentPane(), translations[9], translations[4], JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{translations[14]}, translations[14]);
					try
					{
						// The same as in quran.shutdown()
						quran.sharedDBConnection.close();
						Runtime.getRuntime().exec(new String[]{System.getProperty("java.home") + File.separator + "bin" + File.separator + "java", "-Dsun.java2d.noddraw=true", "-Dsun.java2d.ddscale=true", "-jar", "quran.jar"});
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
					System.exit(0);
				}
			}
		});

		closePanel.add(closeButton);
		closePanel.add(cancelButton);

		if (Quran.language)
		{
			getContentPane().applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			directryTextField.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		}

		pack();
		Quran.centerInScreen(this);
		setVisible(true);
	}
}