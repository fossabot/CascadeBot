/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.utils;

import java.util.List;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import org.cascadebot.cascadebot.UnicodeConstants;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.permissions.objects.Group;
import org.cascadebot.cascadebot.utils.Table;
import org.cascadebot.cascadebot.utils.buttons.Button;
import org.cascadebot.cascadebot.utils.buttons.ButtonGroup;

@UtilityClass
public class PermissionCommandUtils {

    public static void tryGetGroupFromString(CommandContext context, String s, Consumer<Group> groupConsumer, long sender) {
        Group groupById = context.getData().getPermissions().getGroupById(s);
        if (groupById != null) {
            groupConsumer.accept(groupById);
            return;
        }

        List<Group> groupList = context.getData().getPermissions().getGroupsByName(s);
        if (groupList.size() == 1) {
            groupConsumer.accept(groupList.get(0));
            return;
        }

        if (groupList.isEmpty()) {
            context.getTypedMessaging().replyDanger("No group found with name or id " + s);
            return;
        }

        if (groupList.size() > 5) {
            context.getTypedMessaging().replyDanger("Too many groups with the same name! Use these groups ids instead.");
            return;
        }

        EmbedBuilder groupsEmbed = new EmbedBuilder();

        ButtonGroup buttonGroup = new ButtonGroup(sender, context.getChannel().getIdLong(), context.getGuild().getIdLong());

        // integer for creating buttons, and numbering the possible groups to select
        int i = 1;

        StringBuilder groupsBuilder = new StringBuilder();
        groupsBuilder.append("Multiple Groups With the same name found. Select a group to view more info on it, and then use said group\n\n");

        for (Group group : groupList) {
            char unicode = (char) (0x0030 + i);

            groupsBuilder.append(i).append(": ").append(group.getName()).append(" (id: `").append(group.getId()).append("`)\n");

            EmbedBuilder groupEmbed = new EmbedBuilder();
            groupEmbed.setTitle(group.getName() + " (" + group.getId() + ")");

            if (group.getPermissions().isEmpty()) {
                groupEmbed.addField("Permissions", "No permissions", false);
            } else {
                Table.TableBuilder tableBuilder = new Table.TableBuilder();
                tableBuilder.addHeading("Permission");

                //integer for detecting when we hit 5 permissions so we can stop adding more.
                int pi = 0;

                for (String perm : group.getPermissions()) {
                    tableBuilder.addRow(perm);
                    if (pi >= 5) {
                        break;
                    }
                    pi++;
                }

                groupEmbed.addField("Permissions", tableBuilder.build().toString(), false);
            }

            if (group.getRoleIds().isEmpty()) {
                groupEmbed.addField("Linked Roles", "No linked roles", false);
            } else {
                StringBuilder rolesBuilder = new StringBuilder();

                //integer for detecting when we hit 5 roles so we can stop adding more.
                int ri = 0;

                for (Long roleId : group.getRoleIds()) {
                    Role role = context.getGuild().getRoleById(roleId);
                    rolesBuilder.append(role.getName()).append(" (").append(role.getId()).append(")\n");
                    if (ri >= 5) {
                        break;
                    }
                    ri++;
                }

                groupEmbed.addField("Linked Roles", "```" + rolesBuilder.toString() + "```", false);
            }

            ButtonGroup groupButtons = new ButtonGroup(sender, context.getChannel().getIdLong(), context.getGuild().getIdLong());
            groupButtons.addButton(new Button.UnicodeButton(UnicodeConstants.TICK, (runner, channel, message) -> {
                if (runner.getUser().getIdLong() != groupButtons.getOwner().getUser().getIdLong()) {
                    return;
                }
                message.delete().queue();
                groupConsumer.accept(group);
            }));

            groupButtons.addButton(new Button.UnicodeButton(UnicodeConstants.LEFT_ARROW, (runner, channel, message) -> {
                handleSwitchButtons(runner, message, groupsEmbed.build(), buttonGroup, context);
            }));

            buttonGroup.addButton(new Button.UnicodeButton(unicode + "\u20E3", (runner, channel, message) -> {
                handleSwitchButtons(runner, message, groupEmbed.build(), groupButtons, context);
            }));

            i++;
        }

        groupsEmbed.setDescription(groupsBuilder.toString());
        context.getUIMessaging().sendButtonedMessage(groupsEmbed.build(), buttonGroup);
    }

    private static void handleSwitchButtons(Member member, Message message, MessageEmbed embedToSwitchTo, ButtonGroup buttonsToSwitchTo, CommandContext context) {
        if (member.getUser().getIdLong() != buttonsToSwitchTo.getOwner().getUser().getIdLong()) {
            return;
        }
        message.editMessage(embedToSwitchTo).override(true).queue();
        message.clearReactions().queue(aVoid -> {
            buttonsToSwitchTo.addButtonsToMessage(message);
            buttonsToSwitchTo.setMessage(message.getIdLong());
            context.getData().addButtonGroup(context.getChannel(), message, buttonsToSwitchTo);
        });
    }

}
