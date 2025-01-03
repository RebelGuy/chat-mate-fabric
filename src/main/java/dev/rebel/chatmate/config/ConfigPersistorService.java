package dev.rebel.chatmate.config;

import dev.rebel.chatmate.services.FileService;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.util.TaskWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;

public class ConfigPersistorService {
  private final static int CURRENT_SCHEMA = 0;

  private final Class<SerialisedConfig> currentSerialisedVersion;
  private final LogService logService;
  private final FileService fileService;
  private final String fileName;
  private @Nullable Timer timer;
  private final int debounceTime = 500;

  public ConfigPersistorService(Class<SerialisedConfig> currentSerialisedVersion, LogService logService, FileService fileService) {
    this.currentSerialisedVersion = currentSerialisedVersion;
    this.logService = logService;
    this.fileService = fileService;
    this.fileName = "config.json";
    this.timer = null;
  }

  public @Nullable SerialisedConfig load() {
    try {
      SerialisedConfig parsed = this.fileService.readObjectFromFile(this.fileName, SerialisedConfig.class);

      if (parsed.getVersion() == CURRENT_SCHEMA) {
        this.logService.logInfo(this, "Parsed and migrated configuration to schema " + CURRENT_SCHEMA);
        return parsed;
      } else {
        this.logService.logError(this, "Failed to parse config (schema " + parsed.getVersion() + ") to schema version " + CURRENT_SCHEMA);
        return null;
      }
    } catch (Exception e) {
      this.logService.logError(this, "Unable to load or migrate configuration:", e.getMessage(), e.getCause());
      return null;
    }
  }

  public void save(SerialisedConfig data) {
    if (this.timer != null) {
      this.timer.cancel();
    }

    this.timer = new Timer();
    this.timer.schedule(new TaskWrapper(() -> this.onSave(data)), this.debounceTime);
  }

  private void onSave(SerialisedConfig data) {
    try {
      this.fileService.writeObjectToFile(this.fileName, data);
    } catch (Exception e) {
      this.logService.logError(this,"Unable to save configuration:", e.getMessage());
    }
  }
}
