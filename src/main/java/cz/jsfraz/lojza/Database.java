package cz.jsfraz.lojza;

import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

// https://www.baeldung.com/java-mongodb
// https://www.mongodb.com/developer/languages/java/java-mapping-pojos/

public class Database implements IDatabase {
    private MongoDatabase database;

    public Database() {
        SettingSingleton settingSingleton = SettingSingleton.GetInstance();
        // https://stackoverflow.com/questions/65189429/mongodb-java-driver-4-1-1-how-to-configure-timeout-settings
        MongoClientSettings mongoSettings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(1000, TimeUnit.MILLISECONDS);
                    builder.readTimeout(1000, TimeUnit.MILLISECONDS);
                })
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(1000, TimeUnit.MILLISECONDS))
                .applyConnectionString(new ConnectionString(
                        "mongodb://" + settingSingleton.getMongoUser() + ":" + settingSingleton.getMongoPassword() + "@"
                                + settingSingleton.getMongoServer() + ":" + settingSingleton.getMongoPort()))

                .build();
        this.database = MongoClients.create(mongoSettings).getDatabase(settingSingleton.getMongoDatabase());
    }
    
    public void testConnection() throws Exception {
        Document document = new Document();
        document.put("ping", 1);
        database.runCommand(document);
    }
}
