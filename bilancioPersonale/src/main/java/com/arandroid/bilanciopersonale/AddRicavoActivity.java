package com.arandroid.bilanciopersonale;

import com.arandroid.bilanciopersonale.fragments.AddRicavoFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class AddRicavoActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment newFragment = null;
		newFragment = new AddRicavoFragment();
		fragmentManager.beginTransaction()
				.add(android.R.id.content, newFragment).commit();
	}

}
