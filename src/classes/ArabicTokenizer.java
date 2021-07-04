package classes;

import org.apache.lucene.analysis.util.CharTokenizer;

public final class ArabicTokenizer extends CharTokenizer
{
    protected boolean isTokenChar(int c)
    {
        switch(c)
        {
            case 'ء': return true; //U+0621
            case 'آ': return true; //U+0622
            case 'أ': return true; //U+0623
            case 'ؤ': return true; //U+0624
            case 'إ': return true; //U+0625
            case 'ئ': return true; //U+0626
            case 'ا': return true; //U+0627
            case 'ب': return true; //U+0628
            case 'ة': return true; //U+0629
            case 'ت': return true; //U+062A
            case 'ث': return true; //U+062B
            case 'ج': return true; //U+062C
            case 'ح': return true; //U+062D
            case 'خ': return true; //U+062E
            case 'د': return true; //U+062F
            case 'ذ': return true; //U+0630
            case 'ر': return true; //U+0631
            case 'ز': return true; //U+0632
            case 'س': return true; //U+0633
            case 'ش': return true; //U+0634
            case 'ص': return true; //U+0635
            case 'ض': return true; //U+0636
            case 'ط': return true; //U+0637
            case 'ظ': return true; //U+0638
            case 'ع': return true; //U+0639
            case 'غ': return true; //U+063A
            case 'ـ': return true; //U+0640
            case 'ف': return true; //U+0641
            case 'ق': return true; //U+0642
            case 'ك': return true; //U+0643
            case 'ل': return true; //U+0644
            case 'م': return true; //U+0645
            case 'ن': return true; //U+0646
            case 'ه': return true; //U+0647
            case 'و': return true; //U+0648
            case 'ى': return true; //U+0649
            case 'ي': return true; //U+064A
            case 'ً': return true; //U+064B : ARABIC FATHATAN
            case 'ٌ': return true; //U+064C : ARABIC DAMMATAN
            case 'ٍ': return true; //U+064D : ARABIC KASRATAN
            case 'َ': return true; //U+064E : ARABIC FATHA
            case 'ُ': return true; //U+064F : ARABIC DAMMA
            case 'ِ': return true; //U+0650 : ARABIC KASRA
            case 'ّ': return true; //U+0651 : ARABIC SHADDA
            case 'ْ': return true; //U+0652 : ARABIC SUKUN

            // Urdu letters
            case 'ے': return true; //U+06D2
            case 'پ': return true; //U+067E
            case 'ٹ': return true; //U+0679
            case 'چ': return true; //U+0686
            case 'ژ': return true; //U+0698
            case 'ڈ': return true; //U+0688
            case 'گ': return true; //U+06AF
            case 'ک': return true; //U+06A9
            case 'ڑ': return true; //U+0691
            case 'ں': return true; //U+06BA
            case 'ھ': return true; //U+06BE
            case 'ہ': return true; //U+06C1
            case 'ی': return true; //U+06CC Urdu ی is different than Arabic ى
            case 'ۃ': return true; //U+06C3 Urdu ۃ is different than Arabic ة
            default: return false;
        }
    }
}