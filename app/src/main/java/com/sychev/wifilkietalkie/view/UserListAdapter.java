package com.sychev.wifilkietalkie.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sychev.wifilkietalkie.R;
import com.sychev.wifilkietalkie.data.UserItem;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
    private final List<UserItem> mList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public UserListAdapter(List<UserItem> mList, Context context) {
        this.mList = mList;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_userlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull UserListAdapter.ViewHolder holder, int position) {
        UserItem item = mList.get(position);
        if (item != null) {
            holder.setTitle(item.getUserName());
            holder.setOnlineState(item.isOnline());
            holder.setActionIcon(item.getActionState());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public UserItem getItem(int position) {
        return mList.get(position);
    }

    public void setOnClickListener(ItemClickListener clickListener) {
        mClickListener = clickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTitleView;
        private final ImageView mImageView;
//        private final SwitchCompat mSwitch;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.contact_title_view);
            mImageView = itemView.findViewById(R.id.contact_action_icon);
//            mSwitch = itemView.findViewById(R.id.contact_switch_active);
//            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (mClickListener != null)
//                        mClickListener.setActive(getAdapterPosition(), isChecked);
//                }
//            });
            itemView.setOnClickListener(this);
        }

        public void setTitle(String name) {
            mTitleView.setText(name);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onItemClick(v, getAdapterPosition());
        }

        public void setOnlineState(boolean online) {
            mTitleView.setTypeface(null, online ? Typeface.BOLD : Typeface.ITALIC);
            mTitleView.setTextColor(online ? Color.BLACK : Color.LTGRAY);
        }

        public void setActionIcon(UserItem.ActionState actionState) {
            int sourceIcon = 0;
            if (mImageView == null || actionState == null)
                return;
            switch (actionState) {
                case LISTEN:
                    sourceIcon = R.drawable.ic_baseline_record_voice_over_24;
                    break;
//                case TALK:
//                    sourceIcon = R.drawable.ic_baseline_mic_24;
//                    break;
                default:
                    sourceIcon = 0;
            }

            if (sourceIcon == 0)
                mImageView.setVisibility(View.GONE);
            else {
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageResource(sourceIcon);
            }
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void setActive(int position, boolean isChecked);
    }
}
