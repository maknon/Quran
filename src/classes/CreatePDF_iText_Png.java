package classes;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Vector;

import static classes.CreatePDF_QuranDb.*;
import static com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants.RIGHT_TO_LEFT;

public class CreatePDF_iText_Png
{
	final static boolean hafs = true;

	String p = "C:/Users/Ebrahim/Desktop/Quran/";
	String src_hafs = p + "hafs-mobile-png/";
	String src_warsh = p + "warsh-mobile-png/";
	String v = "1.2";

	CreatePDF_iText_Png() throws Exception
	{
		final String title = hafs ? "القرآن الكريم برواية حفص" : "القرآن الكريم برواية ورش";
		final String creator = "إعداد موقع مكنون";
		final String subject = "Holy Quran in " + (hafs ? "Hafs" : "Warsh");

		final PdfDocument pdfDocument = new PdfDocument(new PdfWriter(p + (hafs ? "quran_hafs" : "quran_warsh") + "_s.pdf"
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
		info.setKeywords("النسخة " + v);
		//info.setMoreInfo(new String("النسخة".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), v);
		info.setMoreInfo(new String("المصدر".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), "https://www.maknoon.com/community/threads/164");

		info.setCreator("©2021 maknoon.com");

		final Document document = new Document(pdfDocument);
		document.setMargins(0,0,0,0);

		final Vector<Float> imageHeight = new Vector<>();
		final DecimalFormat formatter = new DecimalFormat("000");
		for (int i = 1; i <= 604; i++)
		{
			final String f = formatter.format(i);
			final ImageData imageData = ImageDataFactory.create((hafs ?
					src_hafs + f + "___Hafs39__DM.png" :
					src_warsh + f + "___Warsh39__DM.png"));

			pdfDocument.setDefaultPageSize(new PageSize(new Rectangle(imageData.getWidth(),imageData.getHeight())) );

			imageHeight.add(imageData.getHeight());

			final Image image = new Image(imageData);
			//image.setWidth(imageData.getWidth());
			//image.scaleAbsolute(imageData.getWidth(), imageData.getHeight());
			//image.setHeight(imageData.getHeight());
			//image.setWidth(pdfDocument.getDefaultPageSize().getWidth() - 50);
			//image.setAutoScaleHeight(true);

			document.add(image);
		}

		final PdfOutline outlines = pdfDocument.getOutlines(false);
		final PdfOutline sura_outline = outlines.addOutline("السورة");
		sura_outline.setOpen(false);
		for (int i = 0; i < sura_ar.length; i++)
		{
			final PdfOutline sura = sura_outline.addOutline(sura_ar[i]);
			//sura.addDestination(PdfExplicitDestination.createFitH(pdfDocument.getPage(sura_page[i]), 0)); // not working properly
			//sura.addDestination(PdfExplicitDestination.createFitH(pdfDocument.getPage(sura_page[i]), pdfDocument.getPage(sura_page[i]).getPageSize().getHeight())); // throws error
			sura.addDestination(PdfExplicitDestination.createFitH(pdfDocument.getPage(sura_page[i]), imageHeight.get(sura_page[i]-1)));
			//sura.addDestination(PdfExplicitDestination.createFit(pdfDocument.getPage(sura_page[i])));
		}

		final PdfOutline juz_outline = outlines.addOutline("الجزء");
		juz_outline.setOpen(false);
		for (int i = 0; i < juz_page.length; i++)
		{
			final PdfOutline juz = juz_outline.addOutline(String.valueOf(i + 1));
			juz.addDestination(PdfExplicitDestination.createFitH(pdfDocument.getPage(juz_page[i]), imageHeight.get(juz_page[i]-1)));
		}

		final PdfOutline hezp_outline = outlines.addOutline("الحزب");
		hezp_outline.setOpen(false);
		for (int i = 0; i < hezp_page.length; i++)
		{
			final PdfOutline hezp = hezp_outline.addOutline(String.valueOf(i + 1));
			hezp.addDestination(PdfExplicitDestination.createFitH(pdfDocument.getPage(hezp_page[i]), imageHeight.get(hezp_page[i]-1)));
		}

		document.close();
		pdfDocument.close();
	}

	public static void main(String[] args) throws Exception
	{
		new CreatePDF_iText_Png();
	}
}