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
            // NetworkSetupActivity comes before us. DateTimeActivity comes after.
            // If the user presses the back button on DateTimeActivity, we can only pass along
            // that information to NetworkSetupActivity if we are still around. But if we finish
            // here, we're gone, and NetworkSetupActivity will get whatever result we give here.
            // We can't predict the future, but we can reasonably assume that the only way for
            // NetworkSetupActivity to be reached later is if the user went backwards. So, we
            // finish this activity faking that the user pressed the back button, which is required
            // for subactivities like NetworkSetupActivity to work properly on backward navigation.
            // TODO: Resolve all this.
            finishAction(RESULT_SKIP, new Intent().putExtra("onBackPressed", true));
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
