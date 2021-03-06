package org.tyszecki.rozkladpkp;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class StationSpinner extends Spinner {

	public interface onDataLoaded{
		void dataLoaded();
	}
	
	private String station;
	private Handler acHandler;
	private String[][] stations;
	private StationSpinner s = this;
	private ProgressDialog pdialog = null;
	private onDataLoaded callback = null;
	
	public StationSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		acHandler	= new Handler();
		
	}

	public void setProgressDialog(ProgressDialog dial)
	{
		pdialog	= dial;
	}
	
	public void setOnDataLoaded(onDataLoaded dl)
	{
		callback = dl;
	}
	
	public void setUserInput(String s)
	{
		station = s;
		Updater u	= new Updater();		
		u.start();
	}
	
	public void setUserInput(String s, String id)
	{
		stations = new String[1][2];
		stations[0][0] = s;
		stations[0][1] = id;
		acHandler.post(showUpdate);
		if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
        if(callback != null)callback.dataLoaded();
	}

	
	public String getCurrentSID()
	{
		return stations[getSelectedItemPosition()][1];
	}
	
	public String getText()
	{
		return stations[getSelectedItemPosition()][0];
	}
	
	private Runnable showUpdate = new Runnable(){
		
	    public void run(){
	    	ArrayAdapter<String> a = new ArrayAdapter<String>(s.getContext(), android.R.layout.simple_spinner_item);
	    	for(int i = 0; i < stations.length; i++)
	    			a.add(stations[i][0]);

	    	a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    	s.setAdapter(a);
	    	
	    }
	};
	
	private class Updater extends Thread
	{
		public void run()
		{
	        try {
	            InputStream inputStream = new StationSearch().search(station);
	       
	            
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            DocumentBuilder db = dbf.newDocumentBuilder();
	            
	            
	            Document doc = db.parse(inputStream);
	            NodeList list = doc.getElementsByTagName("MLc");
	            
	            int j = list.getLength();
	            stations = new String[j][2];
	            
	            for(int i = 0; i < j; i++)
	            { 
	            	Node n = list.item(i);
	            	stations[i][0] = n.getAttributes().getNamedItem("n").getNodeValue();
	            	stations[i][1] = n.getAttributes().getNamedItem("i").getNodeValue();
	            }

	            acHandler.post(showUpdate);
	            if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
	            if(callback != null)callback.dataLoaded();
	        } 
	        catch (IOException e) {
	        	//throw new Exception("Problem communicating with API", e);
	        	Log.e("Sitkol",e.toString());
	        	if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
	        } catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(pdialog != null && pdialog.isShowing()) pdialog.dismiss();
			}
			
		}
	};
}
