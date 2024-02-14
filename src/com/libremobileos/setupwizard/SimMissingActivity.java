/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard;

import android.os.Bundle;
import android.widget.ImageView;

import com.libremobileos.setupwizard.util.SetupWizardUtils;

public class SimMissingActivity extends BaseSetupWizardActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGlifLayout().setDescriptionText(getString(R.string.sim_missing_summary));
        if (!SetupWizardUtils.simMissing(this)) {
            finishAction(RESULT_OK);
        }
        ImageView simLogo = ((ImageView) findViewById(R.id.sim_slot_image));
        simLogo.setImageResource(R.drawable.sim);
        simLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.sim_missing_page;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_sim_missing;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_sim;
    }

}
