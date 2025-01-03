package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

import org.jetbrains.annotations.Nullable;

public class RemoveModRankResponse extends ApiResponseBase<RemoveModRankResponse.RemoveModRankResponseData> {
  public static class RemoveModRankResponseData {
    public @Nullable PublicUserRank removedRank;
    public @Nullable String removedRankError;
    public PublicChannelRankChange[] channelModChanges;
  }
}
