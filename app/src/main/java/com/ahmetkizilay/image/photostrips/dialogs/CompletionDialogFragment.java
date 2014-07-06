package com.ahmetkizilay.image.photostrips.dialogs;

import com.ahmetkizilay.image.photostrips.PhotoBoothActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class CompletionDialogFragment extends DialogFragment{
	
	public static CompletionDialogFragment newInstance() {
		CompletionDialogFragment frag = new CompletionDialogFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		
		return frag;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("wanna see the picture?")
				.setCancelable(false)
				.setPositiveButton("Yes, sure!",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								((PhotoBoothActivity) getActivity()).positiveCompletionCallback();
							}
						})
				.setNegativeButton("Not Now..",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		return builder.create();
	}
}
