package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

public class GetUserRanksResponse extends ApiResponseBase<GetUserRanksResponse.GetUserRanksResponseData> {
  public static class GetUserRanksResponseData {
    public PublicUserRank[] ranks;
  }
}
