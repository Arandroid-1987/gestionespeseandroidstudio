package utils.file.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;

import jxl.CellView;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import jxl.write.Number;
import utils.DateUtils;
import android.graphics.Bitmap;

import com.dto.Ricavo;
import com.dto.Spesa;
import com.dto.TagRicavo;
import com.dto.TagSpesa;

public class XLSExport {

	private Collection<Spesa> spese;
	private Collection<Ricavo> ricavi;
	private File file;
	private String startDate;
	private String endDate;

	private final static int DATA_COLUMN = 0;
	private final static int TAG_COLUMN = DATA_COLUMN + 1;
	private final static int AMOUNT_COLUMN = TAG_COLUMN + 1;

	private double totaleSpese = 0;
	private double totaleRicavi = 0;
	private WritableCellFormat format;
	private WritableCellFormat currencyFormat;
	private CellView cellView;

	public final static String EXTENSION = ".xls";

	private final static String[] header = new String[] { "Data", "Tag",
			"Importo" };

	private Bitmap bitmap;

	public XLSExport(Collection<Spesa> spese, Collection<Ricavo> ricavi,
			File file, String startDate, String endDate) {
		super();
		this.spese = spese;
		this.ricavi = ricavi;
		this.file = file;
		WritableFont font = new WritableFont(WritableFont.ARIAL, 12,
				WritableFont.BOLD);
		format = new WritableCellFormat(font);
		cellView = new CellView();
		cellView.setAutosize(true);
		NumberFormat currency = new NumberFormat(
				NumberFormat.CURRENCY_EURO_PREFIX + " ###,##0.00",
				NumberFormat.COMPLEX_FORMAT);
		currencyFormat = new WritableCellFormat(currency);
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
			WritableWorkbook workbook = Workbook.createWorkbook(file);
			WritableSheet speseSheet = workbook.createSheet("Spese", 0);
			writeSpese(speseSheet);
			WritableSheet ricaviSheet = workbook.createSheet("Ricavi", 1);
			writeRicavi(ricaviSheet);
			WritableSheet bilancioSheet = workbook.createSheet("Bilancio", 2);
			writeBilancio(bilancioSheet);
			workbook.write();
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void writeBilancio(WritableSheet sheet)
			throws RowsExceededException, WriteException {
		Label label = new Label(1, 0, "Importo");
		label.setCellFormat(format);
		sheet.addCell(label);
		int col = 0;
		int row = 1;
		label = new Label(col, row++, "Totale Spese");
		label.setCellFormat(format);
		sheet.addCell(label);
		label = new Label(col, row++, "Totale Ricavi");
		label.setCellFormat(format);
		sheet.addCell(label);
		label = new Label(col, row, "Bilancio");
		label.setCellFormat(format);
		sheet.addCell(label);
		col++;
		row = 1;
		Number number = new Number(col, row++, totaleSpese);
		number.setCellFormat(currencyFormat);
		sheet.addCell(number);
		number = new Number(col, row++, totaleRicavi);
		number.setCellFormat(currencyFormat);
		sheet.addCell(number);
		number = new Number(col, row, totaleRicavi - totaleSpese);
		number.setCellFormat(currencyFormat);
		sheet.addCell(number);
		if (bitmap != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] bytes = stream.toByteArray();
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			WritableImage image = new WritableImage(0, row + 2, width / 100,
					height / 25, bytes);
			sheet.addImage(image);
		}

		sheet.setColumnView(0, cellView);
		sheet.setColumnView(1, cellView);
	}

	private void writeRicavi(WritableSheet sheet) throws RowsExceededException,
			WriteException {
		int row = 0;

		for (int i = DATA_COLUMN; i <= AMOUNT_COLUMN; i++) {
			Label label = new Label(i, row, header[i]);
			label.setCellFormat(format);
			sheet.addCell(label);
			sheet.setColumnView(i, cellView);
		}
		row++;
		for (Ricavo ricavo : ricavi) {
			String data = ricavo.getData();
			data = DateUtils.getPrintableDataFormat(data);
			Label dateLabel = new Label(DATA_COLUMN, row, data);
			sheet.addCell(dateLabel);
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
				Label tagLabel = new Label(TAG_COLUMN, row, builder.toString());
				sheet.addCell(tagLabel);
			} else {
				Label tagLabel = new Label(TAG_COLUMN, row, descrizione);
				sheet.addCell(tagLabel);
			}
			double importo = ricavo.getImporto();
			totaleRicavi += importo;
			Number amountLabel = new Number(AMOUNT_COLUMN, row, importo);
			amountLabel.setCellFormat(currencyFormat);
			sheet.addCell(amountLabel);
			row++;
		}
		row++;
		Label label = new Label(TAG_COLUMN, row, "TOTALE");
		label.setCellFormat(format);
		sheet.addCell(label);
		Number number = new Number(AMOUNT_COLUMN, row, totaleRicavi);
		number.setCellFormat(currencyFormat);
		sheet.addCell(number);
	}

	private void writeSpese(WritableSheet sheet) throws RowsExceededException,
			WriteException {
		int row = 0;
		for (int i = DATA_COLUMN; i <= AMOUNT_COLUMN; i++) {
			Label label = new Label(i, row, header[i]);
			label.setCellFormat(format);
			sheet.addCell(label);
			sheet.setColumnView(i, cellView);
		}
		row++;
		for (Spesa spesa : spese) {
			String data = spesa.getData();
			data = DateUtils.getPrintableDataFormat(data);
			Label dateLabel = new Label(DATA_COLUMN, row, data);
			sheet.addCell(dateLabel);
			String descrizione = spesa.getDescrizione();
			if (descrizione == null || descrizione.length() == 0) {
				StringBuilder builder = new StringBuilder();
				Collection<TagSpesa> tags = spesa.getTags();
				int count = 0;
				for (TagSpesa tag : tags) {
					builder.append(tag.getValore());
					if (count < tags.size() - 1)
						builder.append(" - ");
					count++;
				}
				Label tagLabel = new Label(TAG_COLUMN, row, builder.toString());
				sheet.addCell(tagLabel);
			}
			else{
				Label tagLabel = new Label(TAG_COLUMN, row, descrizione);
				sheet.addCell(tagLabel);
			}
			double importo = spesa.getImporto();
			totaleSpese += importo;
			Number amountLabel = new Number(AMOUNT_COLUMN, row, importo);
			amountLabel.setCellFormat(currencyFormat);
			sheet.addCell(amountLabel);
			row++;
		}
		row++;
		Label label = new Label(TAG_COLUMN, row, "TOTALE");
		label.setCellFormat(format);
		sheet.addCell(label);
		Number number = new Number(AMOUNT_COLUMN, row, totaleSpese);
		number.setCellFormat(currencyFormat);
		sheet.addCell(number);
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

}
