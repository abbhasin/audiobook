package com.enigma.audiobook.utils;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;

public interface ActivityResultLauncherProvider {
    ActivityResultLauncher<PickVisualMediaRequest> getPickVideoLauncher();
    ActivityResultLauncher<PickVisualMediaRequest> getPickImagesLauncher();
    ActivityResultLauncher<Intent> getPickAudioLauncher();
}
