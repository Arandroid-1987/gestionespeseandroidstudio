package com.arandroid.bilanciopersonale;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FileArrayAdapter extends ArrayAdapter<Option> {

	private Context c;
	private int id;
	private List<Option> items;

	public FileArrayAdapter(Context context, int textViewResourceId,
			List<Option> objects) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
	}

	public Option getItem(int i) {
		return items.get(i);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}
		final Option o = items.get(position);
		if (o != null) {
			RelativeLayout rl = (RelativeLayout) v.findViewById(R.id.RelativeLayout1);
			if (position % 2 == 0) {
				rl.setBackgroundColor(Color.WHITE);  
			} else {
				rl.setBackgroundColor(Color.LTGRAY);  
			}
			TextView t1 = (TextView) v.findViewById(R.id.TextView01);
			// TextView t2 = (TextView) v.findViewById(R.id.TextView02);
			ImageView iv = (ImageView) v.findViewById(R.id.imageView1);

			if (t1 != null)
				t1.setText(o.getName());

			if (iv != null) {
				if (o.getData().startsWith("File")) {
					iv.setImageResource(R.drawable.file);
				} else if (o.getData().startsWith("Parent")) {
					t1.setText("...parent directory");
					iv.setImageResource(R.drawable.back);
				} else {
					iv.setImageResource(R.drawable.folder);
				}
			}

		}
		return v;
	}

}
