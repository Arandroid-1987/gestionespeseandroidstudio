package com.arandroid.bilanciopersonale.services;

import com.arandroid.bilanciopersonale.AddRicavoActivity;
import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bilanciopersonale.R;
import com.arandroid.bilanciopersonale.VisualizzaRicaviActivity;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class RicaviWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// Get all ids
		ComponentName thisWidget = new ComponentName(context,
				RicaviWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = updateWidgetListView(context, widgetId);

			// Register an onClickListener
			Intent intent = new Intent(context, MainLayoutActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0);
			remoteViews.setOnClickPendingIntent(R.id.titleBarText,
					pendingIntent);

			Intent intent2 = new Intent(context, AddRicavoActivity.class);
			PendingIntent pendingIntent2 = PendingIntent.getActivity(context,
					0, intent2, 0);
			remoteViews.setOnClickPendingIntent(R.id.addVoce, pendingIntent2);
			
			Intent startActivityIntent = new Intent(context, VisualizzaRicaviActivity.class);
            PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.myList, startActivityPendingIntent);

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

	private RemoteViews updateWidgetListView(Context context, int appWidgetId) {

		// which layout to show on widget
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);

		// RemoteViews Service needed to provide adapter for ListView
		Intent svcIntent = new Intent(context, RicaviWidgetService.class);
		// passing app widget id to that RemoteViews Service
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		// setting a unique Uri to the intent
		// don't know its purpose to me right now
		svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		// setting adapter to listview of the widget
		remoteViews.setRemoteAdapter(R.id.myList, svcIntent);
		// setting an empty view in case of no data
//		remoteViews.setEmptyView(R.id.myList, R.id.empty_view);
		return remoteViews;
	}
}
