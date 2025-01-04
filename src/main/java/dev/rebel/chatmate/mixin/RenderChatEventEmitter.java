package dev.rebel.chatmate.mixin;

import dev.rebel.chatmate.ChatMate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.network.message.ChatVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class RenderChatEventEmitter {
  @Inject(at = @At("HEAD"), method = "render")
  private void render(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo callbackInfo) {
    if (ChatMate.INSTANCE.minecraft.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN) {
      return;
    }

    boolean shouldContinue = ChatMate.INSTANCE.fabricEventService.emitRenderChatEvent(context, currentTick, mouseX, mouseY, focused);
    if (!shouldContinue) {
      return;
    }
  }
}
