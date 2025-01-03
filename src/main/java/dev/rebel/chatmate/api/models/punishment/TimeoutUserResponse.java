package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.TimeoutUserResponse.TimeoutUserResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

import org.jetbrains.annotations.Nullable;

public class TimeoutUserResponse extends ApiResponseBase<TimeoutUserResponseData> {
  public static class TimeoutUserResponseData {
    public @Nullable PublicUserRank newPunishment;
    public @Nullable String newPunishmentError;
    public PublicChannelRankChange[] channelPunishments;
  }
}
