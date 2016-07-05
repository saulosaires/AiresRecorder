package com.airesrecorder;

import android.os.Environment;

import java.io.File;

/**
 * Created by saulo on 19/11/2015.
 */
public class PreferenceUtil {

    private static final byte RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 8000;

    public static String getStorageDir(){

        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();

    }

    public static int getSampleRate(){

        return RECORDER_SAMPLERATE;

    }

    public static byte getBPP(){

        return RECORDER_BPP;

    }

    public static String getChannelConfig(){

        return "STEREO";

    }
}
