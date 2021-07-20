package classes;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.kernel.utils.PdfMerger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import static classes.CreatePDF_QuranDb.*;
import static classes.CreatePDF_QuranDb.hezp_page;
import static com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants.RIGHT_TO_LEFT;

public class CreatePDF_iText_Pdf
{
	final static boolean mobile = false;
	final static boolean hafs = false;

	String p = "C:/Users/Ebrahim/Desktop/Quran/";
	String v = "1.2";

	CreatePDF_iText_Pdf() throws IOException
	{
		final String title = hafs ? "القرآن الكريم برواية حفص" : "القرآن الكريم برواية ورش";
		final String creator = "إعداد موقع مكنون";
		final String subject = "Holy Quran in " + (hafs ? "Hafs" : "Warsh");

		final PdfDocument pdfDocument = new PdfDocument(new PdfWriter(p + (hafs ? "quran_hafs" : "quran_warsh") + (mobile ? "_m" : "_pc") + ".pdf"
				, new WriterProperties()
				.addXmpMetadata()
				.setFullCompressionMode(true) // reduce 0.5%
				//.setCompressionLevel(CompressionConstants.BEST_COMPRESSION)
				.setPdfVersion(PdfVersion.PDF_1_6)));
		pdfDocument.getCatalog().setPageMode(PdfName.UseOutlines);
		pdfDocument.getCatalog().put(PdfName.Lang, new PdfString("AR"));

		final PdfViewerPreferences viewerPreferences = new PdfViewerPreferences();
		viewerPreferences.setDirection(RIGHT_TO_LEFT);
		viewerPreferences.setHideToolbar(true);
		//viewerPreferences.setHideMenubar(true); // noisy

		pdfDocument.getCatalog().setViewerPreferences(viewerPreferences);

		if(!mobile)
			pdfDocument.getCatalog().setPageLayout(PdfName.TwoColumnLeft);

		final PdfDocumentInfo info = pdfDocument.getDocumentInfo();
		info.setTitle(title);
		info.setAuthor(creator);
		info.setSubject(subject);
		info.setKeywords("النسخة " + v);
		//info.setMoreInfo(new String("النسخة".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), v);
		info.setMoreInfo(new String("المصدر".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), "https://www.maknoon.com/community/threads/164");

		info.setCreator("©2021 maknoon.com");

		final PdfMerger merger = new PdfMerger(pdfDocument, false, false);

		final DecimalFormat formatter = new DecimalFormat("000");
		for (int i = 1; i <= 604; i++)
		{
			final String f = formatter.format(i);

			final PdfDocument doc = new PdfDocument(new PdfReader(mobile ?
					(hafs ? (p + "hafs-mobile-pdf/" + f + "___Hafs39__DM.pdf") :
							(p + "warsh-mobile-pdf/" + f + "___Warsh39__DM.pdf")) :
					(hafs ? (p + "hafs-pdf/" + f + "___Hafs39__DM.pdf") :
							(p + "warsh-pdf/" + f + "___Warsh39__DM.pdf"))
			));
			merger.merge(doc, 1, 1);
			doc.close();
		}

		final PdfOutline outlines = pdfDocument.getOutlines(false);
		final PdfOutline sura_outline = outlines.addOutline("السورة");
		sura_outline.setOpen(false);
		for (int i = 0; i < sura_ar.length; i++)
		{
			final PdfOutline sura = sura_outline.addOutline(sura_ar[i]);
			sura.addDestination(PdfExplicitDestination.createFitH(pdfDocument.getPage(sura_page[i]), pdfDocument.getPage(sura_page[i]).getPageSize().getTop()));
		}

		final PdfOutline juz_outline = outlines.addOutline("الجزء");
		juz_outline.setOpen(false);
		for (int i = 0; i < juz_page.length; i++)
		{
			final PdfOutline juz = juz_outline.addOutline(String.valueOf(i + 1));
			juz.addDestination(PdfExplicitDestination.createFitH(pdfDocument.getPage(juz_page[i]), pdfDocument.getPage(juz_page[i]).getPageSize().getTop()));
		}

		final PdfOutline hezp_outline = outlines.addOutline("الحزب");
		hezp_outline.setOpen(false);
		for (int i = 0; i < hezp_page.length; i++)
		{
			final PdfOutline hezp = hezp_outline.addOutline(String.valueOf(i + 1));
			hezp.addDestination(PdfExplicitDestination.createFitH(pdfDocument.getPage(hezp_page[i]), pdfDocument.getPage(hezp_page[i]).getPageSize().getTop()));
		}

		merger.close();
		pdfDocument.close();
	}

	public static void main(String[] args) throws IOException
	{
		new CreatePDF_iText_Pdf();
	}
}