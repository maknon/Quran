package classes;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.utils.PdfMerger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import static classes.CreatePDF_QuranDb.hezp_page;
import static classes.CreatePDF_QuranDb.hezp_end_page;
import static com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants.RIGHT_TO_LEFT;

public class CreatePDF_iText_Hezp
{
	final static boolean hafs = true;

	String p = "F:/Quran/";
	String src_hafs = p + "hafs-mobile-png/";
	String src_warsh = p + "warsh-mobile-png/";
	String v = "1.2";

	CreatePDF_iText_Hezp() throws IOException
	{
		final String title = hafs ? "القرآن الكريم برواية حفص" : "القرآن الكريم برواية ورش";
		final String creator = "إعداد موقع مكنون";
		final String subject = "Holy Quran in " + (hafs ? "Hafs" : "Warsh");

		for (int k = 0; k < 60; k++)
		{
			final PdfDocument pdfDocument = new PdfDocument(new PdfWriter(p + (hafs ? "quran_hafs_mh" : "quran_warsh_mh") + (k + 1) + ".pdf"
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

			final PdfDocumentInfo info = pdfDocument.getDocumentInfo();
			info.setTitle(title);
			info.setAuthor(creator);
			info.setSubject(subject);
			info.setKeywords("الجزء " + (k + 1));
			info.setMoreInfo(new String("النسخة".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), v);
			info.setMoreInfo(new String("المصدر".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), "https://www.maknoon.com/community/threads/164");

			info.setCreator("©2021 maknoon.com");

			final PdfMerger merger = new PdfMerger(pdfDocument, false, false);

			final DecimalFormat formatter = new DecimalFormat("000");
			for (int i = hezp_page[k]; i <= hezp_end_page[k]; i++)
			{
				final String f = formatter.format(i);
				final PdfDocument doc = new PdfDocument(new PdfReader(hafs ?
						(p + "hafs-mobile-pdf/" + f + "___Hafs39__DM.pdf") :
						(p + "warsh-mobile-pdf/" + f + "___Warsh39__DM.pdf")
				));
				merger.merge(doc, 1, 1);
				doc.close();
			}

			/*
			final Document document = new Document(pdfDocument);
			document.setMargins(0, 0, 0, 0);

			final DecimalFormat formatter = new DecimalFormat("000");
			for (int i = hezp_page[k]; i < (k == 59 ? 605 : hezp_page[k + 1]); i++)
			{
				final String f = formatter.format(i);
				final ImageData imageData = ImageDataFactory.create((hafs ?
						src_hafs + f + "___Hafs39__DM.png" :
						src_warsh + f + "___Warsh39__DM.png"));

				pdfDocument.setDefaultPageSize(new PageSize(new Rectangle(imageData.getWidth(), imageData.getHeight())));

				final Image image = new Image(imageData);

				document.add(image);
			}
			document.close();
			*/

			pdfDocument.close();
		}
	}

	public static void main(String[] args) throws IOException
	{
		new CreatePDF_iText_Hezp();
	}
}