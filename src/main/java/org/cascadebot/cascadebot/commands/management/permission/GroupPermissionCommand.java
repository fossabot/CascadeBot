/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.commands.management.permission;

import java.util.Set;
import net.dv8tion.jda.core.entities.Member;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.commandmeta.ICommandExecutable;
import org.cascadebot.cascadebot.commandmeta.ICommandMain;
import org.cascadebot.cascadebot.commandmeta.Module;
import org.cascadebot.cascadebot.permissions.CascadePermission;

public class GroupPermissionCommand implements ICommandMain {

    @Override
    public void onCommand(Member sender, CommandContext context) {
        context.getUIMessaging().replyUsage(this);
    }

    @Override
    public Module getModule() {
        return Module.MANAGEMENT;
    }

    @Override
    public String command() {
        return "groupperms";
    }

    @Override
    public CascadePermission getPermission() {
        return CascadePermission.of("Group permissions command", "permissions.group", false, Module.MANAGEMENT);
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public Set<ICommandExecutable> getSubCommands() {
        return Set.of(new GroupPermissionCreateSubCommand(), new GroupPermissionAddSubCommand(), new GroupPermissionRemoveSubCommand(),
                new GroupPermissionLinkRoleSubCommand(), new GroupPermissionUnlinkRoleSubCommand(), new GroupPermissionMoveSubCommand(),
                new GroupPermissionSwitchSubCommand(), new GroupPermissionListSubCommand(), new GroupPermissionInfoSubCommand());
    }
}
