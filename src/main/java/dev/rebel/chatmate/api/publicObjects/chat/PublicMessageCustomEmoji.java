package dev.rebel.chatmate.api.publicObjects.chat;

import dev.rebel.chatmate.api.publicObjects.emoji.PublicCustomEmoji;

import org.jetbrains.annotations.Nullable;

public class PublicMessageCustomEmoji {
  public @Nullable PublicMessageText textData;
  public @Nullable PublicMessageEmoji emojiData;
  public PublicCustomEmoji customEmoji;
}
