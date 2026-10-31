package ai.braineous.motion.it.jit;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.services.cgo.causal.CausalLLMBridge;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.braineous.dd.llm.query.client.QueryClient;
import io.braineous.dd.llm.query.client.QueryResult;
import io.braineous.dd.llm.query.client.RESTClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    @Test
    @Override
    public void test_1() {
        try {
            super.test_1();

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

            String sql = new String(bytes, StandardCharsets.UTF_8);
            printBlock("LOADED CPL SQL", sql);

            GraphBuilder graphBuilder = GraphBuilder.getInstance();
            GraphSnapshot snapshot = null;
            if (graphBuilder != null) {
                snapshot = graphBuilder.snapshot();
            }
            printPublishedPhase0(snapshot);
            printRelevantSnapshot(snapshot);

            QueryClient client = new RESTClient();
            QueryResult result = client.query(sql);

            printBlock("QUERY RESULT", String.valueOf(result));
            if (result == null) {
                printBlock("QUERY RESULT STATUS", "null");
                printBlock("QUERY RESULT WHY", "null");
                printBlock("QUERY RESULT REQUEST JSON", "null");
                printBlock("QUERY RESULT EXECUTION JSON", "null");
                printBlock("QUERY EXECUTION STATUS", "null");
                printBlock("QUERY EXECUTION STAGE", "null");
                printBlock("PROMPT VALIDATION", "null");
                printBlock("LLM RESPONSE VALIDATION", "null");
                printBlock("DOMAIN VALIDATION", "null");
                printBlock("RAW LLM RESPONSE", "null");
                printBlock("EXTRACTED RESULT", "null");
            } else {
                printBlock("QUERY RESULT STATUS", String.valueOf(result.isOk()));
                if (result.getWhy() == null) {
                    printBlock("QUERY RESULT WHY", "null");
                } else {
                    printBlock("QUERY RESULT WHY", result.getWhy().toString());
                }
                if (result.getRequestJson() == null) {
                    printBlock("QUERY RESULT REQUEST JSON", "null");
                } else {
                    printBlock("QUERY RESULT REQUEST JSON", result.getRequestJson().toString());
                }
                if (result.getQueryExecutionJson() == null) {
                    printBlock("QUERY RESULT EXECUTION JSON", "null");
                    printBlock("QUERY EXECUTION STATUS", "null");
                    printBlock("QUERY EXECUTION STAGE", "null");
                    printBlock("PROMPT VALIDATION", "null");
                    printBlock("LLM RESPONSE VALIDATION", "null");
                    printBlock("DOMAIN VALIDATION", "null");
                    printBlock("RAW LLM RESPONSE", "null");
                    printBlock("EXTRACTED RESULT", "null");
                } else {
                    printBlock(
                            "QUERY RESULT EXECUTION JSON",
                            result.getQueryExecutionJson().toString());
                    QueryExecution<?> execution =
                            QueryExecution.fromJson(result.getQueryExecutionJson());
                    if (execution == null) {
                        printBlock("QUERY EXECUTION STATUS", "null");
                        printBlock("QUERY EXECUTION STAGE", "null");
                        printBlock("PROMPT VALIDATION", "null");
                        printBlock("LLM RESPONSE VALIDATION", "null");
                        printBlock("DOMAIN VALIDATION", "null");
                        printBlock("RAW LLM RESPONSE", "null");
                        printBlock("EXTRACTED RESULT", "null");
                    } else {
                        printBlock("QUERY EXECUTION STATUS", String.valueOf(execution.getStatus()));
                        printBlock("QUERY EXECUTION STAGE", String.valueOf(execution.getStage()));
                        printBlock("PROMPT VALIDATION", String.valueOf(execution.getPromptValidation()));
                        printBlock(
                                "LLM RESPONSE VALIDATION",
                                String.valueOf(execution.getLlmResponseValidation()));
                        printBlock(
                                "DOMAIN VALIDATION",
                                String.valueOf(execution.getDomainValidation()));
                        printBlock("RAW LLM RESPONSE", String.valueOf(execution.getRawResponse()));
                        printBlock(
                                "EXTRACTED RESULT",
                                String.valueOf(extractResultValue(execution.getRawResponse())));
                    }
                }
            }

            printBlock("EXCEPTIONS", "none");
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
    public void test_2() {
        try {
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

            String sql = new String(bytes, StandardCharsets.UTF_8);
            printBlock("LOADED CPL SQL", sql);

            QueryClient client = new RESTClient();
            QueryResult result = client.query(sql);

            printBlock("QUERY RESULT", String.valueOf(result));
            if (result == null) {
                printBlock("QUERY RESULT STATUS", "null");
                printBlock("QUERY RESULT WHY", "null");
                printBlock("QUERY RESULT REQUEST JSON", "null");
                printBlock("QUERY RESULT EXECUTION JSON", "null");
                printBlock("QUERY EXECUTION STATUS", "null");
                printBlock("QUERY EXECUTION STAGE", "null");
                printBlock("PROMPT VALIDATION", "null");
                printBlock("LLM RESPONSE VALIDATION", "null");
                printBlock("DOMAIN VALIDATION", "null");
                printBlock("RAW LLM RESPONSE", "null");
                printBlock("EXTRACTED RESULT", "null");
            } else {
                printBlock("QUERY RESULT STATUS", String.valueOf(result.isOk()));
                if (result.getWhy() == null) {
                    printBlock("QUERY RESULT WHY", "null");
                } else {
                    printBlock("QUERY RESULT WHY", result.getWhy().toString());
                }
                if (result.getRequestJson() == null) {
                    printBlock("QUERY RESULT REQUEST JSON", "null");
                } else {
                    printBlock("QUERY RESULT REQUEST JSON", result.getRequestJson().toString());
                }
                if (result.getQueryExecutionJson() == null) {
                    printBlock("QUERY RESULT EXECUTION JSON", "null");
                    printBlock("QUERY EXECUTION STATUS", "null");
                    printBlock("QUERY EXECUTION STAGE", "null");
                    printBlock("PROMPT VALIDATION", "null");
                    printBlock("LLM RESPONSE VALIDATION", "null");
                    printBlock("DOMAIN VALIDATION", "null");
                    printBlock("RAW LLM RESPONSE", "null");
                    printBlock("EXTRACTED RESULT", "null");
                } else {
                    printBlock(
                            "QUERY RESULT EXECUTION JSON",
                            result.getQueryExecutionJson().toString());
                    QueryExecution<?> execution =
                            QueryExecution.fromJson(result.getQueryExecutionJson());
                    if (execution == null) {
                        printBlock("QUERY EXECUTION STATUS", "null");
                        printBlock("QUERY EXECUTION STAGE", "null");
                        printBlock("PROMPT VALIDATION", "null");
                        printBlock("LLM RESPONSE VALIDATION", "null");
                        printBlock("DOMAIN VALIDATION", "null");
                        printBlock("RAW LLM RESPONSE", "null");
                        printBlock("EXTRACTED RESULT", "null");
                    } else {
                        printBlock("QUERY EXECUTION STATUS", String.valueOf(execution.getStatus()));
                        printBlock("QUERY EXECUTION STAGE", String.valueOf(execution.getStage()));
                        printBlock("PROMPT VALIDATION", String.valueOf(execution.getPromptValidation()));
                        printBlock(
                                "LLM RESPONSE VALIDATION",
                                String.valueOf(execution.getLlmResponseValidation()));
                        printBlock(
                                "DOMAIN VALIDATION",
                                String.valueOf(execution.getDomainValidation()));
                        printBlock("RAW LLM RESPONSE", String.valueOf(execution.getRawResponse()));
                        printBlock(
                                "EXTRACTED RESULT",
                                String.valueOf(extractResultValue(execution.getRawResponse())));
                    }
                }
            }

            printBlock("EXCEPTIONS", "none");
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

    private static String extractResultValue(String response) {
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
