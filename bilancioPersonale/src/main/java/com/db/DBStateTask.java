package com.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import com.arandroid.bilanciopersonale.R;
import com.dao.RicavoDao;
import com.dao.RicavoProgrammatoDao;
import com.dao.SpesaDao;
import com.dao.SpesaProgrammataDao;
import com.dao.mvc.Notifier;
import com.dto.Ricavo;
import com.dto.RicavoProgrammato;
import com.dto.Spesa;
import com.dto.SpesaProgrammata;
import com.dto.VoceBilancio;
import com.ui.gestionespese.ShareBackupDialogCreator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DBStateTask extends AsyncTask<Void, Void, Void> {
	private DatabaseHandler handler;
	private SQLiteDatabase db;
	private String file_path;
	private Context context;
	private boolean restore;
	private String _dirName;
	private String _backupFileName;
	private boolean useSameFile;
	private ProgressDialog dialog;
	private boolean automatic;
	private File backupDB;

	private final static Notifier notifier = new Notifier();

	static {
		notifier.setTag(Notifier.SPESE_TAG);
	}

	public DBStateTask(boolean restore, SQLiteDatabase db, String file_path,
			DatabaseHandler handler, Context context, String _dirName,
			String _backupFileName, boolean useSameFile, boolean automatic) {
		super();
		this.restore = restore;
		this.handler = handler;
		this.db = db;
		this.file_path = file_path;
		this.context = context;
		this._dirName = _dirName;
		this._backupFileName = _backupFileName;
		this.useSameFile = useSameFile;
		this.automatic = automatic;
		setupProgressDialog();
	}

	private void setupProgressDialog() {
		dialog = new ProgressDialog(context);
		dialog.setCancelable(false);
		dialog.setMessage(context.getString(R.string.elaborazione_in_corso));
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (context instanceof Activity) {
			dialog.show();
		}
		if (restore) {
			restore();
		} else {
			backup();
		}
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		if (restore) {
			Toast.makeText(context,
					context.getString(R.string.restore_effettuato),
					Toast.LENGTH_LONG).show();
			notifier.setChanged();
			notifier.notifyObservers(true);
			notifier.setChanged();
			notifier.notifyObservers(false);
		} else {
//			Toast.makeText(context,
//					context.getString(R.string.backup_effettuato_in),
//					Toast.LENGTH_SHORT).show();
		}
		if (context instanceof Activity) {
			dialog.dismiss();
		}
	}

	private void backup() {
//		Toast.makeText(context, "Sto per fare il backup", Toast.LENGTH_LONG).show();
//		System.out.println("Sto per fare il backup");
		try {
			File sd = Environment.getExternalStorageDirectory();
//			Toast.makeText(context, "SD OTTENUTA", Toast.LENGTH_LONG).show();
			if (sd.canWrite()) {
				File directory = new File(sd, _dirName);
				if (!directory.exists())
					directory.mkdirs();
				if (useSameFile) {
					backupDB = new File(sd, _backupFileName);
				} else {
					SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy",
							Locale.ITALIAN);
					String today = sdf.format(new Date());
					String filename = "backup_"+today+".gdb";
					backupDB = new File(directory, filename);
//					System.out.println(filename);
				}
				Collection<VoceBilancio> voci = new LinkedList<VoceBilancio>();
//				db = handler.getReadableDatabase();
				voci.addAll(SpesaDao.getAllSpese(handler));
				voci.addAll(RicavoDao.getAllRicavi(handler));
//				Toast.makeText(context, "VOCI OTTENUTE", Toast.LENGTH_LONG).show();
				voci.addAll(SpesaProgrammataDao.getAllSpese(handler));
				voci.addAll(RicavoProgrammatoDao.getAllRicavi(handler));
//				if (db.isOpen()) {
//					db.close();
//				}
				FileOutputStream fos = new FileOutputStream(backupDB);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(voci);
				if(!automatic){
					ShareBackupDialogCreator.createAndShowDialog(context, backupDB);
				}
//				System.out.println("Backup effettuato");
				oos.close();
				fos.close();
			} else {
				Log.e("Permission denied",
						"Can't write to SD card, add permission");
			}
		} catch (Exception e) {
//			System.out.println("Sono andato in eccezione");
			e.printStackTrace();
		}
	}

	public void restore() {
		try {
			File sd = Environment.getExternalStorageDirectory();
			if (sd.canRead()) {
				File backupDB = new File(file_path);
				FileInputStream fis = new FileInputStream(backupDB);
				ObjectInputStream ois = new ObjectInputStream(fis);
				@SuppressWarnings("unchecked")
				Collection<VoceBilancio> voci = (Collection<VoceBilancio>) ois
						.readObject();
				ois.close();
				fis.close();
				db = handler.getWritableDatabase();
				handler.reset(db);
				Collection<Spesa> spese = new LinkedList<Spesa>();
				Collection<SpesaProgrammata> speseProgrammate = new LinkedList<SpesaProgrammata>();
				Collection<Ricavo> ricavi = new LinkedList<Ricavo>();
				Collection<RicavoProgrammato> ricaviProgrammati = new LinkedList<RicavoProgrammato>();
				for (VoceBilancio voceBilancio : voci) {
					if (voceBilancio instanceof SpesaProgrammata) {
						SpesaProgrammata s = (SpesaProgrammata) voceBilancio;
						speseProgrammate.add(s);
					} else if (voceBilancio instanceof RicavoProgrammato) {
						RicavoProgrammato r = (RicavoProgrammato) voceBilancio;
						ricaviProgrammati.add(r);
					} else if (voceBilancio instanceof Spesa) {
						Spesa s = (Spesa) voceBilancio;
						spese.add(s);
					} else if (voceBilancio instanceof Ricavo) {
						Ricavo r = (Ricavo) voceBilancio;
						ricavi.add(r);
					}
				}
				int x = 0;
				x = x + 1;
				SpesaProgrammataDao.insertAll(db, speseProgrammate);
				SpesaDao.insertAll(db, spese);
				RicavoProgrammatoDao.insertAll(db, ricaviProgrammati);
				RicavoDao.insertAll(db, ricavi);
				if (db.isOpen()) {
					db.close();
				}
			} else {
				Log.e("Permission denied",
						"Can't read from SD card, add permission");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Notifier getNotifier() {
		return notifier;
	}

}
