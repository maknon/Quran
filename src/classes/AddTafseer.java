package classes;

import java.io.StringReader;
import java.sql.*;

// The DB is from Aljamaie Tarikhi [www.mobdii.com]
public class AddTafseer
{
	AddTafseer()
	{
		final String[] tafaseer = {"100", "102", "105", "110", "120", "127"}; // TODO: Add all the rest after filtering

		try
		{
			Class.forName("org.h2.Driver");

			final Connection conAccess = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/Tafseer.mdb;singleconnection=true");
			final Connection conH2 = DriverManager.getConnection("jdbc:h2:./db/quranDatabase");

			// Using setAutoCommit(false) then INSERT then setAutoCommit(true) will speed the process.
			conH2.setAutoCommit(false);

			final Statement stmtH2_1 = conH2.createStatement();
			final ResultSet rsH2_1 = stmtH2_1.executeQuery("SELECT COUNT(*) FROM Tafseer");
			rsH2_1.next();
			int i = rsH2_1.getInt(1);
			rsH2_1.close();

			for (String taf : tafaseer)
			{
				++i;
				final String t = (i < 10 ? "0" : "") + i;
				stmtH2_1.executeUpdate("CREATE TABLE ar_" + t + "(Sura INTEGER, Aya INTEGER, Tafseer CLOB(65536))");

				final Statement stmtAccess_1 = conAccess.createStatement();
				final ResultSet rsAccess_1 = stmtAccess_1.executeQuery("SELECT NOM FROM Tafassir WHERE NTafsir=" + taf);
				rsAccess_1.next();
				stmtH2_1.execute("INSERT INTO Tafseer VALUES('ar_" + t + "','" + rsAccess_1.getString(1) + "')");
				rsAccess_1.close();

				final Statement stmtH2_2 = conH2.createStatement();
				final ResultSet rsH2_2 = stmtH2_2.executeQuery("SELECT Sura, Aya FROM Quran");
				while (rsH2_2.next())
				{
					final int sura = rsH2_2.getInt("Sura");
					final int aya = rsH2_2.getInt("Aya");

					// TODO: stuck here. https://sourceforge.net/p/hsqldb/discussion/73674/thread/619289de/
					final ResultSet rsAccess_2 = stmtAccess_1.executeQuery("SELECT Texte FROM " + taf + " WHERE NSoura=" + sura + " AND NAya=" + aya);
					if (rsAccess_2.next())
					{
						final String tafs = rsAccess_2.getString("Texte");
						final PreparedStatement ps = conH2.prepareStatement("INSERT INTO ar_" + t + " VALUES(?,?,?)");

						ps.setInt(1, sura);
						ps.setInt(2, aya);
						ps.setCharacterStream(3, new StringReader(tafs));
						ps.execute();
						ps.close();
					}
					rsAccess_2.close();
				}
				stmtH2_2.close();
				rsH2_2.close();
				stmtAccess_1.close();

				stmtH2_1.execute("CREATE INDEX AyaIndex_ar_" + t + " ON ar_" + t + "(Aya)");
				stmtH2_1.execute("CREATE INDEX SuraIndex_ar_" + t + " ON ar_" + t + "(Sura)");
			}

			conH2.setAutoCommit(true);

			stmtH2_1.close();
			conAccess.close();
			conH2.createStatement().execute("SHUTDOWN COMPACT");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		new AddTafseer();
	}
}