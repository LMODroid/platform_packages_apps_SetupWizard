/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard;

import static com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP;

import android.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;

import com.libremobileos.setupwizard.util.SetupWizardUtils;

public class SimMissingActivity extends BaseSetupWizardActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SetupWizardUtils.simMissing(this) || !SetupWizardUtils.hasTelephony(this)) {
            finishAction(RESULT_SKIP);
            return;
        }
        getGlifLayout().setDescriptionText(getString(R.string.sim_missing_summary));
        setNextAllowed(true);
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
