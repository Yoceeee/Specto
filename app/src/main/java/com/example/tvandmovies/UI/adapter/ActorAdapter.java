package com.example.tvandmovies.UI.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tvandmovies.R;
import com.example.tvandmovies.model.entities.CastMember;
import java.util.ArrayList;
import java.util.List;

public class ActorAdapter extends RecyclerView.Adapter<ActorAdapter.ActorViewHolder> {

    private List<CastMember> actors = new ArrayList<>();
    private final OnActorClickListener listener;

    public interface OnActorClickListener {
        void onActorClick(CastMember actor);
    }

    public ActorAdapter(OnActorClickListener listener) {
        this.listener = listener;
    }

    public void setActors(List<CastMember> actors) {
        this.actors = actors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actor, parent, false);
        return new ActorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActorViewHolder holder, int position) {
        CastMember actor = actors.get(position);
        holder.actorName.setText(actor.getName());
        
        Glide.with(holder.itemView.getContext())
                .load(actor.getProfileUrl())
                .placeholder(R.drawable.user_icon)
                .circleCrop()
                .into(holder.actorImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActorClick(actor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return actors != null ? actors.size() : 0;
    }

    static class ActorViewHolder extends RecyclerView.ViewHolder {
        ImageView actorImage;
        TextView actorName;

        public ActorViewHolder(@NonNull View itemView) {
            super(itemView);
            actorImage = itemView.findViewById(R.id.actorImage);
            actorName = itemView.findViewById(R.id.actorName);
        }
    }
}