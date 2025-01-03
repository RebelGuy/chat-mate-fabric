package dev.rebel.chatmate.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LruCache<TKey, TValue> {
  private List<TKey> keys;
  private Map<TKey, TValue> map;
  private final int limit;

  public LruCache(int limit) {
    this.keys = new ArrayList<>(limit);
    this.map = new HashMap<>(limit);
    this.limit = limit;
  }

  public TValue getOrSet(TKey key, Supplier<TValue> valueGetter) {
    // re-add the key to the beginning of the list
    this.removeFromKeys(key);
    this.keys.add(key);

    if (!this.map.containsKey(key)) {
      this.map.put(key, valueGetter.get());
      this.pruneCache();
    }

    return this.map.get(key);
  }

  public void set(TKey key, TValue value) {
    this.removeFromKeys(key);
    this.keys.add(key);
    this.map.put(key, value);
    this.pruneCache();
  }

  public boolean has(TKey key) {
    return this.keys.contains(key);
  }

  public TValue get(TKey key) {
    return this.map.get(key);
  }

  public List<TKey> getKeys() {
    return this.keys;
  }

  public void remove(TKey key) {
    this.removeFromKeys(key);
    this.map.remove(key);  }

  public void remove(List<TKey> keys) {
    keys.forEach(this::remove);
  }

  public void clear() {
    this.keys.clear();
    this.map.clear();
  }

  private void pruneCache() {
    while (this.keys.size() > this.limit) {
      TKey firstKey = Collections.first(this.keys);
      this.removeFromKeys(firstKey);
      this.map.remove(firstKey);
    }
  }

  private void removeFromKeys(TKey key) {
    while (this.keys.remove(key));
  }
}
