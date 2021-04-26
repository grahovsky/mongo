@Grab('org.mongodb:mongodb-driver:3.12.8')

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase

import com.mongodb.client.MongoCursor
import com.mongodb.client.FindIterable

import com.mongodb.client.result.UpdateResult
import com.mongodb.client.result.DeleteResult

import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.FindOneAndUpdateOptions

import com.mongodb.client.MongoCollection

import org.bson.Document
import org.bson.types.ObjectId
import org.bson.conversions.Bson
import org.bson.json.JsonWriterSettings

import static com.mongodb.client.model.Filters.*
import static com.mongodb.client.model.Updates.*

import groovy.transform.Field
import groovy.json.JsonSlurper

@Field MongoService mongoService

class MongoService { 
    
    MongoClient mongoClient
    MongoDatabase database

    def context
    def env
    def host = "127.0.0.1"
    def port = 27017
    def databaseName = 'jenkins'
    def mongoUser
    def mongoPass
    JsonWriterSettings prettyPrint

    public MongoService(context) {
        
        this.context = context
        this.env = context.env

    }

    public initClient() {

        context.withCredentials([context.usernamePassword(credentialsId: 'mongo-jenkins', usernameVariable: 'mongoUser', passwordVariable: 'mongoPass')]) {
            (mongoUser, mongoPass) = [env.mongoUser, env.mongoPass]
        }

        ConnectionString connString = new ConnectionString("mongodb://${mongoUser}:${mongoPass}@${host}:${port}/?authSource=${databaseName}")

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connString)
            .retryWrites(true)
            .build()

        mongoClient = MongoClients.create(settings)
        database = mongoClient.getDatabase(databaseName)

        prettyPrint = JsonWriterSettings.builder().indent(true).build();

    } 

    public MongoCollection<Document> getCollection(collectionName) { 
        
        // MongoDatabase db = mongoClient.getDatabase(databaseName)

        return database.getCollection(collectionName) 

    }
    
    public Document toDocument(Map map) {
    
        Document document = new Document("_id", new ObjectId())
        
        map.each { key, value ->
          def putValue = value instanceof Map ? toDocument(value) : value
          document.append(key, putValue)
        }
        
        return document

    }

    private static Bson toBson(Map<String, Object> map) {
        if (map == null) {
            return null
        }
        (Bson) new Document(map)
    }

    public insertDocumentToCollection(String collectionName, Map map) {

        
        def collection = getCollection(collectionName)
        collection.insertOne(toDocument(map))

    }

    public getAllDocumentsInCollection(String collectionName) {
        
        def collection = getCollection(collectionName)

        FindIterable<Document> iterable = collection.find()
        MongoCursor<Document> cursor = iterable.iterator()

        def result = []

        while(cursor.hasNext())
        {
            result.add(new JsonSlurper().parseText(cursor.next().toJson(prettyPrint)))
        }

        return result

    }

    public getDocumentsInCollectionByFilter(String collectionName, Map filter) {

        def collection = getCollection(collectionName)

        def filterBsonCollection = []
        filter.each{ key, value ->
            filterBsonCollection.add(eq("${key}".toString(), value))
        }
        Bson filterBson = and(filterBsonCollection)                

        FindIterable<Document> iterable = collection.find(filterBson)
        MongoCursor<Document> cursor = iterable.iterator()
                
        def result = []

        while(cursor.hasNext())
        {
            result.add(new JsonSlurper().parseText(cursor.next().toJson()))
        }

        return result

    }

    public updateDocumentInCollection(String collectionName, Map filter, Map updateData) {

        def collection = getCollection(collectionName)

        def filterBsonCollection = []
        filter.each{ key, value ->
            filterBsonCollection.add(eq("${key}".toString(), value))
        }
        Bson filterBson = and(filterBsonCollection)  

        def updateOperationList = []

        updateData.each{ key, value ->
            updateOperationList.add(set("${key}".toString(), value))
        }

        Bson updateOperation = combine(updateOperationList)
        
        UpdateOptions options = new UpdateOptions().upsert(true)
        
        UpdateResult updateResult = collection.updateOne(filterBson, updateOperation, options)

        return updateResult

    }

    public deleteDocumentInCollection(String collectionName, Map filter) {

        def collection = getCollection(collectionName)

        def filterBsonCollection = []
        filter.each{ key, value ->
            filterBsonCollection.add(eq("${key}".toString(), value))
        }
        Bson filterBson = and(filterBsonCollection)  
        
        DeleteResult deleteResult = collection.deleteOne(filterBson)

        return deleteResult

    }
    
}

def initService() {

    mongoService = new MongoService(this)
    mongoService.initClient()

}

def insertDocumentToCollection(String collectionName, Map map) {

    initService()

    def result = mongoService.insertDocumentToCollection(collectionName, map)
    mongoService = null

    return result

}

def getAllDocumentsInCollection(String collectionName) {

    initService()

    def result = mongoService.getAllDocumentsInCollection(collectionName)
    mongoService = null

    return result

}

def getDocumentsInCollectionByFilter(String collectionName, Map filter) {

    initService()

    def result = mongoService.getDocumentsInCollectionByFilter(collectionName, filter)
    mongoService = null

    return result

}

def updateDocumentInCollection(String collectionName, Map filter, Map data) {

    initService()

    def result = mongoService.updateDocumentInCollection(collectionName, filter, data)
    mongoService = null

    return result
    
}

def deleteDocumentInCollection(String collectionName, Map filter) {

    initService()

    def result = mongoService.deleteDocumentInCollection(collectionName, filter)
    mongoService = null

    return result

}