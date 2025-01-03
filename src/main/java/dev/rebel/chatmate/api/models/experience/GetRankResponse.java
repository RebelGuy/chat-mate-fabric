package dev.rebel.chatmate.api.models.experience;

import dev.rebel.chatmate.api.models.experience.GetRankResponse.GetRankResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.user.PublicRankedUser;

public class GetRankResponse extends ApiResponseBase<GetRankResponseData> {
  public static class GetRankResponseData {
    public Integer relevantIndex;
    public PublicRankedUser[] rankedUsers;
  }
}
