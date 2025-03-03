package classes;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.sql.*;

import static classes.Quran.pagesFolder;

class QuranPanel extends JFrame
{
	private int startX, startY, endX, endY;
	private final Vector<Integer> startX_v = new Vector<>();
	private final Vector<Integer> startY_v = new Vector<>();
	private final Vector<Integer> width_v = new Vector<>();
	private final Vector<Integer> height_v = new Vector<>();

	private final Vector<Integer> db_startX_v = new Vector<>();
	private final Vector<Integer> db_startY_v = new Vector<>();
	private final Vector<Integer> db_width_v = new Vector<>();
	private final Vector<Integer> db_height_v = new Vector<>();
	private final Quran quran;
	private final JComboBox<?> pageComboBox;
	int w, h;

	QuranPanel(final Quran q)
	{
		super("التخطيط");
		setLayout(new BorderLayout());
		quran = q;

		final String[] suraNames = Quran.StreamConverter("language/suraArabic.txt");

		final JLabel page = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);

				g.setColor(Color.BLACK);

				for (int i = 0; i < db_startX_v.size(); i++)
					g.drawRect(db_startX_v.elementAt(i), db_startY_v.elementAt(i), db_width_v.elementAt(i), db_height_v.elementAt(i));

				g.setColor(Color.RED);

				for (int i = 0; i < startX_v.size(); i++)
					g.drawRect(startX_v.elementAt(i), startY_v.elementAt(i), width_v.elementAt(i), height_v.elementAt(i));

				g.drawRect(startX, startY, endX - startX, endY - startY);
			}
		};
		page.setBorder(null);
		page.setAutoscrolls(true);

		page.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
			}

			public void mousePressed(MouseEvent e)
			{
				startX = endX = e.getX();
				startY = endY = e.getY();
				repaint();
			}

			public void mouseReleased(MouseEvent e)
			{
				if ((endX - startX) > 0 && (endY - startY) > 0)
				{
					startX_v.add(startX);
					startY_v.add(startY);
					width_v.add(endX - startX);
					height_v.add(endY - startY);
				}
				repaint();
			}

			public void mouseEntered(MouseEvent e)
			{
			}

			public void mouseExited(MouseEvent e)
			{
			}
		});

		page.addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseDragged(MouseEvent e)
			{
				endX = e.getX();
				endY = e.getY();
				repaint();

				/* auto scroll while dragging the mouse. Not working well
				final JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, page);
				if (viewPort != null)
				{
					final int deltaX = endX - startX;
					final int deltaY = endY - startY;

					final Rectangle view = viewPort.getViewRect();
					view.x += deltaX / 10;
					view.y += deltaY / 10;

					page.scrollRectToVisible(view);
				}
				*/
			}

			public void mouseMoved(MouseEvent e)
			{
			}
		});

		final Vector<String> pages = new Vector<>();
		for (int i = 1; i < 605; i++) pages.addElement("الصفحة " + i);
		pageComboBox = new JComboBox<>(pages);
		final JComboBox<String> ayaComboBox = new JComboBox<>();
		final JComboBox<String> suraComboBox = new JComboBox<>();

		pageComboBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final int index = ((JComboBox<?>) e.getSource()).getSelectedIndex() + 1;
				if (index != 0)
				{
					final ImageIcon image = new ImageIcon(pagesFolder + "/" + index + ".png");
					page.setIcon(image);
					suraComboBox.removeAllItems();
					db_startX_v.clear();
					db_startY_v.clear();
					db_width_v.clear();
					db_height_v.clear();

					startX = endX = 0;
					startY = endY = 0;
					startX_v.clear();
					startY_v.clear();
					width_v.clear();
					height_v.clear();

					w = image.getIconWidth();
					h = image.getIconHeight();

					try
					{
						ResultSet rs = quran.sharedDBConnection.createStatement().executeQuery("SELECT Sura FROM Quran WHERE Page=" + index + " GROUP BY Sura ORDER BY Sura");
						while (rs.next())
							suraComboBox.addItem(suraNames[rs.getInt("Sura") - 1]);
						rs.close();

						rs = quran.sharedDBConnection.createStatement().executeQuery("SELECT Location FROM Quran WHERE Page=" + index);
						while (rs.next())
						{
							final String l = rs.getString("Location");
							if (!l.isEmpty())
							{
								final String[] locations = l.split("-");
								//locations[0] -> w,h
								for (int i = 1; i < locations.length; i++)
								{
									final String[] location = locations[i].split(",");
									db_startX_v.add(Integer.parseInt(location[0]));
									db_startY_v.add(Integer.parseInt(location[1]));
									db_width_v.add(Integer.parseInt(location[2]));
									db_height_v.add(Integer.parseInt(location[3]));
								}
							}
						}
						rs.close();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});

		suraComboBox.addActionListener((e) ->
		{
			final int index = ((JComboBox<?>) e.getSource()).getSelectedIndex();
			if (index != -1)
			{
				ayaComboBox.removeAllItems();
				try
				{
					final String suraName = (String) ((JComboBox<?>) e.getSource()).getSelectedItem();
					final ResultSet rs = quran.sharedDBConnection.createStatement().executeQuery("SELECT Aya FROM Quran WHERE Page=" + (pageComboBox.getSelectedIndex() + 1) + " AND Sura=" + suraName.split("-")[0] + " GROUP BY Aya ORDER BY Aya");
					while (rs.next())
						ayaComboBox.addItem("الآية " + rs.getInt("Aya"));
					rs.close();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});

		final JButton updateDB = new JButton("تحديث");
		updateDB.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!startX_v.isEmpty())
				{
					String location = w + "," + h;
					for (int i = 0; i < startX_v.size(); i++)
						location = location + '-' + startX_v.elementAt(i) + "," + startY_v.elementAt(i) + ',' + width_v.elementAt(i) + ',' + height_v.elementAt(i);

					final String suraName = (String) suraComboBox.getSelectedItem();
					final String ayaNumber = (String) ayaComboBox.getSelectedItem();
					try
					{
						final Statement stmt = quran.sharedDBConnection.createStatement();
						stmt.executeUpdate("UPDATE Quran SET Location='" + location + "' WHERE Sura=" + suraName.split("-")[0] + " AND Aya=" + ayaNumber.split(" ")[1]);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				refreshPage();
			}
		});

		final JButton clear = new JButton("حذف التحديد");
		clear.addActionListener((e) ->
		{
			final String suraName = (String) suraComboBox.getSelectedItem();
			final String ayaNumber = (String) ayaComboBox.getSelectedItem();
			try
			{
				final Statement stmt = quran.sharedDBConnection.createStatement();
				stmt.executeUpdate("UPDATE Quran SET Location='' WHERE Sura=" + suraName.split("-")[0] + " AND Aya=" + ayaNumber.split(" ")[1]);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			refreshPage();
		});

		final JButton clearAll = new JButton("حذف الكل");
		clearAll.addActionListener((e) ->
		{
			try
			{
				final Statement stmt = quran.sharedDBConnection.createStatement();
				stmt.executeUpdate("UPDATE Quran SET Location='' WHERE Page=" + (pageComboBox.getSelectedIndex() + 1));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			refreshPage();
		});

		final JButton undo = new JButton("تراجع");
		undo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startX = endX = 0;
				startY = endY = 0;
				startX_v.clear();
				startY_v.clear();
				width_v.clear();
				height_v.clear();
				repaint();
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

		final JButton suraIncrementButton = new JButton(new ImageIcon("images/ayaIncrement.png"));
		final JButton suraDecrementButton = new JButton(new ImageIcon("images/ayaDecrement.png"));
		suraIncrementButton.setMargin(new Insets(3, 3, 3, 3));
		suraDecrementButton.setMargin(new Insets(3, 3, 3, 3));
		final ActionListener suraListener = (e) ->
		{
			int index = suraComboBox.getSelectedIndex();
			if (e.getSource() == suraIncrementButton) index++;
			if (e.getSource() == suraDecrementButton) index--;

			if (!(index >= suraComboBox.getItemCount() || index < 0))
				suraComboBox.setSelectedIndex(index);
		};
		suraIncrementButton.addActionListener(suraListener);
		suraDecrementButton.addActionListener(suraListener);

		final JButton ayaIncrementButton = new JButton(new ImageIcon("images/ayaIncrement.png"));
		final JButton ayaDecrementButton = new JButton(new ImageIcon("images/ayaDecrement.png"));
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

		ayaComboBox.setFocusable(false);
		suraComboBox.setFocusable(false);
		pageComboBox.setFocusable(false);

		for (Component component : ayaComboBox.getComponents())
			if (component instanceof AbstractButton)
				if (component.isVisible())
				{
					component.setVisible(false);
					break;
				}

		for (Component component : suraComboBox.getComponents())
			if (component instanceof AbstractButton)
				if (component.isVisible())
				{
					component.setVisible(false);
					break;
				}

		for (Component component : pageComboBox.getComponents())
			if (component instanceof AbstractButton)
				if (component.isVisible())
				{
					component.setVisible(false);
					break;
				}

		final JPanel controlPanel = new JPanel(new GridLayout(1, 6));

		final JPanel pagePanel_ = new JPanel(new BorderLayout());
		pagePanel_.add(pageIncrementButton, BorderLayout.WEST);
		pagePanel_.add(pageComboBox);
		pagePanel_.add(pageDecrementButton, BorderLayout.EAST);
		controlPanel.add(pagePanel_);

		final JPanel suraPanel = new JPanel(new BorderLayout());
		suraPanel.add(suraIncrementButton, BorderLayout.WEST);
		suraPanel.add(suraComboBox);
		suraPanel.add(suraDecrementButton, BorderLayout.EAST);
		controlPanel.add(suraPanel);

		final JPanel ayaPanel = new JPanel(new BorderLayout());
		ayaPanel.add(ayaIncrementButton, BorderLayout.WEST);
		ayaPanel.add(ayaComboBox);
		ayaPanel.add(ayaDecrementButton, BorderLayout.EAST);
		controlPanel.add(ayaPanel);

		controlPanel.add(undo);
		controlPanel.add(clear);
		controlPanel.add(clearAll);
		controlPanel.add(updateDB);
		controlPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		add(new JScrollPane(pagePanel), BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);

		pageComboBox.setSelectedIndex(0);
		setIconImage(Toolkit.getDefaultToolkit().createImage("images/selection.png"));
		setSize(new Dimension(900, 700));
		setVisible(true);
	}

	void refreshPage()
	{
		db_startX_v.clear();
		db_startY_v.clear();
		db_width_v.clear();
		db_height_v.clear();

		startX = endX = 0;
		startY = endY = 0;
		startX_v.clear();
		startY_v.clear();
		width_v.clear();
		height_v.clear();

		try
		{
			final ResultSet rs = quran.sharedDBConnection.createStatement().executeQuery("SELECT Location FROM Quran WHERE Page=" + (pageComboBox.getSelectedIndex() + 1));
			while (rs.next())
			{
				final String l = rs.getString("Location");
				if (!l.isEmpty())
				{
					final String[] locations = l.split("-");
					//locations[0] -> w,h
					for (int i = 1; i < locations.length; i++)
					{
						final String[] location = locations[i].split(",");
						db_startX_v.add(Integer.parseInt(location[0]));
						db_startY_v.add(Integer.parseInt(location[1]));
						db_width_v.add(Integer.parseInt(location[2]));
						db_height_v.add(Integer.parseInt(location[3]));
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		repaint();
	}
}