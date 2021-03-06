package org.tyszecki.rozkladpkp;

import java.io.IOException;

import org.tyszecki.rozkladpkp.R;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class BoardsActivity extends Activity {
	
	StationEdit ed;
	SharedPreferences pref;
	
	TimeButton timeb;
	DateButton dateb;
	ProductsButton prodb;
	
	private ImageButton mButtonLocation;
	
	private Resources res;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boards);

        res = getResources();
        
        final StationEdit autoComplete = (StationEdit)  findViewById(R.id.edittext);
        ed = autoComplete;
        
        autoComplete.setHint("Stacja kolejowa");
        
        pref = getPreferences(MODE_PRIVATE);
        autoComplete.setAutoComplete(pref.getBoolean("EnableStationAC", true));
        
        
        timeb	= (TimeButton) findViewById(R.id.boardsTimeBut);
        timeb.setToNow();
        timeb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(0);
			}
		});
        
        dateb = (DateButton) findViewById(R.id.boardsDateBut);
        dateb.setToNow();
        dateb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(1);
			}
		});
        
        prodb = (ProductsButton) findViewById(R.id.boardsProdBut);
        prodb.setProductString(pref.getString("Products", "11110001111111"));
        prodb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDialog(2);
				
			}
		});
        
        
        Button but = (Button) findViewById(R.id.ok);
        but.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(autoComplete.getText().toString().trim().length() == 0)
				{
					Toast.makeText(getApplicationContext(), "Wprowadź nazwę stacji", Toast.LENGTH_SHORT).show();
					return;
				}
				String sid = autoComplete.getCurrentSID();
				
				Intent ni;
				if(sid.equals(""))
				{
					ni = new Intent(arg0.getContext(),Boards2Activity.class);
					ni.putExtra("userText", autoComplete.getText().toString());
				}
				else
				{
					ni = new Intent(arg0.getContext(),BoardActivity.class);
					ni.putExtra("SID", sid);
					ni.putExtra("Station", autoComplete.getText().toString());
				}
				
				
				ni.putExtra("Time", timeb.getTime());
				ni.putExtra("Date", dateb.getDate());
				ni.putExtra("Type", ((BoardTypeButton)findViewById(R.id.boardsTypeBut)).getType());
				ni.putExtra("Products", prodb.getProductString());
				
				SharedPreferences.Editor e = pref.edit();
				e.putString("Products", prodb.getProductString());
				
				startActivity(ni);
			}
		});
        
        mButtonLocation = (ImageButton) findViewById(R.id.boardsLocation);
        mButtonLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				new GetLocationTask().execute();
					

			}
		});
        
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.boards, menu);
		menu.getItem(0).setTitle((ed.autoComplete() ? "Wyłącz" : "Włącz") + " autouzupełnianie");
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.item01:
			boolean ac = ed.autoComplete();
			ed.setAutoComplete(!ac);
			SharedPreferences.Editor e = pref.edit();
			e.putBoolean("EnableStationAC", !ac);
			e.commit();
			return true;
		}
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case 0:
	        return timeb.timeDialog();
	    case 1:
	    	return dateb.dateDialog();
	    case 2:
	    	return prodb.getDialog();
	    }
	    return null;
	}
	
	
	private class GetLocationTask extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog mProgress;
		
		@Override
		protected void onPostExecute(String result) {
			mProgress.dismiss();
			if(result == null || result == "") {
				Toast.makeText(BoardsActivity.this, 
						res.getText(R.string.toastLocationError), 
						Toast.LENGTH_LONG)
						.show();
			} else {
				ed.setText(result);
				//set cursor at end of text
				final Editable etext = ed.getText();
				final int position = etext.length();  // end of buffer, for instance
				Selection.setSelection(etext, position);
			}
		}


		@Override
		protected void onPreExecute() {
			mProgress = ProgressDialog.show(BoardsActivity.this, 
					res.getText(R.string.progressTitle), 
					res.getText(R.string.progressBodyLocation));
		}


		@Override
		protected String doInBackground(Void... params) {
			try {
				return ((PKPApplication)getApplication()).getLocation();
			} catch (IOException e) {
				return null;
			}
		}
		
	}
	
}
