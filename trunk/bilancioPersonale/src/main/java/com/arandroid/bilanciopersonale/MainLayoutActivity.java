package com.arandroid.bilanciopersonale;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import utils.BilancioMeseCalculator;
import utils.Email;
import utils.KeyButton;

import com.arandroid.bilanciopersonale.fragments.AboutFragment;
import com.arandroid.bilanciopersonale.fragments.AddRicavoFragment;
import com.arandroid.bilanciopersonale.fragments.AddSpesaFragment;
import com.arandroid.bilanciopersonale.fragments.BilanciFragment;
import com.arandroid.bilanciopersonale.fragments.CalendarFragment;
import com.arandroid.bilanciopersonale.fragments.ConfrontiFragment;
import com.arandroid.bilanciopersonale.fragments.DBManagerFragment;
import com.arandroid.bilanciopersonale.fragments.ImpostazioniFragment;
import com.arandroid.bilanciopersonale.fragments.ListFragmentRicavi;
import com.arandroid.bilanciopersonale.fragments.ListFragmentSpese;
import com.arandroid.bilanciopersonale.fragments.NavigationDrawerFragment;
import com.arandroid.bilanciopersonale.fragments.RicaviProgrammatiFragment;
import com.arandroid.bilanciopersonale.fragments.SpeseProgrammateFragment;
import com.dao.SettingsDao;
import com.db.DatabaseHandler;
import com.dto.BilancioMese;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainLayoutActivity extends AppCompatActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	private final static int HOME = 0;
	private final static int NUOVA_SPESA = 1;
	private final static int NUOVO_RICAVO = 2;
	private final static int RIEPILOGO_SPESE = 3;
	private final static int RIEPILOGO_RICAVI = 4;
	private final static int SPESE_PROGRAMMATE = 5;
	private final static int RICAVI_PROGRAMMATI = 6;
	private final static int BILANCI = 7;
	private final static int CONFRONTI = 8;
	private final static int IMPOSTAZIONI = 9;
	private final static int ABOUT = 10;
	private final static int GESTIONE_DB = 11;
	
	private final static String [] TAGS = new String [] {"HOME", "NUOVA_SPESA", "NUOVO_RICAVO", "RIEPILOGO_SPESE", "RIEPILOGO_RICAVI", "SPESE_PROGRAMMATE", "RICAVI PROGRAMMATI", "BILANCI",
		"CONFRONTI", "IMPOSTAZIONI", "ABOUT", "GESTIONE_DB"};

	private Fragment[] fragments = new Fragment[GESTIONE_DB + 1];

	private int count = 0;
	private char[] typedPassword;
	private boolean showPassword;
	private String currentPW;

	private Dialog accessDialog = null;

	private List<BilancioMese> bilanciFragmentConfronti;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	private int currentFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_layout);

		Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
		setSupportActionBar(toolbar);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		setupBilanciFragmentConfronti();
		createAndShowAccessDialog();
	}

	private void createAndShowAccessDialog() {
		final DatabaseHandler dbHandler = DatabaseHandler.getInstance(this);
		final SQLiteDatabase db = dbHandler.getReadableDatabase();
		if (SettingsDao.isUsingPassword(db)) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setCancelable(false);
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.access_dialog,
					(ViewGroup) this.findViewById(R.id.pwLL));

			currentPW = SettingsDao.getCurrentPassword(db);
			typedPassword = new char[currentPW.length()];
			final Button accedi = (Button) v.findViewById(R.id.accedi);
			final Button annulla = (Button) v.findViewById(R.id.annulla);
			final CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox1);
			final TextView pwDimenticata = (TextView) v
					.findViewById(R.id.pwDimenticata);
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
					SQLiteDatabase db = dbHandler.getReadableDatabase();
					String currentPW = SettingsDao.getCurrentPassword(db);
					String typedPW = new String(typedPassword);
					if (typedPW.equals(currentPW)) {
						accessDialog.dismiss();
					} else {
						Toast.makeText(getApplication(),
								getString(R.string.password_errata),
								Toast.LENGTH_LONG).show();
					}
				}
			});

			annulla.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setResult(RESULT_CANCELED);
					finish();
				}
			});

			pwDimenticata.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new Thread() {
						public void run() {
							inviaPWperEmail();
						};
					}.start();

					Toast.makeText(MainLayoutActivity.this,
							getString(R.string.password_inviata_email),
							Toast.LENGTH_SHORT).show();
					setResult(RESULT_CANCELED);
					finish();
				}

				private void inviaPWperEmail() {
					SQLiteDatabase db = dbHandler.getReadableDatabase();
					Cursor c = SettingsDao.getData(db);
					String email = c.getString(c
							.getColumnIndex(SettingsDao.EMAIL));
					String password = c.getString(c
							.getColumnIndex(SettingsDao.PASSWORD));
					c.close();
					db.close();
					Email.sendEmail(email, password);
				}
			});

			builder.setView(v);

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View title = inflater.inflate(R.layout.dialog_title,
						(ViewGroup) this.findViewById(R.id.titleLayout));
				TextView titleText = (TextView) title
						.findViewById(R.id.titleText);
				titleText.setText("Login");
				builder.setCustomTitle(title);
			} else {
				builder.setTitle("Login");
			}

			accessDialog = builder.create();
			accessDialog.show();
		}
		db.close();
	}

	protected void setupPasswordLayout(LinearLayout passwordLayout) {
		passwordLayout.removeAllViews();
		int passwordLength = currentPW.length();
		for (int i = 0; i < passwordLength; i++) {
			KeyButton layout = new KeyButton(this);
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

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment newFragment = null;
		switch (position) {
		case HOME:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new CalendarFragment();
				fragments[position] = newFragment;
			}
			break;
		case NUOVA_SPESA:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new AddSpesaFragment();
				fragments[position] = newFragment;
			}
			break;
		case NUOVO_RICAVO:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new AddRicavoFragment();
				fragments[position] = newFragment;
			}
			break;
		case RIEPILOGO_SPESE:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new ListFragmentSpese();
				fragments[position] = newFragment;
			}
			break;
		case RIEPILOGO_RICAVI:
			if (newFragment == null) {
				newFragment = new ListFragmentRicavi();
				fragments[position] = newFragment;
			}
			break;
		case SPESE_PROGRAMMATE:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new SpeseProgrammateFragment();
				fragments[position] = newFragment;
			}
			break;
		case RICAVI_PROGRAMMATI:
			if (newFragment == null) {
				newFragment = new RicaviProgrammatiFragment();
				fragments[position] = newFragment;
			}
			break;
		case BILANCI:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new BilanciFragment();
				fragments[position] = newFragment;
			}
			break;
		case CONFRONTI:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new ConfrontiFragment();
				fragments[position] = newFragment;
			}
			break;
		case IMPOSTAZIONI:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new ImpostazioniFragment();
				fragments[position] = newFragment;
			}
			break;
		case ABOUT:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new AboutFragment();
				fragments[position] = newFragment;
			}
			break;
		case GESTIONE_DB:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new DBManagerFragment();
				fragments[position] = newFragment;
			}
			break;
		default:
			break;
		}
		fragmentManager.beginTransaction().replace(R.id.container, newFragment, TAGS[position])
				.commit();
		currentFragment = position;
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	private void setupBilanciFragmentConfronti() {
		bilanciFragmentConfronti = new ArrayList<BilancioMese>();

		Calendar c = Calendar.getInstance(Locale.getDefault());

		int currentMonth = c.get(Calendar.MONTH);
		int currentYear = c.get(Calendar.YEAR);

		c.add(Calendar.MONTH, -1);
		int lastMonth = c.get(Calendar.MONTH);

		BilancioMese current = BilancioMeseCalculator.get(currentMonth,
				currentYear, this);
		BilancioMese last = BilancioMeseCalculator.get(lastMonth, currentYear,
				this);

		bilanciFragmentConfronti.add(last);
		bilanciFragmentConfronti.add(current);

	}

	public List<BilancioMese> getBilanciFragmentConfronti() {
		return bilanciFragmentConfronti;
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		if (currentFragment == HOME) {
			super.onBackPressed();
		} else if (currentFragment == IMPOSTAZIONI) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment currentFragmentVisible = fragmentManager.findFragmentByTag(TAGS[IMPOSTAZIONI]);
			if(currentFragmentVisible.isVisible()){
				ImpostazioniFragment fragment = (ImpostazioniFragment) currentFragmentVisible;
				fragment.salva();
				Fragment newFragment = new CalendarFragment();
				fragmentManager.beginTransaction()
						.replace(R.id.container, newFragment, TAGS[HOME]).commit();
				currentFragment = HOME;
				mNavigationDrawerFragment.setSelectedPosition(HOME);
			}
		} else {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment newFragment = new CalendarFragment();
			fragmentManager.beginTransaction()
					.replace(R.id.container, newFragment, TAGS[HOME]).commit();
			currentFragment = HOME;
			mNavigationDrawerFragment.setSelectedPosition(HOME);
		}
	}

}
