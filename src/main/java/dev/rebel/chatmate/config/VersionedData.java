package dev.rebel.chatmate.config;

import static dev.rebel.chatmate.util.JsonHelpers.parseSerialisedObject;

public class VersionedData {
  public final Object data;

  public VersionedData(Object data) {
    this.data = data;
  }

  public SerialisedConfig parseData() {
    return parseSerialisedObject(this.data, SerialisedConfig.class);
  }
}
