package dev.rebel.chatmate.api.models.donation;

import dev.rebel.chatmate.api.models.donation.LinkUserResponse.LinkUserResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;

public class LinkUserResponse extends ApiResponseBase<LinkUserResponseData> {
  public static class LinkUserResponseData {
    public PublicDonation updatedDonation;
  }
}
