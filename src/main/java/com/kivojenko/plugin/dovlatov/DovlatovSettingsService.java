package com.kivojenko.plugin.dovlatov;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.util.HashSet;
import java.util.Set;

public class DovlatovSettingsService {
    public static Set<String> enabledFiles = new HashSet<>();

    public static boolean isFileEnabled(PsiFile file) {
        return enabledFiles.contains(file.getVirtualFile().getUrl());
    }

    public static void toggleFile(VirtualFile file) {
        String url = file.getUrl();
        if (!enabledFiles.remove(url)) enabledFiles.add(url);
    }
}
