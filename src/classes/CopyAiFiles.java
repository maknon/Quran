package classes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class CopyAiFiles
{
    CopyAiFiles() throws Exception
    {
        final String programFolder = new File(CopyAiFiles.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath() + "/";

        Class.forName("org.h2.Driver");
        final Connection sharedDBConnection = DriverManager.getConnection("jdbc:h2:" + programFolder + "db/quranDatabase");

        final String pageFolder = "C:/Users/ias12/Desktop/hafs-page/";
        final String ayaFolder = "C:/Users/ias12/Desktop/hafs-aya/";

        for (int i = 1; i <= 604; i++)
        {
            final Path src = Paths.get(pageFolder + i + ".ai");
            final ResultSet rs = sharedDBConnection.createStatement().executeQuery("SELECT * FROM Quran WHERE Page=" + i);
            while (rs.next())
            {
                final int aya = rs.getInt("Aya");
                final int sura = rs.getInt("Sura");

                final Path to = Paths.get(ayaFolder + i + "-" + sura + "-" + aya + ".ai");
                Files.copy(src, to, StandardCopyOption.REPLACE_EXISTING);
            }

            final Path to = Paths.get(ayaFolder + i + "-f" + ".ai");
            Files.copy(src, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void main(final String[] args) throws Exception
    {
        new CopyAiFiles();
    }
}
