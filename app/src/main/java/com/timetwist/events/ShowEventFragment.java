package com.timetwist.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timetwist.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class ShowEventFragment extends RecyclerView.Adapter<ShowEventFragment.MyViewHolder> {
    private final List<Event> mEventList;
    private final Context mContext;

    public ShowEventFragment(Context context, List<Event> eventList) {
        mEventList = eventList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.fragment_show_event, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Event event = mEventList.get(position);
        holder.mTitle.setText(event.getName());
        holder.mUsername.setText(event.getUsername());
        holder.mDescription.setText(event.getDescription());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        holder.mDateTime.setText(dateFormat.format(event.getDateTime()));
    }

    @Override
    public int getItemCount() {
        return Math.min(mEventList.size(), 10);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle, mUsername, mDescription, mDateTime;
        Button mJoinEvent;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mUsername = itemView.findViewById(R.id.userName);
            mDescription = itemView.findViewById(R.id.description);
            mJoinEvent = itemView.findViewById(R.id.makeEventButton);
            mDateTime = itemView.findViewById(R.id.timeOfEvent);
        }
    }
}