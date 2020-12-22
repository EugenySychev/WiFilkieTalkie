package com.sychev.wifilkietalkie.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.setTitle(mList.get(position).getUserName());
        holder.setOnlineState(mList.get(position).isOnline());
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

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.contact_title_view);
            mTitleView.setOnClickListener(this);
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
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
