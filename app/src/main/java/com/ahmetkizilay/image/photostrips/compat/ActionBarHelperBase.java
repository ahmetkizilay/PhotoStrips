/*
 * Copyright 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ahmetkizilay.image.photostrips.compat;


import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahmetkizilay.image.photostrips.R;

/**
 * A class that implements the action bar pattern for pre-Honeycomb devices.
 */
public class ActionBarHelperBase extends ActionBarHelper {
    private static final String MENU_RES_NAMESPACE = "http://schemas.android.com/apk/res/android";
    private static final String MENU_ATTR_ID = "id";
    private static final String MENU_ATTR_SHOW_AS_ACTION = "showAsAction";

    protected Set<Integer> mActionItemIds = new HashSet<Integer>();

    protected ActionBarHelperBase(Activity activity) {
        super(activity);
    }

    /**{@inheritDoc}*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mActivity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    }

    /**{@inheritDoc}*/
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
    	 mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
    			 com.ahmetkizilay.image.photostrips.R.layout.actionbar_compat);
         setupActionBar();

         SimpleMenu menu = new SimpleMenu(mActivity);
         mActivity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu);
         mActivity.onPrepareOptionsMenu(menu);
         int inx = 0;
         boolean isPortrait = mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

         for (int i = menu.size() - 1; i >= 0; i--) {
             MenuItem item = menu.getItem(i);
             if (mActionItemIds.contains(item.getItemId())) {
            	 if(!isPortrait) {
            		 addActionItemCompatFromMenuItemLandscape(item, inx++);
            	 }
            	 else {
            		 addActionItemCompatFromMenuItemPortrait(item, inx++);
            	 }
             }
         }

    }
    
    private View addActionItemCompatFromMenuItemPortrait(final MenuItem item, int index) {

        final ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return null;
        }

        // Create the button
        ImageButton actionButton = new ImageButton(mActivity, null, R.drawable.actionbar_compat_unselected_button);                
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(48, 48);
        
        if(index > 0) {
    		lp.addRule(RelativeLayout.LEFT_OF, actionBar.getChildAt(actionBar.getChildCount() - index).getId());
    		
    	}
    	else {
        	lp.alignWithParent = true;
        	lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    		lp.setMargins(0, 0, 4, 0);
    	}
        
        actionButton.setLayoutParams(lp);
        actionButton.setId(new Random().nextInt(10000));
        actionButton.setImageDrawable(item.getIcon());
        actionButton.setScaleType(ImageView.ScaleType.CENTER);
        actionButton.setContentDescription(item.getTitle());
        actionButton.setBackgroundResource(R.drawable.actionbar_compat_unselected_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
            }
        });

        actionBar.addView(actionButton);        
        return actionButton;

    }
    
    private View addActionItemCompatFromMenuItemLandscape(final MenuItem item, int index) {
      
        final ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return null;
        }

    	RelativeLayout actionGroup = (RelativeLayout) mActivity.getLayoutInflater().inflate(R.layout.actionbar_compat_large_item, null);     	
    	RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 48);
        
    	if(index > 0) {
    		lp.addRule(RelativeLayout.LEFT_OF, actionBar.getChildAt(actionBar.getChildCount() - index).getId());
    		lp.setMargins(0, 0, 4, 0);
    	}
    	else {
        	lp.alignWithParent = true;
        	lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    		lp.setMargins(0, 0, 16, 0);
    	}
    	
    	        
        actionGroup.setLayoutParams(lp);
        actionGroup.setId(new Random().nextInt(10000));
        ((ImageView) actionGroup.getChildAt(0)).setImageDrawable(item.getIcon());
        ((TextView) actionGroup.getChildAt(1)).setText(item.getTitle().toString().toUpperCase(Locale.US));
        
    	actionGroup.setBackgroundResource(R.drawable.actionbar_compat_unselected_button);
    	actionGroup.setContentDescription(item.getTitle());
    	
    	actionGroup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
            }
        });
    	    	
    	actionBar.addView(actionGroup); 
    	return actionGroup;

    }

    /**
     * Sets up the compatibility action bar with the given title.
     */
    private void setupActionBar() {
        final ViewGroup actionBarCompat = getActionBarCompat();
        if (actionBarCompat == null) {
            return;
        } 
        
        
        
    }

    /**{@inheritDoc}*/
    @Override
    public void onTitleChanged(CharSequence title, int color) {
        TextView titleView = (TextView) mActivity.findViewById(com.ahmetkizilay.image.photostrips.R.id.actionbar_compat_title);
        if (titleView != null) {
            titleView.setText(title);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        for (Integer id : mActionItemIds) {
            menu.findItem(id).setVisible(false);
        }
        return true;
    }

    /**
     * Returns the {@link android.view.ViewGroup} for the action bar on phones (compatibility action
     * bar). Can return null, and will return null on Honeycomb.
     */
    private ViewGroup getActionBarCompat() {
        return (ViewGroup) mActivity.findViewById(com.ahmetkizilay.image.photostrips.R.id.actionbar_compat);
    }

    public MenuInflater getMenuInflater(MenuInflater superMenuInflater) {
        return new WrappedMenuInflater(mActivity, superMenuInflater);
    }

    
    private class WrappedMenuInflater extends MenuInflater {
        MenuInflater mInflater;

        public WrappedMenuInflater(Context context, MenuInflater inflater) {
            super(context);
            mInflater = inflater;
        }

        @Override
        public void inflate(int menuRes, Menu menu) {
            loadActionBarMetadata(menuRes);
            mInflater.inflate(menuRes, menu);
        }

        /**
         * Loads action bar metadata from a menu resource, storing a list of menu item IDs that
         * should be shown on-screen (i.e. those with showAsAction set to always or ifRoom).
         * @param menuResId
         */
        private void loadActionBarMetadata(int menuResId) {
            XmlResourceParser parser = null;
            try {
                parser = mActivity.getResources().getXml(menuResId);

                int eventType = parser.getEventType();
                int itemId;
                int showAsAction;

                boolean eof = false;
                while (!eof) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (!parser.getName().equals("item")) {
                                break;
                            }

                            itemId = parser.getAttributeResourceValue(MENU_RES_NAMESPACE,
                                    MENU_ATTR_ID, 0);
                            if (itemId == 0) {
                                break;
                            }

                            showAsAction = parser.getAttributeIntValue(MENU_RES_NAMESPACE,
                                    MENU_ATTR_SHOW_AS_ACTION, -1);
                            if (showAsAction == MenuItem.SHOW_AS_ACTION_ALWAYS ||
                                    showAsAction == MenuItem.SHOW_AS_ACTION_IF_ROOM || 
                                    showAsAction == (MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT)) {
                                mActionItemIds.add(itemId);
                            }
                            break;

                        case XmlPullParser.END_DOCUMENT:
                            eof = true;
                            break;
                    }

                    eventType = parser.next();
                }
            } catch (XmlPullParserException e) {
                throw new InflateException("Error inflating menu XML", e);
            } catch (IOException e) {
                throw new InflateException("Error inflating menu XML", e);
            } finally {
                if (parser != null) {
                    parser.close();
                }
            }
        }

    }

}
