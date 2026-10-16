/*
 * SPDX-FileCopyrightText: 2024 The LibreMobileOS Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard.settings

import android.os.Bundle
import android.provider.Settings
import android.widget.RadioGroup
import com.libremobileos.providers.LMOSettings
import com.libremobileos.setupwizard.R
import com.libremobileos.setupwizard.base.BaseSetupWizardActivity

class VolumePanelActivity : BaseSetupWizardActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glifLayout.setDescriptionText(getString(R.string.volume_panel_summary))

        val radioGroup: RadioGroup = findViewById(R.id.volume_panel_radio_group)
        val onLeft =
            Settings.Secure.getInt(contentResolver, LMOSettings.Secure.VOLUME_PANEL_ON_LEFT, 0) == 1

        radioGroup.check(if (onLeft) R.id.radio_left else R.id.radio_right)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            Settings.Secure.putInt(
                contentResolver,
                LMOSettings.Secure.VOLUME_PANEL_ON_LEFT,
                if (checkedId == R.id.radio_right) 0 else 1,
            )
        }
    }

    override val layoutResId = R.layout.setup_volume_panel

    override val titleResId = R.string.setup_volume_panel

    override val iconResId = R.drawable.ic_volume_panel
}
