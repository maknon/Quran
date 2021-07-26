package classes;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
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
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;

import static classes.CreatePDF_QuranDb.*;

// USE iText version, this creates bigger file since it drawImage and do not use the compressed png files directly !
public class CreatePDF_PdfBox_Png
{
	final static boolean hafs = true;

	String src_hafs = "C:/Users/Ebrahim/Desktop/Quran/hafs-mobile-png/";
	String src_warsh = "C:/Users/Ebrahim/Desktop/Quran/warsh-mobile-png/";
	String p = "C:/Users/Ebrahim/Desktop/Quran/";
	String v = "1.2";

	CreatePDF_PdfBox_Png()
	{
		final String title = hafs ? "القرآن الكريم برواية حفص" : "القرآن الكريم برواية ورش";
		final String creator = "إعداد موقع مكنون maknoon.com";
		final String subject = "Holy Quran in " + (hafs ? "Hafs" : "Warsh") + " @maknoon.com";

		try
		{
			final DecimalFormat formatter = new DecimalFormat("000");
			final PDDocument doc = new PDDocument();
			for (int i = 1; i <= 604; i++)
			{
				//final BufferedImage awtImage = ImageIO.read(new File((hafs ? src_hafs : src_warsh) + i + ".png"));
				//final PDImageXObject pdImage = LosslessFactory.createFromImage(doc, awtImage);

				final String f = formatter.format(i);
				final PDImageXObject pdImage = PDImageXObject.createFromFile((hafs ?
						src_hafs + f + "___Hafs39__DM.png" :
						src_warsh + f + "___Warsh39__DM.png") , doc);

				float w = pdImage.getWidth();
				float h = pdImage.getHeight();
				final PDPage page = new PDPage(new PDRectangle(w,h));
				doc.addPage(page);

				final PDPageContentStream contents = new PDPageContentStream(doc, page);
				contents.drawImage(pdImage, 0, 0);
				contents.close();

				//PDPageContentStream contents = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false, true);
				//contents.drawImage(pdImage, 0, 0, pdImage.getWidth(), pdImage.getHeight());
				//contents.close();
			}

			final PDViewerPreferences vp = new PDViewerPreferences(new COSDictionary());
			vp.setReadingDirection(PDViewerPreferences.READING_DIRECTION.R2L);

			final PDDocumentCatalog docCatalog = doc.getDocumentCatalog();
			docCatalog.setViewerPreferences(vp);

			// PDF and XMP properties must be identical, otherwise document is not PDF/A compliant
			final COSStream cosStream = new COSStream();
			doc.setDocumentInformation(createPDFDocumentInfo(title, creator, subject));
			docCatalog.setMetadata(createXMPMetadata(cosStream, title, creator, subject));
			cosStream.close();

			final PDPageTree page = doc.getPages();
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
			doc.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);

			doc.save(p + (hafs ? "quran_hafs" : "quran_warsh") + "_s.pdf");
			doc.close();
		}
		catch (IOException | BadFieldValueException | TransformerException e)
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
		new CreatePDF_PdfBox_Png();
	}
}