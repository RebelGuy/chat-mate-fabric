package dev.rebel.chatmate.api.publicObjects.emoji;

import org.jetbrains.annotations.Nullable;

public class PublicCustomEmoji {
  public Integer id;
  public String name;
  public String symbol;
  public String imageUrl;
  public Integer imageWidth;
  public Integer imageHeight;
  public Integer levelRequirement;
  public Boolean canUseInDonationMessage;
  public Integer version;
  public @Nullable Long deletedAt;
  public Integer[] whitelistedRanks;
  public Integer sortOrder;

  /** Emoji versions are immutable, so the cache key never needs to be invalidated. */
  public String getCacheKey() {
    return String.format("custom-emoji/%d/%d.png", this.id, this.version);
  }
}
