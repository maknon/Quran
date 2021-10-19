package classes;

/*
 * Quran Encyclopedia
 * Version 2.1
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.NumericShaper;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.awt.image.*;
import java.sql.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;

import javafx.scene.media.*;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

class Quran extends JFrame
{
	static boolean language = true;// i.e. default is arabic
	//private static final boolean isWindows = System.getProperty("os.name").startsWith("Windows");
	static final boolean isMAC = System.getProperty("os.name").equals("Mac OS X");
	private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public static final boolean derbyInUse = false;

	Connection sharedDBConnection;
	private final Vector<String> sheekhFolders = new Vector<>();
	private final Vector<String> sheekhTypes = new Vector<>();
	private final JComboBox<String> sheekhComboBox, ayaComboBox, suraComboBox, pageComboBox, hezpComboBox, jozComboBox, fromSura, fromAya, toSura, toAya, tafseerComboBox, eerabComboBox;
	private int selectedPage, selectedSura, selectedAya, selectedHezp, selectedJoz;
	boolean selectedByAyaButton, selectedBySuraButton, selectedByPageButton, selectedByHezpButton, selectedByJozButton, selectedByMouse, selectedByTahfeed, selectedBySearch;
	private boolean cancelAyaListener, cancelSuraListener, cancelPageListener, cancelHezpListener, cancelJozListener;
	private final DrawingPanel drawingPanel = new DrawingPanel();
	private final JPanel quranPanel;
	private final JScrollPane drawingScrollPane;
	private final JButton play_pauseButton, startTahfeedButton, stopTahfeedButton, jozDecrementButton, jozIncrementButton, hezpDecrementButton, hezpIncrementButton, pageDecrementButton, pageIncrementButton, suraDecrementButton, suraIncrementButton, ayaDecrementButton, stopButton;
	boolean internetStreaming = true;
	String audioLocation = "";
	private final JButton ayaIncrementButton;
	private MediaPlayer player;
	Search searchDialog;
	static IndexSearcher indexSearcher, arabicRootsTableSearcher, arabicRootsSearcher, arabicLuceneSearcher;
	static ArabicAnalyzer arabicAnalyzer;
	static org.apache.lucene.analysis.ar.ArabicAnalyzer arabicLuceneAnalyzer;
	static ArabicRootAnalyzer arabicRootsAnalyzer;

	static String pagesFolder = "pages-hafs"; // or pages-warsh

	Quran()
	{
		try
		{
			// Making Tahoma font works in Linux and Unix.
			Font defaultFont;

			final Properties prop = new Properties();
			prop.load(new FileInputStream("setting/font.properties"));
			if (prop.getProperty("internal").equals("true"))
			{
				defaultFont = Font.createFont(Font.TRUETYPE_FONT, new File("bin/" + prop.getProperty("font")));
				defaultFont = defaultFont.deriveFont(Font.PLAIN, Float.parseFloat(prop.getProperty("menuSize")));
			}
			else
				defaultFont = new Font(prop.getProperty("font"), Font.PLAIN, Integer.parseInt(prop.getProperty("menuSize")));

			UIManager.put("CheckBox.font", new FontUIResource(defaultFont));
			UIManager.put("Menu.font", new FontUIResource(defaultFont));
			UIManager.put("MenuBar.font", new FontUIResource(defaultFont));
			UIManager.put("MenuItem.font", new FontUIResource(defaultFont));
			UIManager.put("ToolTip.font", new FontUIResource(defaultFont));
			UIManager.put("CheckBoxMenuItem.font", new FontUIResource(defaultFont));
			UIManager.put("RadioButtonMenuItem.font", new FontUIResource(defaultFont));
			UIManager.put("OptionPane.font", new FontUIResource(defaultFont));
			UIManager.put("PopupMenu.font", new FontUIResource(defaultFont));
			UIManager.put("TabbedPane.font", new FontUIResource(defaultFont));
			UIManager.put("ProgressBar.font", new FontUIResource(defaultFont));

			final String splashString = StreamConverter("setting/version.txt")[0];
			final SplashScreen splash = SplashScreen.getSplashScreen();
			final Graphics2D g2d = splash.createGraphics();
			g2d.setColor(new Color(206, 205, 146));
			g2d.setFont(defaultFont);

			final NumericShaper shaper = NumericShaper.getShaper(NumericShaper.ARABIC);
			final char[] version = splashString.toCharArray();
			shaper.shape(version, 0, 3);

			g2d.drawChars(version, 0, 3, 410, 303);
			g2d.drawString(splashString, 68, 287);
			splash.update();

			defaultFont = defaultFont.deriveFont(Float.parseFloat(prop.getProperty("defaultSize")));
			UIManager.put("List.font", new FontUIResource(defaultFont));
			UIManager.put("Tree.font", new FontUIResource(defaultFont));
			UIManager.put("Label.font", new FontUIResource(defaultFont));
			UIManager.put("Button.font", new FontUIResource(defaultFont));
			UIManager.put("TextField.font", new FontUIResource(defaultFont));
			UIManager.put("Table.font", new FontUIResource(defaultFont));
			UIManager.put("ComboBox.font", new FontUIResource(defaultFont));
			UIManager.put("ToolBar.font", new FontUIResource(defaultFont));
			UIManager.put("RadioButton.font", new FontUIResource(defaultFont));
			UIManager.put("Panel.font", new FontUIResource(defaultFont));
			UIManager.put("ToggleButton.font", new FontUIResource(defaultFont));
			UIManager.put("TitledBorder.font", new FontUIResource(defaultFont));
			UIManager.put("TableHeader.font", new FontUIResource(defaultFont));
			UIManager.put("Text.font", new FontUIResource(defaultFont));
			UIManager.put("EditorPane.font", new FontUIResource(defaultFont));
			UIManager.put("ScrollPane.font", new FontUIResource(defaultFont));
			UIManager.put("PasswordField.font", new FontUIResource(defaultFont));
			UIManager.put("TextPane.font", new FontUIResource(defaultFont));
			UIManager.put("TextArea.font", new FontUIResource(defaultFont));

			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(defaultFont);

			// Determine the default language
			final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("setting/setting.txt"), StandardCharsets.UTF_8));
			final String defaultLanguage = in.readLine();

			internetStreaming = in.readLine().equals("true");
			audioLocation = in.readLine();

			in.close();

			// It should be after reading the file since defaultLanguage will write again to setting.txt
			if (defaultLanguage.equals("nothing"))
				new DefaultLanguage(this);
			else
				language = defaultLanguage.equals("true");

			// uthman.otf does not have English letters. you need to mix it yourself if you want to display both in the same book. But JTextPane is handling this by itself, JTextArea is not.
			// Version 2.1, replace uthman.otf with ScheherazadeNew.ttf which is much better. TODO: replace back JTextPane with JTextArea since it contains english letters as well. see above
			if (language)
				//ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("bin/uthman.otf")));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("bin/ScheherazadeNew.ttf")));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		final String[] translation = StreamConverter("language/" + (language ? "QuranArabic.txt" : "QuranEnglish.txt"));
		setTitle(translation[1]);

		final Vector<String> sheekh = new Vector<>();

		try
		{
			final String dbURL;
			if (derbyInUse)
			{
				dbURL = "jdbc:derby:db";
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			}
			else
			{
				// ;CACHE_SIZE=65536;CACHE_TYPE=SOFT_LRU
				dbURL = "jdbc:h2:" + programFolder + "db/quranDatabase"; // ;CACHE_SIZE=32768;LOG=0 (logging is disabled, faster). it is only speed import.
				Class.forName("org.h2.Driver");
			}

			sharedDBConnection = DriverManager.getConnection(dbURL);

			// Create In-memory DB. No need for it now
			/*
			sharedDBConnection = DriverManager.getConnection("jdbc:h2:mem:PUBLIC;DB_CLOSE_DELAY=-1;SCHEMA=PUBLIC;REFERENTIAL_INTEGRITY=FALSE");
			final Statement s = sharedDBConnection.createStatement();
			s.execute("CREATE TABLE quran(Page INTEGER, Sura INTEGER, Aya INTEGER, Location VARCHAR(150))");
			s.execute("CREATE TABLE sheekh(Name VARCHAR(150), Folder VARCHAR(50), Type VARCHAR(3))");
			s.execute("INSERT INTO quran SELECT * FROM CSVREAD('db/quran')");
			s.execute("INSERT INTO sheekh SELECT * FROM CSVREAD('db/sheekh', NULL, 'UTF-8')");

			// Can be done like this but all columns will be VARCHAR!
			// CREATE TABLE quran_ AS SELECT * FROM CSVREAD('quran')
			// CREATE TABLE sheekh_ AS SELECT * FROM CSVREAD('sheekh', NULL, 'UTF-8')
			*/

			//sharedDBConnection.setReadOnly(true); // TODO: re-enable it after finishing updating the tables for x-y
			/*
			final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM sheekh");
			if(language)
			{
				while(rs.next())
				{
					sheekh.add(rs.getString("Name_ar"));
					sheekhFolders.add(rs.getString("Folder"));
					sheekhTypes.add(rs.getString("Type"));
				}
			}
			else
			{
				while(rs.next())
				{
					sheekh.add(rs.getString("Name_en"));
					sheekhFolders.add(rs.getString("Folder"));
					sheekhTypes.add(rs.getString("Type"));
				}
			}
			*/

			final Properties props = new Properties();
			if (language)
			{
				props.load(new InputStreamReader(new FileInputStream("setting/sheekh_ar.properties"), StandardCharsets.UTF_8));
				final Enumeration e = props.propertyNames();

				while (e.hasMoreElements())
				{
					final String key = (String) e.nextElement();
					sheekh.add(props.getProperty(key));
					sheekhFolders.add(key);
					sheekhTypes.add("mp3");
				}

				indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File("arabicIndex").toPath())));
				arabicRootsSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File("arabicRootsIndex").toPath())));
				arabicLuceneSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File("arabicLuceneIndex").toPath())));
				arabicRootsTableSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File("arabicRootsTableIndex").toPath())));

				arabicRootsAnalyzer = new ArabicRootAnalyzer();
				arabicLuceneAnalyzer = new org.apache.lucene.analysis.ar.ArabicAnalyzer();
				arabicAnalyzer = new ArabicAnalyzer();
			}
			else
			{
				props.load(new FileInputStream("setting/sheekh_en.properties"));
				final Enumeration e = props.propertyNames();

				while (e.hasMoreElements())
				{
					final String key = (String) e.nextElement();
					sheekh.add(props.getProperty(key));
					sheekhFolders.add(key);
					sheekhTypes.add("mp3");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (language)
		{
			// To translate FileChooser Dialog, its buttons, ... etc
			UIManager.put("FileChooser.fileNameLabelText", translation[3]);
			UIManager.put("FileChooser.filesOfTypeLabelText", translation[4]);
			UIManager.put("FileChooser.cancelButtonText", translation[5]);
			UIManager.put("FileChooser.saveButtonText", translation[6]);
			UIManager.put("FileChooser.saveInLabelText", translation[7]);
			UIManager.put("FileChooser.lookInLabelText", translation[8]);
			UIManager.put("FileChooser.openButtonText", translation[9]);
			UIManager.put("FileChooser.viewMenuLabelText", translation[10]);
			UIManager.put("FileChooser.refreshActionLabelText", translation[11]);
			UIManager.put("FileChooser.newFolderActionLabelText", translation[12]);
			UIManager.put("FileChooser.listViewActionLabelText", translation[13]);
			UIManager.put("FileChooser.detailsViewActionLabelText", translation[14]);
			UIManager.put("FileChooser.directoryOpenButtonText", translation[15]);
		}

		final Vector<String> pages = new Vector<>();
		for (int i = 1; i < 605; i++) pages.addElement(translation[2] + i);
		pageComboBox = new JComboBox<>(pages);

		final Vector<String> joz = new Vector<>();
		for (int i = 1; i < 31; i++) joz.addElement(translation[16] + i);
		jozComboBox = new JComboBox<>(joz);

		final Vector<String> hezp = new Vector<>();
		for (int i = 1; i < 61; i++) hezp.addElement(translation[17] + i);
		hezpComboBox = new JComboBox<>(hezp);

		sheekhComboBox = new JComboBox<>(sheekh);
		ayaComboBox = new JComboBox<>();

		final String[] suraList = StreamConverter("language/" + ((language) ? "suraArabic.txt" : "suraEnglish.txt"));
		suraComboBox = new JComboBox<>(suraList);
		//suraComboBox.setRenderer(new CellRenderer("sssssssssssssss")); // you should specify the length manually

		/*
		String d[] = StreamConverter("language"+fileSeparator+((language)?"suraArabic.txt":"suraEnglish.txt"));
		for(int i=0; i<d.length; i++)
			suraComboBox.addItem(d[i]);
		*/

		/*
		// problem with Alloy
		suraComboBox.setUI(new javax.swing.plaf.metal.MetalComboBoxUI(){
			public void layoutComboBox(Container parent, MetalComboBoxLayoutManager manager) {
				super.layoutComboBox(parent, manager);
				arrowButton.setBounds(0,0,0,0);
			}
		});
		*/

		//* All of this disappear once SwingUtilities.updateComponentTreeUI(ComboBox) is called. updateComponentTreeUI is used to get rid of the refreshing problem when setting ComboBox index programmatically
		suraComboBox.setFocusable(false); // To avoid the issue with button focus.
		pageComboBox.setFocusable(false);
		ayaComboBox.setFocusable(false);
		sheekhComboBox.setFocusable(false);
		jozComboBox.setFocusable(false);
		hezpComboBox.setFocusable(false);

		/*
		UIManager.put("ComboBox.background", new ColorUIResource(Color.WHITE));
		UIManager.put("ComboBox.selectionBackground", new ColorUIResource(Color.WHITE));

		ayaComboBox.setUI(new javax.swing.plaf.basic.BasicComboBoxUI()
		{
			@Override
			protected JButton createArrowButton()
			{
				JButton button = super.createArrowButton();
				button.setVisible(false);
				return button;
				//return new BasicArrowButton(BasicArrowButton.EAST);
			}

			@Override
			public void layoutComboBox(Container parent, MetalComboBoxLayoutManager manager)
			{
				super.layoutComboBox(parent, manager);
				arrowButton.setBounds(0,0,0,0);
			}
		});

		for(final Component component : suraComboBox.getComponents())
			if(component instanceof AbstractButton)
				if(component.isVisible())
				{
					component.setVisible(false);
					//suraComboBox.remove(component);
					break;
				}

		for(final Component component : ayaComboBox.getComponents())
			if(component instanceof AbstractButton)
				if(component.isVisible())
				{
					component.setVisible(false);
					//component.setBounds(0,0,0,0);
					//component.setSize(0,0);
					//component.setPreferredSize(new Dimension(0, 0));
					break;
				}

		for(final Component component : pageComboBox.getComponents())
			if(component instanceof AbstractButton)
				if(component.isVisible())
				{
					component.setVisible(false);
					break;
				}

		for(final Component component : jozComboBox.getComponents())
			if(component instanceof AbstractButton)
				if(component.isVisible())
				{
					component.setVisible(false);
					break;
				}

		for(final Component component : hezpComboBox.getComponents())
			if(component instanceof AbstractButton)
				if(component.isVisible())
				{
					component.setVisible(false);
					break;
				}
		//*/

		sheekhComboBox.addActionListener((e) -> ayaComboBox.setSelectedIndex(ayaComboBox.getSelectedIndex()));
		suraComboBox.addActionListener((e) ->
		{
			final int index = ((JComboBox) e.getSource()).getSelectedIndex();
			if (index != -1 && !cancelSuraListener)
			{
				selectedBySuraButton = true;
				selectedByPageButton = false;
				selectedByAyaButton = false;
				selectedByHezpButton = false;
				selectedByJozButton = false;
				selectedByTahfeed = false;
				selectedByMouse = false;
				selectedBySearch = false;
				SelectionThread(index + 1);
			}
		});

		suraIncrementButton = new JButton(new ImageIcon(language?"images/ayaIncrement.png":"images/ayaDecrement.png"));
		suraDecrementButton = new JButton(new ImageIcon(language?"images/ayaDecrement.png":"images/ayaIncrement.png"));
		suraIncrementButton.setMargin(new Insets(3, 3, 3, 3));
		suraDecrementButton.setMargin(new Insets(3, 3, 3, 3));
		final ActionListener suraListener = (e) ->
		{
			int index = suraComboBox.getSelectedIndex();
			if (e.getSource() == suraIncrementButton) index++;
			if (e.getSource() == suraDecrementButton) index--;

			if (!(index > 113 || index < 0))
				suraComboBox.setSelectedIndex(index);
		};
		suraIncrementButton.addActionListener(suraListener);
		suraDecrementButton.addActionListener(suraListener);

		ayaComboBox.addActionListener((e) ->
		{
			final int index = ((JComboBox) e.getSource()).getSelectedIndex();
			if (index != -1 && !cancelAyaListener)
			{
				selectedByAyaButton = true;
				selectedByPageButton = false;
				selectedBySuraButton = false;
				selectedByHezpButton = false;
				selectedByJozButton = false;
				selectedByTahfeed = false;
				selectedByMouse = false;
				selectedBySearch = false;
				SelectionThread(index + 1);
			}
		});

		ayaIncrementButton = new JButton(new ImageIcon(language?"images/ayaIncrement.png":"images/ayaDecrement.png"));
		ayaDecrementButton = new JButton(new ImageIcon(language?"images/ayaDecrement.png":"images/ayaIncrement.png"));
		ayaIncrementButton.setMargin(new Insets(3, 3, 3, 3));
		ayaDecrementButton.setMargin(new Insets(3, 3, 3, 3));
		final ActionListener ayaListener = (e) ->
		{
			int index = ayaComboBox.getSelectedIndex();
			if (e.getSource() == ayaIncrementButton) index++;
			if (e.getSource() == ayaDecrementButton) index--;

			if (!(index >= ayaComboBox.getItemCount() || index < 0))
				ayaComboBox.setSelectedIndex(index);
		};
		ayaIncrementButton.addActionListener(ayaListener);
		ayaDecrementButton.addActionListener(ayaListener);

		pageComboBox.addActionListener((e) ->
		{
			final int index = ((JComboBox) e.getSource()).getSelectedIndex();
			if (index != -1 && !cancelPageListener)
			{
				selectedByPageButton = true;
				selectedByAyaButton = false;
				selectedBySuraButton = false;
				selectedByHezpButton = false;
				selectedByJozButton = false;
				selectedByTahfeed = false;
				selectedByMouse = false;
				selectedBySearch = false;
				SelectionThread(index + 1);
			}
		});

		pageIncrementButton = new JButton(new ImageIcon(language?"images/ayaIncrement.png":"images/ayaDecrement.png"));
		pageDecrementButton = new JButton(new ImageIcon(language?"images/ayaDecrement.png":"images/ayaIncrement.png"));
		pageIncrementButton.setMargin(new Insets(3, 3, 3, 3));
		pageDecrementButton.setMargin(new Insets(3, 3, 3, 3));
		final ActionListener pageListener = (e) ->
		{
			int index = pageComboBox.getSelectedIndex();
			if (e.getSource() == pageIncrementButton) index++;
			if (e.getSource() == pageDecrementButton) index--;

			if (!(index >= pageComboBox.getItemCount() || index < 0))
				pageComboBox.setSelectedIndex(index);
		};
		pageIncrementButton.addActionListener(pageListener);
		pageDecrementButton.addActionListener(pageListener);

		jozComboBox.addActionListener((e) ->
		{
			final int index = ((JComboBox) e.getSource()).getSelectedIndex();
			if (index != -1 && !cancelJozListener)
			{
				selectedByJozButton = true;
				selectedByPageButton = false;
				selectedByAyaButton = false;
				selectedBySuraButton = false;
				selectedByHezpButton = false;
				selectedByTahfeed = false;
				selectedByMouse = false;
				selectedBySearch = false;
				SelectionThread(index + 1);
			}
		});

		jozIncrementButton = new JButton(new ImageIcon(language?"images/ayaIncrement.png":"images/ayaDecrement.png"));
		jozDecrementButton = new JButton(new ImageIcon(language?"images/ayaDecrement.png":"images/ayaIncrement.png"));
		jozIncrementButton.setMargin(new Insets(3, 3, 3, 3));
		jozDecrementButton.setMargin(new Insets(3, 3, 3, 3));
		final ActionListener jozListener = (e) ->
		{
			int index = jozComboBox.getSelectedIndex();
			if (e.getSource() == jozIncrementButton) index++;
			if (e.getSource() == jozDecrementButton) index--;

			if (!(index >= jozComboBox.getItemCount() || index < 0))
				jozComboBox.setSelectedIndex(index);
		};
		jozIncrementButton.addActionListener(jozListener);
		jozDecrementButton.addActionListener(jozListener);

		hezpComboBox.addActionListener((e) ->
		{
			final int index = ((JComboBox) e.getSource()).getSelectedIndex();
			if (index != -1 && !cancelHezpListener)
			{
				selectedByHezpButton = true;
				selectedByPageButton = false;
				selectedByAyaButton = false;
				selectedBySuraButton = false;
				selectedByJozButton = false;
				selectedByTahfeed = false;
				selectedByMouse = false;
				selectedBySearch = false;
				SelectionThread(index + 1);
			}
		});

		hezpIncrementButton = new JButton(new ImageIcon(language?"images/ayaIncrement.png":"images/ayaDecrement.png"));
		hezpDecrementButton = new JButton(new ImageIcon(language?"images/ayaDecrement.png":"images/ayaIncrement.png"));
		hezpIncrementButton.setMargin(new Insets(3, 3, 3, 3));
		hezpDecrementButton.setMargin(new Insets(3, 3, 3, 3));
		final ActionListener hezpListener = (e) ->
		{
			int index = hezpComboBox.getSelectedIndex();
			if (e.getSource() == hezpIncrementButton) index++;
			if (e.getSource() == hezpDecrementButton) index--;

			if (!(index >= hezpComboBox.getItemCount() || index < 0))
				hezpComboBox.setSelectedIndex(index);
		};
		hezpIncrementButton.addActionListener(hezpListener);
		hezpDecrementButton.addActionListener(hezpListener);

		play_pauseButton = new JButton(new ImageIcon("images/play.png"));
		play_pauseButton.setRolloverIcon(new ImageIcon("images/play_rollover.png"));
		play_pauseButton.setPressedIcon(new ImageIcon("images/play_pressed.png"));
		play_pauseButton.setDisabledIcon(new ImageIcon("images/play_disable.png"));
		play_pauseButton.setMargin(new Insets(0, 0, 0, 0));
		play_pauseButton.setFocusable(false);
		play_pauseButton.setBorder(null);
		play_pauseButton.setContentAreaFilled(false);
		play_pauseButton.addActionListener((e) ->
		{
			if (player.getStatus() == MediaPlayer.Status.PAUSED)
				player.play();
			else
			{
				if (player.getStatus() == MediaPlayer.Status.PLAYING || player.getStatus() == MediaPlayer.Status.STALLED)
					player.pause();
				else
				{
					if (player.getStatus() == MediaPlayer.Status.STOPPED || player.getStatus() == MediaPlayer.Status.READY || player.getStatus() == MediaPlayer.Status.UNKNOWN || player.getStatus() == MediaPlayer.Status.HALTED)
						ayaComboBox.setSelectedIndex(ayaComboBox.getSelectedIndex());
				}
			}
		});

		// Start Tahfeed
		startTahfeedButton = new JButton(translation[23]);
		stopTahfeedButton = new JButton(translation[31]);
		stopTahfeedButton.setEnabled(false);

		stopButton = new JButton(new ImageIcon("images/stop.png"));
		stopButton.setRolloverIcon(new ImageIcon("images/stop_rollover.png"));
		stopButton.setPressedIcon(new ImageIcon("images/stop_pressed.png"));
		stopButton.setDisabledIcon(new ImageIcon("images/stop_disable.png"));
		stopButton.setMargin(new Insets(0, 0, 0, 0));
		stopButton.setFocusable(false);
		stopButton.setBorder(null);
		stopButton.setContentAreaFilled(false);
		stopButton.addActionListener((e) -> player.stop());

		// Tahfeed
		fromSura = new JComboBox<>(suraList);
		fromAya = new JComboBox<>();
		toSura = new JComboBox<>(suraList);
		toAya = new JComboBox<>();

		fromSura.addActionListener((e) ->
		{
			try
			{
				final int index = ((JComboBox) e.getSource()).getSelectedIndex() + 1;
				final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT MAX(Aya) AS AyaCount FROM Quran WHERE Sura=" + index);
				rs.next();
				final int ayaCount = rs.getInt("AyaCount");

				fromAya.removeAllItems();
				for (int i = 0; i < ayaCount; i++)
					fromAya.addItem((language ? "الآية " : "Aya ") + (i + 1)); // The first item will be selected automatically
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		});

		toSura.addActionListener((e) ->
		{
			try
			{
				final int index = ((JComboBox) e.getSource()).getSelectedIndex() + 1;
				final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT MAX(Aya) AS AyaCount FROM Quran WHERE Sura=" + index);
				rs.next();
				final int ayaCount = rs.getInt("AyaCount");

				toAya.removeAllItems();
				for (int i = 0; i < ayaCount; i++)
					toAya.addItem((language ? "الآية " : "Aya ") + (i + 1)); // The first item will be selected automatically
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		});

		startTahfeedButton.addActionListener((e) ->
		{
			final int fromS = fromSura.getSelectedIndex();
			final int fromA = fromAya.getSelectedIndex();
			final int toS = toSura.getSelectedIndex();
			final int toA = toAya.getSelectedIndex();

			if (toS < fromS)
			{
				JOptionPane.showOptionDialog(getContentPane(), translation[33], translation[32], JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{translation[34]}, translation[34]);
				return;
			}
			else
			{
				if (toS == fromS && toA < fromA)
				{
					JOptionPane.showOptionDialog(getContentPane(), translation[35], translation[32], JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{translation[34]}, translation[34]);
					return;
				}
			}

			startTahfeedButton.setEnabled(false);
			stopButton.setEnabled(false);
			fromSura.setEnabled(false);
			fromAya.setEnabled(false);
			toSura.setEnabled(false);
			toAya.setEnabled(false);
			stopTahfeedButton.setEnabled(true);

			sheekhComboBox.setEnabled(false);
			jozDecrementButton.setEnabled(false);
			jozComboBox.setEnabled(false);
			jozIncrementButton.setEnabled(false);
			hezpDecrementButton.setEnabled(false);
			hezpComboBox.setEnabled(false);
			hezpIncrementButton.setEnabled(false);
			pageDecrementButton.setEnabled(false);
			pageComboBox.setEnabled(false);
			pageIncrementButton.setEnabled(false);
			suraDecrementButton.setEnabled(false);
			suraComboBox.setEnabled(false);
			suraIncrementButton.setEnabled(false);
			ayaDecrementButton.setEnabled(false);
			ayaComboBox.setEnabled(false);
			ayaIncrementButton.setEnabled(false);

			selectedByTahfeed = true;
			selectedBySuraButton = false;
			selectedByPageButton = false;
			selectedByAyaButton = false;
			selectedByHezpButton = false;
			selectedByJozButton = false;
			selectedByMouse = false;
			selectedBySearch = false;
			SelectionThread(-1);
		});

		stopTahfeedButton.addActionListener((e) ->
		{
			player.stop();

			startTahfeedButton.setEnabled(true);
			stopButton.setEnabled(true);
			fromSura.setEnabled(true);
			fromAya.setEnabled(true);
			toSura.setEnabled(true);
			toAya.setEnabled(true);
			stopTahfeedButton.setEnabled(false);

			sheekhComboBox.setEnabled(true);
			jozDecrementButton.setEnabled(true);
			jozComboBox.setEnabled(true);
			jozIncrementButton.setEnabled(true);
			hezpDecrementButton.setEnabled(true);
			hezpComboBox.setEnabled(true);
			hezpIncrementButton.setEnabled(true);
			pageDecrementButton.setEnabled(true);
			pageComboBox.setEnabled(true);
			pageIncrementButton.setEnabled(true);
			suraDecrementButton.setEnabled(true);
			suraComboBox.setEnabled(true);
			suraIncrementButton.setEnabled(true);
			ayaDecrementButton.setEnabled(true);
			ayaComboBox.setEnabled(true);
			ayaIncrementButton.setEnabled(true);
		});

		// Tafseer/Translations
		final Vector<String> tafaseer = new Vector<>();
		try
		{
			final ResultSet rs = sharedDBConnection.createStatement().executeQuery((language ? "SELECT Tafseer FROM Tafseer" : "SELECT Translations FROM Translations") + " ORDER BY TableName");
			while (rs.next())
				tafaseer.addElement(rs.getString(1));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		tafseerComboBox = new JComboBox<>(tafaseer);

		// Eerab
		final Vector<String> eerab = new Vector<>();
		try
		{
			final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT Eerab FROM Eerab ORDER BY TableName");
			while (rs.next())
				eerab.addElement(rs.getString(1));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		eerabComboBox = new JComboBox<>(eerab);

        /*
		final JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0,0));
		controlPanel.add(sheekhComboBox);
		controlPanel.add(new JLabel("   "));
		controlPanel.add(jozDecrementButton);
		controlPanel.add(jozComboBox);
		controlPanel.add(jozIncrementButton);
		controlPanel.add(new JLabel("   "));
		controlPanel.add(hezpDecrementButton);
		controlPanel.add(hezpComboBox);
		controlPanel.add(hezpIncrementButton);
		controlPanel.add(new JLabel("   "));
		controlPanel.add(play_pauseButton);
		controlPanel.add(stopButton);
		controlPanel.add(new JLabel("   "));
		controlPanel.add(pageDecrementButton);
		controlPanel.add(pageComboBox);
		controlPanel.add(pageIncrementButton);
		controlPanel.add(new JLabel("   "));
		controlPanel.add(suraDecrementButton);
		controlPanel.add(suraComboBox);
		controlPanel.add(suraIncrementButton);
		controlPanel.add(new JLabel("   "));
		controlPanel.add(ayaDecrementButton);
		controlPanel.add(ayaComboBox);
		controlPanel.add(ayaIncrementButton);
		getContentPane().add(controlPanel, BorderLayout.SOUTH);
		*/

		add(new JPanel(new BorderLayout())
		{
			{
				add(new JPanel()
				{
					{
						setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
						add(new JPanel(new BorderLayout())
						{
							{
								setLayout(new GridBagLayout());
								setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), translation[24], TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_JUSTIFICATION, null, Color.red));

								add(new JLabel(translation[29]), new GridBagConstraints(
										0, 0, 1, 1, 0.2, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(fromSura, new GridBagConstraints(
										1, 0, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(fromAya, new GridBagConstraints(
										4, 0, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

								add(new JLabel(translation[30]), new GridBagConstraints(
										0, 1, 1, 1, 0.2, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(toSura, new GridBagConstraints(
										1, 1, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(toAya, new GridBagConstraints(
										4, 1, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

								add(startTahfeedButton, new GridBagConstraints(
										1, 2, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 0, 0, 0), 1, 0));
								add(stopTahfeedButton, new GridBagConstraints(
										4, 2, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 0, 0, 0), 1, 0));
							}
						});

						add(new JPanel(new BorderLayout())
						{
							{
								setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), translation[37], TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_JUSTIFICATION, null, Color.red));
								add(tafseerComboBox);
							}
						});

						if (language)
						{
							add(new JPanel(new BorderLayout())
							{
								{
									setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), translation[41], TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_JUSTIFICATION, null, Color.red));
									add(eerabComboBox);
								}
							});

							add(new JPanel()
							{
								{
									setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), translation[36], TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_JUSTIFICATION, null, Color.red));

									final JButton searchByCategory = new JButton(translation[39]);
									searchByCategory.addActionListener((e) ->
									{
										if (searchDialog == null)
										{
											searchDialog = new Search(Quran.this);
											searchDialog.tabbedPane.setSelectedIndex(1);
										}
										else
										{
											searchDialog.setVisible(true);
											searchDialog.tabbedPane.setSelectedIndex(1);
										}
									});

									final JButton searchByWord = new JButton(translation[40]);
									searchByWord.addActionListener((e) ->
									{
										if (searchDialog == null)
											searchDialog = new Search(Quran.this);
										else
										{
											searchDialog.setVisible(true);
											searchDialog.tabbedPane.setSelectedIndex(0);
										}
									});

									add(searchByWord);
									add(searchByCategory);
								}
							});
						}

						add(new JPanel()
						{
							{
								setLayout(new GridBagLayout());
								setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), translation[38], TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_JUSTIFICATION, null, Color.red));

								add(sheekhComboBox, new GridBagConstraints(
										0, 0, 5, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(new JLabel("   "), new GridBagConstraints(
										5, 0, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(jozDecrementButton, new GridBagConstraints(
										6, 0, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(jozComboBox, new GridBagConstraints(
										7, 0, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(jozIncrementButton, new GridBagConstraints(
										10, 0, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

								add(hezpDecrementButton, new GridBagConstraints(
										0, 1, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(hezpComboBox, new GridBagConstraints(
										1, 1, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(hezpIncrementButton, new GridBagConstraints(
										4, 1, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(new JLabel("   "), new GridBagConstraints(
										5, 1, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(pageDecrementButton, new GridBagConstraints(
										6, 1, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(pageComboBox, new GridBagConstraints(
										7, 1, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(pageIncrementButton, new GridBagConstraints(
										10, 1, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

								add(ayaDecrementButton, new GridBagConstraints(
										0, 2, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(ayaComboBox, new GridBagConstraints(
										1, 2, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(ayaIncrementButton, new GridBagConstraints(
										4, 2, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(new JLabel("   "), new GridBagConstraints(
										5, 2, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(suraDecrementButton, new GridBagConstraints(
										6, 2, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(suraComboBox, new GridBagConstraints(
										7, 2, 3, 1, 1.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
								add(suraIncrementButton, new GridBagConstraints(
										10, 2, 1, 1, 0.0, 1.0,
										GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
							}
						});

						add(new JPanel(new BorderLayout())
						{
							{
								setLayout(new FlowLayout(FlowLayout.CENTER, 0, 3));
								if(language)
								{
									add(play_pauseButton);
									add(stopButton);
								}
								else
								{
									add(stopButton);
									add(play_pauseButton);
								}
							}
						});

					}
				}, BorderLayout.NORTH);
			}
		}, language?BorderLayout.EAST:BorderLayout.WEST);
		//}, new GridBagConstraints(
				//0, 0, 1, 1, 0, 0.0,
				//GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		new javafx.embed.swing.JFXPanel(); // It implicitly initializes the JavaFX runtime for the player.

		final ArrayList<Image> images = new ArrayList<>();
		images.add(Toolkit.getDefaultToolkit().createImage("images/icon_64.png"));
		images.add(Toolkit.getDefaultToolkit().createImage("images/icon.png"));
		setIconImages(images);
		setJMenuBar(new JMenuBar()
		{
			{
				add(new JMenu(translation[19]) // File
				{
					{
						add(new JMenuItem(translation[26], new ImageIcon("images/preferences.png"))
						{
							{
								addActionListener((e) ->
								{
									new Setting(Quran.this);
								});
							}
						});

						add(new JMenuItem(translation[22], new ImageIcon("images/selection.png"))
						{
							{
								addActionListener((e) ->
								{
									new QuranPanel(Quran.this);
								});
							}
						});

						addSeparator();

						add(new JMenuItem(translation[21])
						{
							{
								addActionListener((e) -> shutdown());
							}
						});
					}
				});

				add(new JMenu(translation[27]) // Help
				{
					{
						add(new JMenuItem(translation[28], new ImageIcon("images/help.png"))
						{
							{
								addActionListener((e) ->
								{

									try
									{
										Desktop.getDesktop().browse(new URI("https://www.maknoon.com/community/threads/96/")); // Version 2.1
									}
									catch (Exception ex)
									{
										ex.printStackTrace();
									}
								});
							}
						});

						add(new JMenuItem(translation[25], new ImageIcon("images/about.png"))
						{
							{
								addActionListener((e) ->
								{
									new About(Quran.this);
								});
							}
						});
					}
				});

				if (language)
					applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			}
		});

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setBounds(0, 0, screenSize.width, screenSize.height - 40);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				shutdown();
			}
		});

		if (Toolkit.getDefaultToolkit().isFrameStateSupported(MAXIMIZED_BOTH))
			setExtendedState(MAXIMIZED_BOTH);

		if (language)
			getContentPane().applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		// Version 2.1, moved here since drawingPanel is not centered unless it is after applying RIGHT_TO_LEFT
		//setLayout(new GridBagLayout());

		quranPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		quranPanel.add(drawingPanel);
		drawingScrollPane = new JScrollPane(quranPanel);
		quranPanel.setBackground(Color.white);

		//jsc.setLayout(new FlowLayout(FlowLayout.CENTER));
		//jsc.setPreferredSize(new Dimension(drawingPanel.imageWidth, drawingPanel.imageHeight));
		//jsc.getViewport().setBackground(Color.black);

		add(drawingScrollPane, BorderLayout.CENTER);
		//add(drawingScrollPane, new GridBagConstraints(
				//1, 0, 1, 1, 1.0, 1.0,
				//GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		setVisible(true);

		// Set Al-Fateha at the beginning.
		suraComboBox.setSelectedIndex(0);
		fromSura.setSelectedIndex(0);
		toSura.setSelectedIndex(0);

		if (isMAC)
		{
			try
			{
				final Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.APP_ABOUT))
				{
					desktop.setAboutHandler(e ->
					{
						new About(Quran.this);
					});
				}

				if (desktop.isSupported(Desktop.Action.APP_PREFERENCES))
					desktop.setPreferencesHandler(e -> new Setting(Quran.this));

				if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER))
				{
					desktop.setQuitHandler((e, response) ->
					{
						shutdown();
					});
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	void shutdown()
	{
		try
		{
			if (derbyInUse)
				DriverManager.getConnection("jdbc:derby:;shutdown=true"); // It should be the last since it will through exception in normal case "Derby system shutdown."
			else
				//sharedDBConnection.close(); // Close h2 database, No need for it since it will be closed when System.exit(0). Sometimes this statement throughts exception in case search is stopped in the middle then exit.
				sharedDBConnection.createStatement().execute("SHUTDOWN"); // shutdown it here instead of when System.exit(0).
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		System.exit(0);
	}

	public class DrawingPanel extends JPanel
	{
		static Graphics imageGraphics;
		static BufferedImage baseImage, displayImage, filterImage, rolloverImage;
		static int imageWidth, imageHeight;
		static int[] imageData, rolloverData;

		public DrawingPanel()
		{
			//super();

			setLayout(null);
			//setLayout(new FlowLayout(FlowLayout.CENTER));

			/*
			//try
			{
				//displayImage = javax.imageio.ImageIO.read(new File("pages/cover.png"));

				// This to speed up loading. Check [http://forums.sun.com/thread.jspa?threadID=5380606]
				Image image = Toolkit.getDefaultToolkit().createImage(pagesFolder + "/1.png");
				image = new ImageIcon(image).getImage();
				displayImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
				displayImage.createGraphics().drawImage(image, 0, 0, null);
			}
			//catch(IOException e){e.printStackTrace();}

			imageGraphics = displayImage.getGraphics();
			imageWidth = displayImage.getWidth();
			imageHeight = displayImage.getHeight();

			baseImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
			filterImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
			rolloverImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
			imageData = new int[imageWidth * imageHeight];
			rolloverData = new int[imageWidth * imageHeight];

			setPreferredSize(new Dimension(imageWidth, imageHeight));
			*/
		}

		final Vector<Rectangle> ayaRectangles = new Vector<>();
		final Vector<String> ayaLabels = new Vector<>();

		public void setPage(final int page)
		{
			long startTime = System.nanoTime();

			removeAll();
			ayaRectangles.removeAllElements();
			ayaLabels.removeAllElements();

			try
			{
				//baseImage = javax.imageio.ImageIO.read(new File(pagesFolder + "/"+page+".png"));
				Image image = Toolkit.getDefaultToolkit().createImage(pagesFolder + "/" + page + ".png");
				image = new ImageIcon(image).getImage();

				// This is temp code to calculate the dimension of the pages each time (in case pages sizes are not equal). it should be removed once finished since all the pages should have the same size. it can be in the constructor
				displayImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
				displayImage.createGraphics().drawImage(image, 0, 0, null);

				imageGraphics = displayImage.getGraphics();
	        	imageWidth = displayImage.getWidth();
	        	imageHeight = displayImage.getHeight();

	            baseImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	            filterImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	            rolloverImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	            imageData = new int[imageWidth*imageHeight];
	            rolloverData = new int[imageWidth*imageHeight];

	            setPreferredSize(new Dimension(imageWidth, imageHeight));

				SwingUtilities.updateComponentTreeUI(this); // we need this after finishing assigning the x-y (some LaF does not need it e.g. WebLaF)
				//revalidate();
				//repaint();

				quranPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				drawingScrollPane.updateUI();
				//Quran.this.getContentPane().revalidate();
				//Quran.this.getContentPane().repaint();

				baseImage.createGraphics().drawImage(image, 0, 0, Color.WHITE /*getBackground()*/, null);
				imageGraphics.drawImage(baseImage, 0, 0, null);

				filter();

				final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran WHERE Page=" + page);
				while (rs.next())
				{
					final int aya = rs.getInt("Aya");
					final int sura = rs.getInt("Sura");
					final int hezp = rs.getInt("Hezp");

					final StringTokenizer tokens = new StringTokenizer(rs.getString("Location"), "-");
					final int count = tokens.countTokens();

					final int[] x = new int[count];
					final int[] y = new int[count];
					final int[] width = new int[count];
					final int[] height = new int[count];

					for (int i = 0; i < count; i++)
					{
						final StringTokenizer dimensions = new StringTokenizer(tokens.nextToken(), ",");
						if (dimensions.hasMoreTokens())
						{
							x[i] = Integer.parseInt(dimensions.nextToken());
							y[i] = Integer.parseInt(dimensions.nextToken());
							width[i] = Integer.parseInt(dimensions.nextToken());
							height[i] = Integer.parseInt(dimensions.nextToken());
							ayaLabels.add(sura + "-" + aya);
							ayaRectangles.add(new Rectangle(x[i], y[i], width[i], height[i]));
						}
					}

					for (int i = 0; i < count; i++)
					{
						final JPanel a = new JPanel();
						a.setBounds(x[i], y[i], width[i], height[i]);
						add(a);
						a.setOpaque(false);
						a.addMouseListener(new MouseAdapter()
						{
							public void mouseClicked(MouseEvent e)
							{
								if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
								//if(SwingUtilities.isLeftMouseButton(e))
								{
									selectedByMouse = true;
									selectedBySuraButton = false;
									selectedByPageButton = false;
									selectedByAyaButton = false;
									selectedByHezpButton = false;
									selectedByJozButton = false;
									selectedByTahfeed = false;
									selectedBySearch = false;
									selectedSura = sura;
									selectedHezp = hezp;
									SelectionThread(aya);
								}
							}

							public void mouseEntered(MouseEvent e)
							{
								if (aya != (ayaComboBox.getSelectedIndex() + 1) || sura != (suraComboBox.getSelectedIndex() + 1))
								{
									for (int j = 0; j < count; j++)
										imageGraphics.drawImage(rolloverImage.getSubimage(x[j], y[j], width[j], height[j]), x[j], y[j], null);
									repaint();
								}
							}

							public void mouseExited(MouseEvent e)
							{
								if (aya != (ayaComboBox.getSelectedIndex() + 1) || sura != (suraComboBox.getSelectedIndex() + 1))
								{
									for (int j = 0; j < count; j++)
										imageGraphics.drawImage(baseImage.getSubimage(x[j], y[j], width[j], height[j]), x[j], y[j], null);
									repaint();
								}
							}

							public void mouseReleased(final MouseEvent e)
							{
								final Thread thread = new Thread()
								{
									public void run()
									{
										if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
										//if(SwingUtilities.isRightMouseButton(e)) // It works as well
										{
											try
											{
												final JPopupMenu tafseerPopupMenu = new JPopupMenu();
												final Statement stmt = sharedDBConnection.createStatement();
												int i = tafseerComboBox.getSelectedIndex() + 1;
												String t = (i < 10 ? "0" : "") + i;
												ResultSet rs = stmt.executeQuery((language ? ("SELECT Tafseer FROM ar_" + t) : ("SELECT Translations FROM en_" + t)) + " WHERE Sura=" + sura + " AND Aya=" + aya);
												if (rs.next())
												{
	                                                /*
                                                    final ResultSetMetaData rsMetaData = rs.getMetaData();
                                                    final int numberOfColumns = rsMetaData.getColumnCount();

                                                    // Get the column names; column indexes start from 1
                                                    for(int i=1; i <=numberOfColumns; i++)
                                                    {
                                                        final String name = rsMetaData.getColumnName(i);
                                                        if((language && name.contains("تفسير")) || (!language && name.startsWith("Translation")))
                                                        {

                                                            final JMenu tafseerMenuItem = new JMenu(name);
                                                            final Reader description = rs.getCharacterStream(i);
                                                            */

															final JMenu tafseerMenuItem = new JMenu(language ? "التفسير" : "Translation");
															final Reader description = rs.getCharacterStream(1);

															final char[] arr = new char[4 * 1024]; // 4K at a time
															final StringBuilder buf = new StringBuilder();
															int numChars;

															while ((numChars = description.read(arr, 0, arr.length)) > 0)
																buf.append(arr, 0, numChars);

															final JTextArea tafseerTextArea = new JTextArea(buf.toString());
															tafseerTextArea.setLineWrap(true);
															tafseerTextArea.setWrapStyleWord(true);
															tafseerTextArea.setEnabled(false);
															//tafseerTextArea.setFont(new Font("KFGQPC Uthman Taha Naskh", Font.PLAIN, 24));
															tafseerTextArea.setFont(new Font("Scheherazade New", Font.PLAIN, 24)); // Version 2.1
															final JScrollPane sp = new JScrollPane(tafseerTextArea);
															sp.setPreferredSize(new Dimension(800, 400));
															tafseerMenuItem.add(sp);
															tafseerPopupMenu.add(tafseerMenuItem);
													    //}
													//}
												}

												if (language)
												{
													i = eerabComboBox.getSelectedIndex() + 1;
													t = (i < 10 ? "0" : "") + i;
													rs = stmt.executeQuery("SELECT Eerab FROM er_" + t + " WHERE Sura=" + sura + " AND Aya=" + aya);
													if (rs.next())
													{
														final JMenu eerabMenuItem = new JMenu("الإعراب");
														final Reader description = rs.getCharacterStream(1);

														final char[] arr = new char[4 * 1024]; // 4K at a time
														final StringBuilder buf = new StringBuilder();
														int numChars;

														while ((numChars = description.read(arr, 0, arr.length)) > 0)
															buf.append(arr, 0, numChars);

														final JTextArea eerabTextArea = new JTextArea(buf.toString());
														eerabTextArea.setLineWrap(true);
														eerabTextArea.setWrapStyleWord(true);
														eerabTextArea.setEnabled(false);
														//eerabTextArea.setFont(new Font("KFGQPC Uthman Taha Naskh", Font.PLAIN, 24));
														eerabTextArea.setFont(new Font("Scheherazade New", Font.PLAIN, 24)); // Version 2.1

														final JScrollPane sp = new JScrollPane(eerabTextArea);
														sp.setPreferredSize(new Dimension(600, 300));
														eerabMenuItem.add(sp);
														tafseerPopupMenu.add(eerabMenuItem);
													}
												}

												if (tafseerPopupMenu.getSubElements().length != 0)
												{
													SwingUtilities.invokeLater(() ->
													{
														if (language)
														{
															tafseerPopupMenu.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
															tafseerPopupMenu.updateUI(); // For getPreferredSize() to return correct value
															tafseerPopupMenu.show(a, e.getX() - tafseerPopupMenu.getPreferredSize().width + 2, e.getY());
														}
														else
															tafseerPopupMenu.show(a, e.getX(), e.getY());
													});
												}

												stmt.close();
											}
											catch (Exception ex)
											{
												ex.printStackTrace();
											}
										}
									}
								};
								thread.start();
							}
						});
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			System.out.println("Time6: " + ((System.nanoTime() - startTime) / 1000000000.0));
		}

		public void playAya(final int aya, final int sura)
		{
			final String file;
			if (aya == 0) // Basmalah
				file = "001001.mp3";
			else
			{
				imageGraphics.drawImage(baseImage, 0, 0, null);

				int size = ayaLabels.size();
				boolean firstTime = true;
				for (int i = 0; i < size; i++)
				{
					if (ayaLabels.elementAt(i).equals(sura + "-" + aya))
					{
						final Rectangle rec = ayaRectangles.elementAt(i);
						int x = (int) rec.getX();
						int y = (int) rec.getY();
						imageGraphics.drawImage(filterImage.getSubimage(x, y, (int) rec.getWidth(), (int) rec.getHeight()), x, y, null);

						if (firstTime)
						{
							drawingPanel.scrollRectToVisible(rec);
							firstTime = false;
						}
					}
				}

				//for(int j=0; j<count; j++) imageGraphics.drawImage(filterImage.getSubimage(x[j], y[j], width[j], height[j]), x[j], y[j], null);
				repaint();

				// Formatting the audio file name
				file = ((sura < 10) ? ("00" + sura) : ((sura < 100) ? ("0" + sura) : sura)) + String.valueOf((aya < 10) ? ("00" + aya) : ((aya < 100) ? ("0" + aya) : aya)) + '.' + sheekhTypes.elementAt(sheekhComboBox.getSelectedIndex());
			}

			//final Thread thread = new Thread()
			//{
			//	public void run()
			//	{
					final Media media;
					if (internetStreaming)
						media = new Media("https://www.everyayah.com/data/" + sheekhFolders.elementAt(sheekhComboBox.getSelectedIndex()) + '/' + file);
					else
						media = new Media(new File(audioLocation + '/' + sheekhFolders.elementAt(sheekhComboBox.getSelectedIndex()) + '/' + file).toURI().toString());

					if (player != null) player.stop(); // To stop the old one
					player = null; // Even with this, the player instance is still running even if we re-assign it or stop/null. it seems it is natively threaded.
					player = new MediaPlayer(media);
					player.setOnEndOfMedia(() ->
					{
						// SwingUtilities.invokeLater solves the issue for rendering since it is inside a thread
						if (!startTahfeedButton.isEnabled() && ayaComboBox.getSelectedIndex() == toAya.getSelectedIndex() && suraComboBox.getSelectedIndex() == toSura.getSelectedIndex())
						{
							//startTahfeedButton.doClick();
							selectedBySuraButton = false;
							selectedByPageButton = false;
							selectedByAyaButton = false;
							selectedByHezpButton = false;
							selectedByJozButton = false;
							selectedByMouse = false;
							selectedBySearch = false;
							selectedByTahfeed = true;
							SwingUtilities.invokeLater(() -> SelectionThread(-1));
						}
						else
						{
							final int ayaIndex = ayaComboBox.getSelectedIndex() + 1;
							if (ayaIndex >= ayaComboBox.getItemCount())
							{
								final int suraIndex = suraComboBox.getSelectedIndex() + 1;
								if (suraIndex > 113)
									player.stop();
								else
								{
									// Basmalah except for Twpah (Baraa'ah)
									if (suraIndex != 8 && aya != 0) // '&& suraIndex!=0' not important since it will not reach 0. '&& aya!=0' to not repeat basmalah in a loop.
										playAya(0, suraIndex); // special case to run basmalah without changing anything else
									else
										SwingUtilities.invokeLater(() -> suraComboBox.setSelectedIndex(suraIndex));
								}
							}
							else
								SwingUtilities.invokeLater(() -> ayaComboBox.setSelectedIndex(ayaIndex));
						}
					});

					player.setOnPlaying(() ->
					{
						play_pauseButton.setIcon(new ImageIcon("images/pause.png"));
						play_pauseButton.setRolloverIcon(new ImageIcon("images/pause_rollover.png"));
						play_pauseButton.setPressedIcon(new ImageIcon("images/pause_pressed.png"));

					});

					player.setOnPaused(() ->
					{
						play_pauseButton.setIcon(new ImageIcon("images/play.png"));
						play_pauseButton.setRolloverIcon(new ImageIcon("images/play_rollover.png"));
						play_pauseButton.setPressedIcon(new ImageIcon("images/play_pressed.png"));
					});

					player.setOnStalled(() ->
					{
						play_pauseButton.setIcon(new ImageIcon("images/pause_stalled.png"));
						play_pauseButton.setRolloverIcon(new ImageIcon("images/pause_rollover_stalled.png"));
						play_pauseButton.setPressedIcon(new ImageIcon("images/pause_pressed_stalled.png"));
					});

					player.setOnReady(() ->
					{
						play_pauseButton.setIcon(new ImageIcon("images/play.png"));
						play_pauseButton.setRolloverIcon(new ImageIcon("images/play_rollover.png"));
						play_pauseButton.setPressedIcon(new ImageIcon("images/play_pressed.png"));
					});

					player.setOnHalted(() ->
					{
						play_pauseButton.setIcon(new ImageIcon("images/play.png"));
						play_pauseButton.setRolloverIcon(new ImageIcon("images/play_rollover.png"));
						play_pauseButton.setPressedIcon(new ImageIcon("images/play_pressed.png"));
					});

					player.setOnStopped(() ->
					{
						play_pauseButton.setIcon(new ImageIcon("images/play.png"));
						play_pauseButton.setRolloverIcon(new ImageIcon("images/play_rollover.png"));
						play_pauseButton.setPressedIcon(new ImageIcon("images/play_pressed.png"));
					});

					player.setOnError(() ->
					{
						// we cannot get the error message even with MediaException !
						final String[] translation = StreamConverter("language/" + ((language) ? "DrawingPanelArabic.txt" : "DrawingPanelEnglish.txt"));
						if (internetStreaming)
							JOptionPane.showOptionDialog(getContentPane(), translation[0], translation[1], JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{translation[2]}, translation[2]);
						else
							JOptionPane.showOptionDialog(getContentPane(), translation[3], translation[1], JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{translation[2]}, translation[2]);

						play_pauseButton.setIcon(new ImageIcon("images/play.png"));
						play_pauseButton.setRolloverIcon(new ImageIcon("images/play_rollover.png"));
						play_pauseButton.setPressedIcon(new ImageIcon("images/play_pressed.png"));
					});

					player.play();
			//	}
			//};
			//thread.start();
		}

		public void filter()
		{
			//try
			{
				//PixelGrabber grabber = new PixelGrabber(baseImage, 0, 0, -1, -1, true);
				//if(grabber.grabPixels())
				{
					//int width = grabber.getWidth();
					//int height = grabber.getHeight();
					//int[] data = (int[])grabber.getPixels();
					//int[] data = baseImage.getRGB(0, 0, imageWidth, imageHeight, null, 0, imageWidth); // Not faster than PixelGrabber. Check the performance.
					//int[] data = (int[])baseImage.getData().getDataElements(0, 0, imageWidth, imageHeight, null);
					//int[] data = new int[imageWidth*imageHeight]; // Done in the begining
					//baseImage.getRGB(0, 0, imageWidth, imageHeight, data, 0, imageWidth);
					baseImage.getData().getDataElements(0, 0, imageWidth, imageHeight, imageData); // some improvement
					//int[] rolloverData = imageData.clone();
					System.arraycopy(imageData, 0, rolloverData, 0, imageData.length); // faster than clone

					for (int i = 0; i < imageData.length; i++)
					{
						//if(imageData[i] != -1) // -1 -> Transparent
						if ((imageData[i] & 0x000000ff) < 128)
						{
							imageData[i] = Integer.rotateLeft((imageData[i] & 0x000000ff) + 128, 16) | 0xff000000;
							rolloverData[i] = Integer.rotateLeft((rolloverData[i] & 0x000000ff) + 128, 17) | 0xff000000;
							//data[i]=Integer.rotateLeft(255-(data[i]&0x000000ff), 16)|0xff000000;

							//imageData[i]=16711680; // 16711680 -> Red
							//rolloverData[i]=10027008;
						}
					}

					//Image copyImage = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(imageWidth, imageHeight, data, 0, imageWidth));
					//copyImage = new ImageIcon(copyImage).getImage();

	        		/*   No affect in performance. check
	        		 * http://stackoverflow.com/questions/658059/graphics-drawimage-in-java-is-extremely-slow-on-some-computers-yet-much-faster
	        		 * http://helpdesk.objects.com.au/java/how-to-convert-an-image-to-a-bufferedimage
	        		 *
	        		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
				    GraphicsDevice device = env.getDefaultScreenDevice();
				    GraphicsConfiguration config = device.getDefaultConfiguration();
				    filterImage = config.createCompatibleImage(copyImage.getWidth(null), copyImage.getHeight(null), Transparency.OPAQUE);
				    filterImage.createGraphics().drawImage(copyImage, 0, 0,  null);
        			*/

					//filterImage.createGraphics().drawImage(copyImage, 0, 0,  null);
					filterImage.getRaster().setDataElements(0, 0, imageWidth, imageHeight, imageData); // some improvement than normal drawImage()

					//Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(imageWidth, imageHeight, rolloverData, 0, imageWidth));
					//image = new ImageIcon(image).getImage();
					//rolloverImage.createGraphics().drawImage(image, 0, 0,  null);
					rolloverImage.getRaster().setDataElements(0, 0, imageWidth, imageHeight, rolloverData);
				}
			}
			//catch(InterruptedException e){e.printStackTrace();}
		}

		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
            /*
            int w = getWidth();
	        int h = getHeight();
	        int x = (w - imageWidth)/2;
	        int y = (h - imageHeight)/2;
	        g.drawImage(displayImage, x, y, this);
	        */
			g.drawImage(displayImage, 0, 0, this);

			/*
			final Graphics2D g2d = (Graphics2D) g.create();
			final int x = (getWidth() - displayImage.getWidth()) / 2;
			int y = (getHeight() - displayImage.getHeight()) / 2;
			g2d.drawImage(displayImage, x, y, this);
			g2d.dispose();
			*/
		}
	}

	void SelectionThread(final int index)
	{
		try
		{
			if (selectedByAyaButton)
			{
				// No need for SwingUtilities.invokeLater(new Runnable(){public void run(){}}); since it is in the AWT event dispatch thread.
				//SwingUtilities.updateComponentTreeUI(ayaComboBox); // Only when selectedByAyaButton since ayaComboBox.doClick() is the only one which is called without manual click.
				//ayaComboBox.updateUI();
				//ayaComboBox.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

				selectedAya = index;
				final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran WHERE Sura=" + selectedSura + " AND Aya=" + selectedAya);
				rs.next();

				final int page = rs.getInt("Page");
				final int hezp = rs.getInt("Hezp");
				final int joz = rs.getInt("Part");

				if (selectedPage != page)
				{
					selectedPage = page;
					cancelPageListener = true;
					pageComboBox.setSelectedIndex(selectedPage - 1);
					cancelPageListener = false;
					drawingPanel.setPage(selectedPage);
					//SwingUtilities.updateComponentTreeUI(drawingPanel);
					//SwingUtilities.updateComponentTreeUI(pageComboBox);
					//drawingPanel.updateUI();
					//pageComboBox.updateUI();
				}

				if (selectedHezp != hezp)
				{
					selectedHezp = hezp;
					cancelHezpListener = true;
					hezpComboBox.setSelectedIndex(selectedHezp - 1);
					cancelHezpListener = false;
					//SwingUtilities.updateComponentTreeUI(hezpComboBox);
					//hezpComboBox.updateUI();
				}

				if (selectedJoz != joz)
				{
					selectedJoz = joz;
					cancelJozListener = true;
					jozComboBox.setSelectedIndex(selectedJoz - 1);
					cancelJozListener = false;
					//SwingUtilities.updateComponentTreeUI(jozComboBox);
					//jozComboBox.updateUI();
				}

				drawingPanel.playAya(selectedAya, selectedSura);
			}
			else
			{
				if (selectedBySuraButton)
				{
					selectedSura = index;
					selectedAya = 1;
					ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT MAX(Aya) AS AyaCount FROM Quran WHERE Sura=" + selectedSura);
					rs.next();
					final int ayaCount = rs.getInt("AyaCount");

					rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran WHERE Sura=" + selectedSura + " AND Aya=1");
					rs.next();

					final int page = rs.getInt("Page");
					final int hezp = rs.getInt("Hezp");
					final int joz = rs.getInt("Part");

					ayaComboBox.removeAllItems();
					cancelAyaListener = true;
					for (int i = 0; i < ayaCount; i++)
						ayaComboBox.addItem((language ? "الآية " : "Aya ") + (i + 1)); // The first item will be selected automatically
					cancelAyaListener = false;

					if (selectedPage != page)
					{
						selectedPage = page;
						cancelPageListener = true;
						pageComboBox.setSelectedIndex(selectedPage - 1);
						cancelPageListener = false;
						drawingPanel.setPage(selectedPage);
					}

					if (selectedHezp != hezp)
					{
						selectedHezp = hezp;
						cancelHezpListener = true;
						hezpComboBox.setSelectedIndex(selectedHezp - 1);
						cancelHezpListener = false;
					}

					if (selectedJoz != joz)
					{
						selectedJoz = joz;
						cancelJozListener = true;
						jozComboBox.setSelectedIndex(selectedJoz - 1);
						cancelJozListener = false;
					}

					drawingPanel.playAya(selectedAya, selectedSura);
				}
				else
				{
					if (selectedByPageButton)
					{
						selectedPage = index;
						ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran WHERE Page=" + selectedPage + " ORDER BY Sura, Aya");
						rs.next(); // We must get the first Sura,Aya to display the correct page, other wise use ORDER BY Sura, Aya. already done while creating the db. note, it is not working unless using ORDER BY

						final int sura = rs.getInt("Sura");
						selectedAya = rs.getInt("Aya");
						final int hezp = rs.getInt("Hezp");
						final int joz = rs.getInt("Part");

						drawingPanel.setPage(selectedPage);

						if (selectedSura != sura)
						{
							rs = sharedDBConnection.createStatement().executeQuery("SELECT MAX(Aya) AS AyaCount FROM Quran WHERE Sura=" + sura);
							rs.next();
							final int ayaCount = rs.getInt("AyaCount");

							selectedSura = sura;
							cancelSuraListener = true;
							suraComboBox.setSelectedIndex(selectedSura - 1);
							cancelSuraListener = false;

							ayaComboBox.removeAllItems();
							cancelAyaListener = true;
							for (int i = 0; i < ayaCount; i++)
								ayaComboBox.addItem((language ? "الآية " : "Aya ") + (i + 1));
							cancelAyaListener = false;
						}

						cancelAyaListener = true;
						ayaComboBox.setSelectedIndex(selectedAya - 1);
						cancelAyaListener = false;

						if (selectedHezp != hezp)
						{
							selectedHezp = hezp;
							cancelHezpListener = true;
							hezpComboBox.setSelectedIndex(selectedHezp - 1);
							cancelHezpListener = false;
						}

						if (selectedJoz != joz)
						{
							selectedJoz = joz;
							cancelJozListener = true;
							jozComboBox.setSelectedIndex(selectedJoz - 1);
							cancelJozListener = false;
						}

						drawingPanel.playAya(selectedAya, selectedSura);
					}
					else
					{
						if (selectedByHezpButton)
						{
							selectedHezp = index;
							ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Parts WHERE Hezp=" + selectedHezp);
							rs.next();

							final int sura = rs.getInt("Sura");
							selectedAya = rs.getInt("Aya");
							final int page = rs.getInt("Page");
							final int joz = rs.getInt("Part");

							if (selectedSura != sura)
							{
								rs = sharedDBConnection.createStatement().executeQuery("SELECT MAX(Aya) AS AyaCount FROM Quran WHERE Sura=" + sura);
								rs.next();
								final int ayaCount = rs.getInt("AyaCount");

								selectedSura = sura;
								cancelSuraListener = true;
								suraComboBox.setSelectedIndex(selectedSura - 1);
								cancelSuraListener = false;

								ayaComboBox.removeAllItems();
								cancelAyaListener = true;
								for (int i = 0; i < ayaCount; i++)
									ayaComboBox.addItem((language ? "الآية " : "Aya ") + (i + 1));
								cancelAyaListener = false;
							}

							cancelAyaListener = true;
							ayaComboBox.setSelectedIndex(selectedAya - 1);
							cancelAyaListener = false;

							if (selectedPage != page)
							{
								selectedPage = page;
								cancelPageListener = true;
								pageComboBox.setSelectedIndex(selectedPage - 1);
								cancelPageListener = false;
								drawingPanel.setPage(selectedPage);
							}

							if (selectedJoz != joz)
							{
								selectedJoz = joz;
								cancelJozListener = true;
								jozComboBox.setSelectedIndex(selectedJoz - 1);
								cancelJozListener = false;
							}

							drawingPanel.playAya(selectedAya, selectedSura);
						}
						else
						{
							if (selectedByJozButton)
							{
								selectedJoz = index;
								ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Parts WHERE Part=" + selectedJoz + " ORDER BY Hezp");
								rs.next();

								final int sura = rs.getInt("Sura");
								selectedAya = rs.getInt("Aya");
								final int page = rs.getInt("Page");
								final int hezp = rs.getInt("Hezp");

								if (selectedSura != sura)
								{
									rs = sharedDBConnection.createStatement().executeQuery("SELECT MAX(Aya) AS AyaCount FROM Quran WHERE Sura=" + sura);
									rs.next();
									final int ayaCount = rs.getInt("AyaCount");

									selectedSura = sura;
									cancelSuraListener = true;
									suraComboBox.setSelectedIndex(selectedSura - 1);
									cancelSuraListener = false;

									ayaComboBox.removeAllItems();
									cancelAyaListener = true;
									for (int i = 0; i < ayaCount; i++)
										ayaComboBox.addItem((language ? "الآية " : "Aya ") + (i + 1));
									cancelAyaListener = false;
								}

								cancelAyaListener = true;
								ayaComboBox.setSelectedIndex(selectedAya - 1);
								cancelAyaListener = false;

								if (selectedPage != page)
								{
									selectedPage = page;
									cancelPageListener = true;
									pageComboBox.setSelectedIndex(selectedPage - 1);
									cancelPageListener = false;
									drawingPanel.setPage(selectedPage);
								}

								if (selectedHezp != hezp)
								{
									selectedHezp = hezp;
									cancelHezpListener = true;
									hezpComboBox.setSelectedIndex(selectedHezp - 1);
									cancelHezpListener = false;
								}

								drawingPanel.playAya(selectedAya, selectedSura);
							}
							else
							{
								if (selectedByMouse)
								{
									selectedAya = index;

									if (selectedHezp != (hezpComboBox.getSelectedIndex() + 1))
									{
										cancelHezpListener = true;
										hezpComboBox.setSelectedIndex(selectedHezp - 1);
										cancelHezpListener = false;
									}

									if (selectedSura != (suraComboBox.getSelectedIndex() + 1))
									{
										final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT MAX(Aya) AS AyaCount FROM Quran WHERE Sura=" + selectedSura);
										rs.next();
										final int ayaCount = rs.getInt("AyaCount");

										cancelSuraListener = true;
										suraComboBox.setSelectedIndex(selectedSura - 1);
										cancelSuraListener = false;

										ayaComboBox.removeAllItems();
										cancelAyaListener = true;
										for (int i = 0; i < ayaCount; i++)
											ayaComboBox.addItem((language ? "الآية " : "Aya ") + (i + 1));
										cancelAyaListener = false;
									}

									if (selectedAya != (ayaComboBox.getSelectedIndex() + 1))
									{
										cancelAyaListener = true;
										ayaComboBox.setSelectedIndex(selectedAya - 1);
										cancelAyaListener = false;
									}

									drawingPanel.playAya(selectedAya, selectedSura);
								}
								else
								{
									if (selectedByTahfeed)
									{
										//index = -1; Does not matter
										selectedAya = fromAya.getSelectedIndex() + 1;
										selectedSura = fromSura.getSelectedIndex() + 1;

										ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran WHERE Sura=" + selectedSura + " AND Aya=" + selectedAya);
										rs.next();

										final int page = rs.getInt("Page");
										final int hezp = rs.getInt("Hezp");
										final int joz = rs.getInt("Part");

										if (selectedSura != (suraComboBox.getSelectedIndex() + 1))
										{
											rs = sharedDBConnection.createStatement().executeQuery("SELECT MAX(Aya) AS AyaCount FROM Quran WHERE Sura=" + selectedSura);
											rs.next();
											final int ayaCount = rs.getInt("AyaCount");

											cancelSuraListener = true;
											suraComboBox.setSelectedIndex(selectedSura - 1);
											cancelSuraListener = false;

											ayaComboBox.removeAllItems();
											cancelAyaListener = true;
											for (int i = 0; i < ayaCount; i++)
												ayaComboBox.addItem((language ? "الآية " : "Aya ") + (i + 1));
											cancelAyaListener = false;
										}

										if (selectedAya != (ayaComboBox.getSelectedIndex() + 1))
										{
											cancelAyaListener = true;
											ayaComboBox.setSelectedIndex(selectedAya - 1);
											cancelAyaListener = false;
										}

										if (selectedPage != page)
										{
											selectedPage = page;
											cancelPageListener = true;
											pageComboBox.setSelectedIndex(selectedPage - 1);
											cancelPageListener = false;
											drawingPanel.setPage(selectedPage);
										}

										if (selectedHezp != hezp)
										{
											selectedHezp = hezp;
											cancelHezpListener = true;
											hezpComboBox.setSelectedIndex(selectedHezp - 1);
											cancelHezpListener = false;
										}

										if (selectedJoz != joz)
										{
											selectedJoz = joz;
											cancelJozListener = true;
											jozComboBox.setSelectedIndex(selectedJoz - 1);
											cancelJozListener = false;
										}

										drawingPanel.playAya(selectedAya, selectedSura);
									}
									else
									{
										if (selectedBySearch)
										{
											final String s = String.valueOf(index); // index is sura+aya. last 3 digits should be aya
											final String aya = s.substring(s.length() - 3);
											final String sura = s.substring(0, s.length() - 3);

											selectedAya = Integer.parseInt(aya);
											selectedSura = Integer.parseInt(sura);

											ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran WHERE Sura=" + selectedSura + " AND Aya=" + selectedAya);
											rs.next();

											final int page = rs.getInt("Page");
											final int hezp = rs.getInt("Hezp");
											final int joz = rs.getInt("Part");

											if (selectedSura != (suraComboBox.getSelectedIndex() + 1))
											{
												rs = sharedDBConnection.createStatement().executeQuery("SELECT MAX(Aya) AS AyaCount FROM Quran WHERE Sura=" + selectedSura);
												rs.next();
												final int ayaCount = rs.getInt("AyaCount");

												cancelSuraListener = true;
												suraComboBox.setSelectedIndex(selectedSura - 1);
												cancelSuraListener = false;

												ayaComboBox.removeAllItems();
												cancelAyaListener = true;
												for (int i = 0; i < ayaCount; i++)
													ayaComboBox.addItem((language ? "الآية " : "Aya ") + (i + 1));
												cancelAyaListener = false;
											}

											if (selectedAya != (ayaComboBox.getSelectedIndex() + 1))
											{
												cancelAyaListener = true;
												ayaComboBox.setSelectedIndex(selectedAya - 1);
												cancelAyaListener = false;
											}

											if (selectedPage != page)
											{
												selectedPage = page;
												cancelPageListener = true;
												pageComboBox.setSelectedIndex(selectedPage - 1);
												cancelPageListener = false;
												drawingPanel.setPage(selectedPage);
											}

											if (selectedHezp != hezp)
											{
												selectedHezp = hezp;
												cancelHezpListener = true;
												hezpComboBox.setSelectedIndex(selectedHezp - 1);
												cancelHezpListener = false;
											}

											if (selectedJoz != joz)
											{
												selectedJoz = joz;
												cancelJozListener = true;
												jozComboBox.setSelectedIndex(selectedJoz - 1);
												cancelJozListener = false;
											}

											drawingPanel.playAya(selectedAya, selectedSura);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/*
	public synchronized void playAya(int aya)
	{
		try
		{
			final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT Page FROM Quran WHERE Sura="+selectedSura+" AND Aya="+(aya+1));
			rs.next();
			pageSelectedManually = false; // To avoid the loop i.e. aya is selecting page and pageComboBox is selecting ayaComboBox
			pageComboBox.setSelectedIndex(rs.getInt("Page")-1);
			pageSelectedManually = true;
			drawingPanel.playAya(aya+1, selectedSura);
			waitFor = "EOF code: 1"; // Check other conditions 'EOF code: 2'
			blockQueue.take(); // To stop here until the file is finished.
			waitFor = "$^";
			Thread.sleep(800); // Adjusted.
			ayaIncrementButton.doClick();
		}
		catch(SQLException ae){ae.printStackTrace();}
		catch(InterruptedException ie){ie.printStackTrace();}
	}
	*/

	/*
	// This class is to render the JComboBox to display a label when the index is -1
	class CellRenderer extends JLabel implements ListCellRenderer   // javax.swing.plaf.basic.BasicComboBoxRenderer instead of JLabel
	{
		String label;
		public CellRenderer(String label)
		{
			setOpaque(true);
			this.label = label;
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			if(isSelected)
			{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else
			{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			if(index ==-1)
				setText(label);
			else
				setText(value.toString());
			return this;
		}
	}
	*/

	public static String[] StreamConverter(final String filePath)
	{
		try
		{
			final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
			final Vector<String> lines = new Vector<>();

			while (in.ready()) lines.addElement(in.readLine());
			in.close();

			return lines.toArray(new String[lines.size()]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.exit(0);
		return null;
	}

	public static void centerInScreen(final Component component)
	{
		final Rectangle bounds = component.getBounds();
		component.setLocation((screenSize.width - bounds.width) / 2, (screenSize.height - bounds.height) / 2);
	}

	static String programFolder;

	public static void main(final String[] args)
	{
		try
		{
			programFolder = new File(Quran.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath() + "/";
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		com.formdev.flatlaf.FlatLightLaf.setup();
		UIManager.put("TitlePane.menuBarEmbedded", false);
		UIManager.put("Button.arc", 0);
		UIManager.put("MenuItem.selectionType", "underline");
		UIManager.put("OptionPane.maxCharactersPerLine", 0);

		SwingUtilities.invokeLater(Quran::new);
	}
}