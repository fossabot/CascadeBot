/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.commands.management.permission;

import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.commandmeta.ICommandExecutable;
import org.cascadebot.cascadebot.commandmeta.Module;
import org.cascadebot.cascadebot.permissions.CascadePermission;
import org.cascadebot.cascadebot.utils.PermissionCommandUtils;
import org.cascadebot.cascadebot.utils.pagination.Page;
import org.cascadebot.cascadebot.utils.pagination.PageObjects;
import org.cascadebot.cascadebot.utils.pagination.PageUtils;

public class GroupPermissionInfoSubCommand implements ICommandExecutable {

    @Override
    public void onCommand(Member sender, CommandContext context) {
        if (context.getArgs().length < 1) {
            context.getUIMessaging().replyUsage(this, "groupperms");
            return;
        }

        PermissionCommandUtils.tryGetGroupFromString(context, context.getMessage(0), group -> {
            if (group.getPermissions().isEmpty() && group.getRoleIds().isEmpty()) {
                context.getTypedMessaging().replyWarning("Group has no permissions or roles");
                return;
            }

            List<Page> pageList = new ArrayList<>();
            StringBuilder rolesBuilder = new StringBuilder();
            StringBuilder permissionBuilder = new StringBuilder();

            for (Long roleId : group.getRoleIds()) {
                Role role = context.getGuild().getRoleById(roleId);
                if (role == null) {
                    continue;
                }
                rolesBuilder.append(role.getName()).append(" (").append(roleId).append(")\n");
            }

            for (String perm : group.getPermissions()) {
                permissionBuilder.append(perm).append('\n');
            }

            if (!group.getRoleIds().isEmpty()) {
                List<String> rolesPageContent = PageUtils.splitString(rolesBuilder.toString(), 1000, '\n');
                for (String roleContent : rolesPageContent) {
                    EmbedBuilder rolesEmbedBuilder = new EmbedBuilder();
                    rolesEmbedBuilder.setTitle("Linked Roles");
                    rolesEmbedBuilder.setDescription("```" + roleContent + "```");
                    pageList.add(new PageObjects.EmbedPage(rolesEmbedBuilder));
                }
            }

            if (!group.getPermissions().isEmpty()) {
                List<String> permissionsPageContent = PageUtils.splitString(permissionBuilder.toString(), 1000, '\n');
                for (String permsContent : permissionsPageContent) {
                    EmbedBuilder permissionsEmbedBuilder = new EmbedBuilder();
                    permissionsEmbedBuilder.setTitle("Permissions");
                    permissionsEmbedBuilder.setDescription("```" + permsContent + "```");
                    pageList.add(new PageObjects.EmbedPage(permissionsEmbedBuilder));
                }
            }

            context.getUIMessaging().sendPagedMessage(pageList);
        }, sender.getUser().getIdLong());
    }

    @Override
    public String command() {
        return "info";
    }

    @Override
    public CascadePermission getPermission() {
        return CascadePermission.of("Group permissions info sub command", "permissions.group.info", false, Module.MANAGEMENT);
    }

    @Override
    public String description() {
        return null;
    }

}
