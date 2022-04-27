package classes;

import org.apache.lucene.analysis.core.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriterConfig.*;

import java.io.File;
import java.sql.*;

public final class CreateIndices
{
	public static void main(String[] args)
	{
		new CreateIndices();
	}

	private CreateIndices()
	{
		try
		{
			final IndexWriter arabicRootsTableWriter = new IndexWriter(FSDirectory.open(new File("arabicRootsTableIndex").toPath()), new IndexWriterConfig(new KeywordAnalyzer()).setOpenMode(OpenMode.CREATE));

            final Connection con1 = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/Jthr.mdb;memory=false;singleconnection=true");
            final Statement s1 = con1.createStatement();

			final ResultSet rs1 = s1.executeQuery("SELECT * FROM JathrWords");
			while(rs1.next())
			{
				final Document doc = new Document();
                //doc.add(new Field("root", new String(rs1.getBytes("Jathr"), "cp1256").trim(), StringField.TYPE_STORED));
                //doc.add(new Field("word", new String(rs1.getBytes("Word"), "cp1256").trim(), StringField.TYPE_STORED));
                doc.add(new Field("root", rs1.getString("Jathr").trim(), StringField.TYPE_STORED));
				doc.add(new Field("word", rs1.getString("Word").trim(), StringField.TYPE_STORED));
				arabicRootsTableWriter.addDocument(doc);
			}

			arabicRootsTableWriter.close();
			con1.close();

			final ArabicRootAnalyzer arabicRootsAnalyzer = new ArabicRootAnalyzer();
			final org.apache.lucene.analysis.ar.ArabicAnalyzer arabicLuceneAnalyzer = new org.apache.lucene.analysis.ar.ArabicAnalyzer();
			final ArabicAnalyzer arabicAnalyzer = new ArabicAnalyzer();

			final IndexWriter arabicWriter = new IndexWriter(FSDirectory.open(new File("arabicIndex").toPath()), new IndexWriterConfig(arabicAnalyzer).setOpenMode(OpenMode.CREATE));
			final IndexWriter arabicRootsWriter = new IndexWriter(FSDirectory.open(new File("arabicRootsIndex").toPath()), new IndexWriterConfig(arabicRootsAnalyzer).setOpenMode(OpenMode.CREATE));
			final IndexWriter arabicLuceneWriter = new IndexWriter(FSDirectory.open(new File("arabicLuceneIndex").toPath()), new IndexWriterConfig(arabicLuceneAnalyzer).setOpenMode(OpenMode.CREATE));

            final Connection con2 = DriverManager.getConnection("jdbc:ucanaccess://E:/Quran Media/DB/qurandb-2.mdb;memory=false;singleconnection=true");
            final Statement s2 = con2.createStatement();
			
			final ResultSet rs2 = s2.executeQuery("SELECT * FROM Quran");
			while(rs2.next())
			{
				final Document doc = new Document();
				doc.add(new Field("Aya", rs2.getString("Aya").trim(), StringField.TYPE_STORED));
				doc.add(new Field("Sura", rs2.getString("Sura").trim(), StringField.TYPE_STORED));
                //doc.add(new Field("Quran", new String(rs2.getBytes("Quran"), "cp1256").trim(), TextField.TYPE_STORED));
                doc.add(new Field("Quran", rs2.getString("Quran").trim(), TextField.TYPE_STORED));
				arabicWriter.addDocument(doc);
				arabicRootsWriter.addDocument(doc);
				arabicLuceneWriter.addDocument(doc);
			}

			arabicWriter.close();
			arabicRootsWriter.close();
			arabicLuceneWriter.close();
			con2.close();
		}
		catch(Exception e){e.printStackTrace();}
	}
}
