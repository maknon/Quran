package classes;

import javax.swing.*;
import java.io.*;
import java.sql.*;

public class Tafseer
{
    Tafseer()
    {
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("كتب الشاملة", "bok"));
		fc.setAcceptAllFileFilterUsed(false);

        final int returnVal = fc.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            final File file = fc.getSelectedFile();
            try
            {
                Class.forName("org.h2.Driver");

                final Connection conAccess = DriverManager.getConnection("jdbc:ucanaccess://"+file+";memory=false;singleconnection=true");
                final Connection conH2 = DriverManager.getConnection("jdbc:h2:db/quranDatabase");

                final Statement stmtH2_1 = conH2.createStatement();
                final Statement stmtH2_2 = conH2.createStatement();
                final Statement stmtAccess_1 = conAccess.createStatement();

                final ResultSet rsAccess_1 = stmtAccess_1.executeQuery("SELECT * FROM Main");
                if(rsAccess_1.next())
                {
                    final String tafseerColumnName = rsAccess_1.getString("Bk");
                    final String table = 'b'+rsAccess_1.getString("BkId");
                    
                    stmtH2_2.executeUpdate("ALTER TABLE Quran ADD \"تفسير "+tafseerColumnName+"\" LONGVARCHAR");

                    final ResultSet rsH2 = stmtH2_1.executeQuery("SELECT Sura, Aya FROM Quran");
                    while(rsH2.next())
                    {
                        final int sura = rsH2.getInt("Sura");
                        final int aya = rsH2.getInt("Aya");
                        final ResultSet rsAccess_2 = stmtAccess_1.executeQuery("SELECT nass FROM "+table+" WHERE sora="+sura+" AND aya="+aya);
                        if(rsAccess_2.next())
                            stmtH2_2.executeUpdate("UPDATE Quran SET \"تفسير "+tafseerColumnName+"\" = '"+rsAccess_2.getString("nass")+"' WHERE Sura= "+sura+" AND Aya="+aya);
                    }
                }

                conAccess.close();
                conH2.close();
            }
            catch(Exception e){e.printStackTrace();}
        }
    }

    public static void main(String[] args){new Tafseer();}
}
