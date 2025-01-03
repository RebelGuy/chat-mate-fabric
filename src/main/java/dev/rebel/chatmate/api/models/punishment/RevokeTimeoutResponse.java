package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.RevokeTimeoutResponse.RevokeTimeoutResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

import org.jetbrains.annotations.Nullable;

public class RevokeTimeoutResponse extends ApiResponseBase<RevokeTimeoutResponseData> {
  public static class RevokeTimeoutResponseData {
    public @Nullable PublicUserRank removedPunishment;
    public @Nullable String removedPunishmentError;
    public PublicChannelRankChange[] channelPunishments;
  }
}
