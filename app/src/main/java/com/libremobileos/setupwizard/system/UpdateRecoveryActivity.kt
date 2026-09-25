/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard.system

import android.os.Bundle
import android.os.SystemProperties
import android.util.Log
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import com.libremobileos.setupwizard.ENABLE_RECOVERY_UPDATE
import com.libremobileos.setupwizard.R
import com.libremobileos.setupwizard.SetupWizardApp
import com.libremobileos.setupwizard.UPDATE_RECOVERY_PROP
import com.libremobileos.setupwizard.base.BaseSetupWizardActivity
import com.libremobileos.setupwizard.util.SetupWizardUtils

class UpdateRecoveryActivity : BaseSetupWizardActivity() {

    private val updateRecoveryItem by lazy { toggle(R.id.update_recovery_item) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDescriptionText(
            getString(
                R.string.update_recovery_full_description,
                getString(R.string.update_recovery_description),
                getString(R.string.update_recovery_warning),
            )
        )

        if (!SetupWizardUtils.hasRecoveryUpdater(this)) {
            Log.v(TAG, "No recovery updater, skipping UpdateRecoveryActivity")
            SetupWizardUtils.disableComponent(this, UpdateRecoveryActivity::class.java)
            finishAction(RESULT_SKIP)
            return
        }

        // Allow overriding the default switch state
        if (firstTime) {
            SetupWizardApp.settingsBundle.putBoolean(
                ENABLE_RECOVERY_UPDATE,
                SystemProperties.getBoolean(UPDATE_RECOVERY_PROP, true),
            )
        }

        firstTime = false
    }

    override fun onResume() {
        super.onResume()
        updateRecoveryItem.isChecked =
            SetupWizardApp.settingsBundle.getBoolean(ENABLE_RECOVERY_UPDATE, true)
    }

    override fun onNextPressed() {
        SetupWizardApp.settingsBundle.putBoolean(
            ENABLE_RECOVERY_UPDATE,
            updateRecoveryItem.isChecked,
        )
        super.onNextPressed()
    }

    override val layoutResId = R.layout.update_recovery_page

    override val titleResId = R.string.update_recovery_title

    override val iconResId = R.drawable.ic_system_update

    override val itemEntriesResId = R.xml.update_recovery_items

    companion object {
        private const val TAG = "UpdateRecoveryActivity"
        private var firstTime: Boolean = true
    }
}
