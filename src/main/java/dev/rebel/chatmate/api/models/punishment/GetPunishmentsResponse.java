package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.GetPunishmentsResponse.GetPunishmentsResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;

public class GetPunishmentsResponse extends ApiResponseBase<GetPunishmentsResponseData> {
  public static class GetPunishmentsResponseData {
    public PublicUserRank[] punishments;
  }
}
