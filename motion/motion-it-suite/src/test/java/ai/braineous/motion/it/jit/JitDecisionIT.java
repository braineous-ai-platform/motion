package ai.braineous.motion.it.jit;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.motion.perception.cognition.AnalysisQueryStore;
import ai.braineous.motion.perception.cognition.CognitiveQueryExecutor;
import ai.braineous.motion.perception.model.Observable;
import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.model.Perception;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.observe.Console;
import ai.braineous.rag.prompt.services.cgo.causal.CausalLLMBridge;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.braineous.dd.llm.pg.model.TxStepResult;
import io.braineous.dd.llm.query.client.QueryClient;
import io.braineous.dd.llm.query.client.QueryResult;
import io.braineous.dd.llm.query.client.RESTClient;
import io.braineous.dd.llm.transaction.model.TxExecutionResult;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@QuarkusTest
public class JitDecisionIT extends JitSubstrateIT {

    private static final String[] QUERY_FACT_IDS = new String[] {
            "Inventory:INV-001",
            "Sales:SALE-001",
            "Vendor:VENDOR-001",
            "Product:PROD-SHAMPOO-250",
            "Location:LOC-001"
    };

    private static final String QUERY_NAME = "jit_decision_v14";

    @Inject
    CognitiveQueryExecutor cognitiveQueryExecutor;

    @Inject
    AnalysisQueryStore analysisQueryStore;

    @Test
    @Override
    public void test_1() {
        try {
            super.test_1();

            String sql = this.loadJitSql();
            printBlock("LOADED CPL SQL", sql);

            GraphBuilder graphBuilder = GraphBuilder.getInstance();
            GraphSnapshot snapshot = null;
            if (graphBuilder != null) {
                snapshot = graphBuilder.snapshot();
            }
            printPublishedPhase0(snapshot);
            printRelevantSnapshot(snapshot);

            this.analysisQueryStore.put(QUERY_NAME, sql);
            Assertions.assertEquals(sql, this.analysisQueryStore.get(QUERY_NAME));

            Perception perception = this.jitPerception(snapshot);
            List<String> queryNames = new ArrayList<String>();
            queryNames.add(QUERY_NAME);

            TxExecutionResult txResult =
                    this.cognitiveQueryExecutor.analyze(perception, queryNames);

            this.observeTxResult(txResult);
        } catch (RuntimeException exception) {
            printBlock("EXCEPTIONS", String.valueOf(exception));
            exception.printStackTrace(System.out);
            exception.printStackTrace();
            throw exception;
        } catch (Exception exception) {
            printBlock("EXCEPTIONS", String.valueOf(exception));
            exception.printStackTrace(System.out);
            exception.printStackTrace();
            throw new RuntimeException(exception);
        } finally {
            if (this.analysisQueryStore != null) {
                this.analysisQueryStore.remove(QUERY_NAME);
            }
        }
    }

    @Test
    public void test_2() {
        try {
            super.test_1();

            String sql = this.loadJitSql();
            printBlock("LOADED CPL SQL", sql);

            GraphBuilder graphBuilder = GraphBuilder.getInstance();
            GraphSnapshot snapshot = null;
            if (graphBuilder != null) {
                snapshot = graphBuilder.snapshot();
            }
            printPublishedPhase0(snapshot);
            printRelevantSnapshot(snapshot);

            this.observeLoop(sql, true);
        } catch (RuntimeException exception) {
            printBlock("EXCEPTIONS", String.valueOf(exception));
            exception.printStackTrace(System.out);
            exception.printStackTrace();
            throw exception;
        } catch (Exception exception) {
            printBlock("EXCEPTIONS", String.valueOf(exception));
            exception.printStackTrace(System.out);
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }

    @Test
    public void test_3() {
        try {
            this.publishReviewInventory();

            String sql = this.loadJitSql();
            printBlock("LOADED CPL SQL", sql);

            this.observeOnce(sql);
        } catch (RuntimeException exception) {
            printBlock("EXCEPTIONS", String.valueOf(exception));
            exception.printStackTrace(System.out);
            exception.printStackTrace();
            throw exception;
        } catch (Exception exception) {
            printBlock("EXCEPTIONS", String.valueOf(exception));
            exception.printStackTrace(System.out);
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }

    @Test
    public void test_4() {
        try {
            this.publishReviewInventory();

            String sql = this.loadJitSql();
            printBlock("LOADED CPL SQL", sql);

            this.observeLoop(sql, false);
        } catch (RuntimeException exception) {
            printBlock("EXCEPTIONS", String.valueOf(exception));
            exception.printStackTrace(System.out);
            exception.printStackTrace();
            throw exception;
        } catch (Exception exception) {
            printBlock("EXCEPTIONS", String.valueOf(exception));
            exception.printStackTrace(System.out);
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }

    private void publishReviewInventory() {
        super.test_1();

        Product product = new Product();
        product.setProductId("PROD-SHAMPOO-250");
        product.setSku("SHAMPOO-250");
        product.setName("Shampoo 250 mL");
        product.setUnitOfMeasure("bottle");

        Location location = new Location();
        location.setLocationId("LOC-001");
        location.setName("Primary Inventory Location");
        location.setLocationType("storage");

        Inventory inventory = new Inventory();
        inventory.setInventoryId("INV-001");
        inventory.setQuantityOnHand(5);
        inventory.setQuantityReserved(4);
        inventory.setProduct(product);
        inventory.setLocation(location);

        StringBuilder scenario = new StringBuilder();
        scenario.append("Inventory:INV-001\n");
        scenario.append("quantityOnHand=5\n");
        scenario.append("quantityReserved=4\n");
        scenario.append("Sales:SALE-001\n");
        scenario.append("quantitySold=25\n");
        scenario.append("Vendor:VENDOR-001\n");
        scenario.append("leadTimeDays=7\n");
        scenario.append("Product:PROD-SHAMPOO-250 unchanged\n");
        scenario.append("Location:LOC-001 unchanged");
        printBlock("SCENARIO FACT VALUES", scenario.toString());

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("inventory", gson.toJsonTree(inventory));
        String arrayPayload = "[" + root.toString() + "]";

        final JitFactExtractor domainExtractor = new JitFactExtractor();
        FactExtractor publicationExtractor = new FactExtractor() {
            @Override
            public List<Fact> extract(String jsonArray) {
                JsonArray elements =
                        JsonParser.parseString(jsonArray).getAsJsonArray();
                String elementJson = elements.get(0).toString();
                return domainExtractor.extract(elementJson);
            }
        };

        LLMContext context = new LLMContext();
        context.build(
                "jit_substrate",
                arrayPayload,
                publicationExtractor,
                new JitRelationshipProvider(),
                null);

        CausalLLMBridge bridge = new CausalLLMBridge();
        bridge.submit(context);

        GraphBuilder graphBuilder = GraphBuilder.getInstance();
        GraphSnapshot snapshot = null;
        if (graphBuilder != null) {
            snapshot = graphBuilder.snapshot();
        }
        printRelevantSnapshot(snapshot);

        Fact publishedInventory = null;
        if (snapshot != null && snapshot.nodes() != null) {
            publishedInventory = snapshot.nodes().get("Inventory:INV-001");
        }
        String publishedText = null;
        if (publishedInventory != null) {
            publishedText = publishedInventory.getText();
        }
        if (publishedText == null
                || publishedText.indexOf("\"quantityOnHand\":5") < 0
                || publishedText.indexOf("\"quantityReserved\":4") < 0) {
            throw new RuntimeException(
                    "changed Inventory fact was not visible in the CGO snapshot: "
                            + String.valueOf(publishedText));
        }
    }

    private String loadJitSql() throws Exception {
        InputStream stream =
                JitDecisionIT.class.getResourceAsStream(
                        "/jit/v14-jit-decision-query.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /jit/v14-jit-decision-query.json");
        }

        byte[] bytes;
        try {
            bytes = stream.readAllBytes();
        } finally {
            stream.close();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Perception jitPerception(GraphSnapshot snapshot) {
        Map<String, Fact> snapshotNodes = null;
        Map<String, Edge> snapshotEdges = null;
        if (snapshot != null) {
            snapshotNodes = snapshot.nodes();
            snapshotEdges = snapshot.edges();
        }

        List<Fact> facts = new ArrayList<Fact>();
        Map<String, Fact> nodes = new HashMap<String, Fact>();
        int index = 0;
        while (index < QUERY_FACT_IDS.length) {
            String factId = QUERY_FACT_IDS[index];
            Fact fact = null;
            if (snapshotNodes != null) {
                fact = snapshotNodes.get(factId);
            }
            if (fact == null) {
                fact = new Fact(factId, null);
            }
            facts.add(fact);
            nodes.put(factId, fact);
            index = index + 1;
        }

        Fact anchor = nodes.get("Inventory:INV-001");

        Observable observable = new Observable();
        observable.setObservableFactAnchor(anchor);
        observable.setObservableFacts(facts);

        OperationalView operationalView = new OperationalView();
        operationalView.setViewId("ov-jit-decision-v14");
        operationalView.setObservedView(facts);

        Map<String, Edge> edges = new HashMap<String, Edge>();
        if (snapshotEdges != null) {
            for (String edgeId : snapshotEdges.keySet()) {
                Edge edge = snapshotEdges.get(edgeId);
                if (edge == null) {
                    continue;
                }
                if (!nodes.containsKey(edge.getFromFactId())
                        || !nodes.containsKey(edge.getToFactId())) {
                    continue;
                }
                edges.put(edgeId, edge);
            }
        }

        GraphSnapshot reasoningView = new GraphSnapshot(nodes, edges);

        Observation observation = new Observation();
        observation.setObservable(observable);
        observation.setOperationalView(operationalView);
        observation.setReasoningView(reasoningView);

        Perception perception = new Perception();
        perception.setObservation(observation);
        return perception;
    }

    private void observeTxResult(TxExecutionResult txResult) {
        Console.log("JitDecisionIT.txResult", String.valueOf(txResult));
        Assertions.assertNotNull(txResult);
        Assertions.assertNotNull(txResult.getStepResults());

        List<String> commitOrder = txResult.getCommitOrder();
        Assertions.assertNotNull(commitOrder);
        Assertions.assertEquals(1, commitOrder.size());
        Assertions.assertEquals(QUERY_NAME, commitOrder.get(0));

        List<TxStepResult> steps = txResult.getStepResults();
        Console.log(
                "JitDecisionIT.completedSteps",
                String.valueOf(steps.size()));
        Assertions.assertTrue(steps.size() <= commitOrder.size());

        if (txResult.getGateResult() == null) {
            Console.log("JitDecisionIT.gate", "null");
        } else {
            Console.log("JitDecisionIT.gate", String.valueOf(txResult.getGateResult()));
        }
        Console.log(
                "JitDecisionIT.approved",
                String.valueOf(txResult.isApproved()));

        if (txResult.toJson() == null) {
            printBlock("TX RESULT JSON", "null");
        } else {
            printBlock("TX RESULT JSON", txResult.toJson());
        }

        int index = 0;
        while (index < steps.size()) {
            TxStepResult step = steps.get(index);
            Assertions.assertNotNull(step);
            Assertions.assertEquals(commitOrder.get(index), step.getId());
            this.observeStep(step);
            index = index + 1;
        }
    }

    private void observeStep(TxStepResult step) {
        QueryResult queryResult = step.getQueryResult();
        Console.log("JitDecisionIT.step.id", String.valueOf(step.getId()));
        if (queryResult == null) {
            Console.log("JitDecisionIT.queryResult", "null");
            printBlock("REQUEST JSON", "null");
            printBlock("QUERY EXECUTION JSON", "null");
            printBlock("RAW MODEL RESPONSE", "null");
            return;
        }

        Console.log(
                "JitDecisionIT.queryResult.ok",
                String.valueOf(queryResult.isOk()));
        if (queryResult.getWhy() == null) {
            Console.log("JitDecisionIT.queryResult.why", "null");
        } else {
            Console.log(
                    "JitDecisionIT.queryResult.why",
                    String.valueOf(queryResult.getWhy()));
        }

        if (queryResult.getRequestJson() == null) {
            printBlock("REQUEST JSON", "null");
        } else {
            printBlock("REQUEST JSON", queryResult.getRequestJson().toString());
        }

        if (queryResult.getQueryExecutionJson() == null) {
            printBlock("QUERY EXECUTION JSON", "null");
            printBlock("RAW MODEL RESPONSE", "null");
            return;
        }

        printBlock(
                "QUERY EXECUTION JSON",
                queryResult.getQueryExecutionJson().toString());

        QueryExecution<?> execution =
                QueryExecution.fromJson(queryResult.getQueryExecutionJson());
        if (execution == null || execution.getRawResponse() == null) {
            printBlock("RAW MODEL RESPONSE", "null");
        } else {
            printBlock("RAW MODEL RESPONSE", execution.getRawResponse());
        }
    }

    private void observeOnce(String sql) {
        System.out.print("=== RESTCLIENT REQUEST BEGIN ===\n");
        System.out.print(sql);
        System.out.print("\n=== RESTCLIENT REQUEST END ===\n");

        QueryClient client = new RESTClient();
        QueryResult result = client.query(sql);
        String response = this.rawResponse(result);

        System.out.print("=== RESTCLIENT RESPONSE BEGIN ===\n");
        if (response == null) {
            System.out.print("null\n");
        } else {
            System.out.print(response);
            System.out.print("\n");
        }
        System.out.print("=== RESTCLIENT RESPONSE END ===\n");

        String extracted = extractResultValue(response);
        System.out.print("=== EXTRACTED RESULT BEGIN ===\n");
        if (extracted == null) {
            System.out.print("null\n");
        } else {
            System.out.print(extracted);
            System.out.print("\n");
        }
        System.out.print("=== EXTRACTED RESULT END ===\n");
    }

    private void observeLoop(String sql, boolean successExpected) {
        int runCount = Integer.parseInt(
                System.getProperty("jit.runs", "1"));

        int correct = 0;
        int incorrect = 0;
        int unresolved = 0;
        int completed = 0;

        for (int run = 1; run <= runCount; run++) {
            System.out.print("=== RUN " + run + " BEGIN ===\n");
            try {
                System.out.print("=== RESTCLIENT REQUEST BEGIN ===\n");
                System.out.print(sql);
                System.out.print("\n=== RESTCLIENT REQUEST END ===\n");

                QueryClient client = new RESTClient();
                QueryResult result = client.query(sql);
                String response = this.rawResponse(result);

                System.out.print("=== RESTCLIENT RESPONSE BEGIN ===\n");
                if (response == null) {
                    System.out.print("null\n");
                } else {
                    System.out.print(response);
                    System.out.print("\n");
                }
                System.out.print("=== RESTCLIENT RESPONSE END ===\n");

                String extracted = extractCognitiveOutcome(response);
                if (extracted == null) {
                    unresolved = unresolved + 1;
                } else if (successExpected && extracted.equalsIgnoreCase("success")) {
                    correct = correct + 1;
                } else if (successExpected && extracted.equalsIgnoreCase("fail")) {
                    incorrect = incorrect + 1;
                } else if (!successExpected && extracted.equalsIgnoreCase("fail")) {
                    correct = correct + 1;
                } else if (!successExpected && extracted.equalsIgnoreCase("success")) {
                    incorrect = incorrect + 1;
                } else {
                    unresolved = unresolved + 1;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                unresolved = unresolved + 1;
            }
            completed = completed + 1;
            System.out.print("=== RUN " + run + " END ===\n");
        }

        int failures = incorrect + unresolved;
        String failureRate = "0";
        if (completed > 0) {
            failureRate = Integer.toString((failures * 100) / completed);
        }

        System.out.print("=== OBSERVATION SUMMARY BEGIN ===\n");
        System.out.print("completed invocations: " + completed + "\n");
        if (successExpected) {
            System.out.print("correct success: " + correct + "\n");
        } else {
            System.out.print("correct fail: " + correct + "\n");
        }
        System.out.print("incorrect decisions: " + incorrect + "\n");
        System.out.print("missing/unresolved: " + unresolved + "\n");
        System.out.print("semantic failures: " + failures + "\n");
        System.out.print("semantic failure rate: " + failureRate + "%\n");
        System.out.print("=== OBSERVATION SUMMARY END ===\n");
    }

    private String rawResponse(QueryResult result) {
        if (result == null) {
            return null;
        }
        if (result.getQueryExecutionJson() == null) {
            return null;
        }
        QueryExecution<?> execution =
                QueryExecution.fromJson(result.getQueryExecutionJson());
        if (execution == null) {
            return null;
        }
        return execution.getRawResponse();
    }

    private void printPublishedPhase0(GraphSnapshot snapshot) {
        StringBuilder facts = new StringBuilder();
        StringBuilder relationships = new StringBuilder();
        if (snapshot == null) {
            facts.append("null");
            relationships.append("null");
        } else {
            Map<String, Fact> nodes = snapshot.nodes();
            int index = 0;
            while (index < QUERY_FACT_IDS.length) {
                String factId = QUERY_FACT_IDS[index];
                Fact fact = null;
                if (nodes != null) {
                    fact = nodes.get(factId);
                }
                facts.append(factId);
                facts.append('\n');
                if (fact == null) {
                    facts.append("missing");
                } else {
                    facts.append("mode=");
                    facts.append(String.valueOf(fact.getMode()));
                    facts.append('\n');
                    facts.append(String.valueOf(fact.getText()));
                }
                facts.append('\n');
                index = index + 1;
            }

            Set<String> ids = new LinkedHashSet<String>();
            int idIndex = 0;
            while (idIndex < QUERY_FACT_IDS.length) {
                ids.add(QUERY_FACT_IDS[idIndex]);
                idIndex = idIndex + 1;
            }
            Map<String, Edge> edges = snapshot.edges();
            if (edges == null) {
                relationships.append("null");
            } else {
                boolean any = false;
                for (String edgeId : edges.keySet()) {
                    Edge edge = edges.get(edgeId);
                    if (edge == null) {
                        continue;
                    }
                    if (!ids.contains(edge.getFromFactId()) && !ids.contains(edge.getToFactId())) {
                        continue;
                    }
                    any = true;
                    relationships.append("id=");
                    relationships.append(String.valueOf(edge.getId()));
                    relationships.append('\n');
                    relationships.append("from=");
                    relationships.append(String.valueOf(edge.getFromFactId()));
                    relationships.append('\n');
                    relationships.append("to=");
                    relationships.append(String.valueOf(edge.getToFactId()));
                    relationships.append('\n');
                    relationships.append("mode=");
                    relationships.append(String.valueOf(edge.getMode()));
                    relationships.append('\n');
                    relationships.append("text=");
                    relationships.append(String.valueOf(edge.getText()));
                    relationships.append('\n');
                }
                if (!any) {
                    relationships.append("none");
                }
            }
        }
        printBlock("PUBLISHED PHASE 0 FACTS", facts.toString());
        printBlock("PUBLISHED PHASE 0 RELATIONSHIPS", relationships.toString());
    }

    private void printRelevantSnapshot(GraphSnapshot snapshot) {
        if (snapshot == null) {
            printBlock("GRAPH SNAPSHOT", "null");
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append("snapshotId=");
        text.append(String.valueOf(snapshot.id()));
        text.append('\n');
        Map<String, Fact> nodes = snapshot.nodes();
        int index = 0;
        while (index < QUERY_FACT_IDS.length) {
            String factId = QUERY_FACT_IDS[index];
            Fact fact = null;
            if (nodes != null) {
                fact = nodes.get(factId);
            }
            text.append("node=");
            text.append(factId);
            text.append('\n');
            if (fact == null) {
                text.append("missing");
            } else {
                text.append(String.valueOf(fact.getText()));
            }
            text.append('\n');
            index = index + 1;
        }
        printBlock("GRAPH SNAPSHOT", text.toString());
    }

    private void printBlock(String name, String body) {
        System.out.print("=== ");
        System.out.print(name);
        System.out.print(" BEGIN ===\n");
        System.out.print(body);
        System.out.print("\n=== ");
        System.out.print(name);
        System.out.print(" END ===\n");
    }

    private static String extractCognitiveOutcome(String response) {
        if (response == null) {
            return null;
        }
        String content = unwrapMarkdownFence(response);
        if (content == null) {
            return null;
        }
        content = unwrapOuterDecoration(content.trim());
        if (isStandaloneToken(content)) {
            return content;
        }
        return extractWrappedResult(response);
    }

    private static String unwrapOuterDecoration(String content) {
        String current = content.trim();
        if (current.length() >= 4 && current.startsWith("**") && current.endsWith("**")) {
            current = current.substring(2, current.length() - 2).trim();
        }
        if (current.length() >= 2 && current.startsWith("`") && current.endsWith("`")) {
            current = current.substring(1, current.length() - 1).trim();
        }
        if (current.length() >= 2 && current.startsWith("\"") && current.endsWith("\"")) {
            current = current.substring(1, current.length() - 1).trim();
        }
        return current;
    }

    private static boolean isStandaloneToken(String content) {
        if (content == null || content.length() == 0) {
            return false;
        }
        int i = 0;
        while (i < content.length()) {
            if (!Character.isLetter(content.charAt(i))) {
                return false;
            }
            i++;
        }
        return true;
    }

    private static String extractResultValue(String response) {
        if (response == null) {
            return null;
        }

        String content = unwrapMarkdownFence(response);
        if (content == null) {
            return null;
        }
        content = unwrapOuterDecoration(content.trim());
        if ("success".equals(content) || "fail".equals(content)) {
            return content;
        }
        return null;
    }

    private static String extractWrappedResult(String response) {
        if (response == null) {
            return null;
        }

        String content = unwrapMarkdownFence(response);
        if (content == null) {
            return null;
        }
        content = content.trim();
        if (content.length() == 0) {
            return null;
        }

        if (content.charAt(0) == '{') {
            return extractJsonResult(content);
        }
        return extractKeyValueResult(content);
    }

    private static String unwrapMarkdownFence(String response) {
        String trimmed = response.trim();
        int openLen = 0;
        if (trimmed.startsWith("````")) {
            openLen = 4;
        } else {
            if (trimmed.startsWith("```")) {
                openLen = 3;
            }
        }
        if (openLen == 0) {
            return trimmed;
        }

        int pos = openLen;
        if (pos < trimmed.length()) {
            char first = trimmed.charAt(pos);
            if (first == '\n') {
                pos++;
            } else {
                if (first == '\r') {
                    pos++;
                    if (pos < trimmed.length()) {
                        if (trimmed.charAt(pos) == '\n') {
                            pos++;
                        }
                    }
                } else {
                    if (Character.isLetter(first)) {
                        while (pos < trimmed.length()) {
                            char current = trimmed.charAt(pos);
                            if (current == '\n') {
                                pos++;
                                break;
                            }
                            if (current == '\r') {
                                pos++;
                                if (pos < trimmed.length()) {
                                    if (trimmed.charAt(pos) == '\n') {
                                        pos++;
                                    }
                                }
                                break;
                            }
                            pos++;
                        }
                    }
                }
            }
        }

        String afterOpen = trimmed.substring(pos);
        String fence;
        if (openLen == 4) {
            fence = "````";
        } else {
            fence = "```";
        }
        if (afterOpen.endsWith(fence)) {
            afterOpen = afterOpen.substring(0, afterOpen.length() - fence.length());
        }
        return afterOpen.trim();
    }

    private static String extractJsonResult(String content) {
        JsonElement element;
        try {
            element = JsonParser.parseString(content);
        } catch (Exception exception) {
            return null;
        }
        if (element == null) {
            return null;
        }
        if (!element.isJsonObject()) {
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        if (object.size() != 1) {
            return null;
        }
        if (!object.has("result")) {
            return null;
        }

        JsonElement resultElement = object.get("result");
        if (resultElement == null) {
            return null;
        }
        if (resultElement.isJsonNull()) {
            return null;
        }
        if (!resultElement.isJsonPrimitive()) {
            return null;
        }

        JsonPrimitive primitive = resultElement.getAsJsonPrimitive();
        if (!primitive.isString()) {
            return null;
        }
        return primitive.getAsString();
    }

    private static String extractKeyValueResult(String content) {
        if (!content.startsWith("result")) {
            return null;
        }

        int index = "result".length();
        while (index < content.length()) {
            if (!Character.isWhitespace(content.charAt(index))) {
                break;
            }
            index++;
        }
        if (index >= content.length()) {
            return null;
        }

        char separator = content.charAt(index);
        if (separator != '=') {
            if (separator != ':') {
                return null;
            }
        }
        index++;

        while (index < content.length()) {
            if (!Character.isWhitespace(content.charAt(index))) {
                break;
            }
            index++;
        }
        if (index >= content.length()) {
            return null;
        }

        String value;
        if (content.charAt(index) == '"') {
            int endQuote = -1;
            int cursor = index + 1;
            while (cursor < content.length()) {
                if (content.charAt(cursor) == '"') {
                    endQuote = cursor;
                    break;
                }
                cursor++;
            }
            if (endQuote < 0) {
                return null;
            }
            value = content.substring(index + 1, endQuote);
            index = endQuote + 1;
        } else {
            int end = index;
            while (end < content.length()) {
                if (Character.isWhitespace(content.charAt(end))) {
                    break;
                }
                end++;
            }
            value = content.substring(index, end);
            index = end;
        }

        while (index < content.length()) {
            if (!Character.isWhitespace(content.charAt(index))) {
                return null;
            }
            index++;
        }
        if (value.length() == 0) {
            return null;
        }
        return value;
    }
}
