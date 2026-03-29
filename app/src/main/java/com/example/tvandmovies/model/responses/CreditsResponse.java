package com.example.tvandmovies.model.responses;

import com.example.tvandmovies.model.entities.CastMember;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreditsResponse {
    @SerializedName("cast")
    private List<CastMember> cast;

    public List<CastMember> getCast() { return cast; }
}
