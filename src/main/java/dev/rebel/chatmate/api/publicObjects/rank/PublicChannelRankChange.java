package dev.rebel.chatmate.api.publicObjects.rank;

import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;

import org.jetbrains.annotations.Nullable;

public class PublicChannelRankChange {
  public PublicChannel channel;
  public @Nullable String error;
}
