package classes;

import java.sql.*;
import java.io.*;

// Important: need to be executed from outside the IDEA since it enforce the file.encoding to be UTF-8 which will cause issues with importing Arabic strings.
// OR -Dfile.encoding=cp1256 in VM parameters. UPDATE: ucanaccess will do it correctly.
public class CreateQuranDatabase
{
	public static void main(String[] args)
	{
		new CreateQuranDatabase();
		//new AddTafseer(); // TODO, check class
		new AddEeraab();
	}

	private CreateQuranDatabase()
	{
		try
		{
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").getDeclaredConstructor().newInstance();
			Class.forName("org.h2.Driver");

			final Connection accessCon = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/qurandb-2.mdb;singleconnection=true");
			final Connection accessCon_en_sahih = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/en_sahih.mdb;singleconnection=true");
			final Connection accessCon_baghawy = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/baghawy.mdb;singleconnection=true");
			final Connection accessCon_tabary = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/tabary.mdb;singleconnection=true");
			final Connection accessCon_sa3dy = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/sa3dy.mdb;singleconnection=true");
			final Connection accessCon_alnoor = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/quran.mdb;singleconnection=true");
			final Connection h2Con = DriverManager.getConnection("jdbc:h2:./db/quranDatabase");
			final Connection derbyCon = DriverManager.getConnection("jdbc:derby:db.derby;create=true");

			// Using setAutoCommit(false) then INSERT then setAutoCommit(true) will speed the process.
			h2Con.setAutoCommit(false);
			derbyCon.setAutoCommit(false);

			final Statement h2Stmt = h2Con.createStatement();
			final Statement derbyStmt = derbyCon.createStatement();
			h2Stmt.executeUpdate("CREATE TABLE Quran(Page INTEGER, Sura INTEGER, Aya INTEGER, Hezp INTEGER, Part INTEGER, Location VARCHAR(800), Verse VARCHAR(1200))");
			derbyStmt.executeUpdate("CREATE TABLE Quran(Page INTEGER, Sura INTEGER, Aya INTEGER, Hezp INTEGER, Part INTEGER, Location VARCHAR(800), Verse VARCHAR(1200))");

			h2Stmt.execute("CREATE TABLE Tafseer(TableName VARCHAR(5), Tafseer VARCHAR(100))"); // TableName is integer to get the name from the ComboBox index directly. It will be used in sorting the results so that tafseer will be in order
			derbyStmt.execute("CREATE TABLE Tafseer(TableName VARCHAR(5), Tafseer VARCHAR(100))");

			h2Stmt.execute("INSERT INTO Tafseer VALUES('ar_01','تفسير إبن كثير')");
			h2Stmt.execute("INSERT INTO Tafseer VALUES('ar_02','تفسير القرطبي')");
			h2Stmt.execute("INSERT INTO Tafseer VALUES('ar_03','تفسير الجلالين')");
			h2Stmt.execute("INSERT INTO Tafseer VALUES('ar_04','تفسير البغوي')");
			h2Stmt.execute("INSERT INTO Tafseer VALUES('ar_05','تفسير الطبري')");
			h2Stmt.execute("INSERT INTO Tafseer VALUES('ar_06','تفسير السعدي')");
			h2Stmt.execute("INSERT INTO Tafseer VALUES('ar_07','التفسير الميسر')");

			derbyStmt.execute("INSERT INTO Tafseer VALUES('ar_01','تفسير إبن كثير')");
			derbyStmt.execute("INSERT INTO Tafseer VALUES('ar_02','تفسير القرطبي')");
			derbyStmt.execute("INSERT INTO Tafseer VALUES('ar_03','تفسير الجلالين')");
			derbyStmt.execute("INSERT INTO Tafseer VALUES('ar_04','تفسير البغوي')");
			derbyStmt.execute("INSERT INTO Tafseer VALUES('ar_05','تفسير الطبري')");
			derbyStmt.execute("INSERT INTO Tafseer VALUES('ar_06','تفسير السعدي')");
			derbyStmt.execute("INSERT INTO Tafseer VALUES('ar_07','التفسير الميسر')");

			for (int i = 1; i <= 7; i++)
			{
				h2Stmt.execute("CREATE TABLE ar_0" + i + "(Sura INTEGER, Aya INTEGER, Tafseer CLOB(65536))"); // CLOB instead of LONGVARCHAR for performance, http://groups.google.com/group/h2-database/browse_thread/thread/cfa30de79710440c
				derbyStmt.execute("CREATE TABLE ar_0" + i + "(Sura INTEGER, Aya INTEGER, Tafseer CLOB(65 K))"); // LONG VARCHAR is not enough since it is only 32,700

				h2Stmt.execute("CREATE INDEX AyaIndex_ar_0" + i + " ON ar_0" + i + "(Aya)");
				h2Stmt.execute("CREATE INDEX SuraIndex_ar_0" + i + " ON ar_0" + i + "(Sura)");
				derbyStmt.execute("CREATE INDEX AyaIndex_ar_0" + i + " ON ar_0" + i + "(Aya)");
				derbyStmt.execute("CREATE INDEX SuraIndex_ar_0" + i + " ON ar_0" + i + "(Sura)");
			}

			h2Stmt.execute("CREATE TABLE Translations(TableName VARCHAR(5), Translations VARCHAR(100))"); // TableName is integer to sort the results in order. then TableName = this integer + "_en" to avoid duplicates with arabic tafseer tables.
			derbyStmt.execute("CREATE TABLE Translations(TableName VARCHAR(5), Translations VARCHAR(100))");

			h2Stmt.execute("INSERT INTO Translations VALUES('en_01','Yusuf Ali')");
			h2Stmt.execute("INSERT INTO Translations VALUES('en_02','Mohamed Marmaduke Pickthall')");
			h2Stmt.execute("INSERT INTO Translations VALUES('en_03','Umm Muhammad (Sahih International)')");

			derbyStmt.execute("INSERT INTO Translations VALUES('en_01','Yusuf Ali')");
			derbyStmt.execute("INSERT INTO Translations VALUES('en_02','Mohamed Marmaduke Pickthall')");
			derbyStmt.execute("INSERT INTO Translations VALUES('en_03','Umm Muhammad (Sahih International)')");

			for (int i = 1; i <= 3; i++)
			{
				h2Stmt.execute("CREATE TABLE en_0" + i + "(Sura INTEGER, Aya INTEGER, Translations CLOB(65536))"); // CLOB instead of LONGVARCHAR for performance, http://groups.google.com/group/h2-database/browse_thread/thread/cfa30de79710440c
				derbyStmt.execute("CREATE TABLE en_0" + i + "(Sura INTEGER, Aya INTEGER, Translations CLOB(65 K))"); // LONG VARCHAR is not enough since it is only 32,700

				h2Stmt.execute("CREATE INDEX AyaIndex_en_0" + i + " ON en_0" + i + "(Aya)");
				h2Stmt.execute("CREATE INDEX SuraIndex_en_0" + i + " ON en_0" + i + "(Sura)");
				derbyStmt.execute("CREATE INDEX AyaIndex_en_0" + i + " ON en_0" + i + "(Aya)");
				derbyStmt.execute("CREATE INDEX SuraIndex_en_0" + i + " ON en_0" + i + "(Sura)");
			}

			final Statement accessStmt = accessCon.createStatement();
			final Statement accessStmt_en_sahih = accessCon_en_sahih.createStatement();
			final Statement accessStmt_baghawy = accessCon_baghawy.createStatement();
			final Statement accessStmt_tabary = accessCon_tabary.createStatement();
			final Statement accessStmt_sa3dy = accessCon_sa3dy.createStatement();
			final Statement accessStmt_alnoor = accessCon_alnoor.createStatement();
			final PreparedStatement ps2 = accessCon.prepareStatement("SELECT MAX(Part) as maxPart, MAX(Hezp) as maxHezp FROM Parts where ?>=Sura and ?>=Aya");

			final ResultSet rs1 = accessStmt.executeQuery("SELECT * FROM Quran ORDER BY PageNumber, Sura, Aya");
			while (rs1.next())
			{
				final int sura = rs1.getInt("Sura");
				final int aya = rs1.getInt("Aya");
				final int pageNumber = rs1.getInt("PageNumber");
				final String tafsYusuf = rs1.getString("TafsYusufEnglish");
				final String tafsMohamed = rs1.getString("TafsMohamedEnglish");
				final String verse = rs1.getString("Quran");
				final String tafsKather = rs1.getString("TafsKather");
				final String tafsKortoby = rs1.getString("TafsKortoby");
				final String tafsGlalyn = rs1.getString("TafsGlalyn");
				final String tafsMuiassar = rs1.getString("TafsMuiassar");
				final String tafs_baghawy;
				final String tafs_tabary;
				final String tafs_sa3dy;
				final String tafs_en_sahih;

				ResultSet tafs = accessStmt_baghawy.executeQuery("SELECT text FROM baghawy WHERE Sura='" + sura + "' AND Aya='" + aya + "'");
				tafs.next();
				String line = tafs.getString("text");
				if (line == null)
					tafs_baghawy = "";
				else
				{
					line = line.replaceAll("\\\\n", "").replaceAll("\\\\r", "");
					final Html2Text parser1 = new Html2Text();
					parser1.parse(new StringReader(line));
					tafs_baghawy = parser1.getText();
				}

				tafs = accessStmt_tabary.executeQuery("SELECT text FROM tabary WHERE Sura='" + sura + "' AND Aya='" + aya + "'");
				tafs.next();
				line = tafs.getString("text");
				if (line == null)
					tafs_tabary = "";
				else
				{
					line = line.replaceAll("\\\\n", "").replaceAll("\\\\r", "");
					final Html2Text parser2 = new Html2Text();
					parser2.parse(new StringReader(line));
					tafs_tabary = parser2.getText();
				}

				tafs = accessStmt_sa3dy.executeQuery("SELECT text FROM sa3dy WHERE Sura='" + sura + "' AND Aya='" + aya + "'");
				tafs.next();
				line = tafs.getString("text");
				if (line == null)
					tafs_sa3dy = "";
				else
				{
					line = line.replaceAll("\\\\n", "").replaceAll("\\\\r", "");
					final Html2Text parser3 = new Html2Text();
					parser3.parse(new StringReader(line));
					tafs_sa3dy = parser3.getText();
				}

				tafs = accessStmt_en_sahih.executeQuery("SELECT text FROM en_sahih WHERE Sura='" + sura + "' AND Aya='" + aya + "'");
				tafs.next();
				line = tafs.getString("text");
				if (line == null)
					tafs_en_sahih = "";
				else
				{
					line = line.replaceAll("\\\\n", "").replaceAll("\\\\r", "");
					final Html2Text parser4 = new Html2Text();
					parser4.parse(new StringReader(line));
					tafs_en_sahih = parser4.getText();
				}

				ps2.setInt(1, sura);
				ps2.setInt(2, aya);
				final ResultSet rs2 = ps2.executeQuery();
				rs2.next();
				final int maxHezp = rs2.getInt("maxHezp");
				final int maxPart = rs2.getInt("maxPart");

				PreparedStatement ps1 = h2Con.prepareStatement("INSERT INTO Quran VALUES(?,?,?,?,?,'',?)");
				PreparedStatement ps3 = derbyCon.prepareStatement("INSERT INTO Quran VALUES(?,?,?,?,?,'',?)");

				// Removed to avoid special characters within strings.
				//h2Stmt.execute("INSERT INTO Quran VALUES("+rs1.getInt("PageNumber")+','+sura+','+aya+','+rs2.getInt("maxHezp")+','+rs2.getInt("maxPart")+",'','"+rs1.getString("TafsEnglish")+"','"+new String(rs1.getString("TafsKather").getBytes(), "cp1256")+"','"+new String(rs1.getString("TafsKortoby").getBytes(), "cp1256")+"','"+new String(rs1.getString("TafsGlalyn").getBytes(), "cp1256")+"')");
				ps1.setInt(1, pageNumber);
				ps1.setInt(2, sura);
				ps1.setInt(3, aya);
				ps1.setInt(4, maxHezp);
				ps1.setInt(5, maxPart);
				ps1.setString(6, verse);
				ps1.execute();
				ps1.close();

				ps3.setInt(1, pageNumber);
				ps3.setInt(2, sura);
				ps3.setInt(3, aya);
				ps3.setInt(4, maxHezp);
				ps3.setInt(5, maxPart);
				ps3.setString(6, verse);
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO ar_01 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO ar_01 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafsKather));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafsKather));
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO ar_02 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO ar_02 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafsKortoby));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafsKortoby));
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO ar_03 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO ar_03 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafsGlalyn));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafsGlalyn));
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO ar_04 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO ar_04 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafs_baghawy));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafs_baghawy));
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO ar_05 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO ar_05 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafs_tabary));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafs_tabary));
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO ar_06 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO ar_06 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafs_sa3dy));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafs_sa3dy));
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO ar_07 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO ar_07 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafsMuiassar));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafsMuiassar));
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO en_01 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO en_01 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafsYusuf));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafsYusuf));
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO en_02 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO en_02 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafsMohamed));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafsMohamed));
				ps3.execute();
				ps3.close();

				ps1 = h2Con.prepareStatement("INSERT INTO en_03 VALUES(?,?,?)");
				ps3 = derbyCon.prepareStatement("INSERT INTO en_03 VALUES(?,?,?)");

				ps1.setInt(1, sura);
				ps1.setInt(2, aya);
				ps1.setCharacterStream(3, new StringReader(tafs_en_sahih));
				ps1.execute();
				ps1.close();

				ps3.setInt(1, sura);
				ps3.setInt(2, aya);
				ps3.setCharacterStream(3, new StringReader(tafs_en_sahih));
				ps3.execute();
				ps3.close();
			}

			h2Stmt.execute("CREATE INDEX PageIndex ON Quran(Page)");
			h2Stmt.execute("CREATE INDEX SuraIndex ON Quran(Sura)");
			h2Stmt.execute("CREATE INDEX AyaIndex ON Quran(Aya)");

			derbyStmt.execute("CREATE INDEX PageIndex ON Quran(Page)");
			derbyStmt.execute("CREATE INDEX SuraIndex ON Quran(Sura)");
			derbyStmt.execute("CREATE INDEX AyaIndex ON Quran(Aya)");

			h2Stmt.execute("CREATE TABLE Parts(Part INTEGER, Hezp INTEGER, Page INTEGER, Sura INTEGER, Aya INTEGER)");
			derbyStmt.execute("CREATE TABLE Parts(Part INTEGER, Hezp INTEGER, Page INTEGER, Sura INTEGER, Aya INTEGER)");
			final ResultSet rs3 = accessStmt.executeQuery("SELECT * FROM Parts ORDER BY Part, Hezp");

			while (rs3.next())
			{
				final int part = rs3.getInt("Part");
				final int hezp = rs3.getInt("Hezp");
				final int page = rs3.getInt("Page");
				final int sura = rs3.getInt("Sura");
				final int aya = rs3.getInt("Aya");
				h2Stmt.execute("INSERT INTO Parts VALUES(" + part + ',' + hezp + ',' + page + ',' + sura + ',' + aya + ')');
				derbyStmt.execute("INSERT INTO Parts VALUES(" + part + ',' + hezp + ',' + page + ',' + sura + ',' + aya + ')');
			}

			h2Stmt.execute("CREATE INDEX PartIndex ON Parts(Part)");
			h2Stmt.execute("CREATE INDEX HezpIndex ON Parts(Hezp)");

			derbyStmt.execute("CREATE INDEX PartIndex ON Parts(Part)");
			derbyStmt.execute("CREATE INDEX HezpIndex ON Parts(Hezp)");

			h2Stmt.execute("CREATE TABLE Category(Category_id SMALLINT, Category_name VARCHAR(100), Category_parent SMALLINT, PRIMARY KEY(Category_id))");
			derbyStmt.execute("CREATE TABLE Category(Category_id SMALLINT, Category_name VARCHAR(100), Category_parent SMALLINT, PRIMARY KEY(Category_id))");

			final ResultSet rs4 = accessStmt_alnoor.executeQuery("SELECT * FROM topics_name");

			while (rs4.next())
			{
				final String name = rs4.getString("Topic_Name");
				final int parent = rs4.getInt("Mother_Topic");
				final int id = rs4.getInt("Topic_No");

				derbyStmt.execute("INSERT INTO Category VALUES(" + id + ",'" + name + "', " + parent + ')');
				h2Stmt.execute("INSERT INTO Category VALUES(" + id + ",'" + name + "', " + parent + ')');
			}

			derbyStmt.execute("CREATE TABLE QuranCat(Category_id SMALLINT, Sura SMALLINT, Aya SMALLINT, FOREIGN KEY(Category_id) REFERENCES Category(Category_id))");
			h2Stmt.execute("CREATE TABLE QuranCat(Category_id SMALLINT, Sura SMALLINT, Aya SMALLINT, FOREIGN KEY(Category_id) REFERENCES Category(Category_id))");
			//h2Stmt.execute("CREATE TABLE QuranCat(Category_id SMALLINT, Sura SMALLINT, Aya SMALLINT, FOREIGN KEY(Category_id) REFERENCES Category(Category_id), "+ // No need since no user edit in Quran like in AC
			//"FOREIGN KEY(Sura) REFERENCES Quran(Sura) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY(Aya) REFERENCES Quran(Aya) ON DELETE CASCADE ON UPDATE CASCADE)");

			final ResultSet rs5 = accessStmt_alnoor.executeQuery("SELECT * FROM Topic_Ayat");
			while (rs5.next())
			{
				final int category_id = rs5.getInt("Topic_No");
				final int sura = rs5.getInt("Sura_No");
				final String ayat = rs5.getString("Ayat_No");

				if (ayat.contains(","))
				{
					final String[] aya = ayat.split(",");
					for (String ay : aya)
					{
						if (ay.contains("-"))
						{
							final String[] ayaa = ay.split("-");
							final int from = Integer.parseInt(ayaa[0]);
							final int to = Integer.parseInt(ayaa[1]);

							for (int j = from; j <= to; j++)
							{
								derbyStmt.execute("INSERT INTO QuranCat VALUES(" + category_id + ", " + sura + ", " + j + ')');
								h2Stmt.execute("INSERT INTO QuranCat VALUES(" + category_id + ", " + sura + ", " + j + ')');
							}
						}
						else
						{
							derbyStmt.execute("INSERT INTO QuranCat VALUES(" + category_id + ", " + sura + ", " + Integer.parseInt(ay) + ')');
							h2Stmt.execute("INSERT INTO QuranCat VALUES(" + category_id + ", " + sura + ", " + Integer.parseInt(ay) + ')');
						}
					}
				}
				else
				{
					if (ayat.contains("-"))
					{
						final String[] ayaa = ayat.split("-");
						final int from = Integer.parseInt(ayaa[0]);
						final int to = Integer.parseInt(ayaa[1]);

						for (int j = from; j <= to; j++)
						{
							derbyStmt.execute("INSERT INTO QuranCat VALUES(" + category_id + ", " + sura + ", " + j + ')');
							h2Stmt.execute("INSERT INTO QuranCat VALUES(" + category_id + ", " + sura + ", " + j + ')');
						}
					}
					else // single number
					{
						derbyStmt.execute("INSERT INTO QuranCat VALUES(" + category_id + ", " + sura + ", " + Integer.parseInt(ayat) + ')');
						h2Stmt.execute("INSERT INTO QuranCat VALUES(" + category_id + ", " + sura + ", " + Integer.parseInt(ayat) + ')');
					}
				}
			}

			derbyStmt.execute("CREATE INDEX QuranCat_Category_id_Index ON QuranCat(Category_id)");
			derbyStmt.execute("CREATE INDEX QuranCat_Sura_Index ON QuranCat(Sura)");
			derbyStmt.execute("CREATE INDEX QuranCat_Aya_Index ON QuranCat(Aya)");

			h2Stmt.execute("CREATE INDEX QuranCat_Category_id_Index ON QuranCat(Category_id)");
			h2Stmt.execute("CREATE INDEX QuranCat_Sura_Index ON QuranCat(Sura)");
			h2Stmt.execute("CREATE INDEX QuranCat_Aya_Index ON QuranCat(Aya)");

			final Statement closeDerbyStmt = derbyCon.createStatement();
			final ResultSet rs = closeDerbyStmt.executeQuery("SELECT schemaname, tablename FROM sys.sysschemas s, sys.systables t WHERE s.schemaid = t.schemaid AND t.tabletype = 'T'");
			final CallableStatement cs = derbyCon.prepareCall("CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?, ?, 1)");
			while (rs.next())
			{
				cs.setString(1, rs.getString("schemaname"));
				cs.setString(2, rs.getString("tablename"));
				cs.execute();
			}

			h2Con.setAutoCommit(true);
			derbyCon.setAutoCommit(true);

			try
			{
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} // This will throw exception in normal case.
			catch (Exception e)
			{
				e.printStackTrace();
			}

			h2Con.createStatement().execute("SHUTDOWN COMPACT");
			accessCon.close();
			accessCon_en_sahih.close();
			accessCon_baghawy.close();
			accessCon_tabary.close();
			accessCon_sa3dy.close();
			accessCon_alnoor.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}