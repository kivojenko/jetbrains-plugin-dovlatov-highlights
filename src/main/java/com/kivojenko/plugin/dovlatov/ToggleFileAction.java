package com.kivojenko.plugin.dovlatov;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import org.jetbrains.annotations.NotNull;

public class ToggleFileAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (anActionEvent.getProject() == null) return;

        var editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        var file = editor.getVirtualFile();
        DovlatovSettingsService.toggleFile(file);
        DaemonCodeAnalyzer.getInstance(anActionEvent.getProject()).restart();
    }
}
