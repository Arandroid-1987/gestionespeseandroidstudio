package com.arandroid.bilanciopersonale.services;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import utils.DateUtils;
import utils.NumberUtils;
import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import android.widget.TextView;

import com.arandroid.bilanciopersonale.R;
import com.arandroid.bilanciopersonale.VisualizzaSpeseActivity;
import com.dao.SettingsDao;
import com.dao.SpesaDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.Spesa;
import com.dto.TagRicavo;
import com.dto.TagSpesa;
import com.dto.VoceBilancio;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SpeseListProvider implements RemoteViewsFactory {
	private List<VoceBilancio> listItemList = new LinkedList<VoceBilancio>();
	private Context context = null;
	private String [] currency;
	
	public SpeseListProvider(Context context, Intent intent) {
		this.context = context;
		intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);

		populateListItem();
	}

	private void populateListItem() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getReadableDatabase();
		Collection<Spesa> spese = SpesaDao.getMostRecentSpesa(db);
		currency = SettingsDao.getCurrency(db);
		listItemList.clear();
		listItemList.addAll(spese);
		db.close();
	}

	@Override
	public int getCount() {
		return listItemList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/*
	 * Similar to getView of Adapter where instead of Viewwe return RemoteViews
	 */
	@Override
	public RemoteViews getViewAt(int position) {
		final RemoteViews remoteView = new RemoteViews(
				context.getPackageName(), R.layout.widget_row_spesa_ricavo);
		VoceBilancio item = listItemList.get(position);
		remoteView.setTextViewText(R.id.data, context.getString(R.string.data_uc_column_space)+DateUtils.getPrintableDataFormat(item.getData()));
		String space_currency = " "+currency[1];
		remoteView.setTextViewText(R.id.importo, context.getString(R.string.importo_uc_column_space) + NumberUtils.getString(item.getImporto())
				+ space_currency);
		StringBuilder content = new StringBuilder();
		if (item instanceof Spesa) {
			Spesa s = (Spesa) item;
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
			Ricavo r = (Ricavo) item;
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
		remoteView.setTextViewText(R.id.firstTag, content.toString());
		
		Intent intent3 = new Intent(context, VisualizzaSpeseActivity.class);
		intent3.putExtra("position", position);
		remoteView.setOnClickFillInIntent(R.id.rowSpesaInutile, intent3);
		
		return remoteView;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDataSetChanged() {
	}

	@Override
	public void onDestroy() {
	}
	
	static class ViewHolder {
		TextView dataTW;
		TextView importoTW;
		TextView firstTagTW;
		Button mostraTag;
		Button elimina;
		Button duplica;
		Button modifica;
	}
}
