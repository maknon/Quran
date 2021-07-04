package classes;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.util.FixedBitSet;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.*;
import java.sql.*;
import java.text.*;

public class Search extends JDialog
{
	final JTabbedPane tabbedPane; // Global to be accessed from Quran.class
	Search(final Quran quran)
	{
		super(quran, false);

		final String translations[] = Quran.StreamConverter("language/SearchArabic.txt");
		setTitle(translations[8]);

		tabbedPane = new JTabbedPane();
		setContentPane(tabbedPane);

		final DefaultMutableTreeNode searchRootNode = new DefaultMutableTreeNode(new SearchNodeInfo("Root", -1));

		try
		{
			final Statement stmt = quran.sharedDBConnection.createStatement();
			final ResultSet rs = stmt.executeQuery("SELECT * FROM Category");
			while(rs.next())
			{
				final int Category_parent = rs.getInt("Category_parent");
				if(Category_parent==0)
					searchRootNode.add(new DefaultMutableTreeNode(new SearchNodeInfo(rs.getString("Category_name"), rs.getInt("Category_id"))));
				else
				{
					final Enumeration nodes = searchRootNode.postorderEnumeration();
					while(nodes.hasMoreElements())
					{
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodes.nextElement();
						final SearchNodeInfo nodeInfo = (SearchNodeInfo)(node.getUserObject());
						if(Category_parent==nodeInfo.Category_id)
						{
							node.add(new DefaultMutableTreeNode(new SearchNodeInfo(rs.getString("Category_name"), rs.getInt("Category_id"))));
							break;
						}
					}
				}
			}
		}
		catch(Exception e){e.printStackTrace();}

		final JTree searchTree = new JTree(searchRootNode);
		searchTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		searchTree.setRootVisible(false);
		searchTree.setToggleClickCount(1);

		final DefaultTableModel searchByCategoryModel = new DefaultTableModel();
		searchByCategoryModel.addColumn(translations[1]);
		searchByCategoryModel.addColumn(translations[2]);
		searchByCategoryModel.addColumn(translations[3]);

		final DefaultTableModel searchByWordModel = new DefaultTableModel();
		searchByWordModel.addColumn(translations[1]);
		searchByWordModel.addColumn(translations[2]);
		searchByWordModel.addColumn(translations[3]);

		final JTable searchByCategoryTable = new JTable(searchByCategoryModel)
		{
			public boolean isCellEditable(int rowIndex, int colIndex)
			{
				return false;
			}
		};

		final JTable searchByWordTable = new JTable(searchByWordModel)
		{
			public boolean isCellEditable(int rowIndex, int colIndex)
			{
				return false;
			}
		};

		searchByCategoryTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchByWordTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		for(int i=0; i<searchByCategoryModel.getColumnCount(); i++)
		{
			final TableColumn column1 = searchByCategoryTable.getColumnModel().getColumn(i);
			final TableColumn column2 = searchByWordTable.getColumnModel().getColumn(i);
			if(i==2)
			{
				column1.setPreferredWidth(700);
				column2.setPreferredWidth(700);
			}
			column1.setCellRenderer(renderer);
			column2.setCellRenderer(renderer);
		}

		searchTree.addTreeSelectionListener((e)->
		{
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) searchTree.getLastSelectedPathComponent();
            if (node != null)
            {
                searchByCategoryModel.getDataVector().removeAllElements();
                final SearchNodeInfo nodeInfo = (SearchNodeInfo) node.getUserObject();
                try
                {
                    final Statement stmt1 = quran.sharedDBConnection.createStatement();
                    final ResultSet rs1 = stmt1.executeQuery("SELECT QuranCat.Sura, QuranCat.Aya, Quran.Verse FROM QuranCat, Quran  WHERE QuranCat.Category_id = " + nodeInfo.Category_id + " AND QuranCat.Sura=Quran.Sura AND QuranCat.Aya=Quran.Aya");

                    while (rs1.next())
                        searchByCategoryModel.addRow(new Object[]{rs1.getInt("Sura"), rs1.getInt("Aya"), rs1.getString("Verse")});

                    stmt1.close();
                }
                catch (Exception ex) {ex.printStackTrace();}
            }
            else
                searchByCategoryModel.getDataVector().removeAllElements();
            searchByCategoryTable.updateUI();
		});

		final MouseAdapter tableMouseAdapter = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount()==2)
				{
					final JTable table = (tabbedPane.getSelectedIndex()==0)?searchByWordTable:searchByCategoryTable;
					//final Point pnt = e.getPoint();
					//final int row = table.rowAtPoint(pnt);
					final int row = table.getSelectedRow();
					final int aya = (Integer)table.getModel().getValueAt(row, 1);
					final int sura = (Integer)table.getModel().getValueAt(row, 0);

					// Make a number of combining sura+aya e.g. 100123 -> sura 100 aya 123. 2123 -> sura 2 aya 123. 2001 -> sura 2 aya 1. The last 3 digits should be aya
					final NumberFormat formatter = new DecimalFormat("000");
					final String a = formatter.format(aya);

					quran.selectedBySuraButton = false;
					quran.selectedByPageButton = false;
					quran.selectedByAyaButton = false;
					quran.selectedByHezpButton = false;
					quran.selectedByJozButton = false;
					quran.selectedByTahfeed = false;
					quran.selectedByMouse = false;
					quran.selectedBySearch = true;
					quran.SelectionThread(Integer.parseInt(""+sura+a));
				}
			}
		};
		searchByCategoryTable.addMouseListener(tableMouseAdapter);
		searchByWordTable.addMouseListener(tableMouseAdapter);

		final JPanel searchByCategory = new JPanel(new BorderLayout());
		searchByCategory.add(new JPanel(new BorderLayout()){{setPreferredSize(new Dimension(300, 0));add(new JScrollPane(searchTree));}}, BorderLayout.EAST);
		searchByCategory.add(new JScrollPane(searchByCategoryTable), BorderLayout.CENTER);

		// This is to indicate the type of the index search
		final JCheckBoxMenuItem defaultSearchTypeButton = new JCheckBoxMenuItem(translations[4], true);
		final JCheckBoxMenuItem arabicRootsSearchTypeButton = new JCheckBoxMenuItem(translations[5]);
		final JCheckBoxMenuItem arabicLuceneSearchTypeButton = new JCheckBoxMenuItem(translations[6]);

		final ButtonGroup searchGroup = new ButtonGroup();
		searchGroup.add(defaultSearchTypeButton);
		searchGroup.add(arabicRootsSearchTypeButton);
		searchGroup.add(arabicLuceneSearchTypeButton);

		final JPopupMenu searchOptionsPopupMenu = new JPopupMenu();
		searchOptionsPopupMenu.add(defaultSearchTypeButton);
		searchOptionsPopupMenu.add(arabicRootsSearchTypeButton);
		searchOptionsPopupMenu.add(arabicLuceneSearchTypeButton);
		searchOptionsPopupMenu.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		final JButton searchOptionsButton = new JButton(new ImageIcon("images/preferences.png"));
		searchOptionsButton.setToolTipText(translations[7]);
		searchOptionsButton.addActionListener((e)->
		{
            final Component c = (Component)e.getSource();
            searchOptionsPopupMenu.updateUI();
            searchOptionsPopupMenu.show(c, c.getWidth() - searchOptionsPopupMenu.getPreferredSize().width, c.getHeight());
		});

		final JPanel searchTextFieldPanel = new JPanel(new BorderLayout());
		final JTextField searchTextField = new JTextField();
		searchTextFieldPanel.add(searchTextField);

		final JButton searchButton = new JButton(translations[8], new ImageIcon("images/search.png"));
		final ActionListener SearchActionListener = new ActionListener()
		{
			boolean stopSearch = false;
			public void actionPerformed(ActionEvent e)
			{
				stopSearch = false;

				if(searchTextField.getText().trim().length()<2)
					JOptionPane.showOptionDialog(getContentPane(), translations[9], translations[10], JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{translations[11]}, translations[11]);
				else
				{
					final Thread thread = new Thread()
					{
						public void run()
						{
							searchTextField.setEnabled(false);
							searchButton.setEnabled(false);
							searchByWordModel.getDataVector().removeAllElements();
							
							String searchText = searchTextField.getText().trim();

							searchText = searchText.replace('؟', '?');
							searchText = searchText.replaceAll(" و ", " AND ");
							searchText = searchText.replaceAll(" أو ", " OR ");

							try
							{
								final QueryParser queryParser = new QueryParser("Quran", defaultSearchTypeButton.isSelected()?Quran.arabicAnalyzer:(arabicRootsSearchTypeButton.isSelected()?Quran.arabicRootsAnalyzer:Quran.arabicLuceneAnalyzer));

								queryParser.setAllowLeadingWildcard(true);
								queryParser.setDefaultOperator(QueryParser.Operator.AND);
								Query query = queryParser.parse(searchText);

								/* Version 2.0, obsoleted api
								final FixedBitSet bits = new FixedBitSet((defaultSearchTypeButton.isSelected()?Quran.indexSearcher:(arabicRootsSearchTypeButton.isSelected()?Quran.arabicRootsSearcher:Quran.arabicLuceneSearcher)).getIndexReader().maxDoc());
								(defaultSearchTypeButton.isSelected()?Quran.indexSearcher:(arabicRootsSearchTypeButton.isSelected()?Quran.arabicRootsSearcher:Quran.arabicLuceneSearcher)).search(query, new SimpleCollector()
								{
                                    public int docBase;
                                    public void collect(int doc) {bits.set(doc + docBase);}
                                    public void doSetNextReader(LeafReaderContext context) {this.docBase = context.docBase;}
                                    public boolean needsScores() {return false;}
								});
								*/

								final int hitsPerPage = 200;
								final TopDocs results = (defaultSearchTypeButton.isSelected()?Quran.indexSearcher:(arabicRootsSearchTypeButton.isSelected()?Quran.arabicRootsSearcher:Quran.arabicLuceneSearcher)).search(query, hitsPerPage);
								final ScoreDoc[] hits = results.scoreDocs;

								final Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<font color=red>", "</font>"), new QueryScorer(query));
								highlighter.setTextFragmenter(new NullFragmenter());
								if(defaultSearchTypeButton.isSelected())
								{
									for(int j=0, size=hits.length; j<size && !stopSearch; j++)
									{
										final Document doc = Quran.indexSearcher.doc(hits[j].doc);
										final String text = highlighter.getBestFragment(Quran.arabicAnalyzer, "", doc.get("Quran"));
										searchByWordModel.addRow(new Object[]{Integer.parseInt(doc.get("Sura")), Integer.parseInt(doc.get("Aya")), "<HTML>" + text});
									}
								}
								else
								{
									if(arabicRootsSearchTypeButton.isSelected())
									{
										for(int j=0, size=hits.length; j<size && !stopSearch; j++)
										{
											final Document doc = Quran.arabicRootsSearcher.doc(hits[j].doc);
											final String text = highlighter.getBestFragment(Quran.arabicRootsAnalyzer, "", doc.get("Quran"));
											searchByWordModel.addRow(new Object[]{Integer.parseInt(doc.get("Sura")), Integer.parseInt(doc.get("Aya")), "<HTML>" + text});
										}
									}
									else // i.e. arabicLuceneSearchTypeButton.isSelected()
									{
										for(int j=0, size=hits.length; j<size && !stopSearch; j++)
										{
											final Document doc = Quran.arabicLuceneSearcher.doc(hits[j].doc);
											final String text = highlighter.getBestFragment(Quran.arabicLuceneAnalyzer, "", doc.get("Quran"));
											searchByWordModel.addRow(new Object[]{Integer.parseInt(doc.get("Sura")), Integer.parseInt(doc.get("Aya")), "<HTML>" + text});
										}
									}
								}
							}
							catch(Exception e){e.printStackTrace();}

							searchTextField.setEnabled(true);
							searchButton.setEnabled(true);
						}
					};
					thread.start();
				}
			}
		};
		searchButton.addActionListener(SearchActionListener);
		searchTextField.addActionListener(SearchActionListener);

		searchTextFieldPanel.add(searchButton, BorderLayout.WEST);
		searchTextFieldPanel.add(searchTextField, BorderLayout.CENTER);
		searchTextFieldPanel.add(searchOptionsButton, BorderLayout.EAST);

		final JPanel searchByWord = new JPanel(new BorderLayout());
		searchByWord.add(searchTextFieldPanel, BorderLayout.NORTH);
		searchByWord.add(new JScrollPane(searchByWordTable), BorderLayout.CENTER);

		tabbedPane.addTab(translations[12], searchByWord);
		tabbedPane.addTab(translations[0], searchByCategory);

		getContentPane().applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		pack();
		Quran.centerInScreen(this);
		setVisible(true);
	}
}
