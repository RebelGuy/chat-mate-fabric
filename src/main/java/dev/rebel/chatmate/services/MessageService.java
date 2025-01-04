package dev.rebel.chatmate.services;

import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem.ChatPlatform;
import dev.rebel.chatmate.api.publicObjects.event.PublicPlatformRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;
import dev.rebel.chatmate.api.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.stores.RankApiStore;
import dev.rebel.chatmate.util.EnumHelpers;
import dev.rebel.chatmate.util.TextHelpers;
import dev.rebel.chatmate.util.TextHelpers.ExtractedFormatting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;

import static dev.rebel.chatmate.gui.chat.Styles.*;
import static dev.rebel.chatmate.util.ChatHelpers.joinComponents;
import static dev.rebel.chatmate.util.Objects.firstNonNull;
import static dev.rebel.chatmate.util.Objects.firstOrNull;

public class MessageService {
  private static final Text INFO_PREFIX = styledText("ChatMate>", INFO_MSG_PREFIX_STYLE.get());
  private static final int NEW_USER_THRESHOLD_DAYS = 1;

  private final Random random = new Random();
  private final LogService logService;
  private final TextRenderer textRenderer; // todo: implement
  private final Object dimFactory; // todo: implement
  private final DonationService donationService;
  private final RankApiStore rankApiStore;
  private final Object chatComponentRenderer; // todo: implement
  private final DateTimeService dateTimeService;

  public MessageService(LogService logService, TextRenderer textRenderer, Object dimFactory, DonationService donationService, RankApiStore rankApiStore, Object chatComponentRenderer, DateTimeService dateTimeService) {
    this.logService = logService;
    this.textRenderer = textRenderer;
    this.dimFactory = dimFactory;
    this.donationService = donationService;
    this.rankApiStore = rankApiStore;
    this.chatComponentRenderer = chatComponentRenderer;
    this.dateTimeService = dateTimeService;
  }

  public Text getErrorMessage(String msg) {
    List<Text> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(msg, ERROR_MSG_STYLE.get()));
    return joinComponents(" ", list);
  }

  public Text getInfoMessage(String msg) {
    List<Text> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(msg, INFO_MSG_STYLE.get()));
    return joinComponents(" ", list);
  }

  public Text getSmallLevelUpMessage(PublicUser user, int newLevel) {
    List<Text> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(" ", INFO_MSG_STYLE.get()));
    list.add(this.getUserComponent(user));
    list.add(styledText(" has just reached level " + newLevel + "!", INFO_MSG_STYLE.get()));
    return joinComponents("", list);
  }

  public Text getLargeLevelUpMessage(PublicUser user, int newLevel) {
    List<Text> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(this.getLargeLevelUpIntro(user, newLevel));
    list.add(this.getLargeLevelUpBody(user, newLevel));
    list.add(this.getLargeLevelUpOutro(user, newLevel));
    return joinComponents(" ", list);
  }

  // todo: implement
//  public Text getRankedEntryMessage(PublicRankedUser entry, boolean deEmphasise, int rankDigits, int levelDigits, Dim nameWidth, Dim messageWidth) {
//    // example:
//    // #24 ShiroTheS... 41 |⣿⣿⣿⣿⣿     | 42
//    // rank name levelStart barStart barFilled barBlank barEnd levelEnd
//
//    Dim padding = this.dimFactory.fromGui(4);
//    Dim zero = this.dimFactory.zeroGui();
//
//    Dim rankNumberWidth = this.fontEngine.getStringWidthDim("#" + String.join("", Collections.nCopies(rankDigits, "4")));
//    PrecisionLayout rankLayout = new PrecisionLayout(zero, rankNumberWidth, PrecisionAlignment.RIGHT);
//    String rankText = "#" + entry.rank;
//    Dim x = rankNumberWidth.plus(padding);
//
//    PrecisionLayout nameLayout = new PrecisionLayout(x, nameWidth, PrecisionAlignment.LEFT);
//    x = x.plus(nameWidth).plus(padding);
//
//    Dim levelNumberWidth = this.fontEngine.getStringWidthDim(String.join("", Collections.nCopies(levelDigits, "4")));
//    PrecisionLayout levelStartLayout = new PrecisionLayout(x, levelNumberWidth, PrecisionAlignment.CENTRE);
//    String levelStart = String.valueOf(entry.user.levelInfo.level);
//    x = x.plus(levelNumberWidth).plus(padding);
//
//    String barStart = "|";
//    Dim barStartWidth = this.fontEngine.getStringWidthDim(barStart);
//    PrecisionLayout barStartLayout = new PrecisionLayout(x, barStartWidth, PrecisionAlignment.RIGHT);
//    x = x.plus(barStartWidth); // no padding
//
//    String barEnd = "|";
//    Dim barEndWidth = this.fontEngine.getStringWidthDim(barEnd).plus(padding);
//
//    Dim barBodyWidth = messageWidth.minus(x).minus(barEndWidth).minus(levelNumberWidth);
//    Dim fillWidth = barBodyWidth.times(entry.user.levelInfo.levelProgress);
//    String filledBar = stringWithWidthDim(this.fontEngine, "", "", '⣿', fillWidth) + "⣿";
//    PrecisionLayout filledBarLayout = new PrecisionLayout(x, fillWidth, PrecisionAlignment.LEFT, "");
//    x = x.plus(fillWidth); // no padding
//
//    Dim emptyBarWidth = barBodyWidth.minus(fillWidth);
//    String emptyBar = "";
//    PrecisionLayout emptyBarLayout = new PrecisionLayout(x, emptyBarWidth, PrecisionAlignment.LEFT);
//    x = x.plus(emptyBarWidth); // no padding
//
//    PrecisionLayout barEndLayout = new PrecisionLayout(x, barEndWidth, PrecisionAlignment.LEFT);
//    x = x.plus(barEndWidth); // padding already included
//
//    PrecisionLayout levelEndLayout = new PrecisionLayout(x, levelNumberWidth, PrecisionAlignment.CENTRE);
//    String levelEnd = String.valueOf(entry.user.levelInfo.level + 1);
//
//    List<Tuple2<PrecisionLayout, IChatComponent>> list = new ArrayList<>();
//    list.add(new Tuple2<>(rankLayout, styledText(rankText, deEmphasise ? INFO_MSG_STYLE.get() : GOOD_MSG_STYLE.get())));
//    list.add(new Tuple2<>(nameLayout, this.getUserComponent(entry.user, Font.fromChatStyle(deEmphasise ? INFO_MSG_STYLE.get() : VIEWER_NAME_STYLE.get(), this.dimFactory), entry.user.getDisplayName(), true, !deEmphasise, false, false)));
//    list.add(new Tuple2<>(levelStartLayout, styledText(levelStart, deEmphasise ? INFO_MSG_STYLE.get() : getLevelStyle(entry.user.levelInfo.level))));
//    list.add(new Tuple2<>(barStartLayout, styledText(barStart, INFO_MSG_STYLE.get())));
//    list.add(new Tuple2<>(filledBarLayout, styledText(filledBar, INFO_MSG_STYLE.get())));
//    list.add(new Tuple2<>(emptyBarLayout, styledText(emptyBar, INFO_MSG_STYLE.get())));
//    list.add(new Tuple2<>(barEndLayout, styledText(barEnd, INFO_MSG_STYLE.get())));
//    list.add(new Tuple2<>(levelEndLayout, styledText(levelEnd, deEmphasise ? INFO_MSG_STYLE.get() : getLevelStyle(entry.user.levelInfo.level + 1))));
//    return new PrecisionChatComponent(list);
//  }

  // todo: implement
//  public Text getChannelSearchResultMessage(Dim messageWidth, PublicUser user, @Nullable PublicChannel channel, boolean deEmphasise) {
//    // if there is no registered user, we know that this is the only channel attached to the user and thus we are returning the main message including all details
//    boolean isMainMessage = channel == null || user.registeredUser == null;
//    String displayName = channel == null ? firstNonNull(user.registeredUser.displayName, user.registeredUser.username) : channel.displayName;
//    Dim left = this.dimFactory.fromGui(4);
//
//    List<Tuple2<PrecisionLayout, IChatComponent>> layouts = new ArrayList<>();
//
//    // level - only for the primary user
//    Dim levelNumberWidth = this.fontEngine.getStringWidthDim("444");
//    if (isMainMessage) {
//      String level = String.valueOf(user.levelInfo.level);
//      PrecisionLayout levelLayout = new PrecisionLayout(left, levelNumberWidth, PrecisionAlignment.RIGHT);
//      layouts.add(new Tuple2<>(levelLayout, styledText(level, getLevelStyle(user.levelInfo.level))));
//    }
//    left = left.plus(levelNumberWidth);
//
//    // platform - only for channels
//    if (channel != null) {
//      PlatformViewerTagComponent platform = new PlatformViewerTagComponent(this.dimFactory, channel.platform == PublicChannel.Platform.YOUTUBE ? ChatPlatform.Youtube : ChatPlatform.Twitch);
//      ImageChatComponent imageChatComponent = (ImageChatComponent) platform.getComponent();
//      Dim platformWidth = imageChatComponent.getRequiredWidth(this.fontEngine.FONT_HEIGHT_DIM);
//      PrecisionLayout platformLayout = new PrecisionLayout(left, platformWidth, PrecisionAlignment.LEFT);
//      left = left.plus(platformWidth);
//      layouts.add(new Tuple2<>(platformLayout, platform));
//    }
//
//    // name
//    PrecisionLayout nameLayout = new PrecisionLayout(left.plus(this.dimFactory.fromGui(4)), messageWidth, PrecisionAlignment.LEFT);
//    Font font = Font.fromChatStyle(deEmphasise ? INFO_MSG_STYLE.get() : VIEWER_NAME_STYLE.get(), this.dimFactory);
//    IChatComponent component = this.getUserComponent(user, font, displayName, true, true, !isMainMessage, !isMainMessage);
//    layouts.add(new Tuple2<>(nameLayout, component));
//
//    return new PrecisionChatComponent(layouts);
//  }

//  public IChatComponent getPaginationFooterMessage(Dim messageWidth, int currentPage, int maxPage, @Nullable Runnable onPrevPage, @Nullable Runnable onNextPage) {
//    if (onPrevPage == null && onNextPage == null) {
//      IChatComponent footer = styledText(stringWithWidthDim(this.fontEngine, "", "", '-', messageWidth), INFO_MSG_STYLE.get());
//      PrecisionLayout footerLayout = new PrecisionLayout(this.dimFactory.zeroGui(), messageWidth, PrecisionAlignment.CENTRE);
//      return new PrecisionChatComponent(Arrays.asList(new Tuple2<>(footerLayout, footer)));
//    }
//
//    String padding = "  ";
//    Dim paddingWidth = this.fontEngine.getStringWidthDim(padding);
//
//    String shortPageString = String.valueOf(currentPage);
//    Dim shortPageStringWidth = this.fontEngine.getStringWidthDim(shortPageString);
//
//    String longPageString = String.format("%d of %d", currentPage, maxPage);
//    Dim longPageStringWidth = this.fontEngine.getStringWidthDim(longPageString);
//
//    // we use layouts for the buttons because we want them to be fixed, even if the footer contents change sizes slightly
//    // (e.g. the page number width could change - we don't want the buttons to slightly shift their positions as a result)
//    String prevPageMsg = "<< Previous";
//    Dim prevPageMsgWidth = this.fontEngine.getStringWidthDim(prevPageMsg);
//    PrecisionLayout prevPageLayout = new PrecisionLayout(paddingWidth, messageWidth.minus(paddingWidth), PrecisionAlignment.LEFT);
//
//    String nextPageMsg = "Next >>";
//    Dim nextPageMsgWidth = this.fontEngine.getStringWidthDim(nextPageMsg);
//    PrecisionLayout nextPageLayout = new PrecisionLayout(this.dimFactory.zeroGui(), messageWidth.minus(paddingWidth), PrecisionAlignment.RIGHT);
//
//    // we now have to decide what we want to render in the centre of the footer. this is how much free room we have:
//    Dim interiorWidth = messageWidth.minus(paddingWidth.plus(prevPageMsgWidth).plus(paddingWidth).plus(paddingWidth).plus(nextPageMsgWidth).plus(paddingWidth));
//
//    String interior;
//    if (interiorWidth.lt(shortPageStringWidth)) {
//      // fill with `-`
//      interior = stringWithWidthDim(this.fontEngine, "", "", '-', interiorWidth);
//    } else if (interiorWidth.lt(longPageStringWidth)) {
//      interior = shortPageString;
//    } else {
//      interior = longPageString;
//    }
//
//    // now try to also add some `-` fills to the left and right of the interior, if there is enough room.
//    Dim availableToFillSides = interiorWidth.minus(this.fontEngine.getStringWidthDim(interior).plus(paddingWidth).plus(paddingWidth)).over(2);
//    if (availableToFillSides.gte(this.fontEngine.getStringWidthDim("-"))) {
//      String interiorSides = stringWithWidthDim(this.fontEngine, "", "", '-', availableToFillSides);
//      interior = interiorSides + padding + interior + padding + interiorSides;
//    }
//    // centre the interior exactly between the outer buttons
//    PrecisionLayout interiorLayout = new PrecisionLayout(prevPageMsgWidth, messageWidth.minus(prevPageMsgWidth).minus(nextPageMsgWidth), PrecisionAlignment.CENTRE);
//
//    ClickEventWithCallback onPrevClick = new ClickEventWithCallback(this.logService, onPrevPage, true);
//    ClickEventWithCallback onNextClick = new ClickEventWithCallback(this.logService, onNextPage, true);
//
//    ChatComponentText prevComponent = styledText(prevPageMsg, onPrevClick.bind(onPrevPage == null ? INTERACTIVE_STYLE_DISABLED.get() : INTERACTIVE_STYLE.get()));
//    ChatComponentText interiorComponent = styledText(interior, INFO_MSG_STYLE.get());
//    ChatComponentText nextComponent = styledText(nextPageMsg, onNextClick.bind(onNextPage == null ? INTERACTIVE_STYLE_DISABLED.get() : INTERACTIVE_STYLE.get()));
//    return new PrecisionChatComponent(Arrays.asList(
//        new Tuple2<>(prevPageLayout, prevComponent),
//        new Tuple2<>(interiorLayout, interiorComponent),
//        new Tuple2<>(nextPageLayout, nextComponent)
//    ));
//  }

  public Text getNewFollowerMessage(String displayName) {
    List<Text> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(displayName, VIEWER_NAME_STYLE.get()));
    list.add(styledText("has just followed on Twitch!", INFO_MSG_STYLE.get()));
    return joinComponents(" ", list);
  }

  public Text getNewViewerMessage(PublicUser user) {
    List<Text> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText("Welcome", INFO_MSG_STYLE.get()));
    list.add(this.getUserComponent(user));
    list.add(styledText("to the livestream!", INFO_MSG_STYLE.get()));
    return joinComponents(" ", list);
  }

  public Text getRankUpdatedMessage(RankName rankName, boolean isAdded, PublicUser user, @Nullable PublicUser appliedByUser, List<PublicPlatformRank> platformRanks) {
    List<Text> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(Text.literal(" "));

    // hide the punishment indicator
    list.add(this.getUserComponent(user, VIEWER_NAME_STYLE.get(), user.getDisplayName(), false, true, false, false));
    list.add(styledText(" has been ", INFO_MSG_STYLE.get()));
    // list.add(new PlatformRankChatComponent(rankName, isAdded, platformRanks).setChatStyle(HIGHLIGHT_MSG_STYLE.get()));
    if (appliedByUser != null) {
      list.add(styledText(" by ", INFO_MSG_STYLE.get()));
      list.add(this.getUserComponent(appliedByUser, VIEWER_NAME_STYLE.get(), appliedByUser.getDisplayName(), false, true, false, true));
    }
    list.add(styledText(".", INFO_MSG_STYLE.get()));

    return joinComponents("", list);
  }

  private Text getLargeLevelUpIntro(PublicUser user, int newLevel) {
    return pickRandom(INFO_MSG_STYLE.get(),
        "My little rebels... I have news for you.",
        "You won't believe what just happened.",
        "Unbelievable!",
        "And they said it couldn't be done.",
        "Wow!",
        "Would you look at that.",
        "Also in the news:"
      );
  }

  private Text getLargeLevelUpBody(PublicUser user, int newLevel) {
    List<Text> list = new ArrayList<>();
    list.add(this.getUserComponent(user));
    list.add(styledText(" has reached level ", INFO_MSG_STYLE.get()));
    list.add(styledText(String.valueOf(newLevel), getLevelStyle(newLevel)));
    list.add(styledText("!", INFO_MSG_STYLE.get()));
    return joinComponents("", list);
  }

  private Text getLargeLevelUpOutro(PublicUser user, int newLevel) {
    return pickRandom(INFO_MSG_STYLE.get(),
        "Let's celebrate this incredible achievement!",
        "No one would believe it if it wasn't livestreamed!",
        "We respect you.",
        "When level 100?",
        "At this point we can just quit. We have reached the peak. It can't get better than this.",
        "Now maybe it's time to go outside for a bit?",
        "I can't even count that high... " + String.join(", ", IntStream.range(1, newLevel).mapToObj(String::valueOf).toArray(String[]::new)) + "... uhh I give up.",
        "A true rebel among us.",
        "Level 100 is within reach!",
        "Level " + (newLevel + 1) + " is within reach!",
        "Congratulations!",
        styledText("Say 123 if you respect ", INFO_MSG_STYLE.get()).append(this.getUserComponent(user)).append(styledText(".", INFO_MSG_STYLE.get())),
        styledText("Subscribe to ", INFO_MSG_STYLE.get()).append(this.getUserComponent(user)).append(styledText((user.getDisplayName().endsWith("s") ? "'" : "'s") + " YouTube channel for daily let's play videos!", INFO_MSG_STYLE.get()))
      );
  }

  public Text getUserComponent(PublicUser user) {
    return this.getUserComponent(user, null);
  }

  /** Uses the default channel name if no display name is provided. */
  public Text getUserComponent(PublicUser user, @Nullable String displayName) {
    return this.getUserComponent(user, VIEWER_NAME_STYLE.get(), firstOrNull(displayName, user.getDisplayName()), true, true, false, false);
  }

  public Text getUserComponent(PublicUser user, Style style, String displayName, boolean showPunishmentPrefix, boolean useEffects, boolean hideVerificationBadge, boolean hideNewUserIcon) {
    ExtractedFormatting extractedFormatting = TextHelpers.extractFormatting(displayName);
    String unstyledName = extractedFormatting.unformattedText.trim();

    // make sure we don't try to print an empty user name
    if (this.textRenderer.getWidth(unstyledName) == 0) {
      unstyledName = "User " + user.primaryUserId;
    }

    if (showPunishmentPrefix) {
      for (PublicUserRank punishment : user.getActivePunishments()) {
        String prefix;
        if (punishment.rank.name == RankName.MUTE) {
          prefix = "♪ ";
        } else if (punishment.rank.name == RankName.TIMEOUT) {
          prefix = "◔ ";
        } else if (punishment.rank.name == RankName.BAN) {
          prefix = "☠ ";
        } else {
          throw EnumHelpers.<RankName>assertUnreachable(punishment.rank.name);
        }

        unstyledName = prefix + unstyledName;
      }
    }

    // todo: implement
//    boolean showVerificationBadge = !hideVerificationBadge && user.registeredUser != null;
//    boolean showNewUserIcon = !hideNewUserIcon && user.firstSeen > this.dateTimeService.nowPlus(DateTimeService.UnitOfTime.DAY, -NEW_USER_THRESHOLD_DAYS);
//    UserNameChatComponent component = new UserNameChatComponent(
//        this.fontEngine,
//        this.dimFactory,
//        this.donationService,
//        this.rankApiStore,
//        this.chatComponentRenderer,
//        user.primaryUserId,
//        font,
//        unstyledName,
//        useEffects,
//        showVerificationBadge,
//        showNewUserIcon
//    );
//    return new ContainerChatComponent(component, user);
    return Text.literal(unstyledName).setStyle(style);
  }

  public MutableText getRankComponent(List<PublicUserRank> activeRanks) {
    @Nullable RankName rankToShow = EnumHelpers.getFirst(
        dev.rebel.chatmate.util.Collections.map(activeRanks, r -> r.rank.name),
        RankName.OWNER,
        RankName.ADMIN,
        RankName.MOD,
        RankName.MEMBER,
        RankName.SUPPORTER,
        RankName.DONATOR,
        RankName.FAMOUS
    );
    @Nullable PublicUserRank matchingRank = dev.rebel.chatmate.util.Collections.first(activeRanks, r -> r.rank.name == rankToShow);
    String rankText = matchingRank == null ? "VIEWER" : matchingRank.customRankName == null ? matchingRank.rank.displayNameNoun.toUpperCase() : matchingRank.customRankName.trim();
    return styledText(rankText, VIEWER_RANK_STYLE.get());
  }

  /** Ensures the component can be displayed in chat, otherwise replaces it with the provided message. */
  public Text ensureNonempty(Text component, String msgIfEmpty) {
    StringBuilder sb = new StringBuilder();

    for (Text sibling : component.getSiblings()) {
//      if (sibling instanceof ImageChatComponent) {
//        return component;
//      }

      sb.append(sibling.getString());
    }

    String text = sb.toString().trim();
    if (this.textRenderer.getWidth(text) == 0) {
      return styledText(msgIfEmpty, INFO_SUBTLE_MSG_STYLE.get());
    } else {
      return component;
    }
  }

  private Text pickRandom(Style style, Object... stringOrComponent) {
    int r = random.nextInt(stringOrComponent.length);
    Object picked = stringOrComponent[r];
    if (picked instanceof String) {
      return styledText((String)picked, style);
    } else if (picked instanceof Text) {
      return (Text)picked;
    } else {
      throw new RuntimeException("Cannot pick a random message because an input had type " + picked.getClass().getSimpleName());
    }
  }
}
