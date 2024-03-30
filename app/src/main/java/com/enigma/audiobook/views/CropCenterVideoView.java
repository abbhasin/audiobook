package com.enigma.audiobook.views;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.VideoView;

public class CropCenterVideoView extends VideoView {

    public CropCenterVideoView(Context context) {
        super(context);
    }

    public CropCenterVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CropCenterVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Calculate the video aspect ratio
        int videoWidth = getMeasuredWidth();
        int videoHeight = getMeasuredHeight();
        float videoAspectRatio = (float) videoWidth / (float) videoHeight;

        // Calculate the view's aspect ratio
        int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        float viewAspectRatio = (float) viewWidth / (float) viewHeight;

        // Calculate scaling factors for width and height
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        if (videoAspectRatio > viewAspectRatio) {
            // Video is wider than the view
            scaleX = videoAspectRatio / viewAspectRatio;
        } else {
            // Video is taller than or equal to the view
            scaleY = viewAspectRatio / videoAspectRatio;
        }

        // Apply the scaling factors to center-crop the video
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, videoWidth * 0.5f, videoHeight * 0.5f);
            super.transformMatrixToGlobal(matrix);
    }
}

