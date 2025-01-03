package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import org.jetbrains.annotations.Nullable;

public class RemoveModRankRequest extends ApiRequestBase {
  public int userId;
  public @Nullable String message;

  public RemoveModRankRequest(int userId, @Nullable String message) {
    this.userId = userId;
    this.message = message;
  }
}
