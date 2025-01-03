package dev.rebel.chatmate.services;

import dev.rebel.chatmate.util.TextHelpers;
import dev.rebel.chatmate.util.TextHelpers.ExtractedFormatting;
import dev.rebel.chatmate.util.TextHelpers.ExtractedFormatting.Format;
import dev.rebel.chatmate.util.TextHelpers.StringMask;
import dev.rebel.chatmate.util.TextHelpers.WordFilter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static dev.rebel.chatmate.util.TextHelpers.getAllOccurrences;

public class FilterService
{
  private final WordFilter[] filtered;
  private final WordFilter[] whitelisted;

  public FilterService(WordFilter[] filtered, WordFilter[] whitelisted) {
    this.filtered = filtered;
    this.whitelisted = whitelisted;
  }

  public String censorNaughtyWords(@NotNull String text) {
    // this is a somewhat naive implementation that is easy to bypass.
    // spaces and punctuation are not treated specially, and it does 
    // allow for wildcard characters and whitelisted words.

    // the advantage of using masks is that we can perform boolean operations
    StringMask profanityMask = filterWords(text, this.filtered);
    StringMask whitelistMask = filterWords(text, this.whitelisted);
    return applyMask(profanityMask.subtract(whitelistMask), text, '*');
  }

  // returns the mask matching the given words (not case sensitive, valid and invalid chat format codes are ignored).
  public static StringMask filterWords(@NotNull String text, WordFilter... words) {
    ExtractedFormatting formatting = TextHelpers.extractFormatting(text);

    // apply the filter on the unformatted text
    text = formatting.unformattedText.toLowerCase();
    StringMask mask = new StringMask(text.length());

    for (WordFilter word: words) {
      ArrayList<Integer> occurrences = getAllOccurrences(text, word, true);
      occurrences.forEach(occ -> mask.updateRange(occ, word.length, true));
    }

    // re-apply formatting to the mask. formatting is never matched by the filter.
    // can't use `mask` because we get an error (thanks Java)
    // `Local variable mask defined in an enclosing scope must be final or effectively final`
    StringMask maskWithFormatting = mask;
    for (Format format: formatting.extracted) {
      maskWithFormatting = maskWithFormatting.insert(format.index, 2, false);
    }

    return maskWithFormatting;
  }

  public static FilterFileParseResult parseFilterFile(Stream<String> lines) {
    String[] relevantLines = lines
        .map(String::trim)
        .filter(str -> !str.startsWith("#"))
        .toArray(String[]::new);
    WordFilter[] filtered = Arrays.stream(relevantLines)
        .filter(str -> !str.startsWith("+"))
        .flatMap(FilterService::parseLine)
        .map(WordFilter::new)
        .toArray(WordFilter[]::new);
    WordFilter[] whitelisted = Arrays.stream(relevantLines)
        .filter(str -> str.startsWith("+"))
        .map(str -> str.substring(1))
        .flatMap(FilterService::parseLine)
        .map(WordFilter::new)
        .toArray(WordFilter[]::new);

    return new FilterFileParseResult(filtered, whitelisted);
  }

  private static String applyMask(StringMask mask, String text, char censorChar) {
    char[] chars = text.toCharArray();
    mask.forEach((v, i) -> chars[i] = v ? censorChar : chars[i]);
    return new String(chars);
  }

  private static Stream<String> parseLine(String line) {
    // match '/' or ',' (note that the regex doesn't work when including the starting and ending '/'. thanks java)
    String[] split = line == null ? new String[0] : line.split("[,/]");
    return Arrays.stream(split).map(String::trim).map(String::toLowerCase).filter(s -> !s.isEmpty());
  }

  public static class FilterFileParseResult {
    public WordFilter[] filtered;
    public WordFilter[] whitelisted;

    public FilterFileParseResult(WordFilter[] filtered, WordFilter[] whitelisted) {
      this.filtered = filtered;
      this.whitelisted = whitelisted;
    }
  }
}
