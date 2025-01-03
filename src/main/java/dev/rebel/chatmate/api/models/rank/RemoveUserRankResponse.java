package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

public class RemoveUserRankResponse extends ApiResponseBase<RemoveUserRankResponse.RemoveUserRankResponseData> {
  public static class RemoveUserRankResponseData {
    public PublicUserRank removedRank;
  }
}
