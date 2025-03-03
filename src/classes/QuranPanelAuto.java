package classes;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import static classes.Quran.derbyInUse;

class QuranPanelAuto extends JFrame
{
	Connection sharedDBConnection;

	private int startX, startY, endX, endY;
	private final Vector<Integer> db_startX_v = new Vector<>();
	private final Vector<Integer> db_startY_v = new Vector<>();
	private final Vector<Integer> db_width_v = new Vector<>();
	private final Vector<Integer> db_height_v = new Vector<>();
	private final Vector<Boolean> db_aya_end = new Vector<>();
	private final JComboBox<?> pageComboBox;
	int w, h;
	BufferedImage bufferedImage;
	JLabel page;

	int paddingHafs;

	ArrayList<Integer> ignoredPages = new ArrayList<>(
			Arrays.asList(
					50, 77, 106, 128, 151, 177, 187, 208,
					221, 235, 249, 255, 262, 267, 282, 293,
					305, 312, 322, 332, 342, 350, 359, 367,
					377, 385, 396, 404, 411, 415, 418, 428,
					434, 440, 446, 453, 458, 467, 477, 483,
					489, 496, 499, 502, 507, 511, 515, 518,
					520, 523, 526, 528, 531, 534, 537, 542,
					545, 549, 551, 553, 554, 556, 558, 560,
					562, 564, 566, 568, 570, 572, 574, 575,
					577, 578, 580, 582, 583, 585, 586, 587,
					589, 590, 591, 592, 593, 594, 595, 596,
					597, 598, 599, 600, 601, 602, 603, 604
			));

	/*
	final int[][] ignoredLines = {
			{1, 2}, {1, 2}, {6, 7}, {1, 2}, {1, 2}, {1, 2}, {1}, {1, 2},
			{7, 8}, {9, 10}, {1, 2}, {3, 4}, {1, 2}, {7, 8}, {1, 2}, {10, 11},
			{1, 2}, {5, 6}, {1, 2}, {1, 2}, {1, 2}, {1, 2}, {11, 12}, {1, 2},
			{1, 2}, {8, 9}, {8, 9}, {10, 11}, {1, 2}, {1, 2}, {1, 2}, {1, 2},
			{8, 9}, {4, 5}, {1, 2}, {1, 2}, {4, 5}, {3, 4}, {1, 2}, {1, 2},
			{5, 6}, {1, 2}, {1, 2}, {7, 8}, {1, 2}, {1, 2}, {7, 8}, {1, 2},
			{12, 13}, {8, 9}, {1, 2}, {10, 11}, {5, 6}, {7, 8}, {11, 12}, {1, 2},
			{7, 8}, {1, 2}, {7, 8}, {1, 2}, {7, 8}, {1, 2}, {1, 2}, {1, 2},
			{1, 2}, {6, 7}, {10, 11}, {9, 10}, {5, 6}, {1, 2}, {1, 2}, {8, 9},
			{6, 7}, {10, 11}, {7, 8}, {1, 2}, {8, 9}, {1, 2}, {2, 3}, {1, 2, 12, 13},
			{3, 4}, {2, 3}, {1, 2, 10, 11}, {5, 6}, {3, 4}, {6, 7}, {2, 3, 11, 12}, {6, 7, 13, 14},
			{3, 4, 9, 10}, {4, 5, 9, 10}, {6, 7, 12, 13}, {4, 5, 11, 12}, {1, 2, 5, 6, 11, 12}, {1, 2, 6, 7, 12, 13}, {1, 2, 6, 7, 11, 12}, {1, 2, 5, 6, 10, 11}
	};
	*/

	final int[][] basmalahLines = {
			{2}, {2}, {7}, {2}, {2}, {2}, {0}, {2},
			{8}, {10}, {2}, {4}, {2}, {8}, {2}, {11},
			{2}, {6}, {2}, {2}, {2}, {2}, {12}, {2},
			{2}, {9}, {9}, {11}, {2}, {2}, {2}, {2},
			{9}, {5}, {2}, {2}, {5}, {4}, {2}, {2},
			{6}, {2}, {2}, {8}, {2}, {2}, {8}, {2},
			{13}, {9}, {2}, {11}, {6}, {8}, {12}, {2},
			{8}, {2}, {8}, {2}, {8}, {2}, {2}, {2},
			{2}, {7}, {11}, {10}, {6}, {2}, {2}, {9},
			{7}, {11}, {8}, {2}, {9}, {2}, {3}, {2, 13},
			{4}, {3}, {2, 11}, {6}, {4}, {7}, {3, 12}, {7, 14},
			{4, 10}, {5, 10}, {7, 13}, {5, 12}, {2, 6, 12}, {2, 7, 13}, {2, 7, 12}, {2, 6, 11}
	};

	final int[][] suraNamesLines = {
			{1}, {1}, {6}, {1}, {1}, {1}, {1}, {1},
			{7}, {9}, {1}, {3}, {1}, {7}, {1}, {10},
			{1}, {5}, {1}, {1}, {1}, {1}, {11}, {1},
			{1}, {8}, {8}, {10}, {1}, {1}, {1}, {1},
			{8}, {4}, {1}, {1}, {4}, {3}, {1}, {1},
			{5}, {1}, {1}, {7}, {1}, {1}, {7}, {1},
			{12}, {8}, {1}, {10}, {5}, {7}, {11}, {1},
			{7}, {1}, {7}, {1}, {7}, {1}, {1}, {1},
			{1}, {6}, {10}, {9}, {5}, {1}, {1}, {8},
			{6}, {10}, {7}, {1}, {8}, {1}, {2}, {1, 12},
			{3}, {2}, {1, 10}, {5}, {3}, {6}, {2, 11}, {6, 13},
			{3, 9}, {4, 9}, {6, 12}, {4, 11}, {1, 5, 11}, {1, 6, 12}, {1, 6, 11}, {1, 5, 10}
	};

	QuranPanelAuto()
	{
		super("التخطيط");
		setLayout(new BorderLayout());

		page = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);

				g.setColor(Color.BLACK);

				for (int i = 0; i < db_startX_v.size(); i++)
					g.drawRect(db_startX_v.elementAt(i), db_startY_v.elementAt(i), db_width_v.elementAt(i), db_height_v.elementAt(i));

				//g.setColor(Color.RED);
				//g.drawRect(startX, startY, endX - startX, endY - startY);
			}
		};
		page.setBorder(null);
		page.setAutoscrolls(true);

		final Vector<Integer> pages = new Vector<>();
		for (int i = 1; i < 605; i++)
			pages.addElement(i);
		pageComboBox = new JComboBox<>(pages);
		pageComboBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final int index = ((JComboBox<?>) e.getSource()).getSelectedIndex() + 1;
				if (index != 0)
				{
					processPage(index);
				}
			}
		});

		final JButton updateDB = new JButton("بناء البيانات");
		updateDB.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					final String programFolder = new File(Quran.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath() + "/";

					final String dbURL;
					if (derbyInUse)
					{
						dbURL = "jdbc:derby:db";
						Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
					}
					else
					{
						dbURL = "jdbc:h2:" + programFolder + "db/quranDatabase";
						Class.forName("org.h2.Driver");
					}

					sharedDBConnection = DriverManager.getConnection(dbURL);

					for(int p = 3; p <= 604; p++)
					{
						processPage(p);

						final Statement stmt = sharedDBConnection.createStatement();
						final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT Sura, Aya FROM Quran WHERE Page=" + p + " ORDER BY Sura, Aya");
						while (rs.next())
						{
							int sura = rs.getInt("Sura");
							int aya = rs.getInt("Aya");
							String location = w + "," + h;

							if(aya == 1 && p != 187) // add basmalah
							{
								location = location + '-' + db_startX_v.elementAt(0) + "," + db_startY_v.elementAt(0) + ',' + db_width_v.elementAt(0) + ',' + db_height_v.elementAt(0);

								db_startX_v.removeElementAt(0);
								db_startY_v.removeElementAt(0);
								db_width_v.removeElementAt(0);
								db_height_v.removeElementAt(0);
								db_aya_end.removeElementAt(0);
								stmt.execute("INSERT INTO Quran(Page,Sura,Aya,Location) VALUES(" + p + "," + sura + ",0,'" + location + "')");
							}

							location = w + "," + h;
							for (int i = 0; i < db_startX_v.size(); i++)
							{
								location = location + '-' + db_startX_v.elementAt(i) + "," + db_startY_v.elementAt(i) + ',' + db_width_v.elementAt(i) + ',' + db_height_v.elementAt(i);

								db_startX_v.removeElementAt(i);
								db_startY_v.removeElementAt(i);
								db_width_v.removeElementAt(i);
								db_height_v.removeElementAt(i);

								if(db_aya_end.elementAt(i))
								{
									db_aya_end.removeElementAt(i);
									break;
								}
								else
									db_aya_end.removeElementAt(i);

								i--;
							}
							stmt.executeUpdate("UPDATE Quran SET Location='" + location + "' WHERE Sura=" + sura + " AND Aya=" + aya);
						}
						rs.close();
						stmt.close();
					}

					if (derbyInUse)
						DriverManager.getConnection("jdbc:derby:;shutdown=true"); // It should be the last since it will through exception in normal case "Derby system shutdown."
					else
						sharedDBConnection.createStatement().execute("SHUTDOWN"); // shutdown it here instead of when System.exit(0).
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});

		final JPanel pagePanel = new JPanel();
		pagePanel.setBackground(Color.white);
		pagePanel.add(page);

		final JButton pageIncrementButton = new JButton(new ImageIcon("images/ayaIncrement.png"));
		final JButton pageDecrementButton = new JButton(new ImageIcon("images/ayaDecrement.png"));
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

		pageComboBox.setFocusable(false);

		final JPanel controlPanel = new JPanel();
		final JPanel pagePanel_ = new JPanel(new BorderLayout());
		pagePanel_.add(pageIncrementButton, BorderLayout.WEST);
		pagePanel_.add(pageComboBox);
		pagePanel_.add(pageDecrementButton, BorderLayout.EAST);
		controlPanel.add(pagePanel_);
		controlPanel.add(updateDB);
		controlPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		add(new JScrollPane(pagePanel), BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);

		pageComboBox.setSelectedIndex(603);
		setIconImage(Toolkit.getDefaultToolkit().createImage("images/selection.png"));
		setSize(new Dimension(900, 700));
		setVisible(true);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
	}

	private void processPage(int p)
	{
		final String pagesFolder = "pages-hafs"; // or pages-warsh
		final ImageIcon image = new ImageIcon(pagesFolder + "/" + p + ".png");
		page.setIcon(image);
		db_startX_v.clear();
		db_startY_v.clear();
		db_width_v.clear();
		db_height_v.clear();
		db_aya_end.clear();

		startX = endX = 0;
		startY = endY = 0;

		w = image.getIconWidth();
		h = image.getIconHeight();

		paddingHafs = Math.round(21f / 1636 * w); // calculated manually based on the sura's square and borders in page 604

		try
		{
			bufferedImage = ImageIO.read(new File(pagesFolder + "/" + p + ".png"));

			final int width = bufferedImage.getWidth();
			final int height = bufferedImage.getHeight();
			final int[][] pixels = new int[height][width];

			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++)
					pixels[y][x] = bufferedImage.getRGB(x, y);

			boolean startXCaptured = false;
			boolean startYCaptured = false;

			for (int x = 0; x < width; x++)
			{
				if ((pixels[height / 2][x]) == new Color(192, 226, 202).getRGB()) // green color of aya number circle
				{
					endX = x - 1;
					endX = endX - paddingHafs + 1;

					for (int x1 = x; x1 < width; x1++)
					{
						if ((pixels[height / 2][x1]) != new Color(192, 226, 202).getRGB())
						{
							x = x1;
							if (!startXCaptured)
							{
								startX = x;
								startX = startX + paddingHafs;
								startXCaptured = true;
							}
							break;
						}
					}
				}
			}

			for (int y = 0; y < height; y++)
			{
				if ((pixels[y][width / 2]) == new Color(192, 226, 202).getRGB())
				{
					endY = y - 1;
					endY = endY - paddingHafs + 1;

					for (int y1 = y; y1 < height; y1++)
					{
						if ((pixels[y1][width / 2]) != new Color(192, 226, 202).getRGB())
						{
							y = y1;
							if (!startYCaptured)
							{
								startY = y;
								startY = startY + paddingHafs;
								startYCaptured = true;
							}
							break;
						}
					}
				}
			}

			float h = (endY - startY) / 15f;
			int previousAyaX;
			for (int a = 0; a < 15; a++)
			{
				boolean ignore = false;
				int in = ignoredPages.indexOf(p);
				if (in != -1)
				{
					for (int i = 0; i < suraNamesLines[in].length; i++)
					{
						if (suraNamesLines[in][i] == (a + 1))
						{
							ignore = true;
							break;
						}
					}

					for (int i = 0; i < basmalahLines[in].length; i++)
					{
						if (basmalahLines[in][i] == (a + 1))
						{
							db_startX_v.add(235);
							db_startY_v.add(startY + Math.round(a * h) + 10); // 10 shift by eye
							db_width_v.add(708 - 235);
							db_height_v.add((int) h - 10);
							db_aya_end.add(true);

							ignore = true;
							break;
						}
					}
				}

				if (ignore)
					continue;

				previousAyaX = endX;

				// in case Aya is centered in the line, we captured the first harf x position
				// we search block of (15 pixels x height) for the first none transparent pixel
				outerloop:
				for (int x = endX; x > startX; x--)
				{
					for (int y = startY + Math.round((a * h)) + 10; y < startY + Math.round((a * h) + h) - 10; y++)
					{
						if ((pixels[y][x] >> 24) != 0x00) // is not transparent
						{
							if ((endX - x) > 15)
								previousAyaX = x + 8;

							break outerloop;
						}
					}
				}

				int yy = startY + Math.round((a * h) + (h / 2) + 3); // 3 additional pixels since aya number usually below the central line
				for (int xx = endX; xx > startX; xx--)
				{
					if (pixels[yy][xx] == new Color(192, 226, 202).getRGB())
					{
						for (int x2 = xx; x2 > (startX - 10); x2--) // (startX - 10) -> until we reach transparent pixel even after boundary for cases like aya number exceeds the boundary
						{
							if ((pixels[yy][x2] >> 24) == 0x00) // transparent
							{
								//if(x2 <= startX) // To display those cases with aya number exceeds the boundary
									//System.out.println(p + " " + a);

								if ((x2 - startX) < 15) // less than 15 pixel mostly no aya
									x2 = startX;
								else
									x2 = x2 - 2;

								db_startX_v.add(x2);
								db_startY_v.add(startY + Math.round(a * h) - 1); // to make the high bigger by 1 pixel in each side. To avoid these one pixel lines in the middle between lines for the same Aya
								db_width_v.add(previousAyaX - x2);
								db_height_v.add((int) h + 2); // to make the high bigger by 1 pixel in each side. To avoid these one pixel lines in the middle between lines for the same Aya
								db_aya_end.add(true);

								previousAyaX = xx = x2;

								break;
							}
						}
					}
				}

				for (int x2 = previousAyaX; x2 > startX; x2--)
				{
					if ((pixels[yy][x2] >> 24) != 0x00) // is not transparent i.e. aya in the last part of line
					{
						db_startX_v.add(startX);
						db_startY_v.add(startY + Math.round(a * h) - 1);
						db_width_v.add(previousAyaX - startX);
						db_height_v.add((int) h + 2);
						db_aya_end.add(false);

						break;
					}
				}
			}
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static void main(final String[] args)
	{
		SwingUtilities.invokeLater(QuranPanelAuto::new);
	}
}