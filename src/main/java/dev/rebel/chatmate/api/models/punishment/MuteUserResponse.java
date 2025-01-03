package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.MuteUserResponse.MuteUserResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

public class MuteUserResponse extends ApiResponseBase<MuteUserResponseData> {
  public static class MuteUserResponseData {
    public PublicUserRank newPunishment;
  }
}
