package dev.rebel.chatmate.api.models.account;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;

import org.jetbrains.annotations.Nullable;

public class AuthenticateResponse extends ApiResponseBase<AuthenticateResponse.AuthenticateResponseData> {
  public static class AuthenticateResponseData {
    public String username;
    public @Nullable String displayName;
    public Boolean isStreamer;
  }
}
