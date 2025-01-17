/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.commands.management.permission;

import net.dv8tion.jda.core.entities.Member;
import org.apache.commons.lang3.EnumUtils;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.commandmeta.ICommandExecutable;
import org.cascadebot.cascadebot.commandmeta.Module;
import org.cascadebot.cascadebot.data.objects.GuildPermissions;
import org.cascadebot.cascadebot.permissions.CascadePermission;
import org.cascadebot.cascadebot.utils.FormatUtils;

public class GroupPermissionSwitchSubCommand implements ICommandExecutable {

    @Override
    public void onCommand(Member sender, CommandContext context) {
        GuildPermissions.PermissionMode mode = context.getData().getGuildPermissions().getMode();
        if (context.getArgs().length > 1) {
            mode = EnumUtils.getEnumIgnoreCase(GuildPermissions.PermissionMode.class, context.getArg(0));
            if (mode == null) {
                context.getTypedMessaging().replyDanger("The permission mode %s isn't a valid permission mode!", context.getArg(0));
                return;
            }
        } else {
            switch (mode) {
                case HIERARCHICAL:
                    mode = GuildPermissions.PermissionMode.MOST_RESTRICTIVE;
                    break;
                case MOST_RESTRICTIVE:
                    mode = GuildPermissions.PermissionMode.HIERARCHICAL;
                    break;
            }
        }

        context.getData().getPermissions().setMode(mode);
        context.getTypedMessaging().replySuccess("Switch the permission mode to `%s`", FormatUtils.formatEnum(mode));
    }

    @Override
    public String command() {
        return "switch";
    }

    @Override
    public CascadePermission getPermission() {
        return CascadePermission.of("Group permissions switch sub command", "permissions.group.switch", false, Module.MANAGEMENT);
    }

    @Override
    public String description() {
        return "Switches the permissions mode.";
    }

}
