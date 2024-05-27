package com.timetwist.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timetwist.databinding.FragmentShowEventBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ToastUtils;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class ShowEventFragment extends RecyclerView.Adapter<ShowEventFragment.MyViewHolder> {
    private final List<Event> mEventList;
    private final Context mContext;
    private int mEventState;

    public ShowEventFragment(Context context, List<Event> eventList, int eventState) {
        mEventList = eventList;
        this.mContext = context;
        this.mEventState = eventState;
    }

    public void updateEventState(int eventState) {
        mEventState = eventState;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        FragmentShowEventBinding binding = FragmentShowEventBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Event event = mEventList.get(position);
        holder.mBinding.title.setText(event.getName());
        holder.mBinding.username.setText(event.getUsername());
        holder.mBinding.description.setText(event.getDescription());
        holder.mBinding.people.setText("Number of companions: " + event.getJoinedPeople()
                + "/" + event.getMaxPeople());
        holder.mBinding.email.setText(event.getEmail());
        holder.mBinding.status.setText("Status: " + event.getStatus());

        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        holder.mBinding.time.setText(dateFormat.format(event.getDateTime()));

        configureVisibility(holder);
        configureButtons(holder, position);
    }

    @Override
    public int getItemCount() {
        return mEventList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        FragmentShowEventBinding mBinding;

        public MyViewHolder(FragmentShowEventBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

    private void configureButtons(MyViewHolder holder, int position) {

        Event event = mEventList.get(position);
        holder.mBinding.delete.setOnClickListener(v ->
                FirestoreServices.getInstance()
                        .deleteEvent(event.getId(),
                                success -> {
                                    mEventList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, mEventList.size());
                                }, error -> ToastUtils.show(mContext, error)));

        holder.mBinding.join.setOnClickListener(v ->
                FirestoreServices.getInstance()
                        .joinEvent(event.getId(),
                                success -> {
                                    mEventList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, mEventList.size());
                                }));

        holder.mBinding.leave.setOnClickListener(v ->
                FirestoreServices.getInstance()
                        .leaveEvent(event.getId(),
                                success -> {
                                    mEventList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, mEventList.size());
                                }, error -> ToastUtils.show(mContext, error)));

    }

    private void configureVisibility(MyViewHolder holder) {
        holder.mBinding.join.setVisibility(mEventState == 0 ? View.VISIBLE : View.GONE);
        holder.mBinding.email.setVisibility(mEventState == 0 ? View.GONE : View.VISIBLE);
        holder.mBinding.delete.setVisibility(mEventState == 1 ? View.VISIBLE : View.GONE);
        holder.mBinding.status.setVisibility(mEventState == 1 ? View.VISIBLE : View.GONE);
        holder.mBinding.email.setVisibility(mEventState == 1 ? View.GONE : View.VISIBLE);
        holder.mBinding.username.setVisibility(mEventState == 1 ? View.GONE : View.VISIBLE);
        holder.mBinding.leave.setVisibility(mEventState == 2 ? View.VISIBLE : View.GONE);
        holder.mBinding.email.setVisibility(mEventState == 2 ? View.VISIBLE : View.GONE);
    }
}