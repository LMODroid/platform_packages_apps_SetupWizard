/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard.user

import android.content.Intent
import com.libremobileos.setupwizard.base.SubBaseActivity

class ScreenLockActivity : SubBaseActivity() {

    override fun onStartSubactivity() {
        startSubactivity(Intent(ACTION_SETUP_LOCKSCREEN))
    }

    companion object {
        private const val ACTION_SETUP_LOCKSCREEN = "com.android.settings.SETUP_LOCK_SCREEN"
    }
}
