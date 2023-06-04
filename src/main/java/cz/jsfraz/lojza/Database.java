package cz.jsfraz.lojza;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

// https://www.baeldung.com/java-mongodb

public class Database implements IDatabase {
    private MongoDatabase database;
    private final String collectionSuffix = "Collection";

    public Database() {
        SettingSingleton settingSingleton = SettingSingleton.GetInstance();

        // https://www.mongodb.com/developer/languages/java/java-mapping-pojos/
        CodecRegistry pojoCodecRegistry = CodecRegistries
                .fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);

        // https://stackoverflow.com/questions/65189429/mongodb-java-driver-4-1-1-how-to-configure-timeout-settings
        MongoClientSettings mongoSettings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(settingSingleton.getMongoTimeoutMS(), TimeUnit.MILLISECONDS);
                    builder.readTimeout(settingSingleton.getMongoTimeoutMS(), TimeUnit.MILLISECONDS);
                })
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(settingSingleton.getMongoTimeoutMS(),
                        TimeUnit.MILLISECONDS))
                .applyConnectionString(new ConnectionString(
                        "mongodb://" + settingSingleton.getMongoUser() + ":" + settingSingleton.getMongoPassword() + "@"
                                + settingSingleton.getMongoServer() + ":" + settingSingleton.getMongoPort()))
                .codecRegistry(codecRegistry)
                .build();
        this.database = MongoClients.create(mongoSettings).getDatabase(settingSingleton.getMongoDatabase());
    }

    // test database connection
    @Override
    public boolean testConnection() {
        try {
            Document document = new Document();
            document.put("ping", 1);
            database.runCommand(document);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // gets DiscordGuild collection
    private MongoCollection<DiscordGuild> getDiscordGuildCollection() {
        return database.getCollection(Introspector.decapitalize(DiscordGuild.class.getSimpleName()) + collectionSuffix,
                DiscordGuild.class);
    }

    // returns DiscordGuild or null
    private DiscordGuild getFirstOrDefault(long guildId) {
        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        return collection.find(Filters.eq("guildId", guildId)).first();
    }

    // updates DiscordGuild locale
    @Override
    public void updateGuildLocale(long guildId, Locale locale) throws MongoException {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.set("locale", locale));
        } else {
            // insert if doesn't exist
            guild = new DiscordGuild(guildId, locale);
            collection.insertOne(guild);
        }
    }

    // get DiscordGuild locale
    @Override
    public Locale getGuildLocale(long guildId) {
        try {
            // gets guild locale by id
            MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
            Bson filter = Filters.eq("guildId", guildId);
            Bson projection = Projections.fields(Projections.include("locale"));
            DiscordGuild guild = collection.find(filter).projection(projection).first();

            if (guild == null) {
                // if doesn't exist in database, return default
                return SettingSingleton.GetInstance().getDefaultLocale();
            } else {
                // if exists return locale
                return guild.getLocale();
            }
        } catch (Exception e) {
            return SettingSingleton.GetInstance().getDefaultLocale();
        }
    }

    // updates DiscordGuild rssChannel
    @Override
    public void updateRssChannel(long guildId, long rssChannel) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.set("rssChannel", rssChannel));
        } else {
            // insert if doesn't exist
            guild = new DiscordGuild(guildId, SettingSingleton.GetInstance().getDefaultLocale());
            guild.setRssChannel(rssChannel);
            collection.insertOne(guild);
        }
    }

    // get DiscordGuild rssChannel
    @Override
    public long getRssChannel(long guildId) {
        try {
            // gets guild locale by id
            MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
            Bson filter = Filters.eq("guildId", guildId);
            Bson projection = Projections.fields(Projections.include("rssChannel"));
            DiscordGuild guild = collection.find(filter).projection(projection).first();

            if (guild == null) {
                // if doesn't exist in database, return default
                return 0;
            } else {
                // if exists return locale
                return guild.getRssChannel();
            }
        } catch (Exception e) {
            return 0;
        }
    }

    // check whether RSS feed URL exists in DiscordGuild rssFeeds
    @Override
    public boolean rssFeedExists(long guildId, String url) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);
        
        if (guild != null) {
            return guild.getRssFeeds().contains(url);
        } else {
            return false;
        }
    }

    // updates DiscordGuild rssFeeds
    @Override
    public void updateRssFeeds(long guildId, String url) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.addToSet("rssFeeds", url));
        } else {
            // insert if doesn't exist
            guild = new DiscordGuild(guildId,
                    SettingSingleton.GetInstance().getDefaultLocale());
            guild.setRssFeeds(new ArrayList<String>() {
                {
                    add(url);
                }
            });
            collection.insertOne(guild);
        }
    }
}
