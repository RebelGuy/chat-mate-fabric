package dev.rebel.chatmate.api.models.chatMate;

import dev.rebel.chatmate.api.models.chatMate.SetActiveLivestreamResponse.SetActiveLivestreamResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

import org.jetbrains.annotations.Nullable;

public class SetActiveLivestreamResponse extends ApiResponseBase<SetActiveLivestreamResponseData> {
  public static class SetActiveLivestreamResponseData {
    public @Nullable String livestreamLink;
  }
}
