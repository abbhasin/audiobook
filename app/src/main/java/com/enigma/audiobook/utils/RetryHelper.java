package com.enigma.audiobook.utils;

import android.os.Handler;

import java.util.concurrent.Callable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetryHelper {
    public static final int DEFAULT_RETRIES = 3;

    public static <T> void enqueueWithRetry(Call<T> call, final int retryCount, final Callback<T> callback) {
        new RetrofitRetryableCall<>(call, retryCount, true).enqueue(callback);
    }

    public static <T> void enqueueWithRetry(Call<T> call, final Callback<T> callback) {
        enqueueWithRetry(call, DEFAULT_RETRIES, callback);
    }

    /**
     * call only on a new thread, different from the main thread.
     */
    public static <T> Response<T> executeWithRetry(Call<T> call) throws Exception {
        return new RetrofitRetryableCall<>(call, DEFAULT_RETRIES, false).execute();
    }

    /**
     * call only on a new thread, different from the main thread.
     */
    public static <T> T executeWithRetry(Callable<T> callable) throws Exception {
        return new RetryableCallable<>(callable, DEFAULT_RETRIES).call();
    }

    public static class RetryableCallable<T> implements Callable<T> {
        private final Callable<T> callable;
        private final int maxAttempts;
        private int attempts;

        public RetryableCallable(Callable<T> callable, int maxAttempts) {
            this.callable = callable;
            this.maxAttempts = maxAttempts;
            this.attempts = 0;
        }

        @Override
        public T call() throws Exception {
            Exception ex = null;
            while (attempts++ < maxAttempts) {
                try {
                    return callable.call();
                } catch (Exception e) {
                    ex = e;
                }
                try {
                    long delay = (long) (Math.pow(2, attempts) * 1000);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (ex != null) {
                ALog.e("RetryHelper", "error while executing callable", ex);
                throw ex;
            }
            throw new Exception("error while executing api");
        }
    }

    public static class RetrofitRetryableCall<T> {

        private final Call<T> call;
        private final int maxAttempts;
        private int attempts;
        private final Handler handler;

        public RetrofitRetryableCall(Call<T> call, int maxAttempts, boolean enqueueRequest) {
            this.call = call;
            this.maxAttempts = maxAttempts;
            this.attempts = 0;
            if (enqueueRequest) {
                this.handler = new Handler();
            } else {
                this.handler = null;
            }

        }

        public void enqueue(Callback<T> callback) {
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    ALog.i("RetryHelper", "response:" + response.isSuccessful() + " attempts:" + attempts);
                    if (!response.isSuccessful() && attempts < maxAttempts) {
                        retry(this);
                    } else {
                        callback.onResponse(call, response);
                    }
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    ALog.e("RetryHelper", "failed, current attempts:" + attempts, t);
                    if (attempts < maxAttempts) {
                        retry(this);
                    } else {
                        callback.onFailure(call, t);
                    }
                }
            });
        }

        /**
         * call only on a new thread, different from the main thread.
         */
        public Response<T> execute() throws Exception {
            Exception ex = null;
            Response<T> response = null;
            while (attempts++ < maxAttempts) {
                try {
                    response = call.execute();
                    if (response.isSuccessful()) {
                        return response;
                    }
                } catch (Exception e) {
                    ex = e;
                }
                try {
                    long delay = (long) (Math.pow(2, attempts) * 1000);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (response != null) {
                return response;
            }

            if (ex != null) {
                ALog.e("RetryHelper", String.format("error while executing Api call:%s, error:%s", call, ex), ex);
                throw ex;
            }

            throw new Exception("error while executing api");
        }

        private void retry(Callback<T> callback) {
            // Exponential backoff with increasing delay
            ALog.i("RetryHelper", "retry with attempt count:" + attempts);
            long delay = (long) (Math.pow(2, attempts) * 1000);
            handler.postDelayed(() -> call.clone().enqueue(callback), delay);
            attempts++;
        }
    }
}
