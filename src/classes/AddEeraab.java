package classes;

import java.io.StringReader;
import java.sql.*;

public class AddEeraab
{
	AddEeraab()
	{
		try
		{
			Class.forName("org.h2.Driver");

			final Connection conH2 = DriverManager.getConnection("jdbc:h2:./db/quranDatabase");

			final Statement stmtH2_1 = conH2.createStatement();
			stmtH2_1.execute("CREATE TABLE Eerab(TableName VARCHAR(5), Eerab VARCHAR(100))");

			for (int j = 1; j <= 1; j++)
			{
				final Connection conAccess = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/Eeraab" + j + ".accdb;singleconnection=true");
				final String t = (j < 10 ? "0" : "") + j;
				stmtH2_1.executeUpdate("CREATE TABLE er_" + t + "(Sura INTEGER, Aya INTEGER, Eerab CLOB(65536))");
				stmtH2_1.execute("CREATE INDEX AyaIndex_er_" + t + " ON er_" + t + "(Aya)");
				stmtH2_1.execute("CREATE INDEX SuraIndex_er_" + t + " ON er_" + t + "(Sura)");

				final Statement stmtAccess_1 = conAccess.createStatement();
				final ResultSet rsAccess_1 = stmtAccess_1.executeQuery("SELECT * FROM Main");
				rsAccess_1.next();
				stmtH2_1.execute("INSERT INTO Eerab VALUES('er_" + t + "','" + rsAccess_1.getString("Bk") + "')");
				final String table = "b" + rsAccess_1.getString("BkId");
				rsAccess_1.close();

				final Statement stmtH2_2 = conH2.createStatement();
				final ResultSet rsH2_2 = stmtH2_2.executeQuery("SELECT Sura, Aya FROM Quran");
				while (rsH2_2.next())
				{
					final int sura = rsH2_2.getInt("Sura");
					final int aya = rsH2_2.getInt("Aya");
					final ResultSet rsAccess_2 = stmtAccess_1.executeQuery("SELECT nass FROM " + table + " WHERE sora=" + sura + " AND aya=" + aya);
					if (rsAccess_2.next())
					{
						final String eerad = rsAccess_2.getString("nass");
						final PreparedStatement ps = conH2.prepareStatement("INSERT INTO er_" + t + " VALUES(?,?,?)");

						ps.setInt(1, sura);
						ps.setInt(2, aya);
						ps.setCharacterStream(3, new StringReader(eerad));
						ps.execute();
					}
				}

				stmtAccess_1.close();
				conAccess.close();
			}

			conH2.createStatement().execute("SHUTDOWN COMPACT");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		new AddEeraab();
	}
}