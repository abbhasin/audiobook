package com.enigma.audiobook;

import android.content.Context;
import android.net.Uri;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.File;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.enigma.audiobook", appContext.getPackageName());
        Uri uri = Uri.parse("file://media/picker/0/com.android.providers.media.photopicker/media/1000000018");
        System.out.println("uri:"+uri.toString());
        File f = new File(uri.toString());
        System.out.println("file:"+f.toString());
        System.out.println("file name:"+f.getName());
    }
}