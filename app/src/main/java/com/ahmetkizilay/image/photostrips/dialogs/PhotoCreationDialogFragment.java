package com.ahmetkizilay.image.photostrips.dialogs;

import com.ahmetkizilay.image.photostrips.PhotoBoothActivity;
import com.ahmetkizilay.image.photostrips.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoCreationDialogFragment extends DialogFragment{
	
	public static PhotoCreationDialogFragment newInstance() {
		PhotoCreationDialogFragment frag = new PhotoCreationDialogFragment();
		Bundle args = new Bundle();		
		frag.setArguments(args);		
		return frag;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 View view = inflater.inflate(R.layout.photo_create_dialog, container);
	        getDialog().setTitle("creating photo");
	        getDialog().setCancelable(false);
	        return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((PhotoBoothActivity) getActivity()).onPhotoCreationDialogCreated();
	}
}
