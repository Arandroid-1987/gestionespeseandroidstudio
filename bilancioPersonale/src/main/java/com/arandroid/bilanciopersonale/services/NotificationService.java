package com.arandroid.bilanciopersonale.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import utils.DateUtils;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;

import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bilanciopersonale.R;
import com.dao.RicavoProgrammatoDao;
import com.dao.SettingsDao;
import com.dao.SpesaProgrammataDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.RicavoProgrammato;
import com.dto.Spesa;
import com.dto.SpesaProgrammata;
import com.dto.TagRicavo;
import com.dto.TagSpesa;
import com.dto.VoceBilancio;

public class NotificationService extends Service {

	/**
	 * * Simply return null, since our Service will not be communicating with *
	 * any other components. It just does its work silently.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * * This is where we initialize. We call this when onStart/onStartCommand
	 * is * called by the system. We won't do anything with the intent here, and
	 * you * probably won't, either.
	 */
	private void handleIntent(Intent intent) { // obtain the wake lock
		// check the global background data setting do the actual work, in a
		// separate thread
		new PollTask().execute();
	}

	private class PollTask extends AsyncTask<Void, Void, Void> {
		private Collection<VoceBilancio> vociScadute;
		private int[] backupData;
		private String[] currency;

		/**
		 * * This is where YOU do YOUR work. There's nothing for me to write
		 * here * you have to fill this in. Make your HTTP request(s) or
		 * whatever it is * you have to do to get your updates in here, because
		 * this is run in a * separate thread
		 */
		@Override
		protected Void doInBackground(Void... params) {
			DatabaseHandler handler = DatabaseHandler
					.getInstance(NotificationService.this);
			SQLiteDatabase db = handler.getWritableDatabase();

			vociScadute = new LinkedList<VoceBilancio>();
			Collection<SpesaProgrammata> speseProgrammate = SpesaProgrammataDao
					.getSpeseScadute(db);
			vociScadute.addAll(speseProgrammate);
			Collection<RicavoProgrammato> ricaviProgrammati = RicavoProgrammatoDao
					.getRicaviScaduti(db);
			vociScadute.addAll(ricaviProgrammati);
			backupData = SettingsDao.getBackupData(db);
			currency = SettingsDao.getCurrency(db);
			db.close();
			return null;
		}

		/**
		 * * In here you should interpret whatever you fetched in doInBackground
		 * * and push any notifications you need to the status bar, using the *
		 * NotificationManager. I will not cover this here, go check the docs on
		 * * NotificationManager. * * What you HAVE to do is call stopSelf()
		 * after you've pushed your * notification(s). This will: * 1) Kill the
		 * service so it doesn't waste precious resources * 2) Call onDestroy()
		 * which will release the wake lock, so the device * can go to sleep
		 * again and save precious battery.
		 */

		@Override
		protected void onPostExecute(Void result) {
			checkVociScadute();
			checkBackup();
		}

		private void checkBackup() {
			if (backupData != null) {
				System.out.println("BACKUP AUTOMATICO!!");
				boolean useBackup = backupData[0] > 0;
				if (useBackup) {
					int repeat = backupData[1];
					int lastBackupInt = backupData[3];
					boolean useSameFile = backupData[2] > 0;
					Date lastBackup = null;
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd",
							Locale.ITALIAN);
					try {
						lastBackup = sdf.parse("" + lastBackupInt);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if (lastBackup != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(lastBackup);
						c.add(Calendar.DAY_OF_YEAR, repeat);
						Date nextBackup = c.getTime();
						String nextBackupString = sdf.format(nextBackup);
						String todayString = sdf.format(new Date());
						if (nextBackupString.equals(todayString)) {
							// do the backup
							DatabaseHandler handler = DatabaseHandler
									.getInstance(NotificationService.this);
							SQLiteDatabase db = handler.getWritableDatabase();
							handler.backup(db, useSameFile, true);
							SettingsDao.useBackup(db, repeat, useSameFile,
									Integer.parseInt(nextBackupString));
							if (db.isOpen()) {
								db.close();
							}
						}
					}
				}
			}
		}

		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		private void checkVociScadute() {
			int notifCount = 0;
			if (vociScadute != null && !vociScadute.isEmpty()) {
				Intent intent = new Intent(NotificationService.this,
						MainLayoutActivity.class);
				PendingIntent pIntent = PendingIntent.getActivity(
						NotificationService.this, 0, intent, 0);

				for (VoceBilancio voce : vociScadute) {
					String title = "";
					StringBuilder content = new StringBuilder();
					content.append(
							DateUtils.getPrintableDataFormat(voce.getData()))
							.append(" / ");
					if (voce instanceof Spesa) {
						title = getString(R.string.hai_effettuato_spesa);
						Spesa s = (Spesa) voce;
						Collection<TagSpesa> tags = s.getTags();
						int count = 0;
						for (TagSpesa tag : tags) {
							content.append(tag.getValore());
							if (count < tags.size() - 1) {
								content.append("-");
							}
							count++;
						}
					} else {
						title = getString(R.string.hai_ottenuto_ricavo);
						Ricavo r = (Ricavo) voce;
						Collection<TagRicavo> tags = r.getTags();
						int count = 0;
						for (TagRicavo tag : tags) {
							content.append(tag.getValore());
							if (count < tags.size() - 1) {
								content.append("-");
							}
							count++;
						}
					}

					String space_currency_ext = " " + currency[0];
					content.append(" / ").append(voce.getImporto())
							.append(space_currency_ext);

					// Uri soundUri =
					// RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

					// build notification
					// the addAction re-use the same intent to keep the example
					// short
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
						Notification.Builder builder = new Notification.Builder(
								NotificationService.this)
								.setContentTitle(title).setContentText(content)
								.setSmallIcon(R.drawable.icon_notification)
								.setContentIntent(pIntent).setAutoCancel(true)
						// .setSound(soundUri)
						;

						Notification n = null;

						if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
							n = builder.build();
						} else {
							n = builder.getNotification();
						}

						NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

						notificationManager.notify(notifCount++, n);
					}
				}

			}
			stopSelf();
		}
	}

	/**
	 * * This is deprecated, but you have to implement it if you're planning on
	 * * supporting devices with an API level lower than 5 (Android 2.0).
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		handleIntent(intent);
	}

	/**
	 * * This is called on 2.0+ (API level 5 or higher). Returning *
	 * START_NOT_STICKY tells the system to not restart the service if it is *
	 * killed because of poor resource (memory/cpu) conditions.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);
		return START_NOT_STICKY;
	}

}