package com.enigma.audiobook.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

public class ContentUtils {
    private static final String TAG = "ContentUtils";


    public static long getFileSizeApproxOfContentUri(Context context, Uri uri) {
        long size = -1;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            try (ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")) {
                if (parcelFileDescriptor != null) {
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    try (FileInputStream inputStream = new FileInputStream(fileDescriptor)) {
                        size = inputStream.available(); // Get the size of the input stream
                    }
                }
            }
            return size;
        } catch (IOException e) {
            ALog.e(TAG, "unable to get file size for uri:" + uri, e);
            throw new RuntimeException("unable to get file size for uri:" + uri, e);
        }
    }

    public static long getFileSizeOfContentUri(Context context, Uri uri) {
        long size = -1;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.MediaColumns.SIZE}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
                    size = cursor.getLong(sizeIndex);
                }
            } finally {
                cursor.close();
            }
        }
        ALog.i(TAG, "zzz size of content:" + uri + "  is:" + size);
        return size;
    }

    public static long getFileSize(Context context, Uri uri) {
        ALog.i(TAG, "uri is:" + uri);
        ContentSchemeType type = getContentSchemeType(uri.toString());
        switch (type) {
            case CONTENT:
                return getFileSizeOfContentUri(context, uri);
            case FILE:
                return new File(uri.toString()).length();
            default:
                throw new IllegalStateException("unsupported content type:" + type);
        }
    }

    public static ContentSchemeType getContentSchemeType(String fileUri) {
        Uri uri = Uri.parse(fileUri);
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return ContentSchemeType.CONTENT;
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return ContentSchemeType.FILE;
        }
        throw new UnsupportedOperationException("unsupported scheme type");
    }

    public static String getFileName(Context context, Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex);
                } else {
                    return uri.getLastPathSegment();
                }
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    public enum ContentSchemeType {
        CONTENT,
        FILE

    }
}
