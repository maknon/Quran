package classes;

import org.sqlite.SQLiteConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CreateSQLiteDB
{
    CreateSQLiteDB() throws Exception
    {
        final String programFolder = new File(CopyAiFiles.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath() + "/";

        Class.forName("org.sqlite.JDBC");
        Class.forName("org.h2.Driver");

        final SQLiteConfig config = new SQLiteConfig();
        config.setEncoding(SQLiteConfig.Encoding.UTF8);

        final Connection sqliteCon = DriverManager.getConnection("jdbc:sqlite:quran.db", config.toProperties());
        final Connection sharedDBConnection = DriverManager.getConnection("jdbc:h2:" + programFolder + "db/quranDatabase");

        sqliteCon.setAutoCommit(false);
        final Statement sqliteStmt = sqliteCon.createStatement();
        sqliteStmt.execute("CREATE TABLE Quran(rowid INTEGER PRIMARY KEY AUTOINCREMENT, Page INTEGER, Sura INTEGER, Aya INTEGER, Location VARCHAR(500))");

        // Android requirement
        sqliteStmt.execute("CREATE TABLE android_metadata(locale TEXT)");
        sqliteStmt.execute("INSERT INTO android_metadata VALUES('ar')");

        final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran ORDER by Page");
        while (rs.next())
        {
            final int aya = rs.getInt("Aya");
            final int sura = rs.getInt("Sura");
            final int page = rs.getInt("Page");
            final String location = rs.getString("Location");
            sqliteStmt.execute("INSERT INTO Quran(Page,Sura,Aya,Location) VALUES(" + page + "," + sura + "," + aya + ",'" + location + "')");
        }
        rs.close();
        sqliteStmt.execute("CREATE INDEX Quran_Page ON Quran(Page)");
        sqliteCon.setAutoCommit(true);
        sqliteCon.close();

        for (int i = 1; i <= 604; i++)
        {
            final FileWriter fw = new FileWriter(String.valueOf(i), true);
            final BufferedWriter bw = new BufferedWriter(fw);

            final ResultSet rs1 = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran WHERE Page=" + i  + " ORDER BY Sura, Aya");
            while (rs1.next())
            {
                final int aya = rs1.getInt("Aya");
                final int sura = rs1.getInt("Sura");
                final String location = rs1.getString("Location");
                bw.write(aya + "ö" + sura + "ö" + location);
                bw.newLine();
            }
            bw.close();
        }

        sharedDBConnection.close();
    }

    public static void main(final String[] args) throws Exception
    {
        new CreateSQLiteDB();
    }
}
