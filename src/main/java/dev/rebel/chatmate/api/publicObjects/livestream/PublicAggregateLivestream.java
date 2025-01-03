package dev.rebel.chatmate.api.publicObjects.livestream;

import org.jetbrains.annotations.Nullable;

public class PublicAggregateLivestream {
  public Long startTime;
  public @Nullable Long endTime;
  public PublicLivestream[] livestreams;
}
