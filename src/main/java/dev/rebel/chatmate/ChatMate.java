package dev.rebel.chatmate;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.ChatMateWebsocketClient;
import dev.rebel.chatmate.api.proxy.*;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.ConfigPersistorService;
import dev.rebel.chatmate.config.SerialisedConfig;
import dev.rebel.chatmate.events.ChatMateChatService;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.FilterService.FilterFileParseResult;
import dev.rebel.chatmate.stores.*;
import dev.rebel.chatmate.util.ApiPollerFactory;
import dev.rebel.chatmate.util.FileHelpers;
import dev.rebel.chatmate.util.Objects;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatMate implements ModInitializer {
	public static final String MOD_ID = "chat-mate-fabric";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private ChatMateChatService chatMateChatService;
	private Config config;
	private AccountEndpointProxy accountEndpointProxy;

	@Override
	public void onInitialize() {
		try {
			this.initialise();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initialise() throws Exception {
		String currentDir = System.getProperty("user.dir").replace("\\", "/");
		String dataDir = currentDir + "/mods/ChatMate";
		FileService fileService = new FileService(dataDir);
		LogService logService = new LogService(fileService);

		MinecraftClient minecraft = MinecraftClient.getInstance();

		ConfigPersistorService configPersistorService = new ConfigPersistorService(SerialisedConfig.class, logService, fileService);
		this.config = new Config(logService, configPersistorService);
		logService.injectConfig(this.config);

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