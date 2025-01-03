package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import org.jetbrains.annotations.Nullable;

public class BanUserRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;

  public BanUserRequest(int userId, @Nullable String message) {
    this.userId = userId;
    this.message = message;
  }
}
