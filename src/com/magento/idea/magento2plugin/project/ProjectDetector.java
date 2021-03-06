/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.*;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoVersion;
import org.jetbrains.annotations.NotNull;
import javax.swing.event.HyperlinkEvent;

public class ProjectDetector implements DirectoryProjectConfigurator {
    @Override
    public void configureProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Ref<Module> moduleRef, boolean newProject) {
        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            DumbService.getInstance(project).smartInvokeLater(() -> {
                if (!MagentoBasePathUtil.isMagentoFolderValid(baseDir.getPath())) {
                    return;
                }
                Notification notification = new Notification("Magento", "Magento",
                        "<a href='enable'>Enable</a> Magento support for this project?",
                        NotificationType.INFORMATION, new NotificationListener.Adapter() {
                    @Override
                    public void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
                        Settings settings = Settings.getInstance(project);
                        settings.pluginEnabled = true;
                        settings.mftfSupportEnabled = true;
                        settings.magentoPath = project.getBasePath();
                        settings.magentoVersion = MagentoVersion.getInstance().get(project, project.getBasePath());
                        IndexManager.manualReindex();
                        MagentoComponentManager.getInstance(project).flushModules();
                        notification.expire();
                    }
                });
                Notifications.Bus.notify(notification, project);
            });
        });
    }
}
