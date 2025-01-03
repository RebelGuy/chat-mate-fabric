package dev.rebel.chatmate.config;

import dev.rebel.chatmate.config.Config.SeparableHudElement.PlatformIconPosition;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SerialisedConfig {
  public final boolean soundEnabled;
  public final boolean showChatMateOptionsInPauseMenu;
  public final int chatVerticalDisplacement;
  public final String commandMessageChatVisibility;
  public final boolean showCommandMessageStatus;
  public final boolean hudEnabled;
  public final boolean showChatPlatformIcon;
  public final SerialisedSeparableHudElement statusIndicator;
  public final SerialisedSeparableHudElement viewerCount;
  public final boolean onlyShowIndicatorsWhenLive;
  public final boolean debugModeEnabled;
  public final String[] logLevels;
  public final long lastGetChatResponse;
  public final long lastGetChatMateEventsResponse;
  public final SerialisedLoginInfo loginInfo;
  public final List<String> chatMentionFilter;

  public SerialisedConfig(boolean soundEnabled,
                          boolean showChatMateOptionsInPauseMenu,
                          int chatVerticalDisplacement,
                          String commandMessageChatVisibility,
                          boolean showCommandMessageStatus,
                          boolean hudEnabled,
                          boolean showChatPlatformIcon,
                          SerialisedSeparableHudElement statusIndicator,
                          SerialisedSeparableHudElement viewerCount,
                          boolean onlyShowIndicatorsWhenLive,
                          boolean debugModeEnabled,
                          String[] logLevels,
                          long lastGetChatResponse,
                          long lastGetChatMateEventsResponse,
                          SerialisedLoginInfo loginInfo,
                          List<String> chatMentionFilter) {
    this.soundEnabled = soundEnabled;
    this.showChatMateOptionsInPauseMenu = showChatMateOptionsInPauseMenu;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.commandMessageChatVisibility = commandMessageChatVisibility;
    this.showCommandMessageStatus = showCommandMessageStatus;
    this.hudEnabled = hudEnabled;
    this.showChatPlatformIcon = showChatPlatformIcon;
    this.statusIndicator = statusIndicator;
    this.viewerCount = viewerCount;
    this.onlyShowIndicatorsWhenLive = onlyShowIndicatorsWhenLive;
    this.debugModeEnabled = debugModeEnabled;
    this.logLevels = logLevels;
    this.lastGetChatResponse = lastGetChatResponse;
    this.lastGetChatMateEventsResponse = lastGetChatMateEventsResponse;
    this.loginInfo = loginInfo;
    this.chatMentionFilter = chatMentionFilter;
  }

  public int getVersion() {
    return 0;
  }

  public static class SerialisedSeparableHudElement {
    public final boolean enabled;
    public final boolean separatePlatforms;
    public final boolean showPlatformIcon;
    public final String platformIconPosition;

    public SerialisedSeparableHudElement(boolean enabled, boolean separatePlatforms, boolean showPlatformIcon, String platformIconPosition) {
      this.enabled = enabled;
      this.separatePlatforms = separatePlatforms;
      this.showPlatformIcon = showPlatformIcon;
      this.platformIconPosition = platformIconPosition;
    }

    public SerialisedSeparableHudElement(Config.SeparableHudElement separableHudElement) {
      this(separableHudElement.enabled,
          separableHudElement.separatePlatforms,
          separableHudElement.showPlatformIcon,
          separableHudElement.platformIconPosition.toString().toLowerCase()
      );
    }

    public Config.SeparableHudElement deserialise() {
      return new Config.SeparableHudElement(
          this.enabled,
          this.separatePlatforms,
          this.showPlatformIcon,
          Objects.equals(this.platformIconPosition, "left") ? PlatformIconPosition.LEFT
              : Objects.equals(this.platformIconPosition, "top") ? PlatformIconPosition.TOP
              : Objects.equals(this.platformIconPosition, "right") ? PlatformIconPosition.RIGHT
              : Objects.equals(this.platformIconPosition, "bottom") ? PlatformIconPosition.BOTTOM
              : PlatformIconPosition.LEFT
      );
    }
  }

  public static class SerialisedLoginInfo {
    public final @Nullable String username;
    public final @Nullable String displayName;
    public final @Nullable String loginToken;

    public SerialisedLoginInfo(@Nullable String username, @Nullable String displayName, @Nullable String loginToken) {
      this.username = username;
      this.displayName = displayName;
      this.loginToken = loginToken;
    }

    public SerialisedLoginInfo(Config.LoginInfo loginInfo) {
      this(loginInfo.username, loginInfo.displayName, loginInfo.loginToken);
    }

    public Config.LoginInfo deserialise() {
      return new Config.LoginInfo(this.username, this.displayName, this.loginToken);
    }
  }
}
