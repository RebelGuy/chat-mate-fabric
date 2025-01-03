package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

public class AddUserRankResponse extends ApiResponseBase<AddUserRankResponse.AddUserRankResponseData> {
  public static class AddUserRankResponseData {
    public PublicUserRank newRank;
  }
}
