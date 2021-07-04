package classes;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public final class ArabicRootFilter extends TokenFilter
{
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private IndexSearcher rootsTableIndexSearcher;
    static final ClassLoader cl = ArabicRootFilter.class.getClassLoader();
    ArabicRootFilter(TokenStream in)
    {
        super(in);

        try
        {
            //rootsTableIndexSearcher = new IndexSearcher(new RAMDirectory(FSDirectory.open(new File("arabicRootsTableIndex"))), true); // This is faster but consumes memory, no need since 16MB is expensive
            rootsTableIndexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(cl.getResource("arabicRootsTableIndex").toURI()).toPath())));
        }
        catch(Exception e){e.printStackTrace();}
    }

    public boolean incrementToken() throws IOException
    {
        if(!input.incrementToken()) return false;

        String transformed = termAtt.toString();
        final ScoreDoc[] hits = rootsTableIndexSearcher.search(new TermQuery(new Term("word", transformed)), 1).scoreDocs;
        if(hits.length>0)
            transformed = rootsTableIndexSearcher.doc(hits[0].doc).get("root");

        termAtt.setEmpty().append(transformed);

        return true;
    }
}