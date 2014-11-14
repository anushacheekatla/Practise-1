package com.katla.restaurant.maker.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract.Root;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.katla.restaurant.maker.R;

/**
 * A placeholder fragment containing a simple view.
 */
public  class IntroFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static IntroFragment newInstance(int sectionNumber) {
		IntroFragment fragment = new IntroFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public IntroFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		int sectionNumber = this.getArguments().getInt(ARG_SECTION_NUMBER);
		final View rootView;
		switch (sectionNumber){
		case 1:
			rootView = inflater.inflate(R.layout.fragment_intro_1,
					container, false);
			break;
		case 2:
			rootView = inflater.inflate(R.layout.fragment_intro_2,
					container, false);
			break;
		case 3:
			rootView = inflater.inflate(R.layout.fragment_intro_3,
					container, false);
			CheckBox dontShowAgain = (CheckBox) rootView.findViewById(R.id.dont_show_again);
			ImageView doneButton = (ImageView) rootView.findViewById(R.id.done_image);
			doneButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(getActivity(),WelcomeActivity.class);
					startActivity(intent);
					
				}
			});
			break;
	    default:
	    	rootView = inflater.inflate(R.layout.fragment_intro_1,
					container, false);	    	
	    	break;
		}
			
			
			
		
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		 outState.putString("DO NOT CRASH", "OK");
	}
}
