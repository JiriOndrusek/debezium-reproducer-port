package reproducer.test;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ManualTest extends CamelTestSupport {


    private static final String DB_USERNAME = "debezium";
    private static final String DB_PASSWORD = "dbz";
    private static final String DB_HOST = "127.0.0.1";
    private static int DB_PORT = 27017;
//    private static int DB_PORT = 30001;

    private static MongoClient mongoClient;

    private static Path storeFile;

    @BeforeAll
    public static void setUpAll() throws SQLException, IOException {
        final String mongoUrl =  String.format("mongodb://%s:%s@%s:%d", DB_USERNAME, DB_PASSWORD, DB_HOST, DB_PORT);

        mongoClient = MongoClients.create(mongoUrl);

        storeFile = Files.createTempFile(ManualTest.class.getSimpleName() + "-store-", "");
    }

    @AfterAll
    public static void cleanUp() throws SQLException {
        if (mongoClient != null) {
            mongoClient.close();
        }
        try {
            if (storeFile != null) {
                Files.deleteIfExists(storeFile);
            }
        } catch (Exception e) {
            // ignored
        }
    }

    @Test
    @Order(0)
    public void testReceiveInitCompany() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);

        MongoDatabase db = mongoClient.getDatabase("test");
        try {
            MongoCollection comps = db.getCollection("companies");
            comps.drop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.createCollection("companies");
        MongoCollection companies = db.getCollection("companies");
        Document doc = new Document();
        doc.put("_id", "a");
        companies.insertOne(doc);
        System.out.println("*********** inserted *****************");

        mock.assertIsSatisfied(10000);

    }



    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                fromF("debezium-mongodb:NAME?offsetStorageFileName=" +  storeFile.toString() +
                        "&mongodbUser=debezium&mongodbPassword=dbz&mongodbName=test&mongodbHosts="+DB_HOST+":"+DB_PORT)
                        .process(e -> System.out.println("EVENT RECEIVED"))
                        .to("mock:result");
            }
        };
    }


}