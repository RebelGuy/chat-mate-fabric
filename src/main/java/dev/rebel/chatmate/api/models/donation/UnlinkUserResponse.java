package dev.rebel.chatmate.api.models.donation;

import dev.rebel.chatmate.api.models.donation.UnlinkUserResponse.UnlinkUserResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.donation.PublicDonation;

public class UnlinkUserResponse extends ApiResponseBase<UnlinkUserResponseData> {
  public static class UnlinkUserResponseData {
    public PublicDonation updatedDonation;
  }
}
