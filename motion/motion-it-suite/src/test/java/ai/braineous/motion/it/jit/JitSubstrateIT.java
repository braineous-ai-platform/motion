package ai.braineous.motion.it.jit;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.observe.Console;
import ai.braineous.rag.prompt.services.cgo.causal.CausalLLMBridge;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@QuarkusTest
public class JitSubstrateIT {

    private static final long SOLD_AT = 1792584000000L;

    private static final String CONTEXT_NAME = "jit_substrate";

    @Test
    public void test_1() {
        Product product = this.product();
        Location location = this.location();
        Vendor vendor = this.vendor(product);
        Sales sales = this.sales(product);
        Inventory inventory = this.inventory(product, location);

        this.logDomain(product, location, vendor, sales, inventory);

        String json = this.payload(
                product,
                inventory,
                sales,
                vendor,
                location);
        Console.log("JitSubstrateIT.domain.json", json);

        JitFactExtractor extractor = new JitFactExtractor();
        List<Fact> facts = extractor.extract(json);
        this.logFacts(facts);

        JitRelationshipProvider provider = new JitRelationshipProvider();
        List<Relationship> relationships =
                provider.provideRelationships(facts);
        this.logRelationships(relationships);

        Console.log(
                "JitSubstrateIT.publication.contextName",
                CONTEXT_NAME);

        String arrayPayload = "[" + json + "]";
        Console.log("JitSubstrateIT.publication.array", arrayPayload);

        final JitFactExtractor domainExtractor = extractor;
        FactExtractor publicationExtractor = new FactExtractor() {
            @Override
            public List<Fact> extract(String jsonArray) {
                Console.log(
                        "JitSubstrateIT.publication.extractor.input",
                        jsonArray);

                JsonArray elements =
                        JsonParser.parseString(jsonArray).getAsJsonArray();
                Console.log(
                        "JitSubstrateIT.publication.array.count",
                        String.valueOf(elements.size()));

                String elementJson = elements.get(0).toString();
                Console.log(
                        "JitSubstrateIT.publication.unwrapped",
                        elementJson);

                List<Fact> delegated =
                        domainExtractor.extract(elementJson);
                Console.log(
                        "JitSubstrateIT.publication.delegated.count",
                        String.valueOf(delegated.size()));

                return delegated;
            }
        };

        LLMContext context = new LLMContext();
        context.build(
                CONTEXT_NAME,
                arrayPayload,
                publicationExtractor,
                provider,
                null);

        CausalLLMBridge bridge = new CausalLLMBridge();
        GraphView graphView = bridge.submit(context);
        this.logPublication(graphView);
    }

    private void logDomain(
            Product product,
            Location location,
            Vendor vendor,
            Sales sales,
            Inventory inventory) {

        Console.log(
                "JitSubstrateIT.domain.product.productId",
                this.shown(product.getProductId()));
        Console.log(
                "JitSubstrateIT.domain.product.sku",
                this.shown(product.getSku()));
        Console.log(
                "JitSubstrateIT.domain.product.name",
                this.shown(product.getName()));
        Console.log(
                "JitSubstrateIT.domain.product.unitOfMeasure",
                this.shown(product.getUnitOfMeasure()));

        Console.log(
                "JitSubstrateIT.domain.location.locationId",
                this.shown(location.getLocationId()));
        Console.log(
                "JitSubstrateIT.domain.location.name",
                this.shown(location.getName()));
        Console.log(
                "JitSubstrateIT.domain.location.locationType",
                this.shown(location.getLocationType()));

        Console.log(
                "JitSubstrateIT.domain.vendor.vendorId",
                this.shown(vendor.getVendorId()));
        Console.log(
                "JitSubstrateIT.domain.vendor.name",
                this.shown(vendor.getName()));
        Console.log(
                "JitSubstrateIT.domain.vendor.leadTimeDays",
                String.valueOf(vendor.getLeadTimeDays()));
        Console.log(
                "JitSubstrateIT.domain.vendor.product",
                this.shown(vendor.getProduct().getProductId()));

        Console.log(
                "JitSubstrateIT.domain.sales.saleId",
                this.shown(sales.getSaleId()));
        Console.log(
                "JitSubstrateIT.domain.sales.quantitySold",
                String.valueOf(sales.getQuantitySold()));
        Console.log(
                "JitSubstrateIT.domain.sales.soldAt",
                String.valueOf(sales.getSoldAt()));
        Console.log(
                "JitSubstrateIT.domain.sales.product",
                this.shown(sales.getProduct().getProductId()));

        Console.log(
                "JitSubstrateIT.domain.inventory.inventoryId",
                this.shown(inventory.getInventoryId()));
        Console.log(
                "JitSubstrateIT.domain.inventory.quantityOnHand",
                String.valueOf(inventory.getQuantityOnHand()));
        Console.log(
                "JitSubstrateIT.domain.inventory.quantityReserved",
                String.valueOf(inventory.getQuantityReserved()));
        Console.log(
                "JitSubstrateIT.domain.inventory.product",
                this.shown(inventory.getProduct().getProductId()));
        Console.log(
                "JitSubstrateIT.domain.inventory.location",
                this.shown(inventory.getLocation().getLocationId()));
    }

    private void logFacts(List<Fact> facts) {
        if (facts == null) {
            Console.log("JitSubstrateIT.facts", "null");
            return;
        }

        Console.log(
                "JitSubstrateIT.facts.count",
                String.valueOf(facts.size()));

        for (Fact fact : facts) {
            if (fact == null) {
                Console.log("JitSubstrateIT.facts.fact", "null");
                continue;
            }

            Console.log(
                    "JitSubstrateIT.facts.id",
                    this.shown(fact.getId()));
            Console.log(
                    "JitSubstrateIT.facts.mode",
                    this.shown(fact.getMode()));
            Console.log(
                    "JitSubstrateIT.facts.text",
                    this.shown(fact.getText()));
        }
    }

    private void logRelationships(List<Relationship> relationships) {
        if (relationships == null) {
            Console.log("JitSubstrateIT.relationships", "null");
            return;
        }

        Console.log(
                "JitSubstrateIT.relationships.count",
                String.valueOf(relationships.size()));

        for (Relationship relationship : relationships) {
            if (relationship == null) {
                Console.log("JitSubstrateIT.relationships.relationship", "null");
                continue;
            }

            Object edgeObject = relationship.getEdge();

            if (edgeObject == null) {
                Console.log("JitSubstrateIT.relationships.edge", "null");
                continue;
            }

            Edge edge = (Edge) edgeObject;
            Console.log(
                    "JitSubstrateIT.relationships.edge.id",
                    this.shown(edge.getId()));
            Console.log(
                    "JitSubstrateIT.relationships.edge.from",
                    this.shown(edge.getFromFactId()));
            Console.log(
                    "JitSubstrateIT.relationships.edge.to",
                    this.shown(edge.getToFactId()));
            Console.log(
                    "JitSubstrateIT.relationships.edge.mode",
                    this.shown(edge.getMode()));
            Console.log(
                    "JitSubstrateIT.relationships.edge.text",
                    this.shown(edge.getText()));
        }
    }

    private void logPublication(GraphView graphView) {
        if (graphView == null) {
            Console.log("JitSubstrateIT.publication.result", "null");
            Console.log("JitSubstrateIT.publication.type", "null");
            return;
        }

        Console.log("JitSubstrateIT.publication.result", "returned");
        Console.log(
                "JitSubstrateIT.publication.type",
                graphView.getClass().getName());

        if (graphView instanceof GraphSnapshot) {
            this.logGraph((GraphSnapshot) graphView);
        } else {
            Console.log(
                    "JitSubstrateIT.graph.snapshot",
                    "not a GraphSnapshot");
        }
    }

    private void logGraph(GraphSnapshot graphSnapshot) {
        Map<String, Fact> nodes = graphSnapshot.nodes();

        if (nodes == null) {
            Console.log("JitSubstrateIT.graph.nodes", "null");
        } else {
            Console.log(
                    "JitSubstrateIT.graph.nodeCount",
                    String.valueOf(nodes.size()));

            for (String nodeId : nodes.keySet()) {
                Console.log("JitSubstrateIT.graph.node.key", nodeId);
                Fact fact = nodes.get(nodeId);

                if (fact == null) {
                    Console.log("JitSubstrateIT.graph.node", "null");
                } else {
                    Console.log(
                            "JitSubstrateIT.graph.node.id",
                            this.shown(fact.getId()));
                    Console.log(
                            "JitSubstrateIT.graph.node.mode",
                            this.shown(fact.getMode()));
                    Console.log(
                            "JitSubstrateIT.graph.node.text",
                            this.shown(fact.getText()));
                }
            }
        }

        Map<String, Edge> edges = graphSnapshot.edges();

        if (edges == null) {
            Console.log("JitSubstrateIT.graph.edges", "null");
        } else {
            Console.log(
                    "JitSubstrateIT.graph.edgeCount",
                    String.valueOf(edges.size()));

            for (String edgeId : edges.keySet()) {
                Console.log("JitSubstrateIT.graph.edge.key", edgeId);
                Edge edge = edges.get(edgeId);

                if (edge == null) {
                    Console.log("JitSubstrateIT.graph.edge", "null");
                } else {
                    Console.log(
                            "JitSubstrateIT.graph.edge.id",
                            this.shown(edge.getId()));
                    Console.log(
                            "JitSubstrateIT.graph.edge.from",
                            this.shown(edge.getFromFactId()));
                    Console.log(
                            "JitSubstrateIT.graph.edge.to",
                            this.shown(edge.getToFactId()));
                    Console.log(
                            "JitSubstrateIT.graph.edge.mode",
                            this.shown(edge.getMode()));
                    Console.log(
                            "JitSubstrateIT.graph.edge.text",
                            this.shown(edge.getText()));
                }
            }
        }
    }

    private String payload(
            Product product,
            Inventory inventory,
            Sales sales,
            Vendor vendor,
            Location location) {

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("product", gson.toJsonTree(product));
        root.add("inventory", gson.toJsonTree(inventory));
        root.add("sales", gson.toJsonTree(sales));
        root.add("vendor", gson.toJsonTree(vendor));
        root.add("location", gson.toJsonTree(location));
        return root.toString();
    }

    private Product product() {
        Product product = new Product();
        product.setProductId("PROD-SHAMPOO-250");
        product.setSku("SHAMPOO-250");
        product.setName("Shampoo 250 mL");
        product.setUnitOfMeasure("bottle");
        return product;
    }

    private Location location() {
        Location location = new Location();
        location.setLocationId("LOC-001");
        location.setName("Primary Inventory Location");
        location.setLocationType("storage");
        return location;
    }

    private Vendor vendor(Product product) {
        Vendor vendor = new Vendor();
        vendor.setVendorId("VENDOR-001");
        vendor.setName("Shampoo Supplier");
        vendor.setLeadTimeDays(7);
        vendor.setProduct(product);
        return vendor;
    }

    private Sales sales(Product product) {
        Sales sales = new Sales();
        sales.setSaleId("SALE-001");
        sales.setQuantitySold(25);
        sales.setSoldAt(SOLD_AT);
        sales.setProduct(product);
        return sales;
    }

    private Inventory inventory(Product product, Location location) {
        Inventory inventory = new Inventory();
        inventory.setInventoryId("INV-001");
        inventory.setQuantityOnHand(500);
        inventory.setQuantityReserved(0);
        inventory.setProduct(product);
        inventory.setLocation(location);
        return inventory;
    }

    private String shown(String value) {
        if (value == null) {
            return "null";
        }

        return value;
    }
}
