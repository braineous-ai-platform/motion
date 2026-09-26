package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import com.mongodb.client.MongoClient;

public class PerceptionPipelineTestHarness {

    private final MongoClient mongoClient;

    public PerceptionPipelineTestHarness(
            MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void seed() {

        GraphBuilder graphBuilder =
                GraphBuilder.getInstance();

        graphBuilder.addNode(
                new Fact(
                        "F1:1",
                        "{\"id\":\"F1:1\"}"));
        graphBuilder.addNode(
                new Fact(
                        "F2:1",
                        "{\"id\":\"F2:1\"}"));
        graphBuilder.addNode(
                new Fact(
                        "F3:1",
                        "{\"id\":\"F3:1\"}"));
        graphBuilder.addNode(
                new Fact(
                        "F4:1",
                        "{\"id\":\"F4:1\"}"));
        graphBuilder.addNode(
                new Fact(
                        "F5:1",
                        "{\"id\":\"F5:1\"}"));
        graphBuilder.addNode(
                new Fact(
                        "F6:1",
                        "{\"id\":\"F6:1\"}"));
    }
}
