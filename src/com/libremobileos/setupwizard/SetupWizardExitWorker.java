/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.CoroutineWorker;
import androidx.work.WorkerParameters;

import com.libremobileos.setupwizard.util.SetupWizardUtils;

import kotlin.coroutines.Continuation;

public class SetupWizardExitWorker extends CoroutineWorker {

    public SetupWizardExitWorker(@NonNull Context appContext,
            @NonNull WorkerParameters params) {
        super(appContext, params);
    }

    @Nullable
    @Override
    public Object doWork(@NonNull Continuation<? super Result> continuation) {
        SetupWizardUtils.finishSetupWizard(getApplicationContext());
        return Result.success();
    }
}
