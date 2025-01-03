package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import org.jetbrains.annotations.Nullable;

public class TimeoutUserRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;
  public final Integer durationSeconds;

  public TimeoutUserRequest(int userId, @Nullable String message, int durationSeconds) {
    this.userId = userId;
    this.message = message;
    this.durationSeconds = durationSeconds;
  }
}
