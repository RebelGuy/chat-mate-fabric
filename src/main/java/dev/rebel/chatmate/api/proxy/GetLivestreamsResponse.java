package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.publicObjects.livestream.PublicAggregateLivestream;
import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;

public class GetLivestreamsResponse extends ApiResponseBase<GetLivestreamsResponse.GetLivestreamsResponseData> {
  public static class GetLivestreamsResponseData {
    public PublicLivestream[] youtubeLivestreams;
    public PublicLivestream[] twitchLivestreams;
    public PublicAggregateLivestream[] aggregateLivestreams;
  }
}
