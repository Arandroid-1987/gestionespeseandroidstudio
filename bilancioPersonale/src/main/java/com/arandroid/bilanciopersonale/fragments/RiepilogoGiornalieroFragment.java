package com.arandroid.bilanciopersonale.fragments;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.arandroid.bilanciopersonale.AddRicavoActivity;
import com.arandroid.bilanciopersonale.AddSpesaActivity;
import com.arandroid.bilanciopersonale.R;
import com.arandroid.bilanciopersonale.RiepilogoGiornalieroActivityNew;
import com.dao.SettingsDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.RicavoProgrammato;
import com.dto.Spesa;
import com.dto.SpesaProgrammata;
import com.dto.VoceBilancio;
import com.ui.gestionespese.VoceBilancioAdapterDay;

public class RiepilogoGiornalieroFragment extends Fragment implements
		OnClickListener, OnTouchListener {
	private ListView listView;
	private List<VoceBilancio> vociBilancio;
	private Activity context;
	private View rootView;

	private FloatingActionButton newButton;
	private View previousPage;
	private View nextPage;

	private double sommaSpese = 0;
	private double sommaRicavi = 0;
	private double bilancio = 0;

	private TextView sommaSpeseTV;
	private TextView sommaRicaviTV;
	private TextView bilancioTexView;

	private View nuovaSpesa;
	private View nuovoRicavo;
	private AlertDialog dialog;
	private String data;

	public RiepilogoGiornalieroFragment() {
	}

	public static RiepilogoGiornalieroFragment newInstance(String data,
			ArrayList<VoceBilancio> vociBilancio) {
		RiepilogoGiornalieroFragment f = new RiepilogoGiornalieroFragment();

		Bundle b = new Bundle();
		b.putString("data", data);
		b.putSerializable("lista", vociBilancio);

		f.setArguments(b);

		return f;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_riepilogo_giornaliero,
				container, false);
		listView = (ListView) rootView.findViewById(R.id.listView1);
		context = getActivity();
		data = getArguments().getString("data");

		vociBilancio = (List<VoceBilancio>) getArguments().getSerializable(
				"lista");

		sommaSpeseTV = (TextView) rootView.findViewById(R.id.sommaSpese);
		sommaRicaviTV = (TextView) rootView.findViewById(R.id.sommaRicavi);
		bilancioTexView = (TextView) rootView.findViewById(R.id.bilancio);

		calcolaBilanciGiornalieri();

		DecimalFormat df = new DecimalFormat("0.00");

		String sommaSpeseFormattata = df.format(sommaSpese);
		String sommaRicaviFormattata = df.format(sommaRicavi);
		String bilancioFormattato = df.format(bilancio);

		String testoBaseSpese = getString(R.string.spese_column);
		String testoBaseRicavi = getString(R.string.ricavi_column);
		String testoBaseBilancio = getString(R.string.bilancio_column);

		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();

		String valuta = SettingsDao.getCurrency(db)[1];

		db.close();
		handler.close();

		sommaSpeseTV.setText(testoBaseSpese + " " + sommaSpeseFormattata + ""
				+ valuta);
		sommaRicaviTV.setText(testoBaseRicavi + " " + sommaRicaviFormattata
				+ "" + valuta);
		bilancioTexView.setText(testoBaseBilancio + " " + bilancioFormattato
				+ "" + valuta);

		VoceBilancioAdapterDay adapter = new VoceBilancioAdapterDay(context,
				android.R.layout.simple_list_item_1, vociBilancio);
		listView.setAdapter(adapter);

		newButton = (FloatingActionButton) rootView.findViewById(R.id.fab);
		newButton.setOnClickListener(this);

		previousPage = rootView.findViewById(R.id.previousPage);
		previousPage.setOnClickListener(this);
		previousPage.setOnTouchListener(this);
		nextPage = rootView.findViewById(R.id.nextPage);
		nextPage.setOnClickListener(this);
		nextPage.setOnTouchListener(this);

		return rootView;
	}

	private void calcolaBilanciGiornalieri() {
		sommaRicavi = 0;
		sommaSpese = 0;
		for (VoceBilancio voceBilancio : vociBilancio) {
			if (voceBilancio instanceof Spesa
					&& !(voceBilancio instanceof SpesaProgrammata)) {
				sommaSpese += voceBilancio.getImporto();
			} else if (voceBilancio instanceof Ricavo
					&& !(voceBilancio instanceof RicavoProgrammato)) {
				sommaRicavi += voceBilancio.getImporto();
			}
		}

		bilancio = sommaRicavi - sommaSpese;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	private void createAndShowNewDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = getLayoutInflater(getArguments());
		View v = inflater.inflate(R.layout.new_item_dialog, null);

		nuovaSpesa = v.findViewById(R.id.nuova_spesa);
		nuovoRicavo = v.findViewById(R.id.nuovo_ricavo);

		nuovaSpesa.setOnClickListener(this);
		nuovoRicavo.setOnClickListener(this);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title = inflater.inflate(R.layout.dialog_title, null);
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(getString(R.string.aggiungi));
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(getString(R.string.aggiungi));
		}

		builder.setView(v);
		dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.equals(newButton)) {
			createAndShowNewDialog();
		} else if (arg0.equals(nuovaSpesa)) {
			context.setResult(Activity.RESULT_OK);
			context.finish();
			Intent intent;
			intent = new Intent(context, AddSpesaActivity.class);
			intent.putExtra("data", data);
			context.startActivity(intent);
			dialog.dismiss();
		} else if (arg0.equals(nuovoRicavo)) {
			context.setResult(Activity.RESULT_OK);
			context.finish();
			Intent intent;
			intent = new Intent(context, AddRicavoActivity.class);
			intent.putExtra("data", data);
			context.startActivity(intent);
			dialog.dismiss();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (v.equals(nextPage)) {
				RiepilogoGiornalieroActivityNew activity = (RiepilogoGiornalieroActivityNew) getActivity();
				ViewPager viewPager = activity.getViewPager();
				int currentItem = viewPager.getCurrentItem();
				viewPager.setCurrentItem(currentItem + 1, true);
			} else if (v.equals(previousPage)) {
				RiepilogoGiornalieroActivityNew activity = (RiepilogoGiornalieroActivityNew) getActivity();
				ViewPager viewPager = activity.getViewPager();
				int currentItem = viewPager.getCurrentItem();
				viewPager.setCurrentItem(currentItem - 1, true);
			}
		}
		return false;
	}
}
