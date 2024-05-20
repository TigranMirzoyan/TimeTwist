package com.timetwist.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timetwist.databinding.FragmentShowNotVerifiedEventsBinding;
import com.timetwist.firebase.FirestoreServices;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class ShowNotVerifiedEventsFragment extends
        RecyclerView.Adapter<ShowNotVerifiedEventsFragment.MyViewHolder> {
    private final List<Event> mEventList;
    private final Context mContext;

    public ShowNotVerifiedEventsFragment(Context context, List<Event> eventList) {
        mEventList = eventList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        FragmentShowNotVerifiedEventsBinding binding = FragmentShowNotVerifiedEventsBinding.inflate(inflater, parent, false);
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

        holder.mBinding.acceptButton.setOnClickListener(v -> FirestoreServices.getInstance().acceptEvent(event.getId(),
                success -> {
                    mEventList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mEventList.size());
                }));

        holder.mBinding.rejectButton.setOnClickListener(v -> FirestoreServices.getInstance().rejectEvent(event.getId(),
                success -> {
                    mEventList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mEventList.size());
                }));


        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        holder.mBinding.timeOfEvent.setText(dateFormat.format(event.getDateTime()));


    }

    @Override
    public int getItemCount() {
        return Math.min(mEventList.size(), 10);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        FragmentShowNotVerifiedEventsBinding mBinding;

        public MyViewHolder(FragmentShowNotVerifiedEventsBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}
