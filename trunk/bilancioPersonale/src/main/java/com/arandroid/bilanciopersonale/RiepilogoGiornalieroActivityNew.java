package com.arandroid.bilanciopersonale;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import utils.DateUtils;

import com.arandroid.bilanciopersonale.fragments.RiepilogoGiornalieroFragment;
import com.dto.VoceBilancio;
import com.tyczj.extendedcalendarview.Day;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class RiepilogoGiornalieroActivityNew extends AppCompatActivity {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	private String baseDate;
	private List<Day> allDays;
	private int initialPosition;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	// public static final int PAGES_NUMBER = 101;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_riepilogo_giornaliero_activity_new);

		Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
		setSupportActionBar(toolbar);

		baseDate = getIntent().getStringExtra("baseDate");
		allDays = (List<Day>) getIntent().getSerializableExtra("giorni");

		// i primi 7 giorni vanno rimossi
		for (int i = 0; i < 7; i++) {
			allDays.remove(0);
		}

		int c = 0;
		Iterator<Day> iterator = allDays.iterator();
		while (iterator.hasNext()) {
			Day d = iterator.next();
			int year = d.getYear();
			int month = d.getMonth();
			int day = d.getDay();

			Calendar calendar = Calendar.getInstance(Locale.getDefault());
			calendar.setLenient(false);
			try {
				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, month);
				calendar.set(Calendar.DAY_OF_MONTH, day);
				calendar.getTime();
			} catch (Exception x) {
				x.printStackTrace();
				if (day == 0) {
					c++;
				}
				iterator.remove();
			}

			// if()
			// c++;
		}

		initialPosition = getIntent().getIntExtra("position", 0);
		initialPosition -= 7;
		initialPosition -= c;

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager
				.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

					@Override
					public void onPageSelected(int position) {
						int daysToAdd = position - initialPosition;
						Date date = DateUtils.getDate(baseDate);
						date = DateUtils.addDay(date, daysToAdd);
						String dateStr = DateUtils.getDateString(date);
						String printableDate = DateUtils
								.getPrintableDataFormat(dateStr);
						mTitle = printableDate;
						restoreActionBar();

						getIntent().putExtra("nuovaData", dateStr);
					}

					@Override
					public void onPageScrolled(int position,
							float positionOffset, int positionOffsetPixels) {
						mViewPager.getParent().requestDisallowInterceptTouchEvent(true);
					}

					@Override
					public void onPageScrollStateChanged(int state) {
					}
				});

		mTitle = DateUtils.getPrintableDataFormat(baseDate);
		restoreActionBar();

		mViewPager.setCurrentItem(initialPosition);
	}
	
	public ViewPager getViewPager() {
		return mViewPager;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			int daysToAdd = position - initialPosition;
			Date date = DateUtils.getDate(baseDate);
			date = DateUtils.addDay(date, daysToAdd);
			String dateStr = DateUtils.getDateString(date);

			int positionForList = initialPosition + daysToAdd;
			Day day = allDays.get(positionForList);
			ArrayList<VoceBilancio> events = day.getEvents();

			return RiepilogoGiornalieroFragment.newInstance(dateStr, events);
		}

		@Override
		public int getCount() {
			return allDays.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return null;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

}
