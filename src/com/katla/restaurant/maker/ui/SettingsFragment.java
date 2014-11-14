package com.katla.restaurant.maker.ui;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.katla.restaurant.maker.R;
import com.katla.restaurant.maker.core.CloudBackend;
import com.katla.restaurant.maker.core.CloudBackendAsync;
import com.katla.restaurant.maker.core.CloudBackendFragment;
import com.katla.restaurant.maker.core.CloudBackendFragment.OnListener;
import com.katla.restaurant.maker.core.CloudQuery.Scope;
import com.katla.restaurant.maker.core.Filter.Op;
import com.katla.restaurant.maker.core.CloudCallbackHandler;
import com.katla.restaurant.maker.core.CloudEntity;
import com.katla.restaurant.maker.core.CloudQuery;
import com.katla.restaurant.maker.core.Consts;
import com.katla.restaurant.maker.core.Filter;

import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
	
	private static final String PROCESSING_FRAGMENT_TAG = "BACKEND_FRAGMENT";
	
	/*
	 * UI Components
	 */

	 private FragmentManager mFragmentManager;
	 private CloudBackendFragment mProcessingFragment;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initiateFragments();
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_title_key)));	
	}
 
    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);
 
        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
    private void updateRestaurantName(final String value)
    {
    	if(mProcessingFragment.getCloudBackend()!=null)
        {
    		final String userName = mProcessingFragment.getCloudBackend().getCredential().getSelectedAccount().name.toString();
    		//CloudQuery cq = new CloudQuery("RestaurantName");
    		//cq.setFilter(Filter.eq("label", "friends"));
    		mProcessingFragment.getCloudBackend().listByProperty("RestaurantName", "userName", Op.EQ, userName, null, 1, Scope.FUTURE_AND_PAST, new CloudCallbackHandler<List<CloudEntity>>() {
				
				@Override
				public void onComplete(List<CloudEntity> results) {
					if(results.size() > 0)
					{
						CloudEntity editRestaurantName = results.get(0);
						if(value != null){
							editRestaurantName.put("restaurantName",value);
							 CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
					                @Override
					                public void onComplete(final CloudEntity result) {
					                    
					                }

					                @Override
					                public void onError(final IOException exception) {
					                    
					                }
					            };
					            mProcessingFragment.getCloudBackend().update(editRestaurantName, handler);
						}
						else{
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString(getString(R.string.pref_title_key),editRestaurantName.get("restaurantName").toString());
							editor.apply();
						}
						
					}
					else
					{
						CloudEntity insertRestaurantName = new CloudEntity("RestaurantName");
			    		insertRestaurantName.put("userName",userName);
			        	insertRestaurantName.put("restaurantName", value);
			        	// create a response handler that will receive the result or an error
			            CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
			                @Override
			                public void onComplete(final CloudEntity result) {
			                    Toast.makeText(getActivity(), "Name Changed to " + result.get("restaurantName").toString(), Toast.LENGTH_SHORT).show();
			                    
			                }

			                @Override
			                public void onError(final IOException exception) {
			                    
			                }
			            };
			        	// execute the insertion with the handler
			            
			            	mProcessingFragment.getCloudBackend().update(insertRestaurantName, handler);
					}
					
				}
			});
    		
        }
    	
        
    }
    
    private void initiateFragments() {
		 mFragmentManager = getFragmentManager();  
		 FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

	        // Check to see if we have retained the fragment which handles
	        // asynchronous backend calls
	        mProcessingFragment = (CloudBackendFragment) mFragmentManager.
	                findFragmentByTag(PROCESSING_FRAGMENT_TAG);
	        // If not retained (or first time running), create a new one
	        if (mProcessingFragment == null) {
	            mProcessingFragment = new CloudBackendFragment();
	            mProcessingFragment.setRetainInstance(true);
	            fragmentTransaction.add(mProcessingFragment, PROCESSING_FRAGMENT_TAG);
	        }
	        fragmentTransaction.commit();
	 }
 
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
 
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        if(preference.getKey().equals(getString(R.string.pref_title_key)))
        {
        	//new UpdateBackendTask().execute(value.toString());
        	updateRestaurantName(value.toString());
        }
        return true;
    }


}
