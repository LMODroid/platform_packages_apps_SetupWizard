/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard;

import static com.libremobileos.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.libremobileos.providers.LMOSettings;

public class RomSettingsActivity extends BaseSetupWizardActivity {

    private SetupWizardApp mSetupWizardApp;

    private CheckBox mNavKeys;

    private final View.OnClickListener mNavKeysClickListener = view -> {
        boolean checked = !mNavKeys.isChecked();
        mNavKeys.setChecked(checked);
        mSetupWizardApp.getSettingsBundle().putBoolean(DISABLE_NAV_KEYS, checked);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetupWizardApp = (SetupWizardApp) getApplication();
        setNextText(R.string.next);

        String servicesFullDescription = getString(R.string.services_pp_explanation);
        getGlifLayout().setDescriptionText(servicesFullDescription);

        View navKeysRow = findViewById(R.id.nav_keys);
        navKeysRow.setOnClickListener(mNavKeysClickListener);
        mNavKeys = findViewById(R.id.nav_keys_checkbox);
        mNavKeys.setChecked(Settings.System.getIntForUser(getContentResolver(),
                LMOSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisableNavkeysOption();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_rom_settings;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_services;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_features;
    }

    private void updateDisableNavkeysOption() {
        final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
        boolean enabled = Settings.System.getIntForUser(getContentResolver(),
                LMOSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;
        boolean checked = myPageBundle.containsKey(DISABLE_NAV_KEYS) ?
                myPageBundle.getBoolean(DISABLE_NAV_KEYS) :
                enabled;
        mNavKeys.setChecked(checked);
        myPageBundle.putBoolean(DISABLE_NAV_KEYS, checked);
    }
}
