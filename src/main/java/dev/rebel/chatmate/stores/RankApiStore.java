package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.proxy.RankEndpointProxy;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.events.models.ConfigEventOptions;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.LruCache;
import dev.rebel.chatmate.util.Memoiser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class RankApiStore {
  private final RankEndpointProxy rankEndpointProxy;

  private final Set<String> loading;
  private final LruCache<String, CopyOnWriteArrayList<PublicUserRank>> cache;
  private final Memoiser memoiser;

  public RankApiStore(RankEndpointProxy rankEndpointProxy, Config config) {
    this.rankEndpointProxy = rankEndpointProxy;

    this.loading = new HashSet<>();
    this.cache = new LruCache<>(100);
    this.memoiser = new Memoiser();

    config.getLoginInfoEmitter().onChange(_info -> this.clear(), new ConfigEventOptions<>(info -> info.loginToken == null));
  }

  public void clear() {
    this.cache.clear();
    this.memoiser.clear();
    this.loading.clear();
  }

  /** This should be called whenever an action of ours ends up (or may end up) affecting a user's ranks. */
  public void invalidateUserRanks(int userId) {
    this.cache.remove(getKey(userId));
  }

  public void loadUserRanks(int userId, Consumer<List<PublicUserRank>> callback, Consumer<Throwable> errorHandler, boolean forceLoad) {
    String key = getKey(userId);
    if (this.cache.has(key) && !forceLoad) {
      callback.accept(this.cache.get(key));

    } else if (this.loading.contains(key)) {
      callback.accept(new ArrayList<>());

    } else {
      this.loading.add(key);
      this.rankEndpointProxy.getRanksAsync(
          userId,
          true,
          res -> {
            this.cache.set(key, new CopyOnWriteArrayList<>(Collections.list(res.ranks)));
            this.loading.remove(key);
            callback.accept(this.cache.get(key));
          }, err -> {
            this.cache.remove(key);
            this.loading.remove(key);
            errorHandler.accept(err);
          });
    }
  }

  public @Nullable Object getStateToken(int userId) {
    return this.cache.get(getKey(userId));
  }

  public @NotNull List<PublicUserRank> getCurrentUserRanks(int userId) {
    String key = getKey(userId);
    if (!this.cache.has(key)) {
      this.loadUserRanks(userId, r -> {}, e -> {}, false);
      return new ArrayList<>();
    }

    List<PublicUserRank> allRanks = this.cache.get(key);
    return this.memoiser.memoise(String.format("%d-current", userId), () -> Collections.filter(allRanks, rank -> rank.isActive), allRanks);
  }

  public @NotNull List<PublicUserRank> getUserRanksAtTime(int userId, long time) {
    String key = getKey(userId);
    if (!this.cache.has(key)) {
      this.loadUserRanks(userId, r -> {}, e -> {}, false);
      return new ArrayList<>();
    }

    List<PublicUserRank> allRanks = this.cache.get(key);
    return Collections.filter(allRanks, rank -> rank.issuedAt <= time && // rank was issued before the requested time
        (rank.expirationTime == null || rank.expirationTime > time) && // and if it expired, it expired after the requested time
        (rank.revokedAt == null || rank.revokedAt > time) // and if it was revoked, it was revoked after the requested time
    );
  }

  private static String getKey(int userId) {
    return String.format("%d", userId);
  }
}
