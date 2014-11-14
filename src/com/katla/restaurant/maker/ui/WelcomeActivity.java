package com.katla.restaurant.maker.ui;

import java.io.IOException;
import java.util.List;

import com.katla.restaurant.maker.R;
import com.katla.restaurant.maker.core.CloudBackendFragment;
import com.katla.restaurant.maker.core.CloudCallbackHandler;
import com.katla.restaurant.maker.core.CloudBackendFragment.OnListener;
import com.katla.restaurant.maker.core.CloudQuery.Scope;
import com.katla.restaurant.maker.core.Filter.Op;
import com.katla.restaurant.maker.core.CloudEntity;
import com.katla.restaurant.maker.sample.guestbook.GuestbookActivity;
import com.katla.restaurant.maker.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class WelcomeActivity extends Activity implements OnListener{
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	private static final String PROCESSING_FRAGMENT_TAG = "BACKEND_FRAGMENT";
	
	/*
	 * UI Components
	 */

	 private FragmentManager mFragmentManager;
	 private CloudBackendFragment mProcessingFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_welcome);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final TextView contentView = (TextView) findViewById(R.id.welcome_screen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.check_in_button).setOnTouchListener(
				mDelayHideTouchListener);
		findViewById(R.id.check_in_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this,GuestbookActivity.class);
				startActivity(intent);
			}
		});
		initiateFragments();
	}

	@Override
	protected void onStart() {
		super.onStart();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//getRestaurantNameFromCloud();
		final TextView contentView = (TextView) findViewById(R.id.welcome_screen_content);
		contentView.setText(prefs.getString(getString(R.string.pref_title_key),getString(R.string.pref_title_default)));
		
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}



	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
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

	 public void getRestaurantNameFromCloud()
	 {
		 final String userName = mProcessingFragment.getCloudBackend().getCredential().getSelectedAccount().name.toString();
 		 mProcessingFragment.getCloudBackend().listByProperty("RestaurantName", "userName", Op.EQ, userName, null, 1, Scope.FUTURE_AND_PAST , new CloudCallbackHandler<List<CloudEntity>>() {
 			@Override
			public void onComplete(List<CloudEntity> results) {
				if(results.size() > 0)
				{
					CloudEntity restaurantName = results.get(0);
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(getString(R.string.pref_title_key),restaurantName.get("restaurantName").toString());
					editor.apply();
				}
 			}
 			@Override
            public void onError(final IOException exception) {
                
            }
		});
			
		 
	 }
	@Override
	public void onCreateFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBroadcastMessageReceived(List<CloudEntity> message) {
		
		
	}
}
