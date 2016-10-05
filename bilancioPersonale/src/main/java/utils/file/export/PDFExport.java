package utils.file.export;

import harmony.java.awt.Color;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

import utils.DateUtils;
import utils.NumberUtils;
import android.graphics.Bitmap;

import com.dto.Ricavo;
import com.dto.Spesa;
import com.dto.TagRicavo;
import com.dto.TagSpesa;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PDFExport {

	private Document document;
	private Collection<Spesa> spese;
	private Collection<Ricavo> ricavi;
	private static Color verde;
	private static Color rosso;
	private BaseFont bf_times;
	private File file;
	private static Color bluChiaro;
	private double sommaRicavi = 0;
	private double sommaSpese = 0;
	private boolean pdfTotale;
	private Bitmap bitmap;
	private String startDate;
	private String endDate;
	public final static String EXTENSION = ".pdf";

	public PDFExport(Collection<Spesa> spese, Collection<Ricavo> ricavi,
			File file, boolean pdfTotale, String startDate, String endDate) {
		this.spese = spese;
		this.ricavi = ricavi;
		this.file = file;
		this.pdfTotale = pdfTotale;
		this.startDate = startDate;
		this.endDate = endDate;
		String oldName = file.getAbsolutePath();
		if (startDate != null && endDate != null) {
			this.file = new File(oldName.substring(0, oldName.length() - 4)
					+ "_" + this.startDate + "_" + this.endDate + EXTENSION);
		}
	}

	public boolean export() {
		try {
			document = new Document(PageSize.A4, 36, 36, 54, 54);
			PdfWriter.getInstance(document, new FileOutputStream(file));
			bf_times = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1252",
					false);
			Font headerFont = new Font(bf_times, 10);
			Font detailFont = new Font(bf_times, 8);
			Font fontTitleWhite = new Font(bf_times, 12, Element.ALIGN_MIDDLE);
			fontTitleWhite.setColor(Color.WHITE);

			Font fontBilancioRed = new Font(bf_times, 14, Element.ALIGN_MIDDLE);
			fontBilancioRed.setColor(Color.RED);

			Font fontBilancioGreen = new Font(bf_times, 14,
					Element.ALIGN_MIDDLE);
			fontBilancioGreen.setColor(Color.GREEN);

			verde = new Color(0, 102, 51);
			rosso = new Color(255, 0, 0);
			bluChiaro = new Color(179, 192, 250);

			// step 3
			document.open();

			if (spese == null || ricavi == null) {
				document.add(new Paragraph(new Phrase(
						"Errore durante la generazione del pdf")));
				document.close();
				return false;
			}

			if (spese.isEmpty()) {
				document.add(new Paragraph(new Phrase("Nessuna Spesa ",
						detailFont)));
				document.add(new Paragraph(new Phrase(" ", detailFont)));
			}
			if (ricavi.isEmpty()) {
				document.add(new Paragraph(new Phrase("Nessun Ricavo",
						detailFont)));
				document.add(new Paragraph(new Phrase(" ", detailFont)));
			}

			PdfPTable tableTitleSpese = new PdfPTable(1);

			if (spese != null && !spese.isEmpty()) {

				// ******TABELLA SPESE*************
				PdfPCell titleSpesa;

				if (pdfTotale) {
					titleSpesa = new PdfPCell(new Paragraph("Spese",
							fontTitleWhite));
					tableTitleSpese.setWidthPercentage(20);
				} else {
					Spesa s = spese.iterator().next();
					String date = s.getData();
					titleSpesa = new PdfPCell(new Paragraph(
							"Spese di: " + date, fontTitleWhite));
					tableTitleSpese.setWidthPercentage(35); // Code 2
				}

				titleSpesa.setBackgroundColor(rosso);

				tableTitleSpese.addCell(titleSpesa);
				tableTitleSpese.getDefaultCell().setBorderColor(rosso);
				tableTitleSpese.getDefaultCell().setBorderWidth(1f);

				tableTitleSpese.setHorizontalAlignment(Element.ALIGN_LEFT);

				PdfPTable tableSpese = new PdfPTable(new float[] { 5, 10, 5 });

				PdfPCell cellDataSpese = new PdfPCell(new Phrase("Data",
						headerFont));
				cellDataSpese.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellDataSpese.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cellDataSpese.setBackgroundColor(bluChiaro);

				PdfPCell cellDescSpese = new PdfPCell(new Phrase("Tag",
						headerFont));
				cellDescSpese.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellDescSpese.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cellDescSpese.setBackgroundColor(bluChiaro);

				PdfPCell cellImpSpese = new PdfPCell(new Phrase("Importo",
						headerFont));
				cellImpSpese.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellImpSpese.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cellImpSpese.setBackgroundColor(bluChiaro);

				tableSpese.addCell(cellDataSpese);
				tableSpese.addCell(cellDescSpese);
				tableSpese.addCell(cellImpSpese);

				tableSpese.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableSpese.setWidthPercentage(100);
				tableSpese.setSpacingAfter(10);

				sommaSpese = 0;
				for (Spesa spesa : spese) {
					String dataOriginale = spesa.getData();
					String dataNuova = DateUtils
							.getPrintableDataFormat(dataOriginale);
					cellDataSpese = new PdfPCell(new Phrase(dataNuova,
							detailFont));
					cellDataSpese.setHorizontalAlignment(Element.ALIGN_CENTER);
					cellDataSpese.setVerticalAlignment(Element.ALIGN_MIDDLE);
					StringBuilder builder = new StringBuilder();

					String descrizione = spesa.getDescrizione();
					if (descrizione == null || descrizione.length() == 0) {
						Collection<TagSpesa> tags = spesa.getTags();
						int count = 0;
						for (TagSpesa tag : tags) {
							builder.append(tag.getValore());
							if (count < tags.size() - 1)
								builder.append(" - ");
							count++;
						}

						cellDescSpese = new PdfPCell(new Phrase(
								builder.toString(), detailFont));
					} else {
						cellDescSpese = new PdfPCell(new Phrase(descrizione,
								detailFont));
					}
					cellDescSpese.setHorizontalAlignment(Element.ALIGN_CENTER);
					cellDescSpese.setVerticalAlignment(Element.ALIGN_MIDDLE);

					cellImpSpese = new PdfPCell(new Phrase(""
							+ NumberUtils.getString(spesa.getImporto()) + " €",
							detailFont));
					cellImpSpese.setHorizontalAlignment(Element.ALIGN_CENTER);
					cellImpSpese.setVerticalAlignment(Element.ALIGN_MIDDLE);

					tableSpese.addCell(cellDataSpese);
					tableSpese.addCell(cellDescSpese);
					tableSpese.addCell(cellImpSpese);

					sommaSpese += spesa.getImporto();

				}
				PdfPCell cellTot = new PdfPCell(
						new Phrase("TOTALE", detailFont));
				cellTot.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellTot.setVerticalAlignment(Element.ALIGN_MIDDLE);
				tableSpese.addCell(new Phrase("", detailFont));
				tableSpese.addCell(cellTot);
				cellTot = new PdfPCell(new Phrase(""
						+ NumberUtils.getString(sommaSpese) + " €", detailFont));
				cellTot.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellTot.setVerticalAlignment(Element.ALIGN_MIDDLE);
				tableSpese.addCell(cellTot);

				document.add(tableTitleSpese);
				document.add(tableSpese);
			}

			if (ricavi != null && !ricavi.isEmpty()) {
				PdfPCell titleRicavo;
				PdfPTable tableTitleRicavi = new PdfPTable(1);

				if (pdfTotale) {
					titleRicavo = new PdfPCell(new Paragraph("Ricavi",
							fontTitleWhite));
					tableTitleRicavi.setWidthPercentage(20);
				} else {
					Ricavo s = ricavi.iterator().next();
					String date = s.getData();
					titleRicavo = new PdfPCell(new Paragraph("Ricavi di: "
							+ date, fontTitleWhite));
					tableTitleRicavi.setWidthPercentage(35); // Code 2
				}

				// ******TABELLA RICAVI*************

				titleRicavo.setBackgroundColor(verde);

				tableTitleRicavi.addCell(titleRicavo);
				tableTitleRicavi.getDefaultCell().setBorderColor(verde);
				tableTitleRicavi.getDefaultCell().setBorderWidth(1f);
				tableTitleRicavi.setHorizontalAlignment(Element.ALIGN_LEFT);

				PdfPTable tableRicavi = new PdfPTable(new float[] { 5, 10, 5 });

				PdfPCell cellDataRicavi = new PdfPCell(new Phrase("Data",
						headerFont));
				cellDataRicavi.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellDataRicavi.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cellDataRicavi.setBackgroundColor(bluChiaro);

				PdfPCell cellDescRicavi = new PdfPCell(new Phrase("Tag",
						headerFont));
				cellDescRicavi.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellDescRicavi.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cellDescRicavi.setBackgroundColor(bluChiaro);

				PdfPCell cellImpRicavi = new PdfPCell(new Phrase("Importo",
						headerFont));
				cellImpRicavi.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellImpRicavi.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cellImpRicavi.setBackgroundColor(bluChiaro);

				tableRicavi.addCell(cellDataRicavi);
				tableRicavi.addCell(cellDescRicavi);
				tableRicavi.addCell(cellImpRicavi);

				tableRicavi.setSpacingAfter(5);
				tableRicavi.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableRicavi.setWidthPercentage(100);

				sommaRicavi = 0;

				for (Ricavo ricavo : ricavi) {
					String dataOriginale = ricavo.getData();
					String dataNuova = DateUtils
							.getPrintableDataFormat(dataOriginale);
					cellDataRicavi = new PdfPCell(new Phrase(dataNuova,
							detailFont));
					cellDataRicavi.setHorizontalAlignment(Element.ALIGN_CENTER);
					cellDataRicavi.setVerticalAlignment(Element.ALIGN_MIDDLE);

					String descrizione = ricavo.getDescrizione();

					if (descrizione == null || descrizione.length() == 0) {

						StringBuilder builder = new StringBuilder();
						Collection<TagRicavo> tags = ricavo.getTags();
						int count = 0;
						for (TagRicavo tag : tags) {
							builder.append(tag.getValore());
							if (count < tags.size() - 1)
								builder.append(" - ");
							count++;
						}

						cellDescRicavi = new PdfPCell(new Phrase(
								builder.toString(), detailFont));
					}
					else{
						cellDescRicavi = new PdfPCell(new Phrase(
								descrizione, detailFont));
					}
					cellDescRicavi.setHorizontalAlignment(Element.ALIGN_CENTER);
					cellDescRicavi.setVerticalAlignment(Element.ALIGN_MIDDLE);

					cellImpRicavi = new PdfPCell(new Phrase(
							"" + NumberUtils.getString(ricavo.getImporto())
									+ " €", detailFont));
					cellImpRicavi.setHorizontalAlignment(Element.ALIGN_CENTER);
					cellImpRicavi.setVerticalAlignment(Element.ALIGN_MIDDLE);

					tableRicavi.addCell(cellDataRicavi);
					tableRicavi.addCell(cellDescRicavi);
					tableRicavi.addCell(cellImpRicavi);

					sommaRicavi += ricavo.getImporto();

				}
				PdfPCell cellTot = new PdfPCell(
						new Phrase("TOTALE", detailFont));
				cellTot.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellTot.setVerticalAlignment(Element.ALIGN_MIDDLE);
				tableRicavi.addCell(new Phrase("", detailFont));
				tableRicavi.addCell(cellTot);
				cellTot = new PdfPCell(
						new Phrase("" + NumberUtils.getString(sommaRicavi)
								+ " €", detailFont));
				cellTot.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellTot.setVerticalAlignment(Element.ALIGN_MIDDLE);
				tableRicavi.addCell(cellTot);

				// step 4
				document.add(tableTitleRicavi);
				document.add(tableRicavi);

			}

			// if (!spese.isEmpty() && !ricavi.isEmpty()) {
			double totale = sommaRicavi - sommaSpese;
			if (totale >= 0) {
				document.add(new Paragraph(new Phrase("BILANCIO POSITIVO: "
						+ NumberUtils.getString(totale) + " €",
						fontBilancioGreen)));
			} else {
				document.add(new Paragraph(
						new Phrase("BILANCIO NEGATIVO: "
								+ NumberUtils.getString(totale) + " €",
								fontBilancioRed)));
			}

			// }
			if (bitmap != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG /* FileType */,
						100 /* Ratio */, stream);
				Image image = Image.getInstance(stream.toByteArray());
				document.add(image);
			}

			document.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

}
