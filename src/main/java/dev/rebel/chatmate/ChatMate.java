package dev.rebel.chatmate;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.ChatMateWebsocketClient;
import dev.rebel.chatmate.api.proxy.*;
import dev.rebel.chatmate.api.publicObjects.streamer.PublicStreamerSummary;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.ConfigPersistorService;
import dev.rebel.chatmate.config.SerialisedConfig;
import dev.rebel.chatmate.events.ChatMateChatService;
import dev.rebel.chatmate.events.ChatMateEventService;
import dev.rebel.chatmate.events.FabricEventService;
import dev.rebel.chatmate.events.MinecraftChatEventService;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.FilterService.FilterFileParseResult;
import dev.rebel.chatmate.stores.*;
import dev.rebel.chatmate.util.ApiPollerFactory;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.FileHelpers;
import dev.rebel.chatmate.util.Objects;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static dev.rebel.chatmate.util.Objects.firstNonNull;

public class ChatMate implements ModInitializer {
	public static final String MOD_ID = "chat-mate-fabric";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ChatMate INSTANCE = null;
	public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();

	public ChatMateChatService chatMateChatService;
	public Config config;
	public AccountEndpointProxy accountEndpointProxy;
	public FabricEventService fabricEventService;
	public MinecraftClient minecraft;
	public McChatService mcChatService;

	@Override
	public void onInitialize() {
		INSTANCE = this;

		MinecraftClient.getInstance().execute(() -> {
			try {
				this.initialise();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void initialise() throws Exception {
		String currentDir = System.getProperty("user.dir").replace("\\", "/");
		String dataDir = currentDir + "/mods/ChatMate";
		FileService fileService = new FileService(dataDir);
		LogService logService = new LogService(fileService);

		this.minecraft = MinecraftClient.getInstance();

		ConfigPersistorService configPersistorService = new ConfigPersistorService(SerialisedConfig.class, logService, fileService);
		this.config = new Config(logService, configPersistorService);
		logService.injectConfig(this.config);

		this.fabricEventService = new FabricEventService(logService, this.minecraft);

		String environmentPath = "/environment.yml";
		Environment environment = Environment.parseEnvironmentFile(FileHelpers.readLines(environmentPath));

		String apiPath = environment.serverUrl + "/api";
		DateTimeService dateTimeService = new DateTimeService();
		ApiRequestService apiRequestService = new ApiRequestService(config);
		ChatEndpointProxy chatEndpointProxy = new ChatEndpointProxy(logService, apiRequestService, config, apiPath);
		this.accountEndpointProxy = new AccountEndpointProxy(logService, apiRequestService, config, apiPath);
		this.validateLoginDetails();

		StreamerEndpointProxy streamerEndpointProxy = new StreamerEndpointProxy(logService, apiRequestService, config, apiPath);
		UserEndpointProxy userEndpointProxy = new UserEndpointProxy(logService, apiRequestService, config, apiPath);
		ExperienceEndpointProxy experienceEndpointProxy = new ExperienceEndpointProxy(logService, apiRequestService, config, apiPath);
		PunishmentEndpointProxy punishmentEndpointProxy = new PunishmentEndpointProxy(logService, apiRequestService, config, apiPath);
		RankEndpointProxy rankEndpointProxy = new RankEndpointProxy(logService, apiRequestService, config, apiPath);
		DonationEndpointProxy donationEndpointProxy = new DonationEndpointProxy(logService, apiRequestService, config, apiPath);
		LivestreamEndpointProxy livestreamEndpointProxy = new LivestreamEndpointProxy(logService, apiRequestService, config, apiPath);

		LivestreamApiStore livestreamApiStore = new LivestreamApiStore(livestreamEndpointProxy, config);
		DonationApiStore donationApiStore = new DonationApiStore(donationEndpointProxy, config);
		RankApiStore rankApiStore = new RankApiStore(rankEndpointProxy, config);
		CommandApiStore commandApiStore = new CommandApiStore(chatEndpointProxy, config);
		StreamerApiStore streamerApiStore = new StreamerApiStore(streamerEndpointProxy, config);

		String filterPath = "/assets/chat-mate-fabric/filter.txt";
		FilterFileParseResult parsedFilterFile = FilterService.parseFilterFile(FileHelpers.readLines(filterPath));
		FilterService filterService = new FilterService(parsedFilterFile.filtered, parsedFilterFile.whitelisted);

		ApiPollerFactory apiPollerFactory = new ApiPollerFactory(logService, config, streamerApiStore);
		ChatMateWebsocketClient chatMateWebsocketClient = new ChatMateWebsocketClient(logService, environment, config);
		this.chatMateChatService = new ChatMateChatService(logService, chatEndpointProxy, apiPollerFactory, config, dateTimeService, chatMateWebsocketClient);

		MinecraftChatEventService minecraftChatEventService = new MinecraftChatEventService(logService);
		MinecraftProxyService minecraftProxyService = new MinecraftProxyService(minecraft, logService, fabricEventService);
		SoundService soundService = new SoundService(logService, minecraftProxyService, config);
		ChatMateEventService chatMateEventService = new ChatMateEventService(logService, streamerEndpointProxy, apiPollerFactory, config, dateTimeService, chatMateWebsocketClient);
		DonationService donationService = new DonationService(dateTimeService, donationApiStore, livestreamApiStore, rankApiStore, chatMateEventService);
		MessageService messageService = new MessageService(logService, this.minecraft.textRenderer, null, donationService, rankApiStore, null, dateTimeService);

		this.mcChatService = new McChatService(minecraftProxyService,
				logService,
				filterService,
				soundService,
				chatMateEventService,
				messageService,
				null,
				config,
				chatMateChatService,
				this.minecraft.textRenderer,
				minecraftChatEventService
		);

		apiRequestService.setGetStreamers(() -> firstNonNull(streamerApiStore.getData(), new ArrayList<>()));

		config.getChatMateEnabledEmitter().onChange(e -> {
			boolean enabled = e.getData();
			if (enabled) {
				String releaseLabel = "";
				if (environment.env == Environment.Env.LOCAL) {
					releaseLabel = "Local build ";
				} else if (environment.env == Environment.Env.DEBUG) {
					releaseLabel = "Sandbox build ";
				}

				mcChatService.printInfo(String.format("Enabled. %s%s", releaseLabel, environment.buildName));
			}
		});

		// disable ChatMate when logging out, since we hide the checkbox UI to do this manually
		config.getLoginInfoEmitter().onChange(e -> {
			if (e.getData().username == null) {
				config.getChatMateEnabledEmitter().set(false);
			}
		});

		// to make our life easier, auto enable when in a dev environment or if a livestream is running
		if (IS_DEV) {
			config.getChatMateEnabledEmitter().set(true);
		} else if (config.getLoginInfoEmitter().get().username != null) {
			streamerEndpointProxy.getStreamersAsync(streamerRes -> {

				String username = config.getLoginInfoEmitter().get().username;
				@Nullable PublicStreamerSummary streamer = Collections.first(Collections.list(streamerRes.streamers), str -> java.util.Objects.equals(str.username, username));
				if (streamer != null && (streamer.isYoutubeLive() || streamer.isTwitchLive())) {
					logService.logInfo(this, "Auto-enabling ChatMate since the logged in user is a streamer that is currently live");
					config.getChatMateEnabledEmitter().set(true);
				}

			}, streamerErr -> {
				logService.logError(this, "Unable to get streamer list during initialisation", streamerErr);
			}, false);
		}
	}

	private void validateLoginDetails() {
		String loginToken = this.config.getLoginInfoEmitter().get().loginToken;
		if (loginToken == null) {
			return;
		}

		// check whether the login token is still valid - if it isn't, we set it to null
		this.accountEndpointProxy.authenticateAsync(
				r -> this.config.getLoginInfoEmitter().set(new Config.LoginInfo(r.username, r.displayName, loginToken)),
				e -> {
					if (Objects.ifClass(ChatMateApiException.class, e, ex -> ex.apiResponseError.errorCode == 401)) {
						this.config.getLoginInfoEmitter().set(new Config.LoginInfo(null, null, null));
					}
				}
		);
	}
}