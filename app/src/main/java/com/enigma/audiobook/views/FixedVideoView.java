package com.enigma.audiobook.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.enigma.audiobook.utils.ALog;

public class FixedVideoView extends VideoView {
    private static final String TAG = "FixedVideoView";

    private int mVideoWidth = 200;
    private int mVideoHeight = 300;

    public FixedVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FixedVideoView(Context context) {
        super(context);
    }

    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mVideoWidth = (mVideoWidth > 0) ? mVideoWidth : widthMeasureSpec;
        mVideoHeight = (mVideoHeight > 0) ? mVideoHeight : heightMeasureSpec;
        ALog.i(TAG, String.format("provided width:%s and height:%s", widthMeasureSpec, heightMeasureSpec));
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        /*if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (mVideoWidth * height > width * mVideoHeight) {
                // Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            } else if (mVideoWidth * height < width * mVideoHeight) {
                // Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else {
                // Log.i("@@@", "aspect ratio is correct: " +
                // width+"/"+height+"="+
                // mVideoWidth+"/"+mVideoHeight);
            }
        }*/

        ALog.i(TAG, "setting video view width:" + width + ", height:" + height);
        setMeasuredDimension(width, height);
    }
}

