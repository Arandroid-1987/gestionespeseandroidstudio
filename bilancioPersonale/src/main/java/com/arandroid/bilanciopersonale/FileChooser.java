package com.arandroid.bilanciopersonale;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FileChooser extends ListActivity implements OnClickListener {

	private File currentDir;
	private FileArrayAdapter adapter;
	private File sdCardPath;
	private boolean selectFiles;
	private ImageButton newDirectory;
	private ImageButton selectDirectory;
	private LinearLayout action_button_layout;

	private Dialog newDirDialog;

	public final static String SELECT_FILES = "select_files";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout);
		Intent intent = getIntent();
		selectFiles = intent.getBooleanExtra(SELECT_FILES, true);
		sdCardPath = Environment.getExternalStorageDirectory();
		currentDir = Environment.getExternalStorageDirectory();
		if (selectFiles) {
			currentDir = new File(currentDir, "gestionespese");
		} else {
			action_button_layout = (LinearLayout) findViewById(R.id.action_button_layout);
			action_button_layout.setVisibility(View.VISIBLE);
			newDirectory = (ImageButton) findViewById(R.id.new_directory);
			selectDirectory = (ImageButton) findViewById(R.id.select_directory);

			newDirectory.setOnClickListener(this);
			selectDirectory.setOnClickListener(this);

			newDirectory.getBackground().setAlpha(100);
			selectDirectory.getBackground().setAlpha(100);

			TextView title = (TextView) findViewById(R.id.title);
			title.setText("Seleziona una cartella");
		}
		if (!currentDir.exists()) {
			currentDir = sdCardPath;
		}
		fill(currentDir);
	}

	private void fill(File f) {
		File[] dirs = f.listFiles();
		List<File> tmp = new LinkedList<File>();
		for (int i = 0; i < dirs.length; i++) {
			File t = dirs[i];
			if (t.isDirectory()) {
				tmp.add(t);
			} else if (t.isFile() && selectFiles) {
				if (t.getName().endsWith(".gdb")) {
					tmp.add(t);
				}
			}
		}
		dirs = tmp.toArray(dirs);
		this.setTitle("Cartella Corrente: " + f.getName());
		List<Option> dir = new ArrayList<Option>();
		List<Option> fls = new ArrayList<Option>();
		try {
			for (File ff : dirs) {
				if (ff.isDirectory())
					dir.add(new Option(ff.getName(), "Folder", ff
							.getAbsolutePath()));
				else {
					fls.add(new Option(ff.getName(), "File Size: "
							+ ff.length(), ff.getAbsolutePath()));
				}
			}
		} catch (Exception e) {

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (!f.getName().equalsIgnoreCase("sdcard")) {
			dir.add(0, new Option("..", "Parent Directory", f.getParent()));
		}
		adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view,
				dir);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if (o.getData().equalsIgnoreCase("folder")
				|| o.getData().equalsIgnoreCase("parent directory")) {
			currentDir = new File(o.getPath());
			fill(currentDir);
		} else {
			onFileClick(o);
		}
	}

	private void onFileClick(Option o) {
		Intent data = getIntent();
		data.putExtra("file_path", o.getPath());
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	public void onBackPressed() {
		Option o = adapter.getItem(0);
		if (o != null && o.getPath() != null) {
			if (!currentDir.equals(sdCardPath)) {
				currentDir = new File(o.getPath());
				fill(currentDir);
			} else {
				finish();
			}
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.equals(newDirectory)) {
			// show dialog
			createAndShowNewDirDialog();
		} else if (v.equals(selectDirectory)) {
			Intent data = getIntent();
			data.putExtra("file_path", currentDir.getAbsolutePath());
			setResult(RESULT_OK, data);
			finish();
		}
	}

	private void createAndShowNewDirDialog() {
		AlertDialog.Builder builder = new Builder(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.new_dir_dialog_layout, null);
		final Button okButton = (Button) v.findViewById(R.id.okButton);
		final Button annullaButton = (Button) v
				.findViewById(R.id.annullaButton);
		final EditText dirName = (EditText) v.findViewById(R.id.dirName);
		dirName.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    okButton.performClick();
                    return true;
                } else {
                    return false;
                }
            }
        });
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String name = dirName.getText().toString();
				if (name.length() > 0) {
					createNewDir(name);
					newDirDialog.dismiss();
					fill(currentDir);
				}
			}
		});
		annullaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				newDirDialog.dismiss();
			}
		});
		builder.setView(v);
		newDirDialog = builder.create();
		newDirDialog.show();
	}

	protected void createNewDir(String name) {
		File f = new File(currentDir, name);
		if(!f.exists()){
			f.mkdir();
		}
		fill(f);
	}
}