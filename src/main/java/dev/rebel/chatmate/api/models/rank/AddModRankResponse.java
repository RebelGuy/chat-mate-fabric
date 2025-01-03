package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

import org.jetbrains.annotations.Nullable;

public class AddModRankResponse extends ApiResponseBase<AddModRankResponse.AddModRankResponseData> {
  public static class AddModRankResponseData {
    public @Nullable PublicUserRank newRank;
    public @Nullable String newRankError;
    public PublicChannelRankChange[] channelModChanges;
  }
}
