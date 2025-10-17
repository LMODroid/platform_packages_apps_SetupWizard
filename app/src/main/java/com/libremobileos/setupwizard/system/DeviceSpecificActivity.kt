/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard.system

import android.content.ComponentName
import android.content.Intent
import com.libremobileos.setupwizard.base.SubBaseActivity
import com.libremobileos.setupwizard.util.SetupWizardUtils

class DeviceSpecificActivity : SubBaseActivity() {

    override fun onStartSubactivity() {
        val intent = Intent(ACTION_SETUP_DEVICE)
        val name: ComponentName? = intent.resolveActivity(packageManager)
        if (name != null) {
            startSubactivity(intent)
        } else {
            SetupWizardUtils.disableComponent(this, DeviceSpecificActivity::class.java)
            finishAction(RESULT_OK)
        }
    }

    companion object {
        private const val ACTION_SETUP_DEVICE = "org.lineageos.settings.device.SUW_SETTINGS"
    }
}
