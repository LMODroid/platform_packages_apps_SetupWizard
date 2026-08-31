/*
 * SPDX-FileCopyrightText: 2024 The LibreMobileOS Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.provider.Settings;
import androidx.annotation.Nullable;

import com.libremobileos.providers.LMOSettings;

public class VolumePanelActivity extends BaseSetupWizardActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGlifLayout().setDescriptionText(getString(R.string.volume_panel_summary));

        final RadioGroup radioGroup = findViewById(R.id.volume_panel_radio_group);
        radioGroup.check(Settings.Secure.getInt(getContentResolver(),
                LMOSettings.Secure.VOLUME_PANEL_ON_LEFT, 0) == 1 ? R.id.radio_left : R.id.radio_right);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Settings.Secure.putInt(getContentResolver(), LMOSettings.Secure.VOLUME_PANEL_ON_LEFT,
                    checkedId == R.id.radio_right ? 0 : 1);
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_volume_panel;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_volume_panel;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_volume_panel;
    }
}


