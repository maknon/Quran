package classes;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;

import static classes.CreatePDF_QuranDb.*;

// https://github.com/apache/pdfbox/blob/trunk/examples/src/main/java/org/apache/pdfbox/examples/util/PDFMergerExample.java
// Use iText version, it gives more options to control the viewer settings and meta data
public class CreatePDF_PdfBox_Pdf
{
	final static boolean mobile = false;
	final static boolean hafs = false;

	String p = "F:/Quran/";
	String v = "1.2";

	CreatePDF_PdfBox_Pdf()
	{
		final PDFMergerUtility pdfMerger = new PDFMergerUtility();
		pdfMerger.setDestinationFileName(p + "temp.pdf");

		final String title = hafs ? "القرآن الكريم برواية حفص" : "القرآن الكريم برواية ورش";
		final String creator = "إعداد موقع مكنون maknoon.com";
		final String subject = "Holy Quran in " + (hafs ? "Hafs" : "Warsh") + " @maknoon.com";

		try (COSStream cosStream = new COSStream())
		{
			// PDF and XMP properties must be identical, otherwise document is not PDF/A compliant
			pdfMerger.setDestinationDocumentInformation(createPDFDocumentInfo(title, creator, subject));

			// will display a message at the top of Adobe reader which is noisy
			//pdfMerger.setDestinationMetadata(createXMPMetadata(cosStream, title, creator, subject));

			final DecimalFormat formatter = new DecimalFormat("000");
			for (int i = 1; i <= 604; i++)
			{
				final String f = formatter.format(i);
				pdfMerger.addSource(new File(mobile ?
						(hafs ? (p + "hafs-mobile-pdf/" + f + "___Hafs39__DM.pdf") :
								(p + "warsh-mobile-pdf/" + f + "___Warsh39__DM.pdf")) :
						(hafs ? (p + "hafs-pdf/" + f + "___Hafs39__DM.pdf") :
								(p + "warsh-pdf/" + f + "___Warsh39__DM.pdf"))
				));
			}

			pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

			final PDDocument document = Loader.loadPDF(new File(p + "temp.pdf"));

			final PDViewerPreferences vp = new PDViewerPreferences(new COSDictionary());
			vp.setReadingDirection(PDViewerPreferences.READING_DIRECTION.R2L);

			final PDDocumentCatalog docCatalog = document.getDocumentCatalog();
			docCatalog.setViewerPreferences(vp);

			if(!mobile)
				//docCatalog.setPageLayout(PageLayout.TWO_COLUMN_RIGHT);
				docCatalog.setPageLayout(PageLayout.TWO_COLUMN_LEFT);

			final PDPageTree page = document.getPages();
			final PDDocumentOutline outline = new PDDocumentOutline();
			docCatalog.setDocumentOutline(outline);

			final PDOutlineItem suraOutline = new PDOutlineItem();
			suraOutline.setTitle("السورة");
			outline.addLast(suraOutline);
			for (int i = 0; i < sura_ar.length; i++)
			{
				final PDPageDestination dest = new PDPageFitWidthDestination();
				dest.setPage(page.get(sura_page[i] - 1));

				final PDOutlineItem bookmark = new PDOutlineItem();
				bookmark.setDestination(dest);
				bookmark.setTitle(sura_ar[i]);
				suraOutline.addLast(bookmark);
			}
			//suraOutline.openNode();
			//outline.openNode();

			final PDOutlineItem juzOutline = new PDOutlineItem();
			juzOutline.setTitle("الجزء");
			outline.addLast(juzOutline);
			for (int i = 0; i < juz_page.length; i++)
			{
				final PDPageDestination dest = new PDPageFitWidthDestination();
				dest.setPage(page.get(juz_page[i] - 1));

				final PDOutlineItem bookmark = new PDOutlineItem();
				bookmark.setDestination(dest);
				bookmark.setTitle(String.valueOf(i + 1));
				juzOutline.addLast(bookmark);
			}
			//juzOutline.openNode();
			//outline.openNode();

			final PDOutlineItem hezpOutline = new PDOutlineItem();
			hezpOutline.setTitle("الحزب");
			outline.addLast(hezpOutline);
			for (int i = 0; i < hezp_page.length; i++)
			{
				final PDPageDestination dest = new PDPageFitWidthDestination();
				dest.setPage(page.get(hezp_page[i] - 1));

				final PDOutlineItem bookmark = new PDOutlineItem();
				bookmark.setDestination(dest);
				bookmark.setTitle(String.valueOf(i + 1));
				hezpOutline.addLast(bookmark);
			}

			// optional: show the outlines when opening the file
			document.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);

			document.save(p + (hafs ? "quran_hafs" : "quran_warsh") + (mobile ? "_m" : "_pc") + ".pdf");
			document.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private PDDocumentInformation createPDFDocumentInfo(String title, String creator, String subject)
	{
		final PDDocumentInformation documentInformation = new PDDocumentInformation();
		documentInformation.setTitle(title);
		documentInformation.setCreator(creator);
		documentInformation.setSubject(subject);
		documentInformation.setCustomMetadataValue("النسخة", v);
		return documentInformation;
	}

	private PDMetadata createXMPMetadata(COSStream cosStream, String title, String creator, String subject)
			throws BadFieldValueException, TransformerException, IOException
	{
		final XMPMetadata xmpMetadata = XMPMetadata.createXMPMetadata();

		// PDF/A-1b properties
		final PDFAIdentificationSchema pdfaSchema = xmpMetadata.createAndAddPFAIdentificationSchema();
		pdfaSchema.setPart(1);
		pdfaSchema.setConformance("B");

		// Dublin Core properties
		final DublinCoreSchema dublinCoreSchema = xmpMetadata.createAndAddDublinCoreSchema();
		dublinCoreSchema.setTitle(title);
		dublinCoreSchema.addCreator(creator);
		dublinCoreSchema.setDescription(subject);
		//dublinCoreSchema.setTextPropertyValue("النسخة", v);

		// XMP Basic properties
		final XMPBasicSchema basicSchema = xmpMetadata.createAndAddXMPBasicSchema();
		final Calendar creationDate = Calendar.getInstance();
		basicSchema.setCreateDate(creationDate);
		basicSchema.setModifyDate(creationDate);
		basicSchema.setMetadataDate(creationDate);
		basicSchema.setCreatorTool(creator);

		// Create and return XMP data structure in XML format
		try (ByteArrayOutputStream xmpOutputStream = new ByteArrayOutputStream();
			 OutputStream cosXMPStream = cosStream.createOutputStream())
		{
			new XmpSerializer().serialize(xmpMetadata, xmpOutputStream, true);
			cosXMPStream.write(xmpOutputStream.toByteArray());
			return new PDMetadata(cosStream);
		}
	}

	public static void main(String[] args) throws IOException
	{
		new CreatePDF_PdfBox_Pdf();
	}
}