package com.airesrecorder;


import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerFragment extends Fragment implements MediaPlayer.OnPreparedListener,
                                                        MediaPlayer.OnCompletionListener,
                                                        View.OnClickListener {

    private SeekBar seekBar;
    private TextView played,left;
    private ImageButton rewind,playPause,forwad;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager manager;
    private CallBack callBack;

    private MediaPlayer mediaPlayer;

    private int recordPlayed=0;
    List<Track> listTrack;
    Handler mHandler = new Handler();
    Runnable updatePlay;


    public static PlayerFragment newInstance() {

        PlayerFragment fragment = new PlayerFragment();


        return fragment;
    }

    public PlayerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();


    }


    public void init(){

        initCallBack();

        File[] listFile  =  Util.getMusicStorageDir(getString(R.string.storageDir)).listFiles();


        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        listTrack=null;
        listTrack= new ArrayList<Track>();
        for(File f:listFile){

            try {
                String name = f.getName();
                MediaPlayer mp = MediaPlayer.create(getActivity(), Uri.parse(f.getAbsolutePath()));
                String duration =  Util.humanReadableTime((long)mp.getDuration());
                String date = sdf.format(new Date(f.lastModified()));
                String size= Util.humanReadableByteCount(f.length(),true);
                mp=null;

                listTrack.add(new Track(name,duration, size,date,f.getAbsolutePath()));
            }catch(Exception e){
                e.printStackTrace();

            }
        }




        seekBar=(SeekBar) getView().findViewById(R.id.seekBar);
        played =(TextView)getView().findViewById(R.id.played);
        left   =(TextView)getView().findViewById(R.id.left);

        rewind    =(ImageButton)getView().findViewById(R.id.rewind);
        playPause =(ImageButton)getView().findViewById(R.id.play_pause);
        forwad    =(ImageButton)getView().findViewById(R.id.forward);

        rewind.setOnClickListener(this);
        playPause.setOnClickListener(this);
        forwad.setOnClickListener(this);



        mRecyclerView = (RecyclerView)getView().findViewById(R.id.recycler_view);
        manager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(new RecordAdapter(getActivity(), callBack, listTrack));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(fromUser) seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        updatePlay = new Runnable() {

            @Override
            public void run() {

                played.setText(Util.humanReadableTime(getCurrentPosition()));
                left.setText(Util.humanReadableTime(getDuration()- getCurrentPosition()));

                seekBar.setProgress(getCurrentPosition());

                if(isPlaying())
                    mHandler.postDelayed(this,300);

            }
        };

    }

    public void initCallBack(){

        callBack = new CallBack() {

            @Override
            public void onClickMore(final int index) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.option)
                        .setItems(R.array.record_lonpress, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {

                                    case 0:
                                        showDialogRename(listTrack.get(index).getPath());
                                        break;
                                    case 1:
                                        showDialogDelete(listTrack.get(index).getPath());
                                        break;

                                }

                            }
                        });

                builder.create().show();

            }

            @Override
            public void play(int index) {

                try {

                    String path="";
                    if(listTrack!=null && index>=0 && index<listTrack.size()){

                        path=listTrack.get(index).getPath();

                    }else{
                        return;
                    }

                    if (mediaPlayer!=null){
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }



                    mediaPlayer=null;
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnPreparedListener(PlayerFragment.this);
                    mediaPlayer.setOnCompletionListener(PlayerFragment.this);
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();

                    startPlay();

                } catch (IOException e) {
                    //Log.e(TAG, "Could not open file " + audioFile + " for playback.", e);
                    e.printStackTrace();
                }

            }
        };

    }

    private void showDialogDelete(final String path){


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder
                .setMessage(R.string.sure_delete)
                .setPositiveButton(R.string.yes,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        new File(path).delete();
                        init();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .show();

    }

    private void showDialogRename(final String path){

        Context ctx = getActivity();

        final File file = new File(path);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);
        alertDialog.setTitle(R.string.new_name);

        final EditText input = new EditText(ctx);
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

                    Snackbar.make(mRecyclerView, getResources().getText(R.string.record_name_null), Snackbar.LENGTH_LONG).show();

                } else {

                    try {

                        file.renameTo(new File(file.getParent() + File.separator + newName));

                        Snackbar.make(mRecyclerView,
                                      getResources().getText(R.string.record_name_updated),
                                      Snackbar.LENGTH_LONG).show();

                        init();

                    } catch (Exception e) {
                        e.printStackTrace();

                        Snackbar.make(mRecyclerView,
                                      getResources().getText(R.string.record_name_update_fail),
                                      Snackbar.LENGTH_LONG).show();
                    }


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

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void onPrepared(MediaPlayer mediaPlayer) {

        seekBar.setMax(mediaPlayer.getDuration());

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mHandler.removeCallbacks(updatePlay);
        mp.stop();
        mp.reset();


        playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.rewind:     doRewind();   break;
            case R.id.play_pause: doPlayPause();break;
            case R.id.forward:    doForward();  break;
        }
    }



    public void doPlayPause(){

        if(mediaPlayer!=null  && mediaPlayer.isPlaying()){
            playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);

            mediaPlayer.pause();

        }else{//if , its not playing any record, so start to play the first one or continues

            if(mediaPlayer!=null){
                startPlay();
            }else{
                callBack.play(recordPlayed);
            }



        }


    }

    public void doForward(){

        if(listTrack!=null){

            int size= listTrack.size();

            callBack.play((++recordPlayed)%size);

        }

    }

    public void doRewind(){

        if(listTrack!=null){

            int size= listTrack.size();

            callBack.play((--recordPlayed)%size);

        }
    }

    public void startPlay(){

        mediaPlayer.start();
        mHandler.post(updatePlay);
        playPause.setImageResource(R.drawable.ic_pause_white_48dp);


    }

    public interface CallBack{

        public void onClickMore(int index);
        public void play(int index);

    }
}
