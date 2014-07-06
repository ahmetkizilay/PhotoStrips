package com.ahmetkizilay.image.photostrips.utils.notused;

import java.util.ArrayList;
import java.util.List;


import android.R;
import android.app.Activity;
import android.hardware.Camera.Size;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CustomSpinnerAdapter extends BaseAdapter{

	private final List<SpinnerItem> itemList;
	private Activity parentActivity;
	
	public CustomSpinnerAdapter(Activity parentActivity, List<Size> sizeList) {
		this.itemList = new ArrayList<SpinnerItem>();
		for(int i = 0, iLen = sizeList.size();i < iLen; i++) {
			this.itemList.add(new SpinnerItem(sizeList.get(i)));
		}
		this.parentActivity = parentActivity;
	}
	
	public int getCount() {
		return this.itemList.size();
	}

	public SpinnerItem getItem(int position) {
		return itemList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView = new TextView(parentActivity);
		textView.setText(itemList.get(position).getDisplay());
		textView.setTextAppearance(parentActivity, R.style.TextAppearance_DeviceDefault_Large);
		textView.setHeight(48);
		textView.setGravity(Gravity.CENTER);
		return textView;
	}
	
	public int getItemPosition(Size itemValue) {
		for(int i = 0; i < itemList.size(); i++) {
			if(itemList.get(i).getValue().width == itemValue.width &&
			   itemList.get(i).getValue().height == itemValue.height	) {
				return i;
			}
		}
		return 0;
	}

}
