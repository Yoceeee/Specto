package com.example.tvandmovies.UI.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.tvandmovies.R;
import com.example.tvandmovies.model.entities.CastMember;

import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {
    private final List<CastMember> castList;

    public CastAdapter(List<CastMember> castList) {
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        CastMember castMember = castList.get(position);

        holder.name.setText(castMember.getName());

        if (castMember.getProfileUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(castMember.getProfileUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.image);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.account_circle)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(castList.size(), 15);
    }

    static class CastViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.castProfileImage);
            name = itemView.findViewById(R.id.castNameText);
        }
    }
}
