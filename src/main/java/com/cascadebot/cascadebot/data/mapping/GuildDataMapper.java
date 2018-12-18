/*
 * Copyright (c) 2018 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package com.cascadebot.cascadebot.data.mapping;

import com.cascadebot.cascadebot.CascadeBot;
import com.cascadebot.cascadebot.Constants;
import com.cascadebot.cascadebot.commandmeta.CommandManager;
import com.cascadebot.cascadebot.commandmeta.ICommand;
import com.cascadebot.cascadebot.data.database.DebugLogCallback;
import com.cascadebot.cascadebot.data.objects.GuildCommandInfo;
import com.cascadebot.cascadebot.data.objects.GuildData;
import com.cascadebot.cascadebot.utils.CollectionUtils;
import com.cascadebot.shared.Version;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.mongodb.client.model.Filters.eq;

public final class GuildDataMapper {

    public static final String COLLECTION = "guilds";

    private static LoadingCache<Long, GuildData> guilds = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            /*.refreshAfterWrite(5, TimeUnit.MINUTES) hmm? */
            /*.removalListener((key, value, cause) -> {}) I need to think about this */
            .build(id -> {
                AtomicReference<Document> documentReference = new AtomicReference<>();
                CascadeBot.instance().getDatabaseManager().runTask(database -> {
                    documentReference.set(database.getCollection(COLLECTION).find(eq("_id", id)).first());
                });
                if (documentReference.get() == null) {
                    CascadeBot.logger.debug("Attempted to load guild data for ID: " + id + ", none was found so creating new data object");
                    GuildData data = new GuildData(id);
                    GuildDataMapper.insert(id, processGuildData(data));
                    return data;
                }
                // TODO: Migration here
                GuildData data = documentToGuildData(documentReference.get());
                CascadeBot.logger.debug("Loaded data from database for guild ID: " + id);
                return data;
            });


    public static void update(long id, Bson update) {
        CascadeBot.instance().getDatabaseManager().runAsyncTask(database -> {
            database.getCollection(COLLECTION).updateOne(eq("_id", id), update, new DebugLogCallback<>("Updated Guild ID " + id + ":", update));
        });
    }

    public static void insert(long id, Document document) {
        CascadeBot.instance().getDatabaseManager().runAsyncTask(database -> {
            document.put("_id", id);
            database.getCollection(COLLECTION).insertOne(document, new DebugLogCallback<>("Inserted Guild ID " + id + ":" + document));
        });
    }

    public static GuildData getGuildData(long id) {
        return guilds.get(id);
    }

    public static Document processGuildData(GuildData data) {
        Document guildDoc = new Document();

        guildDoc.put("_id", data.getGuildID());
        guildDoc.put("config_version", data.getConfigVersion().toString());
        guildDoc.put("updated_at", new Date());

        Document config = new Document();
        config.put("mention_prefix", data.isMentionPrefix());

        Document commands = new Document();
        for (GuildCommandInfo commandInfo : data.getGuildCommandInfos()) {
            commands.put(commandInfo.getDefaultCommand(), processCommandInfo(commandInfo));
        }
        config.put("commands", commands);

        guildDoc.put("config", config);
        return guildDoc;
    }

    public static Document processCommandInfo(GuildCommandInfo commandInfo) {
        Document commandDoc = new Document();
        commandDoc.put("command", commandInfo.getCommand());
        commandDoc.put("enabled", commandInfo.isEnabled());
        commandDoc.put("aliases", commandInfo.getAliases());
        return commandDoc;
    }

    public static GuildData documentToGuildData(Document document) {
        GuildData.GuildDataBuilder guildDataBuilder = new GuildData.GuildDataBuilder(document.getLong("_id"));
        String[] configVersion = Objects.requireNonNull(document.getString("config_version")).split("\\.");
        if (configVersion.length == 3) { // Dummy check, *should* always be three
            guildDataBuilder.setConfigVersion(Version.of(
                    Integer.valueOf(configVersion[0]),
                    Integer.valueOf(configVersion[1]),
                    Integer.valueOf(configVersion[2]))
            );
        } else {
            guildDataBuilder.setConfigVersion(Constants.CONFIG_VERSION);
        }
        guildDataBuilder.setCreationDate(document.getDate("created_at"));

        Document config = document.get("config", Document.class);
        guildDataBuilder.setMentionPrefix(config.getBoolean("mention_prefix"));

        Document commands = config.get("commands", Document.class);
        for (String key : commands.keySet()) {
            Pair<ICommand, GuildCommandInfo> pair = documentToCommand(key, commands.get(key, Document.class));
            guildDataBuilder.addCommand(pair.getKey(), pair.getValue());
        }
        return guildDataBuilder.build();
    }

    public static Pair<ICommand, GuildCommandInfo> documentToCommand(String defaultCommand, Document document) {
        ICommand command = CommandManager.instance().getCommandByDefault(defaultCommand);
        return Pair.of(command, new GuildCommandInfo(
                document.getString("command"),
                defaultCommand,
                CollectionUtils.hashSet(document.get("aliases", List.class)),
                document.getBoolean("enabled"),
                command.forceDefault()
        ));
    }

}
