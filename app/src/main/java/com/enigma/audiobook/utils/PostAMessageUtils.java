package com.enigma.audiobook.utils;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.enigma.audiobook.activities.GodPageActivity;
import com.enigma.audiobook.models.PostMessageModel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PostAMessageUtils {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1037;
    public static final int MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 1042;

    public static ActivityResultLauncher<Intent> setupAudioPicker(ComponentActivity activity,
                                                                  AtomicReference<? extends RecyclerView.Adapter<?>> adapter,
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
                                                adapter.get().notifyDataSetChanged();
                                            }
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                        });
    }

    public static boolean checkAudioPermission(ComponentActivity activity) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            // For devices running SDK versions lower than Android 6.0 (API level 23)
            // Permissions are granted at installation time, so no need to check or request permissions
            if (ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ) {
                Toast.makeText(activity, "Please grant permission to select audio",
                        Toast.LENGTH_SHORT).show();
                // Request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            } else {
                return true;
                // Permission has already been granted
                // Proceed with accessing media content
            }
        } else {
            if (ContextCompat.checkSelfPermission(activity, READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED ) {
                Toast.makeText(activity, "Please grant permission to select audio",
                        Toast.LENGTH_SHORT).show();
                // Request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{READ_MEDIA_AUDIO},
                        MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO);
                return false;
            } else {
                return true;
                // Permission has already been granted
                // Proceed with accessing media content
            }
        }
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with accessing media content
                    Toast.makeText(activity, "Audio selection permission granted, please select Add Audio",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Audio cannot be chosen until you provide the permission",
                            Toast.LENGTH_SHORT).show();
                    // Permission denied, handle accordingly (e.g., show a message, disable functionality)
                }
            }
        } else {
            if (requestCode == MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with accessing media content
                    Toast.makeText(activity, "Audio selection permission granted, please select Add Audio",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Audio cannot be chosen until you provide the permission",
                            Toast.LENGTH_SHORT).show();
                    // Permission denied, handle accordingly (e.g., show a message, disable functionality)
                }
            }
        }
    }



    public static ActivityResultLauncher<PickVisualMediaRequest> setupVideoPicker(ComponentActivity activity,
                                                                                  AtomicReference<? extends RecyclerView.Adapter<?>> adapter,
                                                                                  Callable<Optional<PostMessageModel>> postMessageModelProvider) {
        return activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                ALog.i("VideoPicker", "video selected: " + uri);
                try {
                    Optional<PostMessageModel> modelOpt = postMessageModelProvider.call();
                    if (modelOpt.isPresent()) {
                        modelOpt.get().clearVideoAudioContent();
                        modelOpt.get().setVideoUrl(uri.toString());
                        adapter.get().notifyDataSetChanged();
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
                                                                                   AtomicReference<? extends RecyclerView.Adapter<?>> adapter,
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
                        adapter.get().notifyDataSetChanged();
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
