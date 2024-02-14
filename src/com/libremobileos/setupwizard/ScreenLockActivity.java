/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard;

import static com.libremobileos.setupwizard.SetupWizardApp.ACTION_SETUP_LOCKSCREEN;

import android.content.Intent;

public class ScreenLockActivity extends SubBaseActivity {

    @Override
    protected void onStartSubactivity() {
        Intent intent = new Intent(ACTION_SETUP_LOCKSCREEN);
        startSubactivity(intent);
    }
}
