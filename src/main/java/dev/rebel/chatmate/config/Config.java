package dev.rebel.chatmate.config;

import dev.rebel.chatmate.config.Config.ConfigType;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.events.EventServiceBase;
import dev.rebel.chatmate.events.models.ConfigEventOptions;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.util.Callback;
import dev.rebel.chatmate.util.Debouncer;
import dev.rebel.chatmate.util.EnumHelpers;

import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Function;

public class Config extends EventServiceBase<ConfigType> {
  private final ConfigPersistorService configPersistorService;

  // NOTE: DO **NOT** REMOVE THESE GETTERS. THEY ARE DISGUSTING BUT REQUIRED FOR TESTING
  private final StatefulEmitter<Boolean> chatMateEnabled;
  public StatefulEmitter<Boolean> getChatMateEnabledEmitter() { return this.chatMateEnabled; }

  private final StatefulEmitter<Boolean> soundEnabled;
  public StatefulEmitter<Boolean> getSoundEnabledEmitter() { return this.soundEnabled; }

  private final StatefulEmitter<Boolean> showChatMateOptionsInPauseMenu;
  public StatefulEmitter<Boolean> getShowChatMateOptionsInPauseMenuEmitter() { return this.showChatMateOptionsInPauseMenu; }

  private final StatefulEmitter<Integer> chatVerticalDisplacement;
  public StatefulEmitter<Integer> getChatVerticalDisplacementEmitter() { return this.chatVerticalDisplacement; }

  private final StatefulEmitter<CommandMessageChatVisibility> commandMessageChatVisibility;
  public StatefulEmitter<CommandMessageChatVisibility> getCommandMessageChatVisibilityEmitter() { return this.commandMessageChatVisibility; }

  private final StatefulEmitter<Boolean> showCommandMessageStatus;
  public StatefulEmitter<Boolean> getShowCommandMessageStatus() { return this.showCommandMessageStatus; }

  private final StatefulEmitter<Boolean> hudEnabled;
  public StatefulEmitter<Boolean> getHudEnabledEmitter() { return this.hudEnabled; }

  private final StatefulEmitter<Boolean> showChatPlatformIcon;
  public StatefulEmitter<Boolean> getShowChatPlatformIconEmitter() { return this.showChatPlatformIcon; }

  private final StatefulEmitter<SeparableHudElement> statusIndicator;
  public StatefulEmitter<SeparableHudElement> getStatusIndicatorEmitter() { return this.statusIndicator; }

  private final StatefulEmitter<SeparableHudElement> viewerCount;
  public StatefulEmitter<SeparableHudElement> getViewerCountEmitter() { return this.viewerCount; }

  private final StatefulEmitter<Boolean> onlyShowIndicatorsWhenLive;
  public StatefulEmitter<Boolean> getOnlyShowIndicatorsWhenLive() { return this.onlyShowIndicatorsWhenLive; }

  private final StatefulEmitter<Boolean> debugModeEnabled;
  public StatefulEmitter<Boolean> getDebugModeEnabledEmitter() { return this.debugModeEnabled; }

  private final StatefulEmitter<LogLevel[]> logLevels;
  public StatefulEmitter<LogLevel[]> getLogLevelsEmitter() { return this.logLevels; }

  private final StatefulEmitter<Long> lastGetChatResponse;
  public StatefulEmitter<Long> getLastGetChatResponseEmitter() { return this.lastGetChatResponse; }

  private final StatefulEmitter<Long> lastGetChatMateEventsResponse;
  public StatefulEmitter<Long> getLastGetChatMateEventsResponseEmitter() { return this.lastGetChatMateEventsResponse; }

  private final StatefulEmitter<LoginInfo> loginInfo;
  public StatefulEmitter<LoginInfo> getLoginInfoEmitter() { return this.loginInfo; }

  private final StatefulEmitter<List<String>> chatMentionFilter;
  public StatefulEmitter<List<String>> getChatMentionFilter() { return this.chatMentionFilter; }

  /** Listeners are notified whenever any change has been made to the config. */
  private final List<Callback> updateListeners;

  /** Only used for holding onto wrapped callback functions when an onChange subscriber uses the automatic unsubscription feature. Write-only. */
  private final Map<ConfigType, WeakHashMap<Object, EventCallback<?>>> weakHandlers;

  private final Debouncer saveDebouncer;

  public Config(LogService logService, ConfigPersistorService configPersistorService) {
    super(ConfigType.class, logService);
    this.configPersistorService = configPersistorService;
    this.saveDebouncer = new Debouncer(400, this::save); // has to be less than the API polling rate
    this.updateListeners = new ArrayList<>();

    this.weakHandlers = new WeakHashMap<>();
    for (ConfigType type : ConfigType.class.getEnumConstants()) {
      this.weakHandlers.put(type, new WeakHashMap<>());
    }

    // if loading fails, the user will be left with the default settings which is fine
    @Nullable SerialisedConfig data = this.load();
    this.chatMateEnabled = new StatefulEmitter<>(
        ConfigType.ENABLE_CHAT_MATE,
        Boolean.class,
        false,
        this::onUpdate
    );
    this.soundEnabled = new StatefulEmitter<>(
        ConfigType.ENABLE_SOUND,
        Boolean.class,
        data == null ? true : data.soundEnabled,
        this::onUpdate
    );
    this.showChatMateOptionsInPauseMenu = new StatefulEmitter<>(
        ConfigType.SHOW_CHAT_MATE_OPTIONS_IN_PAUSE_MENU,
        Boolean.class,
        data == null ? false : data.showChatMateOptionsInPauseMenu,
        this::onUpdate
    );
    this.chatVerticalDisplacement = new StatefulEmitter<>(
        ConfigType.CHAT_VERTICAL_DISPLACEMENT,
        Integer.class,
        data == null ? 10 : data.chatVerticalDisplacement,
        this::onUpdate
    );
    this.commandMessageChatVisibility = new StatefulEmitter<>(
        ConfigType.COMMAND_MESSAGE_CHAT_VISIBILITY,
        CommandMessageChatVisibility.class,
        data == null || data.commandMessageChatVisibility == null
            ? CommandMessageChatVisibility.SHOWN
            : EnumHelpers.fromStringOrDefault(CommandMessageChatVisibility.class, data.commandMessageChatVisibility, CommandMessageChatVisibility.SHOWN),
        this::onUpdate);
    this.showCommandMessageStatus = new StatefulEmitter<>(
        ConfigType.SHOW_COMMAND_MESSAGE_STATUS,
        Boolean.class,
        data == null ? true : data.showCommandMessageStatus,
        this::onUpdate
    );
    this.hudEnabled = new StatefulEmitter<>(
        ConfigType.ENABLE_HUD,
        Boolean.class,
        data == null ? true : data.hudEnabled,
        this::onUpdate
    );
    this.showChatPlatformIcon = new StatefulEmitter<>(
        ConfigType.SHOW_CHAT_PLATFORM_ICON,
        Boolean.class,
        data == null ? true : data.showChatPlatformIcon,
        this::onUpdate
    );
    this.statusIndicator = new StatefulEmitter<>(
        ConfigType.STATUS_INDICATOR,
        SeparableHudElement.class,
        data == null
            ? new SeparableHudElement(true, false, false, SeparableHudElement.PlatformIconPosition.LEFT)
            : data.statusIndicator.deserialise(),
        this::onUpdate
    );
    this.viewerCount = new StatefulEmitter<>(
        ConfigType.VIEWER_COUNT,
        SeparableHudElement.class,
        data == null
            ? new SeparableHudElement(true, false, false, SeparableHudElement.PlatformIconPosition.LEFT)
            : data.viewerCount.deserialise(),
        this::onUpdate
    );
    this.onlyShowIndicatorsWhenLive = new StatefulEmitter<>(
        ConfigType.ONLY_SHOW_INDICATORS_WHEN_LIVE,
        Boolean.class,
        data == null ? false : data.onlyShowIndicatorsWhenLive,
        this::onUpdate
    );
    this.debugModeEnabled = new StatefulEmitter<>(
        ConfigType.DEBUG_MODE_ENABLED,
        Boolean.class,
        data == null ? false : data.debugModeEnabled,
        this::onUpdate
    );
    this.logLevels = new StatefulEmitter<>(
        ConfigType.LOG_LEVEL,
        LogLevel[].class,
        data == null || data.logLevels == null
            ? new LogLevel[] { LogLevel.ERROR, LogLevel.WARNING, LogLevel.INFO }
            : dev.rebel.chatmate.util.Collections.map(
                dev.rebel.chatmate.util.Collections.list(data.logLevels),
                level -> EnumHelpers.fromStringOrDefault(LogLevel.class, level, LogLevel.ERROR)
              ).toArray(new LogLevel[0]),
        this::onUpdate
    );
    this.lastGetChatResponse = new StatefulEmitter<>(
        ConfigType.LAST_GET_CHAT_RESPONSE,
        Long.class,
        data == null ? 0L : data.lastGetChatResponse,
        this::onUpdate
    );
    this.lastGetChatMateEventsResponse = new StatefulEmitter<>(
        ConfigType.LAST_GET_CHAT_MATE_EVENTS_RESPONSE,
        Long.class,
        data == null ? 0L : data.lastGetChatMateEventsResponse,
        this::onUpdate
    );
    this.loginInfo = new StatefulEmitter<>(
        ConfigType.LOGIN_INFO,
        LoginInfo.class,
        data == null ? new LoginInfo("rebel_guy", null, "uSgoy9A7") : data.loginInfo.deserialise(),
        this::onUpdate
    );
    this.chatMentionFilter = new StatefulEmitter<>(
        ConfigType.CHAT_MENTION_FILTER,
        (Class<List<String>>)(Object)List.class,
        data == null ? new ArrayList<>() : data.chatMentionFilter,
        this::onUpdate
    );
  }

  public void listenAny(Callback callback) {
    this.updateListeners.add(callback);
  }

  private <T> void onUpdate(Event<T> _unused) {
    this.saveDebouncer.doDebounce();
    this.updateListeners.forEach(Callback::call);
  }

  @Nullable
  private SerialisedConfig load() {
    SerialisedConfig loaded = this.configPersistorService.load();
    if (loaded != null) {
      this.saveDebouncer.doDebounce();
    }
    return loaded;
  }

  private void save() {
    try {
      SerialisedConfig serialisedConfig = new SerialisedConfig(
          this.soundEnabled.get(),
          this.showChatMateOptionsInPauseMenu.get(),
          this.chatVerticalDisplacement.get(),
          this.commandMessageChatVisibility.get().toString(),
          this.showCommandMessageStatus.get(),
          this.hudEnabled.get(),
          this.showChatPlatformIcon.get(),
          new SerialisedConfig.SerialisedSeparableHudElement(this.statusIndicator.get()),
          new SerialisedConfig.SerialisedSeparableHudElement(this.viewerCount.get()),
          this.debugModeEnabled.get(),
          this.onlyShowIndicatorsWhenLive.get(),
          dev.rebel.chatmate.util.Collections.map(
              dev.rebel.chatmate.util.Collections.list(this.logLevels.get()), Enum::toString
          ).toArray(new String[0]),
          this.lastGetChatResponse.get(),
          this.lastGetChatMateEventsResponse.get(),
          new SerialisedConfig.SerialisedLoginInfo(this.loginInfo.get()),
          this.chatMentionFilter.get()
      );
      this.configPersistorService.save(serialisedConfig);
    } catch (Exception e) {
      this.logService.logError(this, "Failed to save config:", e);
    }
  }

  /** Represents a state that emits an event when modified */
  public class StatefulEmitter<T> {
    private final ConfigType type;
    private final Class<T> clazz;
    private T state;

    @SafeVarargs
    public StatefulEmitter (ConfigType type, Class<T> clazz, T initialState, EventCallback<T>... initialListeners) {
      super();
      this.type = type;
      this.clazz = clazz;
      this.state = initialState;
      Arrays.asList(initialListeners).forEach(l -> Config.this.addListener(this.type, 0, l, null));
    }

    /** Lambda allowed - no automatic unsubscribing. */
    public void onChange(EventCallback<T> handler) {
      this.onChange(handler, null);
    }

    /** **NO LAMBDA** - automatic unsubscribing. */
    public void onChange(EventCallback<T> handler, Object key) {
      this.onChange(handler, key, false);
    }

    // if you try to add a lambda it won't stay subscribed - create a field first which may be a lambda
    /** **NO LAMBDA** - automatic unsubscribing. */
    public void onChange(EventCallback<T> handler, Object key, boolean fireInitialOnChange) {
      this.onChange(handler, new ConfigEventOptions<>(), key, fireInitialOnChange);
    }

    /** Lambda allowed - no automatic unsubscribing. */
    public void onChange(EventCallback<T> handler, @Nullable ConfigEventOptions<T> options) {
      this.onChange(handler, options, null, false);
    }

    /** **NO LAMBDA** - automatic unsubscribing. */
    public void onChange(EventCallback<T> handler, @Nullable ConfigEventOptions<T> options, Object key, boolean fireInitialOnChange) {
      Config.this.addListener(this.type, 0, handler, options, key);

      // fireInitialOnChange is a convenience option for callers that perform logic directly inside the callback function - don't remove it
      if (fireInitialOnChange) {
        if (options != null && options.filter != null && !options.filter.test(this.state)) {
          return;
        }

        try {
          handler.dispatch(new Event<>(this.state));
        } catch (Exception e) {
          Config.this.logService.logError(this, "A problem occurred while notifying listener of the", this.type, "event. Event data:", this.state, "| Error:", e);
        }
      }
    }

    public void off(Object key) {
      Config.this.removeListener(this.type, key);
    }

    public void set(T newValue) {
      if (Objects.equals(this.state, newValue)) {
        return;
      } else {
        this.state = newValue;
      }

      ArrayList<EventHandler<T, ConfigEventOptions<T>>> handlers = Config.this.getListeners(this.type, this.clazz, (Class<ConfigEventOptions<T>>)(Object)ConfigEventOptions.class); // fuck off java
      for (EventHandler<T, ConfigEventOptions<T>> handler : handlers) {
        ConfigEventOptions<T> options = handler.options;
        if (options != null && options.filter != null && !options.filter.test(newValue)) {
          continue;
        }

        Config.this.safeDispatch(this.type, handler, new Event<>(newValue));
      }
    }

    public void set(Function<T, T> newValue) {
      this.set(newValue.apply(this.state));
    }

    public T get() {
      return this.state;
    }
  }

  public static class SeparableHudElement {
    public final boolean enabled;
    public final boolean separatePlatforms;
    public final boolean showPlatformIcon;
    public final PlatformIconPosition platformIconPosition;

    public SeparableHudElement(boolean enabled, boolean separatePlatforms, boolean showPlatformIcon, PlatformIconPosition platformIconPosition) {
      this.enabled = enabled;
      this.separatePlatforms = separatePlatforms;
      this.showPlatformIcon = showPlatformIcon;
      this.platformIconPosition = platformIconPosition;
    }

    public SeparableHudElement withEnabled(boolean enabled) {
      return new SeparableHudElement(enabled, this.separatePlatforms, this.showPlatformIcon, this.platformIconPosition);
    }

    public SeparableHudElement withSeparatePlatforms(boolean separatePlatforms) {
      return new SeparableHudElement(this.enabled, separatePlatforms, this.showPlatformIcon, this.platformIconPosition);
    }

    public SeparableHudElement withShowPlatformIcon(boolean showPlatformIcon) {
      return new SeparableHudElement(this.enabled, this.separatePlatforms, showPlatformIcon, this.platformIconPosition);
    }

    public SeparableHudElement withPlatformIconPosition(PlatformIconPosition platformIconPosition) {
      return new SeparableHudElement(this.enabled, this.separatePlatforms, this.showPlatformIcon, platformIconPosition);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SeparableHudElement that = (SeparableHudElement) o;
      return enabled == that.enabled && separatePlatforms == that.separatePlatforms && showPlatformIcon == that.showPlatformIcon && platformIconPosition == that.platformIconPosition;
    }

    @Override
    public int hashCode() {
      return Objects.hash(enabled, separatePlatforms, showPlatformIcon, platformIconPosition);
    }

    public enum PlatformIconPosition { LEFT, RIGHT, TOP, BOTTOM }
  }

  public static class LoginInfo {
    public final @Nullable String username;
    public final @Nullable String displayName;
    public final @Nullable String loginToken;

    public LoginInfo(@Nullable String username, @Nullable String displayName, @Nullable String loginToken) {
      this.username = username;
      this.displayName = displayName;
      this.loginToken = loginToken;
    }

    public @Nullable String getDisplayName() {
      return this.displayName != null ? this.displayName : this.username;
    }
  }

  public enum CommandMessageChatVisibility { HIDDEN, SHOWN, GREYED_OUT }

  public enum LogLevel { ERROR, WARNING, INFO, DEBUG, API }

  public enum ConfigType {
    ENABLE_CHAT_MATE,
    ENABLE_SOUND,
    SHOW_CHAT_MATE_OPTIONS_IN_PAUSE_MENU,
    CHAT_VERTICAL_DISPLACEMENT,
    COMMAND_MESSAGE_CHAT_VISIBILITY,
    SHOW_COMMAND_MESSAGE_STATUS,
    ENABLE_HUD,
    SHOW_SERVER_LOGS_HEARTBEAT,
    SHOW_SERVER_LOGS_TIME_SERIES,
    SHOW_CHAT_PLATFORM_ICON,
    STATUS_INDICATOR,
    VIEWER_COUNT,
    ONLY_SHOW_INDICATORS_WHEN_LIVE,
    DEBUG_MODE_ENABLED,
    LOG_LEVEL,
    LAST_GET_CHAT_RESPONSE,
    LAST_GET_CHAT_MATE_EVENTS_RESPONSE,
    HUD_TRANSFORMS,
    LOGIN_INFO,
    CHAT_MENTION_FILTER
  }
}
