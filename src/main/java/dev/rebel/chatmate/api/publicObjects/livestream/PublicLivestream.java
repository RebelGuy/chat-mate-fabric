package dev.rebel.chatmate.api.publicObjects.livestream;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

public class PublicLivestream {
  public Integer id;
  public Platform platform;
  public String livestreamLink;
  public LivestreamStatus status;
  public @Nullable Long startTime;
  public @Nullable Long endTime;

  public enum LivestreamStatus {
    @SerializedName("not_started") NotStarted,
    @SerializedName("live") Live,
    @SerializedName("finished") Finished,
  }

  public enum Platform {
    @SerializedName("youtube") Youtube,
    @SerializedName("twitch") Twitch
  }
}
