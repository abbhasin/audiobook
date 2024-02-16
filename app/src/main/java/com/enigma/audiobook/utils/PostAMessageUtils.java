package com.enigma.audiobook.utils;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.models.PostMessageModel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class PostAMessageUtils {

    public static ActivityResultLauncher<Intent> setupAudioPicker(ComponentActivity activity,
                                                                  RecyclerView.Adapter<?> adapter,
                                                                  Callable<Optional<PostMessageModel>> postMessageModelProvider) {
        return
                activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {

                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode() == RESULT_OK) {
                                    Uri audioUri = result.getData().getData();
                                    if (audioUri != null) {
                                        try {
                                            Optional<PostMessageModel> modelOpt =
                                                    postMessageModelProvider.call();
                                            if (modelOpt.isPresent()) {
                                                modelOpt.get().clearVideoAudioContent();
                                                modelOpt.get().setMusicUrl(audioUri.toString());
                                                adapter.notifyDataSetChanged();
                                            }
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                        });
    }

    public static ActivityResultLauncher<PickVisualMediaRequest> setupVideoPicker(ComponentActivity activity,
                                                                                  RecyclerView.Adapter<?> adapter,
                                                                                  Callable<Optional<PostMessageModel>> postMessageModelProvider) {
        return activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                ALog.i("VideoPicker", "video selected: " + uri);
                try {
                    Optional<PostMessageModel> modelOpt = postMessageModelProvider.call();
                    if (modelOpt.isPresent()) {
                        modelOpt.get().clearVideoAudioContent();
                        modelOpt.get().setVideoUrl(uri.toString());
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                ALog.i("VideoPicker", "No media selected");
            }
        });
    }

    public static ActivityResultLauncher<PickVisualMediaRequest> setupImagesPicker(ComponentActivity activity,
                                                                                   RecyclerView.Adapter<?> adapter,
                                                                                   Callable<Optional<PostMessageModel>> postMessageModelProvider) {
        return activity.registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
            if (!uris.isEmpty()) {
                ALog.i("PhotoPicker", "Number of items selected: " + uris.size() + " uris:" + uris);
                try {
                    List<String> imagesUrl = uris.stream().map(Uri::toString).collect(Collectors.toList());
                    Optional<PostMessageModel> modelOpt = postMessageModelProvider.call();
                    if (modelOpt.isPresent()) {
                        modelOpt.get().clearVideoAudioContent();
                        modelOpt.get().setImagesUrl(imagesUrl);
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                ALog.i("PhotoPicker", "No media selected");
            }
        });
    }
}
