/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.commandmeta;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@Getter
public class Argument {

    private final String id;
    private final Set<Argument> subArgs;
    private final ArgumentType type;
    private final Set<String> aliases;

    private Argument(String id, Set<Argument> subArgs, ArgumentType type, Set<String> aliases) {
        this.id = id;
        this.subArgs = subArgs;
        this.type = type;
        this.aliases = aliases;
    }

    public static Argument of(String id, Set<Argument> subArgs, ArgumentType type, Set<String> aliases) {
        return new Argument(id, Set.copyOf(subArgs), type, Set.copyOf(aliases));
    }

    public String getName() {
        // TODO: Implement this into locale
        return "";
    }

    public String getDescription() {
        // TODO: Implement this into locale
        return "";
    }

    /**
     * Gets the usage string.
     * <p>
     * Formatting:
     * - Aliased arguments are shown as {@code <alias1/alias2>} for as many aliases as the argument has.
     * - A required parameter is show as {@code <argument>}
     * - An optional parameter is show as {@code [argument]}
     *
     * @param base The base command/prefix to use. Example: ';help '.
     * @return A string representing the usage.
     */
    protected String getUsageString(String base) {
        StringBuilder usageBuilder = new StringBuilder();
        if (subArgs.size() > 0) {
            String field = this.toString();
            if (!StringUtils.isBlank(getDescription()) && (subArgs.isEmpty() || subArgs.stream().allMatch(argument -> argument.getType() == ArgumentType.OPTIONAL))) {
                usageBuilder.append("`").append(base).append(getName()).append("` - ").append(getDescription()).append('\n');
            }
            for (Argument subArg : subArgs) {
                usageBuilder.append(subArg.getUsageString(base + field + " "));
            }
        } else {
            usageBuilder.append("`").append(base).append(this.toString()).append("`");
            if (!StringUtils.isBlank(getDescription())) {
                usageBuilder.append(" - ").append(getDescription());
            }
            usageBuilder.append('\n');
        }

        return usageBuilder.toString();
    }

    /**
     * Checks for this argument at a given position.
     *
     * @param args The arguments sent in from the command.
     * @param pos  The position this argument should be in.
     * @return If the argument exists at that position.
     */
    public boolean argExists(String[] args, int pos) {
        if (args.length <= pos) {
            return false;
        }
        if (type.equals(ArgumentType.REQUIRED)) {
            return true;
        }
        if (!args[pos].equalsIgnoreCase(getName()) && !this.type.equals(ArgumentType.OPTIONAL)) {
            for (String alias : aliases) {
                if (!args[pos].equalsIgnoreCase(alias)) {
                    return false;
                }
            }
        }
        if (this.type.equals(ArgumentType.COMMAND) && this.subArgs.size() > 0 && this.getDescription().isEmpty()) {
            for (Argument sub : this.subArgs) {
                if (sub.type.equals(ArgumentType.REQUIRED) || sub.type.equals(ArgumentType.COMMAND)) {
                    return sub.argExists(args, pos + 1);
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String argument = getName();
        if (aliases.size() > 0) {
            StringBuilder paramBuilder = new StringBuilder();
            paramBuilder.append(argument);
            for (String alias : aliases) {
                paramBuilder.append("|").append(alias);
            }
            argument = paramBuilder.toString();
        }
        switch (type) {
            case OPTIONAL:
                argument = "[" + argument + "]";
                break;
            case REQUIRED:
                argument = "<" + argument + ">";
                break;
        }
        return argument;
    }

    public boolean argEquals(String id) {
        return this.id.equalsIgnoreCase(id);
    }

    public boolean argStartsWith(String start) {
        return this.id.startsWith(start.toLowerCase());
    }

    //TODO implement utils for checking arguments in the command. we have a class here why not use it.
}
