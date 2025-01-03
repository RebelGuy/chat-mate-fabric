package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.UnbanUserResponse.UnbanUserResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

import org.jetbrains.annotations.Nullable;

public class UnbanUserResponse extends ApiResponseBase<UnbanUserResponseData> {
  public static class UnbanUserResponseData {
    public @Nullable PublicUserRank removedPunishment;
    public @Nullable String removedPunishmentError;
    public PublicChannelRankChange[] channelPunishments;
  }
}
