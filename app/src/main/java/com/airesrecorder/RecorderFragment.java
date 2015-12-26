package com.airesrecorder;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecorderFragment extends Fragment implements View.OnClickListener{

    private static boolean isplaying=false;

    private String name;

    private static int intHour=0;
    private static int intMin=0;
    private static int intSec=0;

    private TextView hour, min, sec,recordName,space;

    private RelativeLayout containerRecord;
    private ImageButton recordPause;
    private TextView recordPauseTxt;

    private RelativeLayout containerDone;
    private ImageButton done;

    MainActivity.CallBack callBack;

    private long freeSpace=-1;
    private long totalBytes=-1;

    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(action.equals(RecorderIntentService.BYTES_RECORDED)){

                 totalBytes = intent.getLongExtra("DATA", -1);
                 updateSpaceLabel(totalBytes,freeSpace);

            }else if(action.equals(RecorderIntentService.ACTION_STOP)){

                showSaveDialog(intent.getStringExtra("DATA"));

            }else if(action.equals(RecorderIntentService.ACTION_PAUSE)){

                callBack.refresh(1);

            }

        }

    }


    public void setCallBack(MainActivity.CallBack callBack){

        this.callBack=callBack;
    }

    public static RecorderFragment newInstance(MainActivity.CallBack callBack) {

        RecorderFragment fragment = new RecorderFragment();
        fragment.setCallBack(callBack);
        return fragment;
    }

    public RecorderFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_recorder, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recordName      = (TextView)view.findViewById(R.id.name);
        space           = (TextView)view.findViewById(R.id.space);
        hour            = (TextView)view.findViewById(R.id.hour);
        min             = (TextView)view.findViewById(R.id.min);
        sec             = (TextView)view.findViewById(R.id.sec);
        recordPause     = (ImageButton)view.findViewById(R.id.record_pause);
        recordPauseTxt  = (TextView)view.findViewById(R.id.record_pause_txt);
        containerRecord = (RelativeLayout)view.findViewById(R.id.container_record_pause);
        done            = (ImageButton)view.findViewById(R.id.done);
        containerDone   = (RelativeLayout)view.findViewById(R.id.container_done);

        containerDone.setOnClickListener(this);
        done.setOnClickListener(this);
        containerRecord.setOnClickListener(this);
        recordPause.setOnClickListener(this);

        updateName();


        freeSpace = new File(PreferenceUtil.getStorageDir()).getFreeSpace();
        updateSpaceLabel(totalBytes, freeSpace);

        initReceiver();

    }

    public void startRecorder(){

        RecorderIntentService.startActionStart(getActivity(), name);

    }

    public void pauseRecorder(){

        RecorderIntentService.startActionPause(getActivity());

    }

    public void stopRecorder(){

        /*
        ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(getString(R.string.loading));
        progress.setMessage(getString(R.string.saving_track));
        progress.show();
        progress.dismiss();
        */

        RecorderIntentService.startActionStop(getActivity());



        intHour=intMin=intSec=0;

        updateName();
        updateCounter();
        resetPlay();

    }

    private void recordPauseClick(){

        if (isplaying){

            recordPause.setImageResource(R.drawable.ic_mic_white_48dp);
            recordPauseTxt.setText(R.string.record);
            isplaying = false;
            pauseRecorder();

        } else{

            recordPause.setImageResource(R.drawable.ic_pause_white_48dp);
            recordPauseTxt.setText(R.string.pause);
            isplaying = true;
            initCounter();
            startRecorder();

        }

    }

    private void doneClick(){
        isplaying = false;
        stopRecorder();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.record_pause:
            case R.id.container_record_pause:
                recordPauseClick();
            break;

            case R.id.done:
            case R.id.container_done:
                doneClick();
            break;

        }

    }

   public void updateSpaceLabel(long totalBytes,long freeSpace){

       StringBuffer label=new StringBuffer("");

       if(totalBytes>=0){
           label.append("Recorded:").append(Util.humanReadableByteCount(totalBytes,true)).append(" ");
       }

       if(freeSpace>=0){
           label.append("Free:").append(Util.humanReadableByteCount(freeSpace,true)).append(" ");
       }

       space.setText(label.toString());
   }

   public void updateName(){

       File[] listFile  =  Util.getMusicStorageDir(getString(R.string.storageDir)).listFiles();

       name="Recorder_#"+(listFile.length+1)+".mp3";
       recordName.setText(name);

   }


   public void initReceiver(){

       IntentFilter intentFilter = new IntentFilter();
       intentFilter.addAction(RecorderIntentService.BYTES_RECORDED);
       intentFilter.addAction(RecorderIntentService.ACTION_STOP);
       intentFilter.addAction(RecorderIntentService.ACTION_PAUSE);
       getActivity().registerReceiver(new ServiceReceiver(), intentFilter);

   }

   public void updateCounter(){

        hour.setText(intHour <= 9 ? ("0" + intHour) : (intHour + ""));
        min.setText(intMin <= 9 ? ("0" + intMin) : (intMin + ""));
        sec.setText(intSec <= 9 ? ("0" + intSec) : (intSec + ""));

   }

   public void resetPlay(){

       recordPause.setImageResource(R.drawable.ic_mic_white_48dp);
       recordPauseTxt.setText(R.string.record);
       isplaying = false;

   }

   public void initCounter(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    while(isplaying) {

                        intSec++;

                        if (intSec >= 59) {
                            intSec = 0;
                            intMin++;
                        }

                        if (intMin >= 59) {
                            intMin = 0;
                            intHour++;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                updateCounter();
                            }
                        });


                        Thread.sleep(1000);

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

   }

   private void showSaveDialog(final String path){

       if(path==null || "".equals(path)) {

           Snackbar.make(getView(), getResources().getText(R.string.path_null), Snackbar.LENGTH_LONG).show();
            return;
       }

       final File file = new File(path);

       AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
       alertDialog.setTitle(R.string.new_name);

       final EditText input = new EditText(getActivity());
       input.setText(file.getName());

       LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
               LinearLayout.LayoutParams.MATCH_PARENT,
               LinearLayout.LayoutParams.MATCH_PARENT
       );

       input.setLayoutParams(lp);
       alertDialog.setView(input);

       alertDialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

           public void onClick(DialogInterface dialog, int which) {

               String newName = input.getText().toString();

               if (newName == null || "".equals(newName)) {

                   Snackbar.make(getView(), getResources().getText(R.string.name_null), Snackbar.LENGTH_LONG).show();

               } else {

                   String newPath = file.getParent() + File.separator + newName;

                   File newFile = new File(newPath);


                   file.renameTo(newFile);
                   callBack.refresh(1);
                   callBack.moveTo(1);

                   Snackbar.make(getView(), getResources().getText(R.string.file_saved), Snackbar.LENGTH_LONG).show();


               }

           }
       });

       alertDialog.setNegativeButton(R.string.getout,
               new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                   }
               });



       alertDialog.show();

   }

}
