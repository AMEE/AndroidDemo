package com.amee.androiddemo;

import android.app.Activity;
import android.os.Bundle;
import com.amee.client.AmeeException;
import com.amee.client.model.profile.*;
import com.amee.client.model.data.*;
import com.amee.client.service.AmeeContext;
import com.amee.client.service.AmeeObjectFactory;
import com.amee.client.util.Choice;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import java.math.*;
import java.util.ArrayList;
import java.util.List;

public class AMEEDemo extends Activity {
	public static final String USERNAME = "Someusername";
	public static final String PASSWORD = "Somepassword";
	public static final String DEVICE_CHOICE = "Select a device.";
	public static final String TYPE_CHOICE = "Select a type.";
	private String device=null;
	private String type=null;
	
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String answer = parent.getItemAtPosition(pos).toString();
			if (answer == DEVICE_CHOICE || answer == TYPE_CHOICE) {
				return;
			}
			String feedback = "You chose " + answer;
			String[] choices = null;
			if (answer.equals("Personal Computers")) {
				String[] answers = { TYPE_CHOICE, "Desktop", "Laptop" };
				choices = answers;
			} else if (answer.equals("Monitor")) {
				String[] answers = { TYPE_CHOICE, "CRT", "LCD" };
				choices = answers;
			}
			if (choices != null) { //first choice was made
			        device=answer;
				setupSpinner(R.id.spinner_type, choices);
				Toast.makeText(parent.getContext(), feedback, Toast.LENGTH_LONG).show();
			} else {//second and final choice was made
				type=answer;
				try {
				  feedback=doAMEE();
				  Toast.makeText(parent.getContext(), feedback, Toast.LENGTH_LONG).show();
				} catch (AmeeException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
				}
			}

		}

		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		String[] answers = { DEVICE_CHOICE, "Personal Computers", "Monitor" };
		setupSpinner(R.id.spinner_device, answers);
    }
    
	@SuppressWarnings("deprecation")
	private String doAMEE() throws AmeeException{
		AmeeContext.getInstance().setUsername(USERNAME);
		AmeeContext.getInstance().setPassword(PASSWORD);
		AmeeContext.getInstance().setBaseUrl("http://stage.amee.com");

		// Use the AmeeObjectFactory to create a new Profile
		AmeeObjectFactory objectFactory = AmeeObjectFactory.getInstance();
		AmeeProfile profile = objectFactory.getProfile();

		// Specify the AMEE category
		String category = "home/appliances/computers/generic";

		// Now perform a drill down.
		//NOTE: Ideally this should be used to populate the dropdowns too, but we've
		//cut a corner by hardcoding the string choices into the drilldowns here.
		AmeeDrillDown ameeDrillDown = objectFactory.getDrillDown(category+"/drill");
		ameeDrillDown.addSelection(ameeDrillDown.getChoiceName(), device);
		ameeDrillDown.fetch(); //the fetch() method forces a call to the AMEE API
		ameeDrillDown.addSelection(ameeDrillDown.getChoiceName(), type);
		ameeDrillDown.fetch(); //the fetch() method forces a call to the AMEE API
		AmeeDataItem ameeDataItem = ameeDrillDown.getDataItem();
		//Now create the profile item
		AmeeProfileCategory profileCategory = objectFactory.getProfileCategory(profile, category);
		List<Choice> values = new ArrayList<Choice>();
		values.add(new Choice("numberOwned", "1")); //ideally you'd ask the user for this
		AmeeProfileItem profileItem = profileCategory.addProfileItem(ameeDataItem,values);
                
		BigDecimal result = profileItem.getAmount();
		MathContext mc = new MathContext(3);
		
                return "Typical annual CO2 emission is "+result.round(mc)+" "+profileItem.getAmountUnit();
	}
	
	public void setupSpinner(int spinnerID, String[] choices) {
		Spinner spinner = (Spinner) findViewById(spinnerID);

		ArrayAdapter<String> adapter = new ArrayAdapter(this,
				android.R.layout.simple_spinner_item, choices);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}
}