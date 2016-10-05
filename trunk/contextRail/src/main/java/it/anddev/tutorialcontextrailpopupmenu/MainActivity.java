package it.anddev.tutorialcontextrailpopupmenu;

import it.anddev.tutorialcontextrailpopupmenu.R;
import net.londatiga.android.ActionItem;
import net.londatiga.android.PopupMenu;
import net.londatiga.android.ContextRail;
import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Creiamo le voci da inserire nei menu
		final ActionItem block = new ActionItem();
		block.setTitle("Block");
		block.setIcon(getResources().getDrawable(R.drawable.ic_block_default_rail));
		block.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Block selected" , Toast.LENGTH_SHORT).show();
			}
		});

		final ActionItem delete = new ActionItem();
		delete.setTitle("Delete");
		delete.setIcon(getResources().getDrawable(R.drawable.ic_delete_default_rail));
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Delete selected" , Toast.LENGTH_SHORT).show();
			}
		});


		final ActionItem directMessage = new ActionItem();
		directMessage.setTitle("Direct Message");
		directMessage.setIcon(getResources().getDrawable(R.drawable.ic_directmessage_default_rail));
		directMessage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Direct Message selected", Toast.LENGTH_SHORT).show();
			}
		});
		
		final ActionItem editList = new ActionItem();
		editList.setTitle("Edit List");
		editList.setIcon(getResources().getDrawable(R.drawable.ic_edit_list_default_rail));
		editList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Edit List selected", Toast.LENGTH_SHORT).show();
			}
		});
		
		final ActionItem favorite = new ActionItem();
		favorite.setIcon(getResources().getDrawable(R.drawable.ic_favorite_default_rail));
		favorite.setTitle("Favorite");
		favorite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Favorite selected" , Toast.LENGTH_SHORT).show();
			}
		});

		final ActionItem locationPin = new ActionItem();
		locationPin.setTitle("Location Pin");
		locationPin.setIcon(getResources().getDrawable(R.drawable.ic_location_pin_default_rail));
		locationPin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Location Pin selected" , Toast.LENGTH_SHORT).show();
			}
		});


		final ActionItem reportSpam = new ActionItem();
		reportSpam.setTitle("Report Spam");
		reportSpam.setIcon(getResources().getDrawable(R.drawable.ic_report_spam_default_rail));
		reportSpam.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Report Spam selected", Toast.LENGTH_SHORT).show();
			}
		});

		final ActionItem retweet = new ActionItem();
		retweet.setTitle("Retweet");
		retweet.setIcon(getResources().getDrawable(R.drawable.ic_retweet_default_rail));
		retweet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "Retweet selected", Toast.LENGTH_SHORT).show();
			}
		});

		// Creiamo i menu e inseriamo le voci
		findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ContextRail cr = new ContextRail(v);

				cr.addActionItem(block);				
				cr.addActionItem(delete);
				cr.addActionItem(directMessage);
				cr.addActionItem(editList);
				cr.addActionItem(favorite);
				cr.addActionItem(locationPin);
				cr.addActionItem(reportSpam);
				cr.addActionItem(retweet);
				//qa.setAnimStyle(ContextRail.ANIM_GROW_FROM_RIGHT);

				cr.show();
			}
		});



		findViewById(R.id.btn2).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu pm = new PopupMenu(v);

				pm.addActionItem(block);				
				pm.addActionItem(delete);
				pm.addActionItem(directMessage);
				pm.addActionItem(editList);
				pm.addActionItem(favorite);
				pm.addActionItem(locationPin);
//				pm.addActionItem(reportSpam);
//				pm.addActionItem(retweet);
				pm.setAnimStyle(PopupMenu.ANIM_REFLECT);

				pm.show();
			}
		});

		findViewById(R.id.btn3).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ContextRail cr = new ContextRail(v);

				cr.addActionItem(locationPin);
				cr.addActionItem(reportSpam);
				cr.addActionItem(retweet);
				//qa.setAnimStyle(ContextRail.ANIM_GROW_FROM_RIGHT);

				cr.show();
			}
		});

		findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu pm = new PopupMenu(v);

				pm.addActionItem(locationPin);
				pm.addActionItem(reportSpam);
				pm.addActionItem(retweet);
				//pm.setAnimStyle(PopupMenu.ANIM_GROW_FROM_CENTER);

				pm.show();
			}
		});

		findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ContextRail cr = new ContextRail(v);

				cr.addActionItem(editList);
				cr.addActionItem(favorite);
				cr.addActionItem(locationPin);
				cr.addActionItem(reportSpam);
				//qa.setAnimStyle(ContextRail.ANIM_GROW_FROM_CENTER);

				cr.show();
			}
		});

		findViewById(R.id.btn6).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu pm = new PopupMenu(v);

				pm.addActionItem(block);				
				pm.addActionItem(delete);
				pm.addActionItem(directMessage);
				pm.addActionItem(editList);
				pm.addActionItem(favorite);
				pm.addActionItem(locationPin);
				pm.addActionItem(reportSpam);
				pm.addActionItem(retweet);
				//qa.setAnimStyle(PopupMenu.ANIM_REFLECT);

				pm.show();
			}
		});
	}
}