/*
 * Copyright (C) 2014 The LiquidSmooth Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aosmp.settings.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Switch;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class WakeLockBlocker extends SettingsPreferenceFragment {

    private static final String TAG = "WakeLockBlocker";

    private Switch mBlockerEnabled;
    private ListView mWakeLockList;
    private List<String> mSeenWakeLocks;
    private List<String> mBlockedWakeLocks;
    private LayoutInflater mInflater;
    private Map<String, Boolean> mWakeLockState;
    private WakeLockListAdapter mListAdapter;
    private boolean mEnabled;
    private TextView mWakeLockListHeader;
    private TextView mTextView;
    private View switchBar;
    private int mBackgroundActivatedColor;
    private int mBackgroundColor;

    private static final int MENU_RELOAD = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;

    public class WakeLockListAdapter extends ArrayAdapter<String> {

        public WakeLockListAdapter(Context context, int resource, List<String> values) {
            super(context, R.layout.wakelock_item, resource, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = mInflater.inflate(R.layout.wakelock_item, parent, false);
            final CheckBox check = (CheckBox)rowView.findViewById(R.id.wakelock_blocked);
            check.setText(mSeenWakeLocks.get(position));

            Boolean checked = mWakeLockState.get(check.getText().toString());
            check.setChecked(checked.booleanValue());

            if(checked.booleanValue()){
                check.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }

            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        mWakeLockState.put(v.getText().toString(), new Boolean(checked));
                        if(checked){
                            check.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            mListAdapter.notifyDataSetChanged();
                        } else {
                            check.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
                            mListAdapter.notifyDataSetChanged();
                        }
                    }
            });
            return rowView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.wakelock_blocker_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.wakelock_blocker, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBackgroundActivatedColor = ContextCompat.getColor(getActivity(), R.color.switchBarBackgroundActivatedColor);
        mBackgroundColor = ContextCompat.getColor(getActivity(), R.color.switch_bar_background);

        mTextView = (TextView) view.findViewById(R.id.nos_switch_text);
        mTextView.setText(getString(isEnabled() ?
                R.string.switch_bar_on : R.string.switch_bar_off));

        switchBar = view.findViewById(R.id.nos_switch_bar);
        switchBar.setBackgroundColor(isEnabled() ? mBackgroundActivatedColor : mBackgroundColor);

        mBlockerEnabled  = (Switch) switchBar.findViewById(android.R.id.switch_widget);
        mBlockerEnabled.setChecked(isEnabled());

        mBlockerEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {

                mTextView.setText(getString(checked ? R.string.switch_bar_on : R.string.switch_bar_off));
                switchBar.setBackgroundColor(checked ? mBackgroundActivatedColor : mBackgroundColor);

                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.WAKELOCK_BLOCKING_ENABLED,
                        checked ? 1 : 0 );

                updateSwitches();
            }
        });

        switchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBlockerEnabled.setChecked(!mBlockerEnabled.isChecked());
            }
        });

        mWakeLockState = new HashMap<String, Boolean>();
        updateSeenWakeLocksList();
        updateBlockedWakeLocksList();

        mWakeLockList = (ListView) getActivity().findViewById(
                R.id.wakelock_list);
        mWakeLockListHeader = (TextView) getActivity().findViewById(
                R.id.wakelock_list_header);

        mListAdapter = new WakeLockListAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice,
                mSeenWakeLocks);
        mWakeLockList.setAdapter(mListAdapter);

        updateSwitches();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.AOSMP;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().setTitle(R.string.misc_title);
    }

    private boolean isEnabled() {
        return Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.WAKELOCK_BLOCKING_ENABLED, 0) != 0;
     }

    private void updateSwitches() {
        mEnabled = mBlockerEnabled.isChecked();
        mWakeLockList.setVisibility(mEnabled ? View.VISIBLE : View.INVISIBLE);
        mWakeLockListHeader.setVisibility(mEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateSeenWakeLocksList() {
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);

        String seenWakeLocks =  pm.getSeenWakeLocks();
        mSeenWakeLocks = new ArrayList<String>();

        if (seenWakeLocks!=null && seenWakeLocks.length()!=0){
            String[] parts = seenWakeLocks.split("\\|");
            for(int i = 0; i < parts.length; i++){
                mSeenWakeLocks.add(parts[i]);
                mWakeLockState.put(parts[i], new Boolean(false));
            }
        }
    }

    private void updateBlockedWakeLocksList() {
        String blockedWakelockList = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.WAKELOCK_BLOCKING_LIST);

        mBlockedWakeLocks = new ArrayList<String>();

        if (blockedWakelockList!=null && blockedWakelockList.length()!=0){
            String[] parts = blockedWakelockList.split("\\|");
            for(int i = 0; i < parts.length; i++){
                mBlockedWakeLocks.add(parts[i]);

                // add all blocked but not seen so far
                if(!mSeenWakeLocks.contains(parts[i])){
                    mSeenWakeLocks.add(parts[i]);
                }
                mWakeLockState.put(parts[i], new Boolean(true));
            }
        }

        Collections.sort(mSeenWakeLocks);
    }

    private void save(){
        StringBuffer buffer = new StringBuffer();
        Iterator<String> nextState = mWakeLockState.keySet().iterator();
        while(nextState.hasNext()){
            String name = nextState.next();
            Boolean state=mWakeLockState.get(name);
            if(state.booleanValue()){
                buffer.append(name + "|");
            }
        }
        if(buffer.length()>0){
            buffer.deleteCharAt(buffer.length() - 1);
        }
        Settings.System.putString(getActivity().getContentResolver(),
                Settings.System.WAKELOCK_BLOCKING_LIST, buffer.toString());
    }

    private void reload(){
        mWakeLockState = new HashMap<String, Boolean>();
        updateSeenWakeLocksList();
        updateBlockedWakeLocksList();

        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RELOAD, 0, R.string.wakelock_blocker_reload)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setAlphabeticShortcut('r')
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                        MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, MENU_SAVE, 0, R.string.wakelock_blocker_save)
                .setIcon(R.drawable.ic_wakelockblocker_save)
                .setAlphabeticShortcut('s')
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                        MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RELOAD:
                if (mEnabled){
                    reload();
                }
                return true;
            case MENU_SAVE:
                if (mEnabled){
                    save();
                }
                return true;
            default:
                return false;
        }
    }

}
