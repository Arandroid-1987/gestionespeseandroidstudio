package com.arandroid.bilanciopersonale.fragments;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.KeyButton;
import utils.file.export.CSVExport;
import utils.file.export.PDFExport;
import utils.file.export.TXTExport;
import utils.file.export.XLSExport;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arandroid.bilanciopersonale.FileChooser;
import com.arandroid.bilanciopersonale.R;
import com.dao.RicavoDao;
import com.dao.SettingsDao;
import com.dao.SpesaDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.Spesa;

public class ImpostazioniFragment extends Fragment implements OnClickListener,
		OnCheckedChangeListener {
	private Button salvaButton;
	private Button resetButton;
	private CheckBox usePassword;
	private Button inserisciPassword;
	private LinearLayout passwordLL;
	private EditText email;
	private Checkable tutorialButton;
	private Button esportaButton;
	private Checkable exportPDF;
	private Checkable exportCSV;
	private Checkable exportText;
	private Checkable exportExcel;
	private Spinner separatorCSV;
	private LinearLayout separatorCSVLayout;
	private boolean usePass = false;
	private boolean tutorialOn = false;
	private Checkable useBackup;
	private Checkable useSameFile;
	private LinearLayout backupLL;
	private EditText numberET;
	private Spinner spinnerPeriodo;
	private EditText currencyET;
	private EditText symbolET;

	private final static int FILE_CHOOSER_ACTIVITY = 0;

	private int count = 0;
	private char[] typedPassword;
	private boolean showPassword;
	private Dialog passwordDialog;
	private String currentPassword;

	private View rootView;
	private Activity context;

	private final static int PASSWORD_LENGTH = 4;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rootView = inflater.inflate(R.layout.impostazioni_activity_layout,
				container, false);
		context = getActivity();

		salvaButton = (Button) rootView.findViewById(R.id.salvaButton);
		resetButton = (Button) rootView.findViewById(R.id.resetButton);
		usePassword = (CheckBox) rootView.findViewById(R.id.usePassword);
		inserisciPassword = (Button) rootView
				.findViewById(R.id.inserisciPassword);
		passwordLL = (LinearLayout) rootView.findViewById(R.id.passwordLL);
		email = (EditText) rootView.findViewById(R.id.email);
		esportaButton = (Button) rootView.findViewById(R.id.esportaButton);
		exportPDF = (Checkable) rootView.findViewById(R.id.exportPDF);
		exportCSV = (Checkable) rootView.findViewById(R.id.exportCSV);
		exportExcel = (Checkable) rootView.findViewById(R.id.exportExcel);
		exportText = (Checkable) rootView.findViewById(R.id.exportText);
		separatorCSV = (Spinner) rootView.findViewById(R.id.separatorCSV);
		separatorCSVLayout = (LinearLayout) rootView
				.findViewById(R.id.separatorCSVLayout);
		useBackup = (Checkable) rootView.findViewById(R.id.useBackup);
		useSameFile = (Checkable) rootView.findViewById(R.id.useSameFile);
		backupLL = (LinearLayout) rootView.findViewById(R.id.backupLL);
		numberET = (EditText) rootView.findViewById(R.id.numberET);
		spinnerPeriodo = (Spinner) rootView.findViewById(R.id.spinnerPeriodo);
		currencyET = (EditText) rootView.findViewById(R.id.currencyET);
		symbolET = (EditText) rootView.findViewById(R.id.symbolET);

		esportaButton.setOnClickListener(this);

		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();

		SwitchCompat s = (SwitchCompat) exportCSV;
		s.setOnCheckedChangeListener(this);

		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item);

		String[] names = new String[] { CSVExport.DEFAULT, CSVExport.EXCEL };

		for (String name : names) {
			spinnerAdapter.add(name);
		}

		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		separatorCSV.setAdapter(spinnerAdapter);

		tutorialButton = (Checkable) rootView.findViewById(R.id.tutorialButton);

		tutorialOn = SettingsDao.isTutorialOn(db);

		salvaButton.setOnClickListener(this);
		resetButton.setOnClickListener(this);
		inserisciPassword.setOnClickListener(this);

		usePassword.setOnCheckedChangeListener(this);

		usePass = SettingsDao.isUsingPassword(db);

		int[] backupData = SettingsDao.getBackupData(db);
		if (backupData != null) {
			boolean useBack = backupData[0] > 0;
			int repeatDays = backupData[1];
			boolean useSame = backupData[2] > 0;

			useBackup.setChecked(useBack);
			useSameFile.setChecked(useSame);
			numberET.setText("" + repeatDays);
		}

		Cursor c = SettingsDao.getData(db);
		if (c != null) {
			String currentEmail = c.getString(c
					.getColumnIndex(SettingsDao.EMAIL));
			email.setText(currentEmail);
			currentPassword = c.getString(c
					.getColumnIndex(SettingsDao.PASSWORD));
			c.close();
		}

		String[] currency = SettingsDao.getCurrency(db);
		currencyET.setText(currency[0]);
		symbolET.setText(currency[1]);

		db.close();

		((SwitchCompat) useBackup).setOnCheckedChangeListener(this);

		if (usePass) {
			passwordLL.setVisibility(View.VISIBLE);
			typedPassword = currentPassword.toCharArray();
			count = PASSWORD_LENGTH;
		} else {
			passwordLL.setVisibility(View.GONE);
		}

		if (useBackup.isChecked()) {
			backupLL.setVisibility(View.VISIBLE);
		} else {
			backupLL.setVisibility(View.GONE);
		}

		ArrayAdapter<String> spinnerPeriodoAdapter = new ArrayAdapter<String>(
				context, android.R.layout.simple_spinner_item);

		String[] values = new String[] { getString(R.string.giorni),
				getString(R.string.settimane), getString(R.string.mesi) };

		for (String value : values) {
			spinnerPeriodoAdapter.add(value);
		}

		spinnerPeriodoAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerPeriodo.setAdapter(spinnerPeriodoAdapter);

		tutorialButton.setChecked(tutorialOn);

		usePassword.setChecked(usePass);
		
		return rootView;
	}

	@Override
	public void onClick(View v) {
		if (v.equals(salvaButton)) {
			salva();
		} else if (v.equals(resetButton)) {
			resetta();
		} else if (v.equals(esportaButton)) {
			esporta();
		} else if (v.equals(inserisciPassword)) {
			createAndShowPasswordDialog();
		}
	}

	private void createAndShowPasswordDialog() {
		final DatabaseHandler dbHandler = DatabaseHandler.getInstance(context);
		final SQLiteDatabase db = dbHandler.getReadableDatabase();
		AlertDialog.Builder builder = new Builder(context);
		builder.setCancelable(false);
		LayoutInflater inflater = getLayoutInflater(getArguments());
		View v = inflater.inflate(R.layout.insert_password_dialog, null);

		typedPassword = new char[PASSWORD_LENGTH];
		count = 0;
		final Button accedi = (Button) v.findViewById(R.id.submitPassword);
		final Button annulla = (Button) v.findViewById(R.id.annulla);
		final CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox1);
		final LinearLayout keyView = (LinearLayout) v
				.findViewById(R.id.keyView);
		final LinearLayout passwordLayout = (LinearLayout) v
				.findViewById(R.id.passwordLayout);
		setupPasswordLayout(passwordLayout);
		int childLayouts = keyView.getChildCount();
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (count < 0)
					return;
				if (v.getParent() instanceof KeyButton) {
					KeyButton key = (KeyButton) v.getParent();
					int value = key.getValue();
					if (value < 10) {
						if (count == passwordLayout.getChildCount())
							return;
						char c = (value + "").charAt(0);
						typedPassword[count] = c;
						KeyButton layout = (KeyButton) passwordLayout
								.getChildAt(count);
						if (showPassword) {
							layout.setUseText(true);
							layout.setText(value + "");
							layout.setTextColorResourceInt(getResources()
									.getColor(R.color.black));
							layout.createLayout();
						} else {
							layout.setDrawable(getResources().getDrawable(
									R.drawable.pass_typed));
							layout.setUseText(false);
							layout.createLayout();
						}
						count++;
					} else if (value == 10) {
						if (count == 0)
							return;
						count--;
						typedPassword[count] = 0;
						KeyButton layout = (KeyButton) passwordLayout
								.getChildAt(count);
						layout.setDrawable(getResources().getDrawable(
								R.drawable.pass_empty));
						layout.setUseText(false);
						layout.createLayout();
					} else if (value == 11) {
						accedi.performClick();
					}
				}
			}
		};
		for (int i = 0; i < childLayouts; i++) {
			LinearLayout ll = (LinearLayout) keyView.getChildAt(i);
			int childKeys = ll.getChildCount();
			for (int j = 0; j < childKeys; j++) {
				KeyButton key = (KeyButton) ll.getChildAt(j);
				key.setOnClickListener(listener);
				View b = key.getChildAt(0);
				b.setOnClickListener(listener);
			}
		}

		checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				showPassword = isChecked;
				setupPasswordLayout(passwordLayout);
			}
		});

		accedi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (count < passwordLayout.getChildCount())
					return;
				passwordDialog.dismiss();
			}
		});

		annulla.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				passwordDialog.dismiss();
			}
		});

		builder.setView(v);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title = inflater.inflate(R.layout.dialog_title, null);
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText("Login");
			builder.setCustomTitle(title);
		} else {
			builder.setTitle("Login");
		}

		passwordDialog = builder.create();
		passwordDialog.show();
		db.close();
	}

	protected void setupPasswordLayout(LinearLayout passwordLayout) {
		passwordLayout.removeAllViews();
		int passwordLength = PASSWORD_LENGTH;
		for (int i = 0; i < passwordLength; i++) {
			KeyButton layout = new KeyButton(context);
			char c = typedPassword[i];
			if (showPassword) {
				if (c != '\u0000') {
					layout.setUseText(true);
					layout.setTextColorResourceInt(getResources().getColor(
							R.color.black));
					String value = "" + c;
					layout.setText(value);
				} else {
					layout.setDrawable(getResources().getDrawable(
							R.drawable.pass_empty));
					layout.setUseText(false);
				}
			} else {
				if (c != '\u0000') {
					layout.setDrawable(getResources().getDrawable(
							R.drawable.pass_typed));
				} else {
					layout.setDrawable(getResources().getDrawable(
							R.drawable.pass_empty));
				}
				layout.setUseText(false);
			}
			layout.setBackgroundResourceInt(R.drawable.white_round_corner);
			layout.setWidth(50);
			layout.setHeight(50);
			layout.createLayout();
			passwordLayout.addView(layout);
		}
	}

	private void esporta() {
		boolean esportaPDF = exportPDF.isChecked();
		boolean esportaCSV = exportCSV.isChecked();
		boolean esportaXSL = exportExcel.isChecked();
		boolean esportaTXT = exportText.isChecked();
		if (esportaCSV || esportaPDF || esportaTXT || esportaXSL) {
			Intent intent = new Intent(context, FileChooser.class);
			intent.putExtra(FileChooser.SELECT_FILES, false);
			startActivityForResult(intent, FILE_CHOOSER_ACTIVITY);
		}
	}

	private void resetta() {
		useBackup.setChecked(false);
		usePassword.setChecked(false);
		if (typedPassword != null) {
			for (int i = 0; i < typedPassword.length; i++) {
				typedPassword[i] = '\u0000';
			}
		}
		email.setText("");
		currencyET.setText("");
		symbolET.setText("");
		tutorialButton.setChecked(true);
	}

	public void salva() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		boolean ok = true;
		saveTutorial();
		ok = savePassword();
		ok = ok & saveBackup();
		ok = ok & saveCurrency();
		if (ok) {
			Toast.makeText(context, context.getString(R.string.salvataggio_avvenuto_con_successo), Toast.LENGTH_SHORT).show();
			FragmentActivity activity = (FragmentActivity) context;
			FragmentManager fragmentManager = activity
					.getSupportFragmentManager();
			Fragment newFragment = new CalendarFragment();
			fragmentManager.beginTransaction()
					.replace(R.id.container, newFragment).commit();
			db.close();
		}
	}

	private boolean saveCurrency() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		boolean ok = true;
		String currency = currencyET.getText().toString();
		String symbol = symbolET.getText().toString();
		if (currency != null && currency.length() > 0 && symbol != null
				&& symbol.length() == 1) {
			SettingsDao.setCurrency(db, currency, symbol);
		} else {
			String msg = getString(R.string.inserisci_valore_valido);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			ok = false;
		}
		db.close();
		handler.close();
		return ok;
	}

	private boolean saveBackup() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		boolean ok = true;
		if (useBackup.isChecked()) {
			boolean useSame = useSameFile.isChecked();
			int repeatDays;
			String valueET = numberET.getText().toString();
			if (valueET != null && valueET.length() > 0) {
				repeatDays = Integer.parseInt(valueET);
				if (repeatDays > 0) {
					String repeatType = (String) spinnerPeriodo
							.getSelectedItem();
					if (repeatType.equals(getString(R.string.settimane))) {
						repeatDays *= 7;
					} else if (repeatType.equals(getString(R.string.mesi))) {
						repeatDays *= 30;
					}
					int[] backupData = SettingsDao.getBackupData(db);
					if (backupData == null || backupData[1] != repeatDays
							|| (backupData[2] > 0) != useSame) {
						Date d = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd",
								getResources().getConfiguration().locale);
						String ds = sdf.format(d);
						int di = Integer.parseInt(ds);
						SettingsDao.useBackup(db, repeatDays, useSame, di);
						handler.backup(db, useSame, true);
					} else {
						SettingsDao.useBackup(db, repeatDays, useSame);
					}
				} else {
					String msg = getString(R.string.inserisci_valore_valido);
					Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
					ok = false;
				}
			} else {
				String msg = getString(R.string.inserisci_valore);
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
				ok = false;
			}

		} else {
			SettingsDao.doNotUseBackup(db);
		}
		db.close();
		handler.close();
		return ok;
	}

	private boolean savePassword() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		boolean ok = true;
		if (usePass) {
			String msg = "";
			if (count < PASSWORD_LENGTH) {
				ok = false;
				msg += getString(R.string.password_troppo_corta);
			}
			if (!correctEmail(email.getText().toString())) {
				ok = false;
				if (msg.length() > 0)
					msg += " - ";
				msg += getString(R.string.email_non_corretta);
			}
			if (ok) {
				SettingsDao.setPassword(db, new String(typedPassword), email
						.getText().toString());
			} else {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		} else {
			SettingsDao.doNotUsePassword(db);
		}
		db.close();
		handler.close();
		return ok;
	}

	private void saveTutorial() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		tutorialOn = tutorialButton.isChecked();
		SettingsDao.tutorialOn(db, tutorialOn);
		db.close();
		handler.close();
	}

	private boolean correctEmail(String hex) {
		Pattern pattern;
		Matcher matcher;
		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(hex);
		return matcher.matches();
	}

	@Override
	public void onCheckedChanged(CompoundButton view, boolean checked) {
		if (view.equals(usePassword)) {
			if (checked) {
				passwordLL.setVisibility(View.VISIBLE);
			} else {
				passwordLL.setVisibility(View.GONE);
			}
			usePass = checked;
		} else if (view.equals(exportCSV)) {
			if (checked) {
				separatorCSVLayout.setVisibility(View.VISIBLE);
			} else {
				separatorCSVLayout.setVisibility(View.GONE);
			}
		} else if (view.equals(useBackup)) {
			if (checked) {
				backupLL.setVisibility(View.VISIBLE);
			} else {
				backupLL.setVisibility(View.GONE);
			}
		}
		else if(view.equals(tutorialButton)){
			saveTutorial();
		}
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILE_CHOOSER_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				String path = data.getStringExtra("file_path");
				esportaReport(path);
			} else {
				Toast.makeText(context,
						getString(R.string.attenzione_nessun_file_selezionato),
						Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void esportaReport(String path) {
		boolean esportaPDF = exportPDF.isChecked();
		boolean esportaCSV = exportCSV.isChecked();
		boolean esportaXSL = exportExcel.isChecked();
		boolean esportaTXT = exportText.isChecked();
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getReadableDatabase();
		// filechooser
		String filename = "report";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		Collection<Spesa> spese = SpesaDao.getAllSpese(db);
		Collection<Ricavo> ricavi = RicavoDao.getAllRicavi(db);
		db.close();
		boolean ok = true;
		int count = 0;
		String startDate = null, endDate = null;
		if (esportaPDF) {
			count++;
			File file = new File(dir, filename + ".pdf");
			PDFExport export = new PDFExport(spese, ricavi, file, true,
					startDate, endDate);
			ok = ok && export.export();
		}
		if (esportaCSV) {
			count++;
			String filenameRicavi = filename + "_ricavi" + CSVExport.EXTENSION;
			String filenameSpese = filename + "_spese" + CSVExport.EXTENSION;
			File fileRicavi = new File(dir, filenameRicavi);
			File fileSpese = new File(dir, filenameSpese);
			CSVExport export = new CSVExport(spese, ricavi, fileRicavi,
					fileSpese, startDate, endDate);
			String separatorStr = (String) separatorCSV.getSelectedItem();
			if (separatorStr.equals(CSVExport.EXCEL)) {
				export.setSeparator(CSVExport.EXCEL_SEPARATOR);
			} else if (separatorStr.equals(CSVExport.DEFAULT)) {
				export.setSeparator(CSVExport.DEFAULT_SEPARATOR);
			}
			ok = ok && export.export();
		}
		if (esportaXSL) {
			count++;
			File file = new File(dir, filename + XLSExport.EXTENSION);
			XLSExport export = new XLSExport(spese, ricavi, file, startDate,
					endDate);
			ok = ok && export.export();
		}
		if (esportaTXT) {
			count++;
			File file = new File(dir, filename + TXTExport.EXTENSION);
			TXTExport export = new TXTExport(spese, ricavi, file, startDate,
					endDate);
			ok = ok && export.export();
		}
		if (count > 0) {
			if (ok) {
				Toast.makeText(context,
						getString(R.string.esportazione_successo),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context,
						getString(R.string.errore_esportazione_dati),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
