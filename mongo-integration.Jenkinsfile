@Grab('org.mongodb:mongodb-driver:3.2.2')

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.DBCollection
import com.mongodb.DB
import com.mongodb.BasicDBObject
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress

class MongoService { 
    private MongoClient mongoClient 

    def host = "mongo_mongo_1" //your host name 
    def port = 27017 //your port no. 
    def databaseName = 'test'
    def user = "root"
    def password = "pass"

    public MongoClient client() {
        
        MongoClient mongoClient = new MongoClient(
            new MongoClientURI( "mongodb://${user}:${password}@${host}:${port}" )
        )

        return mongoClient
    } 

    public DBCollection collection(collectionName) { 
        DB db = client().getDB(databaseName)

        return db.getCollection(collectionName) 
    }
}

// def service = new MongoService(databaseName: 'test')
// def client = service.client()
// def db = client.getDB('test')
// def collection = db.getCollection('example')
// collection.insert(info)

BasicDBObject info = new BasicDBObject()
info.put("x", 204)
info.put("y", 105)

def example = new MongoService().collection("example")
example.insert(info)