package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import org.jetbrains.annotations.Nullable;

public class UnbanUserRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;

  public UnbanUserRequest(int userId, @Nullable String message) {
    this.userId = userId;
    this.message = message;
  }
}
