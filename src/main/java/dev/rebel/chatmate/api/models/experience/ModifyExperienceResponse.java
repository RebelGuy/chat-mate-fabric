package dev.rebel.chatmate.api.models.experience;

import dev.rebel.chatmate.api.models.experience.ModifyExperienceResponse.ModifyExperienceResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

public class ModifyExperienceResponse extends ApiResponseBase<ModifyExperienceResponseData> {
  public static class ModifyExperienceResponseData {
    public PublicUser updatedUser;
  }
}
