package com.ui.gestionespese;

import java.io.File;

import com.arandroid.bilanciopersonale.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ShareBackupDialogCreator implements OnClickListener{
	private Button shareButton;
	private File filePath;
	private Context context;
	private static AlertDialog dialog;
	
	public static void createAndShowDialog(Context context, File filePath){
		ShareBackupDialogCreator creator = new ShareBackupDialogCreator(context, filePath);
		dialog = creator.create();
		dialog.show();
	}
	
	private ShareBackupDialogCreator(Context context, File filePath){
		this.filePath = filePath;
		this.context = context;
	}
	
	private AlertDialog create(){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View convertView = inflater.inflate(R.layout.share_backup_layout,
				null);
		
		shareButton = (Button) convertView.findViewById(R.id.shareButton);
		shareButton.setOnClickListener(this);
		
		builder.setView(convertView);
		
		return builder.create();
	}

	@Override
	public void onClick(View v) {
		if(shareButton.equals(v)){
			Intent sendIntent = new Intent();
			Uri uri = Uri.fromFile(filePath);

			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.setType("audio/*");
			sendIntent.putExtra(Intent.EXTRA_STREAM, uri);

			context.startActivity(Intent.createChooser(sendIntent, context
					.getResources().getText(R.string.condividi)));
			dialog.dismiss();
		}
	}

}
