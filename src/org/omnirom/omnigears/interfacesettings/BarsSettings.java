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
package org.omnirom.omnigears.interfacesettings;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;

import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import org.omnirom.omnilib.preference.AppMultiSelectListPreference;
import org.omnirom.omnilib.preference.ScrollAppsViewPreference;
import org.omnirom.omnilib.preference.SeekBarPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SearchIndexable
public class BarsSettings extends SettingsPreferenceFragment implements Indexable {
    private static final String TAG = "BarsSettings";
    private static final String KEY_STATUSBAR_CATEGORY = "statusbar_settings_category";
    private static final String KEY_USE_OLD_MOBILETYPE = "use_old_mobiletype";

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bars_settings);
        final PreferenceCategory statusBarCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(KEY_STATUSBAR_CATEGORY);
        final Preference mobileDataStatusIcon = getPreferenceScreen().findPreference(KEY_USE_OLD_MOBILETYPE);
        // TODO actually mobile data is not necessary voice but its better then nothing for now
        if (!Utils.isVoiceCapable(getContext())) {
            statusBarCategory.removePreference(mobileDataStatusIcon);
        }
        if (statusBarCategory.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(statusBarCategory);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.bars_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    if (!Utils.isVoiceCapable(context)) {
                        result.add(KEY_USE_OLD_MOBILETYPE);
                    }
                    return result;
                }
            };
}
