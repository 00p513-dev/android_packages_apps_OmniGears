/*
 *  Copyright (C) 2017 The OmniROM Project
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
package org.omnirom.omnigears;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import java.util.List;
import java.util.ArrayList;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;

import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.util.omni.OmniSwitchConstants;
import com.android.internal.util.omni.PackageUtils;
import com.android.internal.util.omni.DeviceUtils;

import org.omnirom.omnilib.preference.SystemSettingSwitchPreference;

@SearchIndexable
public class ButtonSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {

    private static final String CATEGORY_KEYS = "button_keys";
    private static final String CATEGORY_OTHER = "button_other";
    private static final String CATEGORY_POWER = "button_power";
    private static final String CATEGORY_VOLUME = "button_volume_keys";
    private static final String NAVIGATION_BAR_RECENTS_STYLE = "navbar_recents_style";
    private static final String SYSTEM_PROXI_CHECK_ENABLED = "system_proxi_check_enabled";
    private static final String KEY_ADVANCED_REBOOT = "advanced_reboot";

    private ListPreference mNavbarRecentsStyle;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.button_settings);

        final ContentResolver resolver = getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final PreferenceCategory powerCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_POWER);
        final PreferenceCategory volumeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_VOLUME);

        mNavbarRecentsStyle = (ListPreference) findPreference(NAVIGATION_BAR_RECENTS_STYLE);
        int recentsStyle = Settings.System.getInt(resolver,
                Settings.System.OMNI_NAVIGATION_BAR_RECENTS, 0);

        mNavbarRecentsStyle.setValue(Integer.toString(recentsStyle));
        mNavbarRecentsStyle.setSummary(mNavbarRecentsStyle.getEntry());
        mNavbarRecentsStyle.setOnPreferenceChangeListener(this);

        if(getResources().getBoolean(R.bool.config_has_power_button)){
            boolean supportPowerButtonProxyCheck = getResources().getBoolean(com.android.internal.R.bool.config_proxiSensorWakupCheck);
            SwitchPreference proxyCheckPreference = (SwitchPreference) findPreference(SYSTEM_PROXI_CHECK_ENABLED);
            if (!DeviceUtils.deviceSupportsProximitySensor(getActivity()) || !supportPowerButtonProxyCheck) {
                powerCategory.removePreference(proxyCheckPreference);
            }

            String[] rebootList = getResources().getStringArray(com.android.internal.R.array.config_rebootActionsList);
            if (rebootList.length == 0) {
                Preference advancedReboot = findPreference(KEY_ADVANCED_REBOOT);
                powerCategory.removePreference(advancedReboot);
            }
            if(powerCategory.getPreferenceCount() == 0){
                prefScreen.removePreference(powerCategory);
            }
        } else {
            prefScreen.removePreference(powerCategory);
        }
        if(!getResources().getBoolean(R.bool.config_has_volume_button)){
            prefScreen.removePreference(volumeCategory);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavbarRecentsStyle) {
            int value = Integer.valueOf((String) newValue);
            if (value == 1) {
                if (!isOmniSwitchInstalled()){
                    doOmniSwitchUnavail();
                } else if (!OmniSwitchConstants.isOmniSwitchRunning(getActivity())) {
                    doOmniSwitchConfig();
                }
            }
            int index = mNavbarRecentsStyle.findIndexOfValue((String) newValue);
            mNavbarRecentsStyle.setSummary(mNavbarRecentsStyle.getEntries()[index]);
            Settings.System.putInt(getContentResolver(), Settings.System.OMNI_NAVIGATION_BAR_RECENTS, value);
            return true;
        }
        return false;
    }

    private void checkForOmniSwitchRecents() {
        if (!isOmniSwitchInstalled()){
            doOmniSwitchUnavail();
        } else if (!OmniSwitchConstants.isOmniSwitchRunning(getActivity())) {
            doOmniSwitchConfig();
        }
    }

    private void doOmniSwitchConfig() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.omniswitch_title);
        alertDialogBuilder.setMessage(R.string.omniswitch_dialog_running_new)
            .setPositiveButton(R.string.omniswitch_settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    startActivity(OmniSwitchConstants.INTENT_LAUNCH_APP);
                }
            });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void doOmniSwitchUnavail() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.omniswitch_title);
        alertDialogBuilder.setMessage(R.string.omniswitch_dialog_unavail);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private boolean isOmniSwitchInstalled() {
        return PackageUtils.isAvailableApp(OmniSwitchConstants.APP_PACKAGE_NAME, getActivity());
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.button_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    final Resources res = context.getResources();

                    if (!res.getBoolean(R.bool.config_has_power_button)) {
                        result.add(CATEGORY_POWER);
                    }
                    return result;
                }
            };
}
