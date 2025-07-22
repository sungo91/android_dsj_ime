/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.inputmethod.pinyin.setting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.android.inputmethod.pinyin.R;

import java.util.List;

/**
 * Setting activity of Pinyin IME.
 */
public class SettingsActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener {

  private static String TAG = "SettingsActivity";

  private CheckBoxPreference mKeySoundPref;
  private CheckBoxPreference mVibratePref;
  private CheckBoxPreference mPredictionPref;
  private Preference mVersionPref;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings);

    PreferenceScreen prefSet = getPreferenceScreen();

    mKeySoundPref =
        (CheckBoxPreference) prefSet.findPreference(getString(R.string.setting_sound_key));
    mVibratePref =
        (CheckBoxPreference) prefSet.findPreference(getString(R.string.setting_vibrate_key));
    mPredictionPref =
        (CheckBoxPreference) prefSet.findPreference(getString(R.string.setting_prediction_key));

    mVersionPref = prefSet.findPreference(getString(R.string.setting_version_key));
    mVersionPref.setSummary(getAppVersionName(this) + "-v" + getAppVersionCode(this));

    prefSet.setOnPreferenceChangeListener(this);

    Settings.getInstance(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

    updatePreference(prefSet, getString(R.string.setting_advanced_key));

    updateWidgets();
  }

  @Override protected void onResume() {
    super.onResume();
    updateWidgets();
  }

  @Override protected void onDestroy() {
    Settings.releaseInstance();
    super.onDestroy();
  }

  @Override protected void onPause() {
    super.onPause();
    Settings.setKeySound(mKeySoundPref.isChecked());
    Settings.setVibrate(mVibratePref.isChecked());
    Settings.setPrediction(mPredictionPref.isChecked());

    Settings.writeBack();
  }

  public boolean onPreferenceChange(Preference preference, Object newValue) {
    return true;
  }

  private void updateWidgets() {
    mKeySoundPref.setChecked(Settings.getKeySound());
    mVibratePref.setChecked(Settings.getVibrate());
    mPredictionPref.setChecked(Settings.getPrediction());
  }

  public void updatePreference(PreferenceGroup parentPref, String prefKey) {
    Preference preference = parentPref.findPreference(prefKey);
    if (preference == null) {
      return;
    }
    Intent intent = preference.getIntent();
    if (intent != null) {
      PackageManager pm = getPackageManager();
      List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
      int listSize = list.size();
      if (listSize == 0) parentPref.removePreference(preference);
    }
  }

  /**
   * 获取当前app version code
   */
  public static long getAppVersionCode(Context context) {
    long appVersionCode = 0;
    try {
      PackageInfo packageInfo = context.getApplicationContext()
              .getPackageManager()
              .getPackageInfo(context.getPackageName(), 0);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        appVersionCode = packageInfo.getLongVersionCode();
      } else {
        appVersionCode = packageInfo.versionCode;
      }
    } catch (PackageManager.NameNotFoundException e) {
      Log.e("", e.getMessage());
    }
    return appVersionCode;
  }

  /**
   * 获取当前app version name
   */
  public static String getAppVersionName(Context context) {
    String appVersionName = "";
    try {
      PackageInfo packageInfo = context.getApplicationContext()
              .getPackageManager()
              .getPackageInfo(context.getPackageName(), 0);
      appVersionName = packageInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e("", e.getMessage());
    }
    return appVersionName;
  }
}
