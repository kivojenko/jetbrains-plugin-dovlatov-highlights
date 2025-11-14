package com.kivojenko.plugin.dovlatov;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPlainText;
import org.jetbrains.annotations.NotNull;

import java.text.BreakIterator;
import java.util.*;

public class DovlatovAnnotator implements Annotator {
    private static final String PHRASE_SPLIT_REGEX = "(?<=[.!?;\"])";

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof PsiPlainText)) return;
        PsiFile file = element.getContainingFile();
        if (file == null || !DovlatovSettingsService.isFileEnabled(file)) return;

        CharSequence text = element.getText();
        if (text.isEmpty()) return;

        String[] phrases = text.toString().split(PHRASE_SPLIT_REGEX);
        int globalOffset = element.getTextRange().getStartOffset();
        for (String phrase : phrases) {
            var lettersToRanges = getLetterToRanges(phrase, globalOffset);
            addHighlights(holder, lettersToRanges);
            globalOffset += phrase.length();
        }
    }

    private static @NotNull Map<Integer, List<TextRange>> getLetterToRanges(String phrase, int offsetInElement) {
        Map<Integer, List<TextRange>> letterToRanges = new HashMap<>();
        var it = BreakIterator.getWordInstance(Locale.ROOT);
        it.setText(phrase);
        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            if (start >= end || end - start <= 3) continue;

            int cp = firstLetterCodePoint(phrase.substring(start, end));
            if (cp == -1) continue;

            cp = Character.toUpperCase(cp);

            var range = new TextRange(offsetInElement + start, offsetInElement + end);
            letterToRanges.putIfAbsent(cp, new ArrayList<>());
            letterToRanges.get(cp).add(range);
        }
        return letterToRanges;
    }

    private static void addHighlights(AnnotationHolder holder, Map<Integer, List<TextRange>> lettersToRanges) {
        var totalWords = lettersToRanges.values().stream().mapToInt(List::size).sum();

        for (var letter : lettersToRanges.keySet()) {
            var ranges = lettersToRanges.get(letter);

            var occurrence = ranges.size();
            if (occurrence < 2) continue;

            var frequency = occurrence / (float) totalWords;
            var key = LetterColorPalette.keyFor(letter, frequency);
            ranges.forEach(range -> holder
                    .newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(range)
                    .textAttributes(key)
                    .create()
            );
        }
    }

    private static int firstLetterCodePoint(String t) {
        for (Character c : t.toCharArray()) {
            if (Character.isLetter(c)) return c;
        }
        return -1;
    }
}
