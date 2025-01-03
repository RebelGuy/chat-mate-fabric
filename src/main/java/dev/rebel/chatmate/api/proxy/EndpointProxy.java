package dev.rebel.chatmate.api.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.HttpException;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.util.Objects;
import dev.rebel.chatmate.util.RequestBackoff;
import org.jetbrains.annotations.Nullable;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.Objects.ifClass;

public class EndpointProxy {
  private final LogService logService;
  private final ApiRequestService apiRequestService;
  private final Config config;
  private final String basePath;
  private final Gson gson;
  private final ConcurrentMap<String, RequestBackoff> requestBackoffs;
  private final HttpClient client;

  private int requestId = 0;

  public EndpointProxy(LogService logService, ApiRequestService apiRequestService, Config config, String basePath) {
    this.logService = logService;
    this.apiRequestService = apiRequestService;
    this.config = config;
    this.basePath = basePath;
    this.gson = new GsonBuilder()
        .serializeNulls()
        .create();
    this.requestBackoffs = new ConcurrentHashMap<>();

    // must be version 1.1, else the server returns 400 without ever reaching user code
    this.client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build();
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(method, path, null, returnClass, callback, errorHandler, true);
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Object data, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(method, path, data, returnClass, callback, errorHandler, true);
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler, boolean isActiveRequest) {
    this.makeRequestAsync(method, path, null, returnClass, callback, errorHandler, isActiveRequest);
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Object data, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler, boolean isActiveRequest) {
    this.requestBackoffs.forEach((key, value) -> {
      if (value.canDispose()) {
        this.requestBackoffs.remove(key);
      }
    });

    if (!this.requestBackoffs.containsKey(path)) {
      this.requestBackoffs.put(path, new RequestBackoff());
    }
    RequestBackoff backoff = this.requestBackoffs.get(path);
    backoff.wait(() -> {
      // we got there eventually.....
      CompletableFuture.supplyAsync(() -> {
        Runnable onComplete = isActiveRequest ? this.apiRequestService.onNewRequest() : () -> {
        };
        try {
          Data result = this.makeRequest(method, path, returnClass, data);
          backoff.onSuccess();
          onComplete.run();
          return result;

        } catch (Exception e) {
          if (Objects.ifClass(ChatMateApiException.class, e, ex -> ex.apiResponseError.errorCode == 401)) {
            this.config.getLoginInfoEmitter().set(new Config.LoginInfo(null, null, null));
          }

          backoff.onError(e);
          onComplete.run();
          if (errorHandler != null) {
            errorHandler.accept(e);
          }
          return null;
        }
      }).thenAccept(res -> {
        if (res != null) {
          // if there is an exception here, it will bubble up
          callback.accept(res);
        }
      });
    });
  }

  private <Data, Res extends ApiResponseBase<Data>> Data makeRequest(Method method, String path, Class<Res> returnClass, Object data) throws ConnectException, ChatMateApiException, HttpException, Exception {
    int id = ++this.requestId;
    this.logService.logApiRequest(this, id, method, this.basePath + path);

    ApiResponse result;
    try {
      result = downloadString(method, path, data);
    } catch (ConnectException e) {
      String message = "Failed to connect to the server - is it running? " + e.getMessage();
      this.logService.logApiResponse(this, id, null, true, message);
      throw e;
    } catch (JsonSyntaxException e) {
      String message = "Failed to parse JSON response to " + returnClass.getSimpleName() + " - has the schema changed? " + e.getMessage();
      this.logService.logApiResponse(this, id, null, true, message);
      throw e;
    } catch (Exception e) {
      String message = "Failed to get response. " + e.getMessage();
      this.logService.logApiResponse(this, id, null, true, message);
      throw e;
    }

    this.logService.logApiResponse(this, id, result.statusCode, !result.success, result.responseBody);
    try {
      Res parsed = this.parseResponse(result.responseBody, returnClass);
      parsed.assertIntegrity();
      if (!parsed.success) {
        throw new ChatMateApiException(parsed.error, result.loginToken);
      } else {
        return parsed.data;
      }
    } catch (ChatMateApiException e) {
      this.logService.logError(this, "API response for", method, this.basePath + path, "failed:", e);
      throw e;
    } catch (Exception e) {
      // errors reaching here are most likely due to a response with an unexpected format, e.g. 502 errors.
      this.logService.logError(this, "API response for", method, this.basePath + path, "failed:", e);
      throw new HttpException(e.getMessage(), result.statusCode, result.responseBody);
    }
  }

  private ApiResponse downloadString(Method method, String path, Object data) throws Exception {
    URI uri = URI.create(this.basePath + path);

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
        .method(method.toString(), HttpRequest.BodyPublishers.noBody());

    // some requests don't require these headers, but if we have it we might as well add it all the time
    @Nullable String loginToken = this.apiRequestService.getLoginToken();
    if (loginToken != null) {
      requestBuilder.header("X-Login-Token", loginToken);
    }

    @Nullable String streamer = this.apiRequestService.getStreamer();
    if (streamer != null) {
      requestBuilder.header("X-Streamer", streamer);
    }

    if (method != Method.GET && data != null) {
      String json = this.gson.toJson(data);
      byte[] input = json.getBytes(StandardCharsets.UTF_8);

      requestBuilder.header("Content-Type", "application/json");
      requestBuilder.header("charset", "utf-8");
      requestBuilder.header("Content-Length", String.valueOf(input.length));
      requestBuilder.method(method.toString(), HttpRequest.BodyPublishers.ofByteArray(input));
    }

    HttpRequest request = requestBuilder.build();
    HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
    boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
    return new ApiResponse(success, response.statusCode(), response.body(), loginToken);
  }

  private <T extends ApiResponseBase<?>> T parseResponse(String response, Class<T> returnClass) throws Exception {
    T parsed = this.gson.fromJson(response, returnClass);
    if (parsed == null) {
      throw new Exception("Parsed response is null - is the JSON conversion implemented correctly?");
    }

    return parsed;
  }

  public static String getApiErrorMessage(Throwable e) {
    String msg;
    if (e instanceof ConnectException) {
      msg = "Unable to connect.";
    } else if (e instanceof ChatMateApiException) {
      ChatMateApiException error = (ChatMateApiException) e;
      msg = error.apiResponseError.message;
      if (msg == null) {
        msg = error.apiResponseError.errorType;
      }
    } else if (ifClass(HttpException.class, e, ex -> ex.statusCode != 200)) {
      msg = String.format("Something went wrong (code %d).", ((HttpException)e).statusCode);
    } else {
      msg = "Something went wrong.";
    }

    return msg;
  }

  private static class ApiResponse {
    public final boolean success;
    public final int statusCode;
    public final String responseBody;
    public final @Nullable String loginToken;

    public ApiResponse(boolean success, @Nullable Integer statusCode, String responseBody, @Nullable String loginToken) {
      this.success = success;
      this.statusCode = statusCode;
      this.responseBody = responseBody;
      this.loginToken = loginToken;
    }
  }

  public enum Method { GET, POST, PATCH, DELETE }
}
