package com.airesrecorder;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;


public class RecorderIntentService extends IntentService {

    public static final String ACTION_STOP = "com.airesrecorder.action.STOP";
    public static final String ACTION_PAUSE = "com.airesrecorder.action.PAUSE";
    public static final String ACTION_START = "com.airesrecorder.action.START";

    private static final String EXTRA_NAME = "com.airesrecorder.extra.EXTRA_NAME";

    public static final String BYTES_RECORDED ="com.airesrecorder.action.BYTES_RECORDED";

    private static  byte RECORDER_BPP ;
    private static  int RECORDER_SAMPLERATE ;
    private static  int bufferSize=0;
    private static  long nBytes=0;

    private static File myFile=null;

    private static AudioRecord audioRecord;

    private static  boolean isplaying=false;

    public static void startActionStop(Context context) {

        Intent intent = new Intent(context, RecorderIntentService.class);
        intent.setAction(ACTION_STOP);

        context.startService(intent);
    }

    public static void startActionPause(Context context) {

        Intent intent = new Intent(context, RecorderIntentService.class);
        intent.setAction(ACTION_PAUSE);

        context.startService(intent);
    }

    public static void startActionStart(Context context,String name) {

        Intent intent = new Intent(context, RecorderIntentService.class);

        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_NAME, name);

        context.startService(intent);
    }

    public RecorderIntentService() {super("RecorderIntentService");}

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {

            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {

                final String name = intent.getStringExtra(EXTRA_NAME);

                handleActionStart(name);

            } else if (ACTION_PAUSE.equals(action)) {

                 handleActionPause();

            }else if (ACTION_STOP.equals(action)) {

                handleActionStop();

            }

        }
    }


    private void handleActionStart(final String name) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                FileOutputStream outputStream=null;
              try {

                  initRecorder(name);

                  audioRecord.startRecording();



                  outputStream = new FileOutputStream(myFile,true);

                  byte[] buffer = new byte[bufferSize];
                  isplaying = true;
                  nBytes=0;
                  double gain =1.8;

                  while (isplaying) {

                      int numRead= audioRecord.read(buffer, 0, buffer.length);

                      nBytes+=numRead;

                      if (numRead > 0) {

                          for (int i = 0; i < numRead; ++i)
                              buffer[i] = (byte)Math.min((int)(buffer[i] * gain), (int)Short.MAX_VALUE);
                      }

                      outputStream.write(buffer);

                      sendMessenge(BYTES_RECORDED,nBytes);

                  }

              } catch (FileNotFoundException e) {
                  e.printStackTrace();
              } catch (IOException e) {
                  e.printStackTrace();
              }finally {

                  if (outputStream != null) {

                      try {
                          outputStream.flush();
                          outputStream.close();
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  }

              }

          }
      }).start();

    }

    private void handleActionPause() {

        isplaying=false;
        audioRecord.stop();
        if(myFile!= null)
        WriteWaveFileHeader(myFile);
        sendMessenge(ACTION_PAUSE, -1);
    }

    private void handleActionStop() {

        isplaying = false;
        nBytes=0;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

        if (myFile != null) {

            WriteWaveFileHeader(myFile);

            sendMessenge(BYTES_RECORDED, -1);
            sendMessenge(ACTION_STOP, myFile.getAbsolutePath());
            myFile = null;
        }
    }

    private void initRecorder(String name) throws IOException {

        if(myFile==null || !myFile.exists()){
           myFile = new File( Util.getMusicStorageDir(getString(R.string.storageDir)), File.separator + name);
           myFile.createNewFile();

            WriteWaveFileHeader(new FileOutputStream(myFile.getAbsolutePath()));
        }



        if (audioRecord == null) {

            RECORDER_BPP        = PreferenceUtil.getBPP();
            RECORDER_SAMPLERATE = PreferenceUtil.getSampleRate();

            bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                          RECORDER_SAMPLERATE,
                                          AudioFormat.CHANNEL_IN_STEREO,
                                          AudioFormat.ENCODING_PCM_16BIT,
                                          bufferSize*10);

        }

    }

    private void WriteWaveFileHeader(File track) {

        try {

            RandomAccessFile out= new RandomAccessFile(track, "rw");
            long totalAudioLen =  out.getChannel().size();
            long totalDataLen = totalAudioLen + 44;
            long longSampleRate = RECORDER_SAMPLERATE;
            int channels = 2;
            long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

            byte[] header = new byte[44];

            header[0] = 'R';  // RIFF/WAVE header
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';  // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1;  // format = 1
            header[21] = 0;
            header[22] = (byte) channels;
            header[23] = 0;
            header[24] = (byte) (longSampleRate & 0xff);
            header[25] = (byte) ((longSampleRate >> 8) & 0xff);
            header[26] = (byte) ((longSampleRate >> 16) & 0xff);
            header[27] = (byte) ((longSampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) (2 * 16 / 8);  // block align
            header[33] = 0;
            header[34] = RECORDER_BPP;  // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

            out.seek(0);
            out.write(header);
            out.close();
            out=null;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (0 & 0xff);
        header[5] = (byte) ((0 >> 8) & 0xff);
        header[6] = (byte) ((0 >> 16) & 0xff);
        header[7] = (byte) ((0 >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) 0;
        header[23] = 0;
        header[24] = (byte) (0 & 0xff);
        header[25] = (byte) ((0 >> 8) & 0xff);
        header[26] = (byte) ((0 >> 16) & 0xff);
        header[27] = (byte) ((0 >> 24) & 0xff);
        header[28] = (byte) (0 & 0xff);
        header[29] = (byte) ((0 >> 8) & 0xff);
        header[30] = (byte) ((0 >> 16) & 0xff);
        header[31] = (byte) ((0 >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (0 & 0xff);
        header[41] = (byte) ((0 >> 8) & 0xff);
        header[42] = (byte) ((0 >> 16) & 0xff);
        header[43] = (byte) ((0 >> 24) & 0xff);

        out.write(header, 0, 44);
        out.close();
        out=null;
    }

    public void sendMessenge(String action, long data){

    Intent intent = new Intent();
    intent.setAction(action);

    intent.putExtra("DATA",data);

    sendBroadcast(intent);
   }

    public void sendMessenge(String action, String path){

        Intent intent = new Intent();
        intent.setAction(action);

        intent.putExtra("DATA",path);

        sendBroadcast(intent);
    }



}
