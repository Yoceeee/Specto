package com.example.tvandmovies.model.responses;

import com.example.tvandmovies.model.entities.CastMember;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ActorResponse {
    @SerializedName("results")
    private List<CastMember> results;

    public List<CastMember> getResults() {
        return results;
    }
}