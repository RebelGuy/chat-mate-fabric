package dev.rebel.chatmate.services;

import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.FabricEventService;
import dev.rebel.chatmate.events.models.RenderChatGameOverlayEventData;
import dev.rebel.chatmate.util.Tuple2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

// if we try to access/modify the Minecraft world on a separate thread, we may get a concurrency-related crash.
// the solution is to schedule work on the minecraft thread, so it can be executed when safe.
// there is no harm in scheduling things while already on the minecraft thread, so for conciseness don't do any checking.

/** Use this proxy for thread-unsafe Minecraft operations. */
public class MinecraftProxyService {
  private final MinecraftClient minecraft;
  private final LogService logService;
  private final FabricEventService fabricEventService;

  private final List<Tuple2<String, Text>> pendingChatAdditions;
  private final List<Text> pendingChatDeletions;
  private final List<Tuple2<Predicate<Text>, UnaryOperator<Text>>> pendingChatReplacements;
  private boolean refreshChat;

  public MinecraftProxyService(MinecraftClient minecraft, LogService logService, FabricEventService fabricEventService) {
    this.minecraft = minecraft;
    this.logService = logService;
    this.fabricEventService = fabricEventService;

    this.pendingChatAdditions = Collections.synchronizedList(new ArrayList<>());
    this.pendingChatDeletions = Collections.synchronizedList(new ArrayList<>());
    this.pendingChatReplacements = Collections.synchronizedList(new ArrayList<>());
    this.refreshChat = false;

    this.fabricEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay);
  }

  public void playSound(SoundInstance sound) {
    this.schedule(mc -> mc.getSoundManager().play(sound));
  }

  /** Prints the chat message immediately, or holds on to the message until the chat GUI is visible again. */
  public void printChatMessage(String type, Text component) {
    synchronized (this.pendingChatAdditions) {
      this.pendingChatAdditions.add(new Tuple2<>(type, component));
    }
  }

  /** Will regenerate the chat lines as soon as possible. */
  public void refreshChat() {
    this.refreshChat = true;
  }

  public void deleteComponentFromChat(Text component) {
    synchronized (this.pendingChatDeletions) {
      this.pendingChatDeletions.add(component);
    }
  }

  public void replaceComponentInChat(Predicate<Text> predicate, UnaryOperator<Text> componentGenerator) {
    synchronized (this.pendingChatReplacements) {
      this.pendingChatReplacements.add(new Tuple2<>(predicate, componentGenerator));
    }
  }

  public boolean checkCurrentScreen(@Nullable Screen screen) {
    return this.minecraft.currentScreen == screen;
  }

  private void schedule(Consumer<MinecraftClient> work) {
    this.minecraft.execute(() -> work.accept(this.minecraft));
  }

  private void onRenderChatGameOverlay(Event<RenderChatGameOverlayEventData> event) {
    this.flushPendingChatChanges();
  }

  private void flushPendingChatChanges() {
    // save up messages until it becomes available
    if (this.minecraft.inGameHud.getChatHud() == null) {
      return;
    }

    synchronized (this.pendingChatAdditions) {
      for (Tuple2<String, Text> chatItem : this.pendingChatAdditions) {
        String type = chatItem._1;
        Text text = chatItem._2;
        String error = null;
        try {
          MinecraftClient client = MinecraftClient.getInstance();
          client.execute(() -> {
            if (client.player != null) {
              client.player.sendMessage(text, false);
            }
          });
        } catch (Exception e) {
          error = e.getMessage();
        }

        if (error == null) {
          this.logService.logInfo(this, String.format("[Chat %s] %s", type, text.getString()));
        } else {
          this.logService.logError(this, String.format("Could not print chat %s message '%s'. Error: %s", type, text.getString(), error));
        }
      }

      this.pendingChatAdditions.clear();
    }

    synchronized (this.pendingChatDeletions) {
      for (Text chatItem : this.pendingChatDeletions) {
        // todo: implement
        // this.customGuiNewChat.deleteComponent(chatItem);
        this.refreshChat = true;
      }

      this.pendingChatDeletions.clear();
    }

    synchronized (this.pendingChatReplacements) {
      for (Tuple2<Predicate<Text>, UnaryOperator<Text>> args : this.pendingChatReplacements) {
        Predicate<Text> predicate = args._1;
        UnaryOperator<Text> componentGenerator = args._2;
        // todo: implement
        // this.customGuiNewChat.replaceLine(abstractChatLine -> predicate.test(abstractChatLine.getChatComponent()), componentGenerator);
        this.refreshChat = true;
      }

      this.pendingChatReplacements.clear();
    }

    if (this.refreshChat) {
      this.refreshChat = false;
      // todo: implement
      // this.customGuiNewChat.refreshChat(true);
    }
  }
}
