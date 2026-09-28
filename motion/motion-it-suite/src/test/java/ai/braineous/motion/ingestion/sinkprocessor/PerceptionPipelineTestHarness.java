package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
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

        Fact f1 = new Fact(
                "F1:1",
                "{\"id\":\"F1:1\"}");
        Fact f2 = new Fact(
                "F2:1",
                "{\"id\":\"F2:1\"}");
        Fact f3 = new Fact(
                "F3:1",
                "{\"id\":\"F3:1\"}");
        Fact f4 = new Fact(
                "F4:1",
                "{\"id\":\"F4:1\"}");
        Fact f5 = new Fact(
                "F5:1",
                "{\"id\":\"F5:1\"}");
        Fact f6 = new Fact(
                "F6:1",
                "{\"id\":\"F6:1\"}");

        graphBuilder.addNode(f1);
        graphBuilder.addNode(f2);
        graphBuilder.addNode(f3);
        graphBuilder.addNode(f4);
        graphBuilder.addNode(f5);
        graphBuilder.addNode(f6);

        bindDirectedEdge(
                graphBuilder,
                f1,
                f2,
                "E:F1:1->F2:1");
        bindDirectedEdge(
                graphBuilder,
                f3,
                f5,
                "E:F3:1->F5:1");
        bindDirectedEdge(
                graphBuilder,
                f2,
                f4,
                "E:F2:1->F4:1");
        bindDirectedEdge(
                graphBuilder,
                f4,
                f6,
                "E:F4:1->F6:1");
    }

    private void bindDirectedEdge(
            GraphBuilder graphBuilder,
            Fact from,
            Fact to,
            String edgeId) {
        Edge edge = new Edge();
        edge.setId(edgeId);
        edge.setFromFactId(from.getId());
        edge.setToFactId(to.getId());

        BindResult bindResult =
                graphBuilder.bind(
                        new Input(from, to, edge),
                        null);

        if (bindResult == null || bindResult.isOk() == false) {
            throw new IllegalStateException(
                    "failed to bind directed edge " + edgeId);
        }
    }
}
