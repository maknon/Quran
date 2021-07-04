package classes;

import java.io.*;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;

public final class ArabicAnalyzer extends StopwordAnalyzerBase
{
    private static class DefaultSetHolder
    {
        static final CharArraySet DEFAULT_STOP_SET;
        static final ClassLoader cl = ArabicAnalyzer.class.getClassLoader();
        static
        {
            try
            {
                DEFAULT_STOP_SET = loadStopwordSet(new File(cl.getResource("setting/ArabicTokens.txt").toURI()).toPath());
            }
            catch (Exception ex){throw new RuntimeException("Unable to load default stopword set");}
        }
    }

    ArabicAnalyzer()
    {
        super(DefaultSetHolder.DEFAULT_STOP_SET);
    }

    protected TokenStreamComponents createComponents(final String fieldName)
    {
        final ArabicTokenizer src = new ArabicTokenizer();
        TokenStream result = new StopFilter(src, DefaultSetHolder.DEFAULT_STOP_SET);
        result = new PatternReplaceFilter(result, Pattern.compile("[\u0650\u064D\u064E\u064B\u064F\u064C\u0652\u0651]"), null, true);
        return new TokenStreamComponents(src, result);
    }
}