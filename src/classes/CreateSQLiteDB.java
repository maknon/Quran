package classes;

import org.sqlite.SQLiteConfig;

import java.io.File;
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
        sqliteStmt.execute("CREATE TABLE Quran(rowid INTEGER PRIMARY KEY AUTOINCREMENT, Page INTEGER, Sura INTEGER, Aya INTEGER)");

        // Android requirement
        sqliteStmt.execute("CREATE TABLE android_metadata(locale TEXT)");
        sqliteStmt.execute("INSERT INTO android_metadata VALUES('ar')");

        final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran");
        while (rs.next())
        {
            final int aya = rs.getInt("Aya");
            final int sura = rs.getInt("Sura");
            final int page = rs.getInt("Page");
            sqliteStmt.execute("INSERT INTO Quran(Page,Sura,Aya) VALUES(" + page + "," + sura + "," + aya + ")");
        }

        sqliteStmt.execute("CREATE INDEX Quran_Page ON Quran(Page)");

        sqliteCon.setAutoCommit(true);
        sqliteCon.close();
        sharedDBConnection.close();
    }

    public static void main(final String[] args) throws Exception
    {
        new CreateSQLiteDB();
    }
}
