/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard.settings

import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import android.view.View
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.libremobileos.providers.LMOSettings
import com.libremobileos.util.DeviceKeysConstants.KEY_MASK_APP_SWITCH
import com.libremobileos.setupwizard.DISABLE_NAV_KEYS
import com.libremobileos.setupwizard.NAVIGATION_OPTION_KEY
import com.libremobileos.setupwizard.R
import com.libremobileos.setupwizard.SetupWizardApp
import com.libremobileos.setupwizard.base.BaseSetupWizardActivity
import com.libremobileos.setupwizard.util.SetupWizardUtils
import com.libremobileos.setupwizard.util.updateCheckedIcons

class NavigationSettingsActivity : BaseSetupWizardActivity() {

    private var selection: String = NAV_BAR_MODE_GESTURAL_OVERLAY

    private lateinit var hideGesturalHint: MaterialSwitch
    private lateinit var hideGesturalHintCard: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navBarEnabled = SetupWizardApp.settingsBundle.getBoolean(DISABLE_NAV_KEYS, false)

        val deviceKeys =
            resources.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys
            )
        val hasHomeKey = (deviceKeys and KEY_MASK_APP_SWITCH) != 0

        glifLayout.setDescriptionText(getString(R.string.navigation_summary))

        val modeGroup = findViewById<MaterialButtonToggleGroup>(R.id.navigation_mode_group)
        val gestureButton = findViewById<MaterialButton>(R.id.mode_gesture)
        val swKeysButton = findViewById<MaterialButton>(R.id.mode_sw_keys)
        val navigationIllustration = findViewById<LottieAnimationView>(R.id.navigation_illustration)
        hideGesturalHint = findViewById(R.id.hide_navigation_hint)
        hideGesturalHintCard = findViewById(R.id.hide_navigation_hint_card)

        modeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) {
                return@addOnButtonCheckedListener
            }
            when (checkedId) {
                R.id.mode_gesture -> {
                    selection = NAV_BAR_MODE_GESTURAL_OVERLAY
                    navigationIllustration.setAnimation(R.raw.lottie_system_nav_fully_gestural)
                    setHintRevealed(revealed = true)
                }

                R.id.mode_sw_keys -> {
                    selection = NAV_BAR_MODE_3BUTTON_OVERLAY
                    navigationIllustration.setAnimation(R.raw.lottie_system_nav_3_button)
                    setHintRevealed(revealed = false)
                }
            }
            modeGroup.updateCheckedIcons(R.drawable.ic_check)
            navigationIllustration.playAnimation()
        }

        var available = 3
        if (!SetupWizardUtils.isPackageInstalled(this, NAV_BAR_MODE_GESTURAL_OVERLAY)) {
            gestureButton.visibility = View.GONE
            modeGroup.check(R.id.mode_sw_keys)
            available--
        }

        if (!SetupWizardUtils.isPackageInstalled(this, NAV_BAR_MODE_3BUTTON_OVERLAY)) {
            swKeysButton.visibility = View.GONE
            available--
        }

        if (!navBarEnabled && hasHomeKey || available <= 1) {
            SetupWizardApp.settingsBundle.putString(
                NAVIGATION_OPTION_KEY,
                NAV_BAR_MODE_3BUTTON_OVERLAY,
            )
            finishAction(RESULT_OK)
        }

        modeGroup.updateCheckedIcons(R.drawable.ic_check)

        setHintRevealed(selection == NAV_BAR_MODE_GESTURAL_OVERLAY)
    }

    private fun setHintRevealed(revealed: Boolean) {
        hideGesturalHintCard.visibility = if (revealed) View.VISIBLE else View.GONE
    }

    override fun onNextPressed() {
        SetupWizardApp.settingsBundle.putString(NAVIGATION_OPTION_KEY, selection)
        val hideHint = hideGesturalHint.isChecked
        Settings.System.putIntForUser(
            contentResolver,
            LMOSettings.System.NAVIGATION_BAR_HINT,
            if (hideHint) 0 else 1,
            UserHandle.USER_CURRENT,
        )
        super.onNextPressed()
    }

    override val layoutResId = R.layout.setup_navigation

    override val titleResId = R.string.setup_navigation

    override val iconResId = R.drawable.ic_navigation
}
