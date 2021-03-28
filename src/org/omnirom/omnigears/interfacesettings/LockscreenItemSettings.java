/*
 *  Copyright (C) 2015-2018 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package org.omnirom.omnigears.interfacesettings;
import android.os.Bundle;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.omni.PackageUtils;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;

import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import org.omnirom.omnilib.preference.SeekBarPreference;
import org.omnirom.omnilib.preference.SystemSettingSwitchPreference;

import java.util.List;
import java.util.ArrayList;

@SearchIndexable
public class LockscreenItemSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "LockscreenItemSettings";
    //private static final String KEY_LOCKSCREEN_MEDIA_BLUR = "lockscreen_media_blur";
    private static final String KEY_LOCKSCREEN_WEATHER = "lockscreen_weather_enabled";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
    private static final String LOCKSCREEN_LEFT_UNLOCK = "sysui_keyguard_left_unlock";
    private static final String LOCKSCREEN_RIGHT_UNLOCK = "sysui_keyguard_right_unlock";
    private static final String KEYGUARD_SHOW_BATTERY_BAR = "sysui_keyguard_show_battery_bar";
    private static final String KEYGUARD_SHOW_BATTERY_BAR_ALWAYS = "sysui_keyguard_show_battery_bar_always";
    private static final String KEYGUARD_SHOW_WATT_ON_CHARGING = "sysui_keyguard_show_watt";
    private static final String KEYGUARD_SHOW_CURRENT_ON_CHARGING = "sysui_keyguard_show_current";
    private static final String KEYGUARD_SHOW_BATTERY_TEMP = "sysui_keyguard_show_battery_temp";
    private static final String PREFERENCE_BATTERY = "category_battery";

    //private SeekBarPreference mLockscreenMediaBlur;
    private Preference mLeftShortcut;
    private Preference mRightShortcut;
    private Preference mBatteryInfo;
    private SystemSettingSwitchPreference mBatteryBar;
    private SystemSettingSwitchPreference mBatteryBarAlways;
    private SystemSettingSwitchPreference mShowWatt;
    private SystemSettingSwitchPreference mShowCurrent;
    private SystemSettingSwitchPreference mShowTemp;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreenitems);

        /*mLockscreenMediaBlur = (SeekBarPreference) findPreference(KEY_LOCKSCREEN_MEDIA_BLUR);
        int value = Math.min(Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_LOCKSCREEN_MEDIA_BLUR, 12), 25);
        mLockscreenMediaBlur.setValue(value);
        mLockscreenMediaBlur.setOnPreferenceChangeListener(this);*/

        mBatteryBar = (SystemSettingSwitchPreference) findPreference(KEYGUARD_SHOW_BATTERY_BAR);
        mBatteryBar.setChecked(Settings.System.getInt(getContentResolver(),
                        Settings.System.OMNI_KEYGUARD_SHOW_BATTERY_BAR, 1) != 0);
        mBatteryBar.setOnPreferenceChangeListener(this);

        mBatteryBarAlways = (SystemSettingSwitchPreference) findPreference(KEYGUARD_SHOW_BATTERY_BAR_ALWAYS);
        mBatteryBarAlways.setChecked(Settings.System.getInt(getContentResolver(),
                        Settings.System.OMNI_KEYGUARD_SHOW_BATTERY_BAR_ALWAYS, 1) != 0);
        mBatteryBarAlways.setOnPreferenceChangeListener(this);

        mBatteryInfo = getPreferenceScreen().findPreference(PREFERENCE_BATTERY);
        boolean showBatteryCategory = getResources().getBoolean(R.bool.config_show_top_level_battery);
        if (!showBatteryCategory) {
            getPreferenceScreen().removePreference(mBatteryInfo);
        }

        mShowWatt = (SystemSettingSwitchPreference) findPreference(KEYGUARD_SHOW_WATT_ON_CHARGING);
        mShowWatt.setChecked(Settings.System.getInt(getContentResolver(),
                        Settings.System.OMNI_KEYGUARD_SHOW_WATT_ON_CHARGING, 1) != 0);
        mShowWatt.setOnPreferenceChangeListener(this);

        mShowCurrent = (SystemSettingSwitchPreference) findPreference(KEYGUARD_SHOW_CURRENT_ON_CHARGING);
        mShowCurrent.setChecked(Settings.System.getInt(getContentResolver(),
                        Settings.System.OMNI_KEYGUARD_SHOW_CURRENT_ON_CHARGING, 1) != 0);
        mShowCurrent.setOnPreferenceChangeListener(this);

        mShowTemp = (SystemSettingSwitchPreference) findPreference(KEYGUARD_SHOW_BATTERY_TEMP);
        mShowTemp.setChecked(Settings.System.getInt(getContentResolver(),
                        Settings.System.OMNI_KEYGUARD_SHOW_BATTERY_TEMP, 1) != 0);
        mShowTemp.setOnPreferenceChangeListener(this);

        if (!PackageUtils.isAvailableApp(WEATHER_SERVICE_PACKAGE, getContext())) {
            Preference pref = getPreferenceScreen().findPreference(KEY_LOCKSCREEN_WEATHER);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }
    
        mLeftShortcut = findPreference(ShortcutPicker.LOCKSCREEN_LEFT_BUTTON);
        mRightShortcut = findPreference(ShortcutPicker.LOCKSCREEN_RIGHT_BUTTON);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateShortcutSummary();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        /*if (preference == mLockscreenMediaBlur) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_LOCKSCREEN_MEDIA_BLUR, value);
            return true;
        }*/
        if (preference == mBatteryBar) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_KEYGUARD_SHOW_BATTERY_BAR, value ? 1 : 0);
        } else if (preference == mBatteryBarAlways) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_KEYGUARD_SHOW_BATTERY_BAR_ALWAYS, value ? 1 : 0);
        } else if (preference == mShowWatt) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_KEYGUARD_SHOW_WATT_ON_CHARGING, value ? 1 : 0);
        } else if (preference == mShowCurrent) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_KEYGUARD_SHOW_CURRENT_ON_CHARGING, value ? 1 : 0);
        } else if (preference == mShowTemp) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_KEYGUARD_SHOW_BATTERY_TEMP, value ? 1 : 0);
        }
        return true;
    }

    private void updateShortcutSummary() {
        String leftValue = Settings.Secure.getString(getContentResolver(), ShortcutPicker.LOCKSCREEN_LEFT_BUTTON);
        setShortcutSummary(mLeftShortcut, leftValue);
        String rightValue = Settings.Secure.getString(getContentResolver(), ShortcutPicker.LOCKSCREEN_RIGHT_BUTTON);
        setShortcutSummary(mRightShortcut, rightValue);
    }

    private void setShortcutSummary(Preference shortcut, String value) {
        if (value == null) {
            shortcut.setSummary(R.string.lockscreen_default);
            return;
        } else if (value.equals(ShortcutPicker.LOCKSCREEN_HIDDEN_BUTTON)) {
            shortcut.setSummary(R.string.lockscreen_hidden);
            return;
        } else if (value.contains("::")) {
            ShortcutParser.Shortcut info = getShortcutInfo(getContext(), value);
            shortcut.setSummary(info != null ? info.label : null);
        } else if (value.contains("/")) {
            ActivityInfo info = getActivityinfo(getContext(), value);
            shortcut.setSummary(info != null ? info.loadLabel(getContext().getPackageManager())
                    : null);
        } else {
            shortcut.setSummary(R.string.lockscreen_default);
        }
    }

    private ActivityInfo getActivityinfo(Context context, String value) {
        ComponentName component = ComponentName.unflattenFromString(value);
        try {
            return context.getPackageManager().getActivityInfo(component, 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private ShortcutParser.Shortcut getShortcutInfo(Context context, String value) {
        return ShortcutParser.Shortcut.create(context, value);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.lockscreenitems;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}

