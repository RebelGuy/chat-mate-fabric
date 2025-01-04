package dev.rebel.chatmate.services;

import dev.rebel.chatmate.config.Config;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class SoundService {
  private final LogService logService;
  private final MinecraftProxyService minecraftProxyService;
  private final Config config;

  public SoundService(LogService logService, MinecraftProxyService minecraftProxyService, Config config) {
    // see https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/mapping-and-modding-tutorials/2213619-1-8-all-playsound-sound-arguments for all possible ResourceLocations.
    // there is also a list in .minecraft/assets/indexes/1.8.json
    // testing can be done in-game with commands enabled using
    // /playsound <resource.location> @a ~ ~ ~ <volume> <pitch>
    // note that 0 <= volume <= 1 and 0.5 <= pitch <= 2

    this.logService = logService;
    this.minecraftProxyService = minecraftProxyService;
    this.config = config;
  }

  public void playDing() {
    this.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.5F);
  }

  public void playButtonSound() { this.playSound(SoundEvents.UI_BUTTON_CLICK.value()); }

  public void playLevelUp(float pitch) {
    this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, pitch);
  }

  public void playDragonKill(float pitch) {
    this.playSound(SoundEvents.ENTITY_ENDER_DRAGON_DEATH, pitch);
  }

  public void playFireworkBlast(float pitch) {
    this.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, pitch);
  }

  public void playThunder(float pitch) {
    this.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, pitch);
  }

  private void playSound(SoundEvent sound) {
    this.playSound(sound, 1);
  }
  private void playSound(SoundEvent sound, float pitch) {
    if (!this.config.getSoundEnabledEmitter().get()) {
      return;
    }

    if (pitch < 0.5f) {
      pitch = 0.5f;
    } else if (pitch > 2) {
      pitch = 2;
    }

    this.logService.logInfo(this, "Playing sound", sound.id(), "with pitch", pitch);
    this.minecraftProxyService.playSound(PositionedSoundInstance.ambient(sound, pitch, 1));
  }
}
