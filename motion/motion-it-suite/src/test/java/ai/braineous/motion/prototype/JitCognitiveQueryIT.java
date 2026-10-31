package ai.braineous.motion.prototype;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.dd.llm.query.client.QueryClient;
import io.braineous.dd.llm.query.client.QueryResult;
import io.braineous.dd.llm.query.client.RESTClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

@Disabled("Legacy arithmetic CPL prototype; superseded by the Phase 2 decision-oriented contract.")
@QuarkusTest
public class JitCognitiveQueryIT {

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
        Console.log("JitCognitiveQueryIT.sql", SQL);
        Console.log(
                "JitCognitiveQueryIT.control.intent",
                "determine_remaining_inventory");
        Console.log(
                "JitCognitiveQueryIT.control.action",
                "subtract");
        Console.log(
                "JitCognitiveQueryIT.control.subject",
                "Inventory.quantityOnHand");
        Console.log(
                "JitCognitiveQueryIT.control.operation",
                "Inventory.quantityOnHand - Sales.quantitySold");
        Console.log(
                "JitCognitiveQueryIT.control.basis",
                "related_system_facts");
        Console.log(
                "JitCognitiveQueryIT.control.goal",
                "remainingQuantity");

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

        QueryClient client = new RESTClient();
        QueryResult result = client.query(SQL);

        if (result == null) {
            Console.log("JitCognitiveQueryIT.queryResult", "null");
            Console.log("JitCognitiveQueryIT.queryRequest", "null");
            Console.log("JitCognitiveQueryIT.execution", "null");
            Console.log("JitCognitiveQueryIT.rawResponse", "null");
            Console.log("JitCognitiveQueryIT.validation", "null");
            Console.log("JitCognitiveQueryIT.remainingQuantity", "null");
            return;
        }

        Console.log(
                "JitCognitiveQueryIT.queryResult.ok",
                String.valueOf(result.isOk()));
        if (result.getWhy() == null) {
            Console.log("JitCognitiveQueryIT.queryResult.why", "null");
        } else {
            Console.log(
                    "JitCognitiveQueryIT.queryResult.why",
                    result.getWhy().toString());
        }
        if (result.getRequestJson() == null) {
            Console.log("JitCognitiveQueryIT.queryRequest", "null");
        } else {
            Console.log(
                    "JitCognitiveQueryIT.queryRequest",
                    result.getRequestJson().toString());
        }

        QueryExecution<?> execution = null;
        if (result.getQueryExecutionJson() != null) {
            execution = QueryExecution.fromJson(result.getQueryExecutionJson());
        }

        if (execution == null) {
            Console.log("JitCognitiveQueryIT.execution", "null");
            Console.log("JitCognitiveQueryIT.rawResponse", "null");
            Console.log("JitCognitiveQueryIT.validation", "null");
            Console.log("JitCognitiveQueryIT.remainingQuantity", "null");
            return;
        }

        Console.log(
                "JitCognitiveQueryIT.execution.status",
                String.valueOf(execution.getStatus()));
        Console.log(
                "JitCognitiveQueryIT.execution.stage",
                String.valueOf(execution.getStage()));
        Console.log(
                "JitCognitiveQueryIT.execution.ok",
                String.valueOf(execution.isOk()));
        Console.log(
                "JitCognitiveQueryIT.validation.prompt",
                String.valueOf(execution.getPromptValidation()));
        Console.log(
                "JitCognitiveQueryIT.validation.llmResponse",
                String.valueOf(execution.getLlmResponseValidation()));
        Console.log(
                "JitCognitiveQueryIT.validation.domain",
                String.valueOf(execution.getDomainValidation()));
        Console.log(
                "JitCognitiveQueryIT.rawResponse",
                String.valueOf(execution.getRawResponse()));
    }

    private void addFact(GraphBuilder graphBuilder, String id, String text) {
        Console.log("JitCognitiveQueryIT.fact.id", id);
        Console.log("JitCognitiveQueryIT.fact.text", text);
        graphBuilder.addNode(new Fact(id, text, new HashSet<String>(), "atomic"));
    }
}
