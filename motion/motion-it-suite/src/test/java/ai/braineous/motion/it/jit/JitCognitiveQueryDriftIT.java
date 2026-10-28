package ai.braineous.motion.it.jit;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.braineous.dd.llm.query.client.QueryClient;
import io.braineous.dd.llm.query.client.QueryResult;
import io.braineous.dd.llm.query.client.RESTClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@QuarkusTest
public class JitCognitiveQueryDriftIT {

    private static final String INVENTORY_FACT_ID = "Inventory:INV-001";

    private static final String SALE_FACT_ID = "Sales:SALE-002";

    private static final String SQL =
            "select remainingQuantity "
                    + "from llm "
                    + "where factId = '" + INVENTORY_FACT_ID + "' "
                    + "and relatedFactIds = '" + SALE_FACT_ID + "' "
                    + "control method = 'determine_remaining_inventory', "
                    + "parameters = 'Inventory.quantityOnHand Sales.quantitySold', "
                    + "expression = 'Inventory.quantityOnHand - Sales.quantitySold', "
                    + "spec = 'Subtract Sales.quantitySold from Inventory.quantityOnHand. The runtime result is remainingQuantity.', "
                    + "result = 'result'";

    @Test
    public void test_1() {
        int runCount = Integer.parseInt(
                System.getProperty("motion.jit.drift.runs", "5"));

        QueryClient client = new RESTClient();
        List<String> failures = new ArrayList<String>();
        HashSet<String> uniqueRawResponses = new HashSet<String>();

        int run = 1;
        while (run <= runCount) {
            Console.log("JitCognitiveQueryDriftIT.run.begin", String.valueOf(run));

            GraphBuilder graphBuilder = GraphBuilder.getInstance();
            graphBuilder.clear();
            this.addFact(
                    graphBuilder,
                    "Product:PROD-SHAMPOO-250",
                    "{\"id\":\"Product:PROD-SHAMPOO-250\",\"kind\":\"Product\",\"mode\":\"atomic\",\"productId\":\"PROD-SHAMPOO-250\",\"sku\":\"SHAMPOO-250\",\"name\":\"Shampoo 250 mL\",\"unitOfMeasure\":\"bottle\"}");
            this.addFact(
                    graphBuilder,
                    INVENTORY_FACT_ID,
                    "{\"id\":\"Inventory:INV-001\",\"kind\":\"Inventory\",\"mode\":\"atomic\",\"inventoryId\":\"INV-001\",\"quantityOnHand\":500,\"quantityReserved\":0,\"product\":\"Product:PROD-SHAMPOO-250\",\"location\":\"Location:LOC-001\"}");
            this.addFact(
                    graphBuilder,
                    "Sales:SALE-001",
                    "{\"id\":\"Sales:SALE-001\",\"kind\":\"Sales\",\"mode\":\"atomic\",\"saleId\":\"SALE-001\",\"quantitySold\":25,\"soldAt\":1792584000000,\"product\":\"Product:PROD-SHAMPOO-250\"}");
            this.addFact(
                    graphBuilder,
                    SALE_FACT_ID,
                    "{\"id\":\"Sales:SALE-002\",\"kind\":\"Sales\",\"mode\":\"atomic\",\"saleId\":\"SALE-002\",\"quantitySold\":1,\"product\":\"Product:PROD-SHAMPOO-250\"}");
            this.addFact(
                    graphBuilder,
                    "Vendor:VENDOR-001",
                    "{\"id\":\"Vendor:VENDOR-001\",\"kind\":\"Vendor\",\"mode\":\"atomic\",\"vendorId\":\"VENDOR-001\",\"name\":\"Shampoo Supplier\",\"leadTimeDays\":7,\"product\":\"Product:PROD-SHAMPOO-250\"}");
            this.addFact(
                    graphBuilder,
                    "Location:LOC-001",
                    "{\"id\":\"Location:LOC-001\",\"kind\":\"Location\",\"mode\":\"atomic\",\"locationId\":\"LOC-001\",\"name\":\"Primary Inventory Location\",\"locationType\":\"storage\"}");

            QueryResult result = client.query(SQL);
            QueryExecution<?> execution = null;
            String rawResponse = null;
            String status = null;
            String promptValidation = null;
            String llmResponseValidation = null;
            String domainValidation = null;

            if (result != null && result.getQueryExecutionJson() != null) {
                execution = QueryExecution.fromJson(result.getQueryExecutionJson());
            }

            if (execution != null) {
                rawResponse = execution.getRawResponse();
                status = String.valueOf(execution.getStatus());
                promptValidation = String.valueOf(execution.getPromptValidation());
                llmResponseValidation = String.valueOf(execution.getLlmResponseValidation());
                domainValidation = String.valueOf(execution.getDomainValidation());
            }

            if (rawResponse == null) {
                uniqueRawResponses.add("null");
            } else {
                uniqueRawResponses.add(rawResponse);
            }

            String extracted = extractResultValue(rawResponse);
            boolean accepted = false;
            if (extracted != null) {
                if (extracted.equals("remainingQuantity=499")) {
                    accepted = true;
                } else {
                    if (extracted.equals("499")) {
                        accepted = true;
                    }
                }
            }

            Console.log("JitCognitiveQueryDriftIT.run", String.valueOf(run));
            Console.log("JitCognitiveQueryDriftIT.rawResponse", String.valueOf(rawResponse));
            Console.log("JitCognitiveQueryDriftIT.execution.status", String.valueOf(status));
            Console.log("JitCognitiveQueryDriftIT.validation.prompt", String.valueOf(promptValidation));
            Console.log("JitCognitiveQueryDriftIT.validation.llmResponse", String.valueOf(llmResponseValidation));
            Console.log("JitCognitiveQueryDriftIT.validation.domain", String.valueOf(domainValidation));
            Console.log("JitCognitiveQueryDriftIT.extracted", String.valueOf(extracted));
            Console.log("JitCognitiveQueryDriftIT.correct", String.valueOf(accepted));

            if (!accepted) {
                failures.add(
                        "run=" + run
                                + " extracted=" + extracted
                                + " status=" + status
                                + " promptValidation=" + promptValidation
                                + " llmResponseValidation=" + llmResponseValidation
                                + "\n=== RAW RESPONSE BEGIN ===\n"
                                + rawResponse
                                + "\n=== RAW RESPONSE END ===\n");
            }

            Console.log("JitCognitiveQueryDriftIT.run.end", String.valueOf(run));
            run = run + 1;
        }

        Console.log(
                "JitCognitiveQueryDriftIT.uniqueRawResponseCount",
                String.valueOf(uniqueRawResponses.size()));
        Console.log(
                "JitCognitiveQueryDriftIT.failureCount",
                String.valueOf(failures.size()));

        int failureIndex = 0;
        while (failureIndex < failures.size()) {
            Console.log(
                    "JitCognitiveQueryDriftIT.failure",
                    failures.get(failureIndex));
            failureIndex = failureIndex + 1;
        }
    }

    private void addFact(GraphBuilder graphBuilder, String id, String text) {
        graphBuilder.addNode(new Fact(id, text, new HashSet<String>(), "atomic"));
    }

    static String extractResultValue(String response) {
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

    static String unwrapMarkdownFence(String response) {
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

    static String extractJsonResult(String content) {
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

    static String extractKeyValueResult(String content) {
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
