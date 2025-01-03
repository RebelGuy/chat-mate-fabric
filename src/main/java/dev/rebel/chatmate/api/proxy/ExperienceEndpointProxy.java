package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.experience.GetLeaderboardResponse;
import dev.rebel.chatmate.api.models.experience.GetLeaderboardResponse.GetLeaderboardResponseData;
import dev.rebel.chatmate.api.models.experience.GetRankResponse;
import dev.rebel.chatmate.api.models.experience.GetRankResponse.GetRankResponseData;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceRequest;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceResponse;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceResponse.ModifyExperienceResponseData;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.LogService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class ExperienceEndpointProxy extends EndpointProxy {
  public ExperienceEndpointProxy(LogService logService, ApiRequestService apiRequestService, Config config, String basePath) {
    super(logService, apiRequestService, config, basePath + "/experience");
  }

  public void getLeaderboardAsync(Consumer<GetLeaderboardResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/leaderboard", GetLeaderboardResponse.class, callback, errorHandler);
  }

  public void getRankAsync(@NotNull Integer channelId, Consumer<GetRankResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/rank?id=%d", channelId);
    this.makeRequestAsync(Method.GET, url, GetRankResponse.class, callback, errorHandler);
  }

  public void modifyExperienceAsync(@NotNull ModifyExperienceRequest request, Consumer<ModifyExperienceResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/modify", request, ModifyExperienceResponse.class, callback, errorHandler);
  }
}
