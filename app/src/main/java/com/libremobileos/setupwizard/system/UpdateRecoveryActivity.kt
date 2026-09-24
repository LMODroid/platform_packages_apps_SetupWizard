/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard.system

import android.os.Bundle
import android.os.SystemProperties
import android.util.Log
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import com.google.android.setupdesign.GlifRecyclerLayout
import com.google.android.setupdesign.items.RecyclerItemAdapter
import com.google.android.setupdesign.items.SwitchItem
import com.libremobileos.setupwizard.ENABLE_RECOVERY_UPDATE
import com.libremobileos.setupwizard.R
import com.libremobileos.setupwizard.SetupWizardApp
import com.libremobileos.setupwizard.UPDATE_RECOVERY_PROP
import com.libremobileos.setupwizard.base.BaseSetupWizardActivity
import com.libremobileos.setupwizard.util.SetupWizardUtils

class UpdateRecoveryActivity : BaseSetupWizardActivity() {

    private val itemAdapter by lazy {
        (glifLayout as GlifRecyclerLayout).adapter as RecyclerItemAdapter
    }

    private val updateRecoveryItem by lazy {
        itemAdapter.findItemById(R.id.update_recovery_item) as SwitchItem
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glifLayout.setDescriptionText(
            getString(
                R.string.update_recovery_full_description,
                getString(R.string.update_recovery_description),
                getString(R.string.update_recovery_warning),
            )
        )

        if (!SetupWizardUtils.hasRecoveryUpdater(this)) {
            Log.v(TAG, "No recovery updater, skipping UpdateRecoveryActivity")
            finishAction(RESULT_SKIP)
            return
        }

        itemAdapter.setOnItemSelectedListener { item ->
            if (item is SwitchItem) {
                item.isChecked = !item.isChecked
            }
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

    companion object {
        private const val TAG = "UpdateRecoveryActivity"
        private var firstTime: Boolean = true
    }
}
