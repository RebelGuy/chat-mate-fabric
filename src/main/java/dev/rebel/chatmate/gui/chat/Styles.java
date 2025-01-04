package dev.rebel.chatmate.gui.chat;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Supplier;

public class Styles {
  public static final Supplier<Style> VIEWER_RANK_STYLE = () -> Style.EMPTY.withColor(Formatting.DARK_PURPLE).withBold(true);
  public static final Supplier<Style> VIEWER_NAME_STYLE = () -> Style.EMPTY.withColor(Formatting.YELLOW).withBold(false);
//  public static final FontFactory VIEWER_NAME_FONT = df -> Font.fromChatStyle(VIEWER_NAME_STYLE.get(), df);
  public static final Supplier<Style> YOUTUBE_CHANNEL_STYLE = () -> Style.EMPTY.withColor(Formatting.RED).withBold(false);
  public static final Supplier<Style> TWITCH_CHANNEL_STYLE = () -> Style.EMPTY.withColor(Formatting.DARK_PURPLE).withBold(false);
  public static final Supplier<Style> YT_CHAT_MESSAGE_TEXT_STYLE = () -> Style.EMPTY.withColor(Formatting.WHITE);
  public static final Supplier<Style> YT_CHAT_MESSAGE_EMOJI_STYLE = () -> Style.EMPTY.withColor(Formatting.GRAY);
  public static final Supplier<Style> YT_CHAT_MESSAGE_CHEER_STYLE = () -> Style.EMPTY.withColor(Formatting.GRAY);
  public static final Supplier<Style> MENTION_TEXT_STYLE = () -> Style.EMPTY.withColor(Formatting.GOLD);

  public static final Supplier<Style> LEVEL_0_TO_19 = () -> Style.EMPTY.withColor(Formatting.GRAY);
  public static final Supplier<Style> LEVEL_20_TO_39 = () -> Style.EMPTY.withColor(Formatting.BLUE);
  public static final Supplier<Style> LEVEL_40_TO_59 = () -> Style.EMPTY.withColor(Formatting.DARK_GREEN);
  public static final Supplier<Style> LEVEL_60_TO_79 = () -> Style.EMPTY.withColor(Formatting.GOLD);
  public static final Supplier<Style> LEVEL_80_TO_99 = () -> Style.EMPTY.withColor(Formatting.RED);
  public static final Supplier<Style> LEVEL_100_UPWARDS = () -> Style.EMPTY.withColor(Formatting.BLACK);

  public static final Supplier<Style> INFO_MSG_STYLE = () -> Style.EMPTY.withColor(Formatting.GRAY);
  public static final Supplier<Style> INFO_SUBTLE_MSG_STYLE = () -> Style.EMPTY.withColor(Formatting.GRAY).withItalic(true);
  public static final Supplier<Style> GOOD_MSG_STYLE = () -> Style.EMPTY.withColor(Formatting.GREEN);
  public static final Supplier<Style> BAD_MSG_STYLE = () -> Style.EMPTY.withColor(Formatting.RED);
  public static final Supplier<Style> HIGHLIGHT_MSG_STYLE = () -> Style.EMPTY.withColor(Formatting.YELLOW);
  public static final Supplier<Style> ERROR_MSG_STYLE = () -> Style.EMPTY.withColor(Formatting.RED);
  public static final Supplier<Style> INFO_MSG_PREFIX_STYLE = () -> Style.EMPTY.withColor(Formatting.BLUE);

  public static final Supplier<Style> INTERACTIVE_STYLE = () -> Style.EMPTY.withColor(Formatting.BLUE).withUnderline(true);
  public static final Supplier<Style> INTERACTIVE_STYLE_DE_EMPHASISE = () -> Style.EMPTY.withColor(Formatting.DARK_GRAY).withUnderline(true);
  public static final Supplier<Style> INTERACTIVE_STYLE_DISABLED = () -> Style.EMPTY.withColor(Formatting.DARK_GRAY).withUnderline(false);

  public static Style getLevelStyle(Integer level) {
    if (level < 20) {
      return LEVEL_0_TO_19.get();
    } else if (level < 40) {
      return LEVEL_20_TO_39.get();
    } else if (level < 60) {
      return LEVEL_40_TO_59.get();
    } else if (level < 80) {
      return LEVEL_60_TO_79.get();
    } else if (level < 100) {
      return LEVEL_80_TO_99.get();
    } else {
      return LEVEL_100_UPWARDS.get();
    }
  }

  public static MutableText styledText(String text, Style style) {
    MutableText component = Text.literal(text);
    component.setStyle(style);
    return component;
  }

//  @FunctionalInterface
//  public interface FontFactory {
//    Font create(DimFactory df);
//  }
}
