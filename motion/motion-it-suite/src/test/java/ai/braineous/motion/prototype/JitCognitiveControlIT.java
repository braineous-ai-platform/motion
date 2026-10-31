package ai.braineous.motion.prototype;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.observe.Console;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Disabled("Legacy arithmetic CPL prototype; superseded by the Phase 2 decision-oriented contract.")
@QuarkusTest
public class JitCognitiveControlIT {

    private static final String INVENTORY_FACT_ID = "Inventory:INV-001";

    private static final String SALE_FACT_ID = "MotionEvent:SALE-002";

    private static final String SQL =
            "select remainingQuantity "
                    + "from llm "
                    + "where factId = '" + INVENTORY_FACT_ID + "' "
                    + "and relatedFactIds = '" + SALE_FACT_ID + "' "
                    + "control intent = 'determine_remaining_inventory', "
                    + "action = 'calculate', "
                    + "subject = 'inventory_for_product', "
                    + "decision = 'remaining_quantity', "
                    + "basis = 'starting_inventory_minus_sold_quantity', "
                    + "goal = 'remaining_quantity'";

    @Test
    public void test_1() {
        Console.log("JitCognitiveControlIT.sql", SQL);
        Console.log(
                "JitCognitiveControlIT.control.intent",
                "determine_remaining_inventory");
        Console.log(
                "JitCognitiveControlIT.control.action",
                "calculate");
        Console.log(
                "JitCognitiveControlIT.control.subject",
                "inventory_for_product");
        Console.log(
                "JitCognitiveControlIT.control.decision",
                "remaining_quantity");
        Console.log(
                "JitCognitiveControlIT.control.basis",
                "starting_inventory_minus_sold_quantity");
        Console.log(
                "JitCognitiveControlIT.control.goal",
                "remaining_quantity");

        GraphSnapshot snapshot = GraphBuilder.getInstance().snapshot();
        boolean inventoryPresent = false;
        boolean salePresent = false;

        if (snapshot == null) {
            Console.log("JitCognitiveControlIT.graph", "null");
        } else {
            Map<String, Fact> nodes = snapshot.nodes();
            if (nodes == null) {
                Console.log("JitCognitiveControlIT.facts", "null");
            } else {
                Console.log(
                        "JitCognitiveControlIT.facts.count",
                        String.valueOf(nodes.size()));

                for (String nodeId : nodes.keySet()) {
                    Fact fact = nodes.get(nodeId);
                    if (fact == null) {
                        Console.log("JitCognitiveControlIT.fact", "null");
                        continue;
                    }

                    String factId = fact.getId();
                    Console.log(
                            "JitCognitiveControlIT.fact.id",
                            shown(factId));
                    Console.log(
                            "JitCognitiveControlIT.fact.mode",
                            shown(fact.getMode()));
                    Console.log(
                            "JitCognitiveControlIT.fact.text",
                            shown(fact.getText()));

                    if (INVENTORY_FACT_ID.equals(factId)) {
                        inventoryPresent = true;
                    }
                    if (SALE_FACT_ID.equals(factId)) {
                        salePresent = true;
                    }
                }
            }
        }

        Console.log(
                "JitCognitiveControlIT.fact.Inventory:INV-001",
                presence(inventoryPresent));
        Console.log(
                "JitCognitiveControlIT.fact.MotionEvent:SALE-002",
                presence(salePresent));

        if (!inventoryPresent || !salePresent) {
            Console.log(
                    "JitCognitiveControlIT.queryRequest",
                    "null");
            Console.log(
                    "JitCognitiveControlIT.execution",
                    "not_invoked");
            Console.log(
                    "JitCognitiveControlIT.llm.response",
                    "null");
            Console.log(
                    "JitCognitiveControlIT.validation",
                    "null");
            Console.log(
                    "JitCognitiveControlIT.remainingQuantity",
                    "null");
            return;
        }

        Console.log(
                "JitCognitiveControlIT.execution",
                "not_invoked");
        Console.log(
                "JitCognitiveControlIT.contract",
                "AnalysisOrchestrator.analyze requires a Perception");
        Console.log(
                "JitCognitiveControlIT.queryRequest",
                "null");
        Console.log(
                "JitCognitiveControlIT.llm.response",
                "null");
        Console.log(
                "JitCognitiveControlIT.validation",
                "null");
        Console.log(
                "JitCognitiveControlIT.remainingQuantity",
                "null");
    }

    private String presence(boolean present) {
        if (present) {
            return "present";
        }
        return "absent";
    }

    private String shown(String value) {
        if (value == null) {
            return "null";
        }
        return value;
    }
}
