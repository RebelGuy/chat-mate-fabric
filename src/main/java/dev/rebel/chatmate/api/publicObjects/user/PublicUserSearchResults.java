package dev.rebel.chatmate.api.publicObjects.user;

import org.jetbrains.annotations.Nullable;

public class PublicUserSearchResults {
  public PublicUser user;
  public @Nullable PublicChannel matchedChannel;
  public PublicChannel[] allChannels;
}
