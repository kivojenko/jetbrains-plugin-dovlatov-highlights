package com.kivojenko.plugin.dovlatov;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LetterColorPalette {
    private static final Map<Integer, Map<Float, TextAttributesKey>> cache = new ConcurrentHashMap<>();
    private static final Map<Character.UnicodeBlock, List<Integer>> alphabetCache = new ConcurrentHashMap<>();

    public static TextAttributesKey keyFor(int codePoint, float frequency) {
        int normalized = Character.toUpperCase(codePoint);
        return cache
                .computeIfAbsent(normalized, (v) -> new ConcurrentHashMap<>())
                .computeIfAbsent(frequency, f -> makeKey(normalized, f));
    }

    private static TextAttributesKey makeKey(int codePoint, float occurrence) {
        float hue = hueFor(codePoint);
        float saturation = JBColor.isBright() ? occurrence * 0.3f + 0.08f : 1 - occurrence;
        float brightness = JBColor.isBright() ? 1.0f : 0.4f;

        Color bg = Color.getHSBColor(hue, saturation, brightness);
        Color fg = getBestForeground(bg);

        TextAttributes attrs = new TextAttributes(fg, bg, null, EffectType.BOXED, Font.PLAIN);
        String id = "DOVLATOV_LETTER_" + codePoint + "_" + occurrence;
        return TextAttributesKey.createTextAttributesKey(id, attrs);
    }

    public static float hueFor(int codePoint) {
        var block = Character.UnicodeBlock.of(codePoint);
        List<Integer> letters = getAlphabetLetters(block);
        return (letters.indexOf(codePoint) + 1) / (letters.size() * 0.5f);
    }

    private static List<Integer> getAlphabetLetters(Character.UnicodeBlock block) {
        return alphabetCache.computeIfAbsent(block, b -> {
            List<Integer> letters = new ArrayList<>();
            for (int codePoint = Character.MIN_CODE_POINT; codePoint <= Character.MAX_CODE_POINT; codePoint++) {
                if (Character.isAlphabetic(codePoint) && Character.UnicodeBlock.of(codePoint) == b) {
                    letters.add(codePoint);
                }
            }
            return letters;
        });
    }

    private static Color getBestForeground(Color bg) {
        double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        return luminance > 0.7 != JBColor.isBright() ? JBColor.background() : JBColor.foreground();
    }
}
