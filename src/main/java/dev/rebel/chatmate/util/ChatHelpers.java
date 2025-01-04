package dev.rebel.chatmate.util;

import dev.rebel.chatmate.util.TextHelpers.StringMask;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static dev.rebel.chatmate.gui.chat.Styles.styledText;

public class ChatHelpers {

  /** Overwrites the style in one or more parts of the text. */
  public static Collection<Text> styledTextWithMask(String text, Style baseStyle, StringMask mask, Style maskStyle) {
    Collection<Text> collection = new ArrayList<>();
    char[] chars = text.toCharArray();

    boolean isSnippetMasked = false;
    StringBuilder snippet = new StringBuilder();

    // keep collecting chars until the mask value flips
    for (int i = 0; i <= text.length(); i++) {
      if (i == text.length() || mask.mask[i] != isSnippetMasked) {
        // flush previous
        if (snippet.length() > 0) {
          Style style = isSnippetMasked ? maskStyle : baseStyle;
          collection.add(styledText(snippet.toString(), style));

          if (i == text.length()) {
            continue;
          }
        }

        snippet = new StringBuilder();
        isSnippetMasked = !isSnippetMasked;
      }

      snippet.append(chars[i]);
    }

    return collection;
  }

  public static Text joinComponents(String joinText, List<Text> components) {
    return joinComponents(joinText, components, null);
  }

  /** Does not add the `joinText` before the ignored components. */
  public static Text joinComponents(String joinText, List<Text> components, @Nullable Predicate<Text> ignoreComponents) {
    Text result = Text.empty();

    boolean isFirst = true;
    for (Text comp: components) {
      if (!isFirst && (ignoreComponents == null || !ignoreComponents.test(comp))) {
        if (joinText.length() > 0) {
          result = result.copy().append(joinText);
        }
      } else {
        isFirst = false;
      }

      result = result.copy().append(comp);
    }

    return result;
  }

  // todo: implement
//  public static String stringWithWidth(FontEngine fontEngine, String msg, String truncationSuffix, char paddingChar, Dim maxWidth) {
//    Dim currentWidth = fontEngine.getStringWidthDim(msg);
//    if (currentWidth.lte(maxWidth)) {
//      Dim padWidth = fontEngine.getCharWidth(paddingChar);
//      int paddingRequired = (int)maxWidth.minus(currentWidth).over(padWidth);
//      String padding = String.join("", Collections.nCopies(paddingRequired, String.valueOf(paddingChar)));
//      return msg + padding;
//
//    } else {
//      Dim truncationWidth = fontEngine.getStringWidthDim(truncationSuffix);
//      return fontEngine.trimStringToWidth(msg, maxWidth.minus(truncationWidth), new Font(), false) + truncationSuffix;
//    }
//  }

  // todo: implement
  /** Preferred over `stringWithWidth` because it leads to more accurate results that are exactly consistent with the render engine. */
//  public static String stringWithWidthDim(FontEngine fontEngine, String msg, String truncationSuffix, char paddingChar, Dim maxWidth) {
//    Dim currentWidth = fontEngine.getStringWidthDim(msg);
//    if (currentWidth.lte(maxWidth)) {
//      Dim padWidth = fontEngine.getStringWidthDim("" + paddingChar);
//      int paddingRequired = (int)Math.floor(maxWidth.minus(currentWidth).over(padWidth));
//      String padding = String.join("", Collections.nCopies(paddingRequired, String.valueOf(paddingChar)));
//      return msg + padding;
//
//    } else {
//      Dim truncationWidth = fontEngine.getStringWidthDim(truncationSuffix);
//      return fontEngine.trimStringToWidth(msg, maxWidth.minus(truncationWidth), new Font(), false) + truncationSuffix;
//    }
//  }

  // todo: implement
//  public static char getThinnestCharacter(FontEngine fontEngine) {
//    char hairSpace = '\u200a'; // ?
//    char thinSpace = '\u2009'; // ?
//    char punctuationSpace = '\u2008'; // 2?
//    char brailleBlank = '\u2800'; // space for blind people
//    char dot = 'ᐧ'; // 2
//    char space = ' '; // 4 - worst-case scenario
//
//    return dot;
//  }

  // todo: implement
//  public static class ClickEventWithCallback extends ClickEvent {
//    private final static Action IDENTIFIER_ACTION = Action.RUN_COMMAND;
//    private final static String IDENTIFIER_VALUE = "ChatMate Custom Click Event";
//
//    private final LogService logService;
//    private Runnable callback;
//    private final boolean singleUse;
//
//    public ClickEventWithCallback(LogService logService, Runnable callback, boolean singleUse) {
//      super(IDENTIFIER_ACTION, IDENTIFIER_VALUE);
//      this.logService = logService;
//      this.callback = callback;
//      this.singleUse = singleUse;
//    }
//
//    public ChatStyle bind(ChatStyle style) {
//      return style.setChatClickEvent(this);
//    }
//
//    public static boolean isClickEventWithCallback(ClickEvent event) {
//      return event.getAction() == IDENTIFIER_ACTION && Objects.equals(event.getValue(), IDENTIFIER_VALUE);
//    }
//
//    /** Returns true if the click was handled successfully. */
//    public boolean handleClick() {
//      if (this.callback == null) {
//        return false;
//      } else {
//        boolean success;
//        try {
//          this.callback.run();
//          success = true;
//        } catch (Exception e) {
//          this.logService.logError(this, e);
//          success = false;
//        }
//
//        if (this.singleUse) {
//          this.callback = null;
//        }
//        return success;
//      }
//    }
//
//    public boolean isClickable() {
//      return this.callback != null;
//    }
//  }
}
