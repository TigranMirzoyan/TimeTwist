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
    private boolean mIsChangeEventsClicked;

    public ShowEventFragment(Context context, List<Event> eventList, boolean isChangeEventsClicked) {
        mEventList = eventList;
        this.mContext = context;
        this.mIsChangeEventsClicked = isChangeEventsClicked;
    }

    public void updateIsChangeEventsClicked(boolean isChangeEventsClicked) {
        mIsChangeEventsClicked = isChangeEventsClicked;
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
        holder.mBinding.userName.setText(event.getUsername());
        holder.mBinding.description.setText(event.getDescription());
        holder.mBinding.people.setText("Number of companions: " + event.getPeople());
        holder.mBinding.contacts.setText(event.getContacts());

        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        holder.mBinding.timeOfEvent.setText(dateFormat.format(event.getDateTime()));
        holder.mBinding.delete.setVisibility(mIsChangeEventsClicked ? View.VISIBLE : View.GONE);

        holder.mBinding.delete.setOnClickListener(v -> FirestoreServices.getInstance().deleteEvent(event.getId(),
                success -> {
                    mEventList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mEventList.size());
                }, error -> ToastUtils.show(mContext, error)));

        holder.mBinding.joinEvent.setOnClickListener(v -> FirestoreServices.getInstance().deleteEvent(event.getId(),
                success -> {
                    mEventList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mEventList.size());
                }, error -> ToastUtils.show(mContext, error)));
    }

    @Override
    public int getItemCount() {
        return Math.min(mEventList.size(), 10);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        FragmentShowEventBinding mBinding;

        public MyViewHolder(FragmentShowEventBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}
