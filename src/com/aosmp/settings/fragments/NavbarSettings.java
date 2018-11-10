/*
 * Copyright (C) 2014 TeamEos
 * Copyright (C) 2019 AOSmP
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

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.util.aosmp.aosmpUtils;

import com.android.settings.R;
import com.android.settings.aosmp.CustomSettingsPreferenceFragment;

public class NavbarSettings extends CustomSettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String NAVIGATION_BAR_ENABLED = "navigation_bar_enabled";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.aosmp_settings_navigation);
        addCustomPreference(findPreference(NAVIGATION_BAR_ENABLED), SECURE_TWO_STATE,
                getActivity().getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar) ? 1 : 0);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
