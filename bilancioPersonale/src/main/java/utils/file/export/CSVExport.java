package utils.file.export;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;

import utils.DateUtils;
import utils.NumberUtils;

import com.dto.Ricavo;
import com.dto.Spesa;
import com.dto.TagRicavo;
import com.dto.TagSpesa;

public class CSVExport {

	private Collection<Spesa> spese;
	private Collection<Ricavo> ricavi;
	private File fileRicavi;
	private File fileSpese;
	private String startDate;
	private String endDate;
	private String separator = DEFAULT_SEPARATOR;
	public final static String DEFAULT_SEPARATOR = ",";
	public final static String EXCEL_SEPARATOR = ";";
	public final static String DEFAULT = "DEFAULT";
	public final static String EXCEL = "EXCEL";
	public final static String EXTENSION = ".csv";

	public CSVExport(Collection<Spesa> spese, Collection<Ricavo> ricavi,
			File fileRicavi, File fileSpese, String startDate, String endDate) {
		super();
		this.spese = spese;
		this.ricavi = ricavi;
		this.fileRicavi = fileRicavi;
		this.fileSpese = fileSpese;
		this.startDate = startDate;
		this.endDate = endDate;
		String oldName = fileRicavi.getAbsolutePath();
		if (startDate != null && endDate != null) {
			this.fileRicavi = new File(oldName.substring(0,
					oldName.length() - 4)
					+ "_"
					+ this.startDate
					+ "_"
					+ this.endDate + EXTENSION);
			oldName = fileSpese.getAbsolutePath();
			this.fileSpese = new File(
					oldName.substring(0, oldName.length() - 4) + "_"
							+ this.startDate + "_" + this.endDate + EXTENSION);
		}
	}

	public boolean export() {
		boolean ris = exportRicavi();
		ris = ris && exportSpese();
		return ris;

	}

	private boolean exportSpese() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileSpese);
			pw.println("Data" + separator + "Tag" + separator + "Importo");
			for (Spesa spesa : spese) {
				String dataOriginale = spesa.getData();
				String dataNuova = DateUtils
						.getPrintableDataFormat(dataOriginale);
				StringBuilder builder = new StringBuilder();

				String descrizione = spesa.getDescrizione();
				String tagString = null;
				if (descrizione == null || descrizione.length() == 0) {
					Collection<TagSpesa> tags = spesa.getTags();
					int count = 0;
					for (TagSpesa tag : tags) {
						builder.append(tag.getValore());
						if (count < tags.size() - 1)
							builder.append(" - ");
						count++;
					}
					tagString = builder.toString();
				} else {
					tagString = descrizione;
				}
				String importo = NumberUtils.getString(spesa.getImporto())
						.replace(',', '.');
				pw.println(dataNuova + separator + tagString + separator
						+ importo);
			}
		} catch (Exception e) {
			return false;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		return true;
	}

	private boolean exportRicavi() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileRicavi);
			pw.println("Data" + separator + "Tag" + separator + "Importo");
			for (Ricavo ricavo : ricavi) {
				String dataOriginale = ricavo.getData();
				String dataNuova = DateUtils
						.getPrintableDataFormat(dataOriginale);
				StringBuilder builder = new StringBuilder();

				String descrizione = ricavo.getDescrizione();
				String tagString = null;
				if (descrizione == null || descrizione.length() == 0) {
					Collection<TagRicavo> tags = ricavo.getTags();
					int count = 0;
					for (TagRicavo tag : tags) {
						builder.append(tag.getValore());
						if (count < tags.size() - 1)
							builder.append(" - ");
						count++;
					}
					tagString = builder.toString();
				}
				else{
					tagString = descrizione;
				}
				String importo = NumberUtils.getString(ricavo.getImporto())
						.replace(',', '.');
				pw.println(dataNuova + separator + tagString + separator
						+ importo);
			}
		} catch (Exception e) {
			return false;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		return true;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}
}
