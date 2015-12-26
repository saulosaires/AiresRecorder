package com.airesrecorder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder>{

    private List<Track> listTrack ;
    private PlayerFragment.CallBack callBack;
    Context context;

    public RecordAdapter(Context context,PlayerFragment.CallBack callBack, List<Track> listTrack ) {

        super();

        this.listTrack=listTrack;
        this.context=context;
        this.callBack=callBack;


    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.track_row, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v,viewGroup.getContext());


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {

        final Track track = listTrack.get(i);

        StringBuffer sb = new StringBuffer(track.getDuration())
                              .append(" - ").append(track.getDate())
                              .append(" - ").append(track.getSize());

        viewHolder.title.setText(track.getName());
        viewHolder.subtitle.setText(sb.toString());

        viewHolder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onClickMore(i);
            }
        });

        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.play(i);
            }
        });



    }


    @Override
    public int getItemCount() {

        return listTrack.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView more;
        public TextView title;
        public TextView subtitle;
        public LinearLayout container;
        Context context;

        public ViewHolder(View itemView,Context context) {
            super(itemView);
            this.context=context;

            more       = (ImageView)itemView.findViewById(R.id.more);
            title      = (TextView)itemView.findViewById(R.id.title);
            subtitle   = (TextView)itemView.findViewById(R.id.subtitle);
            container  = (LinearLayout)itemView.findViewById(R.id.container);
        }



    }




}