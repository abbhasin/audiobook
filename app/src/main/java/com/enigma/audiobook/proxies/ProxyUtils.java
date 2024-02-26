package com.enigma.audiobook.proxies;

import com.enigma.audiobook.backend.models.Following;
import com.enigma.audiobook.backend.models.FollowingType;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.RetryHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProxyUtils {
    private static final String TAG = "ProxyUtils";

    public static void updateFollowing(FollowingsService followingsService,
                                       boolean toFollow, String userId, String followeeId,
                                       FollowingType followingType) {
        Following following = new Following();
        following.setFollowerUserId(userId);
        following.setFollowingType(followingType);
        following.setFolloweeId(followeeId);
        if (toFollow) {
            Call<Void> addFollowingCall = followingsService.addFollowing(following);
            RetryHelper.enqueueWithRetry(addFollowingCall, 1, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        ALog.i(TAG, "unable to add following");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    ALog.e(TAG, "unable to add following", t);
                }
            });
        } else {
            Call<Void> addFollowingCall = followingsService.removeFollowing(following);
            RetryHelper.enqueueWithRetry(addFollowingCall, 1, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        ALog.i(TAG, "unable to remove following");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    ALog.e(TAG, "unable to remove following", t);
                }
            });
        }

    }
}
