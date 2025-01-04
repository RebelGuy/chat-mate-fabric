package dev.rebel.chatmate.events;

import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.events.models.RenderChatGameOverlayEventData;
import dev.rebel.chatmate.events.models.ScreenResizeData;
import dev.rebel.chatmate.services.LogService;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

// why? because I would like to keep Forge event subscriptions centralised for an easier overview and for easier debugging.
// it also makes testing specific events A LOT easier because we can just mock out this class and test the event handler
// in complete isolation.
// thank Java for the verbose typings.
public class FabricEventService extends EventServiceBase<FabricEventService.EventType> {
  private final MinecraftClient minecraft;

  private int prevDisplayWidth;
  private int prevDisplayHeight;

  public FabricEventService(LogService logService, MinecraftClient minecraft) {
    super(EventType.class, logService);
    this.minecraft = minecraft;

    // initialise the display size. this has to be deferred because the window is not available directly within the constructor
    this.minecraft.execute(() -> {
      this.prevDisplayHeight = this.minecraft.getWindow().getHeight();
      this.prevDisplayWidth = this.minecraft.getWindow().getWidth();
    });

    HudRenderCallback.EVENT.register(this::onRenderHudEmitted);
    ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEmitted);
  }

//  public void onOpenGuiModList(EventCallback<OpenGuiEventData> handler) {
//    this.addListener(EventType.OPEN_GUI_MOD_LIST, 0, handler, null);
//  }

//  public void onOpenGuiIngameMenu(EventCallback<OpenGuiEventData> handler) {
//    this.addListener(EventType.OPEN_GUI_IN_GAME_MENU, 0, handler, null);
//  }

//  public void onOpenChatSettingsMenu(EventCallback<OpenGuiEventData> handler) {
//    this.addListener(EventType.OPEN_CHAT_SETTINGS_MENU, 0, handler, null);
//  }

  /** Fires when the GuiChat (GuiScreen) is shown. */
//  public void onOpenChat(EventCallback<OpenGuiEventData> handler) {
//    this.addListener(EventType.OPEN_CHAT, 0, handler, null);
//  }

  /** Fires after the minecraft.currentScreen has changed reference. Occurs AFTER any onOpen* events - it is read-only. */
//  public void onGuiScreenChanged(EventCallback<GuiScreenChangedEventData> handler, @Nullable GuiScreenChangedEventOptions options) {
//    this.addListener(EventType.GUI_SCREEN_CHANGED, 0, handler, options);
//  }

//  public void onRenderGameOverlay(EventCallback<RenderGameOverlayEventData> handler, @Nonnull RenderGameOverlayEventOptions options) {
//    this.addListener(EventType.RENDER_GAME_OVERLAY, 0, handler, options);
//  }

  /** Fires before the main chat box GUI component is rendered. */
  public void onRenderChatGameOverlay(EventCallback<RenderChatGameOverlayEventData> handler) {
    this.addListener(EventType.RENDER_CHAT_GAME_OVERLAY, 0, handler, null);
  }

  public void onRenderTick(EventCallback<?> handler) {
    this.addListener(EventType.RENDER_TICK, 0, handler, null);
  }

  public void onClientTick(EventCallback<?> handler) {
    this.addListener(EventType.CLIENT_TICK, 0, handler, null);
  }

  /** Fires for mouse events within a GUI screen. */
  public void onGuiScreenMouse(EventCallback<?> handler) {
    this.addListener(EventType.GUI_SCREEN_MOUSE, 0, handler, null);
  }

  /** Fires for keyboard events within a GUI screen. */
  public void onGuiScreenKeyboard(EventCallback<?> handler) {
    this.addListener(EventType.GUI_SCREEN_KEYBOARD, 0, handler, null);
  }

  /** Stored as a weak reference - lambda forbidden. */
  public void onScreenResize(EventCallback<ScreenResizeData> handler, Object key) {
    this.addListener(EventType.SCREEN_RESIZE, 0, handler, null, key);
  }

  public void off(EventType event, Object key) {
    this.removeListener(event, key);
  }

//  public void forgeEventSubscriber(GuiOpenEvent forgeEvent) {
//    Screen originalScreen = this.minecraft.currentScreen;
//    EventType eventType;
//    if (forgeEvent.gui instanceof GuiModList) {
//      eventType = EventType.OPEN_GUI_MOD_LIST;
//    } else if (forgeEvent.gui instanceof GuiIngameMenu) {
//      eventType = EventType.OPEN_GUI_IN_GAME_MENU;
//    } else if (forgeEvent.gui instanceof ScreenChatOptions) {
//      eventType = EventType.OPEN_CHAT_SETTINGS_MENU;
//    } else if (forgeEvent.gui instanceof GuiChat) {
//      eventType = EventType.OPEN_CHAT;
//    } else {
//      eventType = null;
//    }
//
//    if (eventType != null) {
//      Event<OpenGuiEventData> event = new Event<>(new OpenGuiEventData(forgeEvent.gui));
//      for (EventHandler<OpenGuiEventData,?> handler : this.getListeners(eventType, OpenGuiEventData.class)) {
//        super.safeDispatch(eventType, handler, event);
//
//        if (event.hasModifiedData) {
//          forgeEvent.gui = event.getData().gui;
//        }
//        if (event.stoppedPropagation) {
//          forgeEvent.setCanceled(event.stoppedPropagation);
//          break;
//        }
//      }
//    }
//
//    GuiScreen newScreen = forgeEvent.gui;
//    if (originalScreen != newScreen) {
//      Event<GuiScreenChangedEventData> event = new Event<>(new GuiScreenChangedEventData(originalScreen, newScreen), false);
//      for (EventHandler<GuiScreenChangedEventData, GuiScreenChangedEventOptions> handler : this.getListeners(EventType.GUI_SCREEN_CHANGED, GuiScreenChangedEventData.class, GuiScreenChangedEventOptions.class)) {
//        GuiScreenChangedEventOptions options = handler.options;
//
//        // check if the subscriber is interested in this event, and skip notifying them if not
//        if (options != null) {
//          Class<? extends GuiScreen> filter = options.screenFilter;
//          GuiScreenChangedEventData.ListenType listenType = options.listenType;
//
//          boolean matchClose, matchOpen;
//          if (filter == null) {
//            matchClose = originalScreen == null;
//            matchOpen = newScreen == null;
//          } else {
//            matchClose = originalScreen != null && filter.isAssignableFrom(originalScreen.getClass());
//            matchOpen = newScreen != null && filter.isAssignableFrom(newScreen.getClass());
//          }
//
//          if (listenType == GuiScreenChangedEventData.ListenType.OPEN_ONLY) {
//            if (!matchOpen) continue;
//          } else if (listenType == GuiScreenChangedEventData.ListenType.CLOSE_ONLY) {
//            if (!matchClose) continue;
//          } else if (listenType == GuiScreenChangedEventData.ListenType.OPEN_AND_CLOSE) {
//            if (!matchOpen && !matchClose) continue;
//          } else {
//            throw EnumHelpers.<GuiScreenChangedEventData.ListenType>assertUnreachable(listenType);
//          }
//        }
//
//        super.safeDispatch(EventType.GUI_SCREEN_CHANGED, handler, event);
//      }
//    }
//  }

  // Once all default rendering has been completed, this event is fired.
  //
  // writing text to the screen will place it only on top of the in-game GUI (underneath e.g. menu overlays)
  public void onRenderHudEmitted(DrawContext context, RenderTickCounter renderTickCounter) {
    // this is a bit naughty, but pretend there is no render event when the F3 screen is open.
    // we should probably just let the handlers decide for themselves if they want to render or not.
    if (this.minecraft.getDebugHud().shouldShowDebugHud()) {
      return;
    }

    EventType eventType = EventType.RENDER_GAME_OVERLAY;
    Event<Object> event = new Event<>(null);
    for (EventHandler<Object, Object> handler : this.getListeners(eventType, Object.class, Object.class)) {
      super.safeDispatch(eventType, handler, event);

      if (event.stoppedPropagation) {
        return;
      }
    }
  }

  public boolean emitRenderChatEvent(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused) {
    EventType eventType = EventType.RENDER_CHAT_GAME_OVERLAY;
    Event<RenderChatGameOverlayEventData> event = new Event<>(new RenderChatGameOverlayEventData(mouseX, mouseY));
    for (EventHandler<RenderChatGameOverlayEventData, ?> handler : this.getListeners(eventType, RenderChatGameOverlayEventData.class)) {
      super.safeDispatch(eventType, handler, event);

      if (event.stoppedPropagation) {
        return false;
      }
    }

    return true;
  }

  // similar to RenderHud, except fires even in menus
  // writing text to the screen will put the text on TOP of everything.
  public void onClientTickEmitted(MinecraftClient minecraft) {
    // check if the screen was resized every frame
    int displayWidth = minecraft.getWindow().getWidth();
    int displayHeight = minecraft.getWindow().getHeight();
    if (this.prevDisplayWidth != displayWidth || this.prevDisplayHeight != displayHeight) {
      this.prevDisplayWidth = displayWidth;
      this.prevDisplayHeight = displayHeight;

      Event<ScreenResizeData> resizeEvent = new Event<>(new ScreenResizeData(displayWidth, displayHeight), false);
      for (EventHandler<ScreenResizeData, ?> handler : this.getListeners(EventType.SCREEN_RESIZE, ScreenResizeData.class)) {
        super.safeDispatch(EventType.SCREEN_RESIZE, handler, resizeEvent);
      }
    }

    EventType eventType = EventType.RENDER_TICK;
    Event<?> renderEvent = new Event<>(null, false);
    for (EventHandler<?,?> handler : this.getListeners(eventType)) {
      super.safeDispatch(eventType, handler, renderEvent);
    }

    // HACK - something is disabling alpha, but Forge expects alpha to be enabled else it causes problems when rendering textures in the server menu. reset it here after we are done rendering all our custom stuff.
    // another example why OpenGL's state machine is fucking dumb
    // GlStateManager.enableAlpha();
  }

  // note:
  // * GuiScreenEvent.MouseInputEvent/KeyboardInputEvent: Fired from the GUI once it has been informed to handle input.
  // * InputEvent.MouseInputEvent/KeyboardInputEvent: Fired for any events that haven't already been fired by screens
//  @SideOnly(Side.CLIENT)
//  @SubscribeEvent
//  public void forgeEventSubscriber(GuiScreenEvent.MouseInputEvent.Pre forgeEvent) {
//    EventType eventType = EventType.GUI_SCREEN_MOUSE;
//    Event<?> event = new Event<>();
//    for (EventHandler<?,?> handler : this.getListeners(eventType)) {
//      super.safeDispatch(eventType, handler, event);
//
//      if (event.stoppedPropagation) {
//        forgeEvent.setCanceled(true);
//        return;
//      }
//    }
//  }

//  @SideOnly(Side.CLIENT)
//  @SubscribeEvent
//  public void forgeEventSubscriber(GuiScreenEvent.KeyboardInputEvent.Pre forgeEvent) {
//    EventType eventType = EventType.GUI_SCREEN_KEYBOARD;
//    Event<?> event = new Event<>();
//    for (EventHandler<?,?> handler : this.getListeners(eventType)) {
//      super.safeDispatch(eventType, handler, event);
//
//      if (event.stoppedPropagation) {
//        forgeEvent.setCanceled(true);
//        return;
//      }
//    }
//  }

  public enum EventType {
    OPEN_GUI_MOD_LIST,
    OPEN_GUI_IN_GAME_MENU,
    OPEN_CHAT_SETTINGS_MENU,
    OPEN_CHAT,
    GUI_SCREEN_CHANGED,
    RENDER_GAME_OVERLAY,
    RENDER_CHAT_GAME_OVERLAY,
    RENDER_TICK,
    CLIENT_TICK,
    GUI_SCREEN_MOUSE,
    GUI_SCREEN_KEYBOARD,
    SCREEN_RESIZE
  }
}
