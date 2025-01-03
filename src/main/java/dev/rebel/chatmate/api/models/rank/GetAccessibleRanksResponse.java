package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.models.rank.GetAccessibleRanksResponse.GetAccessibleRanksResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;

public class GetAccessibleRanksResponse extends ApiResponseBase<GetAccessibleRanksResponseData> {
  public static class GetAccessibleRanksResponseData {
    public PublicRank[] accessibleRanks;
  }
}
