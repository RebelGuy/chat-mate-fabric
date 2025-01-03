package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.user.SearchUserRequest;
import dev.rebel.chatmate.api.models.user.SearchUserResponse;
import dev.rebel.chatmate.api.models.user.SearchUserResponse.SearchUserResponseData;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.LogService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class UserEndpointProxy extends EndpointProxy {
  public UserEndpointProxy(LogService logService, ApiRequestService apiRequestService, Config config, String basePath) {
    super(logService, apiRequestService, config, basePath + "/user");
  }

  public void searchUser(@NotNull SearchUserRequest searchRequest, Consumer<SearchUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/search", searchRequest, SearchUserResponse.class, callback, errorHandler);
  }
}
