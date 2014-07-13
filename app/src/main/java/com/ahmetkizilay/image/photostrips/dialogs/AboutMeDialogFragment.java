package com.ahmetkizilay.image.photostrips.dialogs;

import com.ahmetkizilay.image.photostrips.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AboutMeDialogFragment extends DialogFragment{
	
	public static AboutMeDialogFragment newInstance() {
		AboutMeDialogFragment frag = new AboutMeDialogFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		
		return frag;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(
				"Version 1.0\n\nPERISONiC Sound And Media")
				.setCancelable(false)
				.setTitle("PhotoStrips")
				.setIcon(R.drawable.ic_photostrips)
				.setNeutralButton("DONATE", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				})
                .setPositiveButton("RATE ME", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {

                       Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getActivity().getPackageName()));
                       startActivity(rateIntent);
                   }
                });
		return builder.create();
	}
}
