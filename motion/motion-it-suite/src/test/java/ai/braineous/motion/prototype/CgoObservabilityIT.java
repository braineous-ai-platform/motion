package ai.braineous.motion.prototype;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@QuarkusTest
public class CgoObservabilityIT {

    private static final String BASE_URL = "http://localhost:8088";

    private static final String AXIS_SQL =
            "SELECT 'ILLUSTRATIVE' AS evidence_source, "
                    + "'NOT_A_MOTION_RECORD' AS record_kind, "
                    + "'CGO_NOW_ILLUSTRATIVE' AS now_state_label, "
                    + "'Inventory:INV-ILLUSTRATIVE-001' AS fact_id, "
                    + "'Inventory' AS kind, "
                    + "'quantityOnHand' AS attribute_name, "
                    + "'500' AS attribute_value "
                    + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'CGO_NOW_ILLUSTRATIVE', "
                    + "'Inventory:INV-ILLUSTRATIVE-001', 'Inventory', 'quantityReserved', '0' "
                    + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'CGO_NOW_ILLUSTRATIVE', "
                    + "'Product:PROD-ILLUSTRATIVE-250', 'Product', 'sku', 'SHAMPOO-250' "
                    + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'CGO_NOW_ILLUSTRATIVE', "
                    + "'Product:PROD-ILLUSTRATIVE-250', 'Product', 'name', 'Shampoo 250 mL' "
                    + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'CGO_NOW_ILLUSTRATIVE', "
                    + "'Sales:SALE-ILLUSTRATIVE-001', 'Sales', 'quantitySold', '25' "
                    + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'CGO_NOW_ILLUSTRATIVE', "
                    + "'Sales:SALE-ILLUSTRATIVE-001', 'Sales', 'product', 'Product:PROD-ILLUSTRATIVE-250'";

    private static final String EDGE_SQL =
            "SELECT 'ILLUSTRATIVE' AS evidence_source, "
                    + "'NOT_A_MOTION_RECORD' AS record_kind, "
                    + "'CGO_NOW_ILLUSTRATIVE' AS now_state_label, "
                    + "'Sales:SALE-ILLUSTRATIVE-001' AS source_fact_id, "
                    + "'Product:PROD-ILLUSTRATIVE-250' AS target_fact_id, "
                    + "'product' AS relationship_kind";

    private static final String EXPANDED_GRAPH_SQL =
            "SELECT 'SYNTHETIC_PROTOTYPE' AS evidence_source, "
                    + "'NOT_A_MOTION_RECORD' AS record_kind, "
                    + "'SYNTHETIC_PROTOTYPE_DATA' AS synthetic_flag, "
                    + "'Sales' AS source_kind, 'Sales:SALE-ILLUSTRATIVE-001' AS source_fact_id, "
                    + "'Product' AS target_kind, 'Product:PROD-ILLUSTRATIVE-250' AS target_fact_id, "
                    + "'product' AS relationship_kind "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Sales', 'Sales:SYN-SALE-002', 'Product', 'Product:SYN-PROD-A', 'product' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Sales', 'Sales:SYN-SALE-003', 'Product', 'Product:SYN-PROD-B', 'product' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory', 'Inventory:SYN-INV-A', 'Product', 'Product:SYN-PROD-A', 'stocks' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory', 'Inventory:SYN-INV-B', 'Product', 'Product:SYN-PROD-B', 'stocks' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory', 'Inventory:SYN-INV-C', 'Product', 'Product:SYN-PROD-C', 'stocks' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory', 'Inventory:SYN-INV-A', 'Warehouse', 'Warehouse:SYN-WH-EAST', 'stored_at' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory', 'Inventory:SYN-INV-B', 'Warehouse', 'Warehouse:SYN-WH-WEST', 'stored_at' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory', 'Inventory:SYN-INV-C', 'Warehouse', 'Warehouse:SYN-WH-EAST', 'stored_at' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Supplier', 'Supplier:SYN-SUP-1', 'Product', 'Product:SYN-PROD-A', 'supplies' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Supplier', 'Supplier:SYN-SUP-2', 'Product', 'Product:SYN-PROD-B', 'supplies' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Supplier', 'Supplier:SYN-SUP-3', 'Product', 'Product:SYN-PROD-C', 'supplies' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Supplier', 'Supplier:SYN-SUP-1', 'Warehouse', 'Warehouse:SYN-WH-EAST', 'delivers_to' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Supplier', 'Supplier:SYN-SUP-2', 'Warehouse', 'Warehouse:SYN-WH-WEST', 'delivers_to' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Supplier', 'Supplier:SYN-SUP-3', 'Warehouse', 'Warehouse:SYN-WH-EAST', 'delivers_to' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Sales', 'Sales:SYN-SALE-002', 'Warehouse', 'Warehouse:SYN-WH-EAST', 'fulfilled_from' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Sales', 'Sales:SYN-SALE-003', 'Warehouse', 'Warehouse:SYN-WH-WEST', 'fulfilled_from' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Sales', 'Sales:SALE-ILLUSTRATIVE-001', 'Warehouse', 'Warehouse:SYN-WH-EAST', 'fulfilled_from'";

    private static final String HISTORICAL_T1_RANGE =
            "2026-01-15T00:00:00 : 2026-01-16T00:00:00";

    private static final String HISTORICAL_T2_RANGE =
            "2026-02-15T00:00:00 : 2026-02-16T00:00:00";

    private static final String HISTORICAL_MARCH_RANGE =
            "2026-03-01T00:00:00 : 2026-03-02T00:00:00";

    private static final String HISTORICAL_GRAPH_SQL =
            "SELECT 'SYNTHETIC_PROTOTYPE' AS evidence_source, "
                    + "'NOT_A_MOTION_RECORD' AS record_kind, "
                    + "'SYNTHETIC_PROTOTYPE_DATA' AS synthetic_flag, "
                    + "TIMESTAMP '2026-01-15 12:00:00' AS observed_at, "
                    + "'T1' AS historical_label, "
                    + "'Sales' AS source_kind, 'Sales:SALE-ILLUSTRATIVE-001' AS source_fact_id, "
                    + "'Product' AS target_kind, 'Product:PROD-ILLUSTRATIVE-250' AS target_fact_id, "
                    + "'product' AS relationship_kind "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "TIMESTAMP '2026-01-15 12:00:00', 'T1', "
                    + "'Inventory', 'Inventory:INV-ILLUSTRATIVE-001', 'Product', 'Product:PROD-ILLUSTRATIVE-250', 'stocks' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "TIMESTAMP '2026-02-15 12:00:00', 'T2', "
                    + "'Sales', 'Sales:SALE-ILLUSTRATIVE-001', 'Product', 'Product:PROD-ILLUSTRATIVE-250', 'product' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "TIMESTAMP '2026-02-15 12:00:00', 'T2', "
                    + "'Inventory', 'Inventory:INV-ILLUSTRATIVE-001', 'Product', 'Product:PROD-ILLUSTRATIVE-250', 'stocks' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "TIMESTAMP '2026-02-15 12:00:00', 'T2', "
                    + "'Sales', 'Sales:SALE-ILLUSTRATIVE-001', 'Warehouse', 'Warehouse:WH-ILLUSTRATIVE-EAST', 'fulfilled_from'";

    private static final String HISTORICAL_CHANGE_SQL =
            "WITH states AS ("
                    + HISTORICAL_GRAPH_SQL
                    + ") "
                    + "SELECT t2.evidence_source, t2.record_kind, t2.synthetic_flag, "
                    + "t2.source_kind, t2.source_fact_id, t2.target_kind, t2.target_fact_id, "
                    + "t2.relationship_kind, t2.observed_at, "
                    + "'ADDED_IN_T2' AS observed_change "
                    + "FROM states t2 "
                    + "WHERE t2.historical_label = 'T2' "
                    + "AND NOT EXISTS ("
                    + "SELECT 1 FROM states t1 "
                    + "WHERE t1.historical_label = 'T1' "
                    + "AND t1.source_fact_id = t2.source_fact_id "
                    + "AND t1.target_fact_id = t2.target_fact_id "
                    + "AND t1.relationship_kind = t2.relationship_kind"
                    + ") "
                    + "UNION ALL "
                    + "SELECT t1.evidence_source, t1.record_kind, t1.synthetic_flag, "
                    + "t1.source_kind, t1.source_fact_id, t1.target_kind, t1.target_fact_id, "
                    + "t1.relationship_kind, t1.observed_at, "
                    + "'REMOVED_IN_T2' AS observed_change "
                    + "FROM states t1 "
                    + "WHERE t1.historical_label = 'T1' "
                    + "AND NOT EXISTS ("
                    + "SELECT 1 FROM states t2 "
                    + "WHERE t2.historical_label = 'T2' "
                    + "AND t2.source_fact_id = t1.source_fact_id "
                    + "AND t2.target_fact_id = t1.target_fact_id "
                    + "AND t2.relationship_kind = t1.relationship_kind"
                    + ")";

    private static final String TREND_FULL_RANGE =
            "2026-01-01T00:00:00 : 2026-04-01T00:00:00";

    private static final String TREND_JAN_RANGE =
            "2026-01-01T00:00:00 : 2026-02-01T00:00:00";

    private static final String TREND_MAR_RANGE =
            "2026-03-01T00:00:00 : 2026-04-01T00:00:00";

    private static final String INVENTORY_TREND_SQL =
            "SELECT 'SYNTHETIC_PROTOTYPE' AS evidence_source, "
                    + "'NOT_A_MOTION_RECORD' AS record_kind, "
                    + "'SYNTHETIC_PROTOTYPE_DATA' AS synthetic_flag, "
                    + "'Inventory:INV-ILLUSTRATIVE-001' AS fact_id, "
                    + "'Inventory' AS kind, "
                    + "'quantityOnHand' AS attribute_name, "
                    + "CAST(500 AS DOUBLE PRECISION) AS attribute_value, "
                    + "TIMESTAMP '2026-01-15 12:00:00' AS observed_at "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory:INV-ILLUSTRATIVE-001', 'Inventory', 'quantityOnHand', "
                    + "CAST(420 AS DOUBLE PRECISION), TIMESTAMP '2026-01-31 12:00:00' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory:INV-ILLUSTRATIVE-001', 'Inventory', 'quantityOnHand', "
                    + "CAST(350 AS DOUBLE PRECISION), TIMESTAMP '2026-02-15 12:00:00' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory:INV-ILLUSTRATIVE-001', 'Inventory', 'quantityOnHand', "
                    + "CAST(310 AS DOUBLE PRECISION), TIMESTAMP '2026-02-28 12:00:00' "
                    + "UNION ALL SELECT 'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                    + "'Inventory:INV-ILLUSTRATIVE-001', 'Inventory', 'quantityOnHand', "
                    + "CAST(275 AS DOUBLE PRECISION), TIMESTAMP '2026-03-15 12:00:00'";

    private String accessToken;

    private String csrfToken;

    private String sessionCookie;

    @Test
    public void test_1() {
        Console.log("CgoObservabilityIT.base_url", BASE_URL);
        Console.log(
                "CgoObservabilityIT.disclaimer",
                "ILLUSTRATIVE Axis facts only; record_kind=NOT_A_MOTION_RECORD; not CGO NOW runtime state");
        Console.log("CgoObservabilityIT.axis.sql", AXIS_SQL);
        Console.log(
                "CgoObservabilityIT.edge.sql.provenance",
                "source/target taken from AXIS_SQL Sales product attribute pointing at Product fact_id");
        Console.log("CgoObservabilityIT.edge.sql", EDGE_SQL);
        Console.log(
                "CgoObservabilityIT.expanded.disclaimer",
                "expanded graph is SYNTHETIC_PROTOTYPE_DATA; not CGO relationships; keeps original Sales product edge");
        Console.log("CgoObservabilityIT.expanded.sql", EXPANDED_GRAPH_SQL);

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }

        if (!this.preserveFactExplorerResources()) {
            return;
        }
        if (!this.preserveExistingGraphResources()) {
            return;
        }
        if (!this.inspectGraphChartSupport()) {
            return;
        }

        Integer databaseId = this.readExamplesDatabaseId();
        if (databaseId == null) {
            Console.log(
                    "CgoObservabilityIT.blocker",
                    "no examples database id from GET /api/v1/database/");
            return;
        }

        Integer datasetId = this.createExpandedGraphDataset(databaseId.intValue());
        if (datasetId == null) {
            return;
        }
        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        if (!this.postGraphChartData(datasetId.intValue())) {
            return;
        }

        Integer dashboardId = this.createExpandedGraphDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer circularChartId = this.createGraphChartWithLayout(
                datasetId.intValue(),
                dashboardId.intValue(),
                "circular",
                "circular");
        if (circularChartId == null) {
            return;
        }
        Integer forceChartId = this.createGraphChartWithLayout(
                datasetId.intValue(),
                dashboardId.intValue(),
                "force",
                "force");
        if (forceChartId == null) {
            return;
        }
        this.placeExpandedGraphChartsOnDashboard(
                dashboardId.intValue(),
                circularChartId.intValue(),
                forceChartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(circularChartId.intValue());
        this.readChart(forceChartId.intValue());
        this.logExistingResource("preserve.dataset_31.related", "/api/v1/dataset/31/related_objects");
        this.logExistingResource("preserve.dataset_32.related", "/api/v1/dataset/32/related_objects");
        this.logExistingResource("preserve.dashboard_17", "/api/v1/dashboard/17");
        this.logExistingResource("preserve.dashboard_18", "/api/v1/dashboard/18");
    }

    @Test
    public void test_2() {
        Console.log("CgoObservabilityIT.test_2.base_url", BASE_URL);
        Console.log(
                "CgoObservabilityIT.test_2.goal",
                "evaluate native graph_chart hover/select and REST relationship filtering on existing dataset 33");
        Console.log(
                "CgoObservabilityIT.test_2.disclaimer",
                "uses existing SYNTHETIC_PROTOTYPE_DATA on dataset 33; not CGO relationships; no new edges");
        Console.log(
                "CgoObservabilityIT.test_2.existing",
                "dataset 33; charts 129 circular, 130 force; dashboard 19");

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }
        if (!this.preserveFactExplorerResources()) {
            return;
        }
        if (!this.preserveExistingGraphResources()) {
            return;
        }
        if (!this.preserveExpandedGraphResources()) {
            return;
        }

        this.inspectChartRelationshipCapabilities(129, "circular");
        this.inspectChartRelationshipCapabilities(130, "force");
        JsonObject dataset = this.readDataset(33);
        if (dataset == null) {
            return;
        }
        this.logDatasetColumns(dataset);

        this.postRelationshipDetailRows(33);
        this.postRelationshipKindFilter(33, "product");
        this.postGraphQueryWithRelationshipFilter(33, "product");
        this.postGraphQueryWithRelationshipFilter(33, "fulfilled_from");

        Console.log(
                "CgoObservabilityIT.test_2.observation.hover_api",
                "OpenAPI has no endpoint that simulates hovering or selecting an edge on dashboard 19");
        Console.log(
                "CgoObservabilityIT.test_2.observation.edge_tooltip",
                "installed graph_chart tooltip formatter is source > target plus metric count; relationship_kind is not in tooltip");
        Console.log(
                "CgoObservabilityIT.test_2.observation.edge_click",
                "left-click cross-filter looks up series data by e.data.id (nodes); edge click does not emit relationship_kind");
        Console.log(
                "CgoObservabilityIT.test_2.observation.edge_contextmenu",
                "right-click drillToDetail on an edge filters formData.source and formData.target only; no relationship identity");
        Console.log(
                "CgoObservabilityIT.test_2.observation.query_fields",
                "graph_chart buildQuery columns are source, target, source_category, target_category; relationship_kind is not a viz field");
        Console.log(
                "CgoObservabilityIT.test_2.observation.filter_api",
                "POST /api/v1/chart/data queries[].filters can restrict dataset 33 by relationship_kind without altering chart 129/130");

        Integer dashboardId = this.createRelationshipDetailDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer chartId = this.createRelationshipDetailTableChart(33, dashboardId.intValue());
        if (chartId == null) {
            return;
        }
        this.placeRelationshipDetailChartOnDashboard(dashboardId.intValue(), chartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(chartId.intValue());
        this.logExistingResource("test_2.preserve.dataset_33", "/api/v1/dataset/33");
        this.logExistingResource("test_2.preserve.chart_129", "/api/v1/chart/129");
        this.logExistingResource("test_2.preserve.chart_130", "/api/v1/chart/130");
        this.logExistingResource("test_2.preserve.dashboard_19", "/api/v1/dashboard/19");
        this.logExistingResource("test_2.preserve.dashboard_17", "/api/v1/dashboard/17");
        this.logExistingResource("test_2.preserve.dashboard_18", "/api/v1/dashboard/18");
    }

    @Test
    public void test_3() {
        Console.log("CgoObservabilityIT.test_3.base_url", BASE_URL);
        Console.log(
                "CgoObservabilityIT.test_3.goal",
                "evaluate native temporal filtering of illustrative historical CGO graph states");
        Console.log(
                "CgoObservabilityIT.test_3.disclaimer",
                "SYNTHETIC_PROTOTYPE_DATA; NOT_A_MOTION_RECORD; not CGO History runtime");
        Console.log("CgoObservabilityIT.test_3.sql", HISTORICAL_GRAPH_SQL);
        Console.log("CgoObservabilityIT.test_3.t1_range", HISTORICAL_T1_RANGE);
        Console.log("CgoObservabilityIT.test_3.t2_range", HISTORICAL_T2_RANGE);
        Console.log("CgoObservabilityIT.test_3.march_range", HISTORICAL_MARCH_RANGE);

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }
        if (!this.preserveFactExplorerResources()) {
            return;
        }
        if (!this.preserveExistingGraphResources()) {
            return;
        }
        if (!this.preserveExpandedGraphResources()) {
            return;
        }
        this.logExistingResource("test_3.preserve.chart_131", "/api/v1/chart/131");
        this.logExistingResource("test_3.preserve.dashboard_20", "/api/v1/dashboard/20");
        this.inspectNativeTemporalExample();
        this.inspectNowGraphHasNoObservedAt();

        Integer databaseId = this.readExamplesDatabaseId();
        if (databaseId == null) {
            Console.log(
                    "CgoObservabilityIT.blocker",
                    "no examples database id from GET /api/v1/database/");
            return;
        }

        Integer datasetId = this.createHistoricalGraphDataset(databaseId.intValue());
        if (datasetId == null) {
            return;
        }
        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        if (!this.ensureObservedAtIsTemporal(datasetId.intValue(), dataset)) {
            return;
        }
        dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        this.logObservedAtColumn(dataset);

        if (!this.postHistoricalRows(datasetId.intValue(), "No filter", "unfiltered")) {
            return;
        }
        if (!this.postHistoricalRows(datasetId.intValue(), HISTORICAL_T1_RANGE, "t1")) {
            return;
        }
        if (!this.postHistoricalRows(datasetId.intValue(), HISTORICAL_T2_RANGE, "t2")) {
            return;
        }
        if (!this.postHistoricalRows(datasetId.intValue(), HISTORICAL_T1_RANGE, "t1_repeat")) {
            return;
        }
        if (!this.postHistoricalRows(datasetId.intValue(), HISTORICAL_MARCH_RANGE, "march_empty")) {
            return;
        }
        if (!this.postHistoricalGraphQuery(datasetId.intValue(), HISTORICAL_T1_RANGE, "t1")) {
            return;
        }
        if (!this.postHistoricalGraphQuery(datasetId.intValue(), HISTORICAL_T2_RANGE, "t2")) {
            return;
        }

        Console.log(
                "CgoObservabilityIT.test_3.observation.relative_vs_absolute",
                "dashboard charts pin absolute T1 range; relative Last week would move as wall-clock advances");
        Console.log(
                "CgoObservabilityIT.test_3.observation.fixed_view",
                "repeating T1 TEMPORAL_RANGE does not include T2 fulfilled_from; March range has no rows");

        Integer dashboardId = this.createHistoricalDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer graphChartId = this.createHistoricalGraphChart(
                datasetId.intValue(),
                dashboardId.intValue());
        if (graphChartId == null) {
            return;
        }
        Integer tableChartId = this.createHistoricalTableChart(
                datasetId.intValue(),
                dashboardId.intValue());
        if (tableChartId == null) {
            return;
        }
        this.placeHistoricalChartsOnDashboard(
                dashboardId.intValue(),
                graphChartId.intValue(),
                tableChartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(graphChartId.intValue());
        this.readChart(tableChartId.intValue());
        this.logExistingResource("test_3.preserve.dataset_33", "/api/v1/dataset/33");
        this.logExistingResource("test_3.preserve.dashboard_19", "/api/v1/dashboard/19");
        this.logExistingResource("test_3.preserve.dashboard_20", "/api/v1/dashboard/20");
        this.logExistingResource("test_3.preserve.dashboard_17", "/api/v1/dashboard/17");
        this.logExistingResource("test_3.preserve.dashboard_18", "/api/v1/dashboard/18");
    }

    @Test
    public void test_4() {
        Console.log("CgoObservabilityIT.test_4.base_url", BASE_URL);
        Console.log(
                "CgoObservabilityIT.test_4.goal",
                "evaluate native Change Explorer of illustrative T1 vs T2 Axis graph states on dataset 34");
        Console.log(
                "CgoObservabilityIT.test_4.disclaimer",
                "SYNTHETIC_PROTOTYPE_DATA; NOT_A_MOTION_RECORD; not CGO History; no fabricated removed or modified edges");
        Console.log("CgoObservabilityIT.test_4.t1_range", HISTORICAL_T1_RANGE);
        Console.log("CgoObservabilityIT.test_4.t2_range", HISTORICAL_T2_RANGE);
        Console.log("CgoObservabilityIT.test_4.change.sql", HISTORICAL_CHANGE_SQL);

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }
        if (!this.preserveFactExplorerResources()) {
            return;
        }
        if (!this.preserveExistingGraphResources()) {
            return;
        }
        if (!this.preserveExpandedGraphResources()) {
            return;
        }
        if (!this.preserveHistoricalGraphResources()) {
            return;
        }
        this.logExistingResource("test_4.preserve.chart_131", "/api/v1/chart/131");
        this.logExistingResource("test_4.preserve.dashboard_20", "/api/v1/dashboard/20");

        this.inspectNativeComparisonCapabilities();

        JsonObject dataset34 = this.readDataset(34);
        if (dataset34 == null) {
            return;
        }
        if (!this.postHistoricalRows(34, HISTORICAL_T1_RANGE, "test_4_t1")) {
            return;
        }
        if (!this.postHistoricalRows(34, HISTORICAL_T2_RANGE, "test_4_t2")) {
            return;
        }
        if (!this.postHistoricalGraphQuery(34, HISTORICAL_T1_RANGE, "test_4_t1")) {
            return;
        }
        if (!this.postHistoricalGraphQuery(34, HISTORICAL_T2_RANGE, "test_4_t2")) {
            return;
        }

        Console.log(
                "CgoObservabilityIT.test_4.observation.native_compare",
                "graph_chart has no compare/diff control; timeseries time comparison is metric-over-time not edge identity");
        Console.log(
                "CgoObservabilityIT.test_4.observation.sql_diff",
                "independent virtual SQL EXCEPT-style query over the same illustrative states as dataset 34");

        Integer databaseId = this.readExamplesDatabaseId();
        if (databaseId == null) {
            Console.log(
                    "CgoObservabilityIT.blocker",
                    "no examples database id from GET /api/v1/database/");
            return;
        }

        Integer changeDatasetId = this.createChangeExplorerDataset(databaseId.intValue());
        if (changeDatasetId == null) {
            return;
        }
        this.refreshDataset(changeDatasetId.intValue());
        JsonObject changeDataset = this.readDataset(changeDatasetId.intValue());
        if (changeDataset == null) {
            return;
        }
        if (!this.postChangeExplorerRows(changeDatasetId.intValue())) {
            return;
        }

        Integer dashboardId = this.createChangeExplorerDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer tableChartId = this.createChangeExplorerTableChart(
                changeDatasetId.intValue(),
                dashboardId.intValue());
        if (tableChartId == null) {
            return;
        }
        Integer graphChartId = this.createChangeExplorerGraphChart(
                changeDatasetId.intValue(),
                dashboardId.intValue());
        if (graphChartId == null) {
            return;
        }
        this.placeChangeExplorerChartsOnDashboard(
                dashboardId.intValue(),
                tableChartId.intValue(),
                graphChartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(tableChartId.intValue());
        this.readChart(graphChartId.intValue());
        this.logExistingResource("test_4.preserve.dataset_34", "/api/v1/dataset/34");
        this.logExistingResource("test_4.preserve.chart_132", "/api/v1/chart/132");
        this.logExistingResource("test_4.preserve.chart_133", "/api/v1/chart/133");
        this.logExistingResource("test_4.preserve.dashboard_21", "/api/v1/dashboard/21");
        this.logExistingResource("test_4.preserve.dashboard_19", "/api/v1/dashboard/19");
        this.logExistingResource("test_4.preserve.dashboard_17", "/api/v1/dashboard/17");
        this.logExistingResource("test_4.preserve.dashboard_18", "/api/v1/dashboard/18");
    }

    @Test
    public void test_5() {
        Console.log("CgoObservabilityIT.test_5.base_url", BASE_URL);
        Console.log(
                "CgoObservabilityIT.test_5.goal",
                "evaluate native timeseries of illustrative Inventory quantityOnHand values");
        Console.log(
                "CgoObservabilityIT.test_5.disclaimer",
                "SYNTHETIC_PROTOTYPE_DATA; NOT_A_MOTION_RECORD; not CGO History runtime");
        Console.log("CgoObservabilityIT.test_5.sql", INVENTORY_TREND_SQL);
        Console.log("CgoObservabilityIT.test_5.full_range", TREND_FULL_RANGE);
        Console.log("CgoObservabilityIT.test_5.jan_range", TREND_JAN_RANGE);
        Console.log("CgoObservabilityIT.test_5.mar_range", TREND_MAR_RANGE);

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }
        if (!this.preserveFactExplorerResources()) {
            return;
        }
        if (!this.preserveExistingGraphResources()) {
            return;
        }
        if (!this.preserveExpandedGraphResources()) {
            return;
        }
        if (!this.preserveHistoricalGraphResources()) {
            return;
        }
        if (!this.preserveChangeExplorerResources()) {
            return;
        }
        this.inspectNativeTimeseriesContract();

        Integer databaseId = this.readExamplesDatabaseId();
        if (databaseId == null) {
            Console.log(
                    "CgoObservabilityIT.blocker",
                    "no examples database id from GET /api/v1/database/");
            return;
        }

        Integer datasetId = this.createInventoryTrendDataset(databaseId.intValue());
        if (datasetId == null) {
            return;
        }
        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        if (!this.ensureObservedAtIsTemporal(datasetId.intValue(), dataset)) {
            return;
        }
        dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        if (!this.ensureSumAttributeValueMetric(datasetId.intValue(), dataset)) {
            return;
        }
        dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        this.logTrendDatasetColumns(dataset);

        if (!this.postTrendRows(datasetId.intValue(), "No filter", "unfiltered")) {
            return;
        }
        if (!this.postTrendChartData(datasetId.intValue(), "No filter", "unfiltered")) {
            return;
        }
        if (!this.postTrendChartData(datasetId.intValue(), TREND_JAN_RANGE, "january")) {
            return;
        }
        if (!this.postTrendChartData(datasetId.intValue(), TREND_MAR_RANGE, "march")) {
            return;
        }
        if (!this.postTrendChartData(datasetId.intValue(), TREND_FULL_RANGE, "full")) {
            return;
        }

        Console.log(
                "CgoObservabilityIT.test_5.observation.viz",
                "echarts_timeseries_line with SUM(attribute_value) over observed_at; not CGO History");
        Console.log(
                "CgoObservabilityIT.test_5.observation.filter",
                "TEMPORAL_RANGE on observed_at via POST /api/v1/chart/data; dashboard pins absolute full range");

        Integer dashboardId = this.createInventoryTrendDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer chartId = this.createInventoryTrendChart(
                datasetId.intValue(),
                dashboardId.intValue());
        if (chartId == null) {
            return;
        }
        this.placeInventoryTrendChartOnDashboard(dashboardId.intValue(), chartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(chartId.intValue());
        this.logExistingResource("test_5.preserve.dataset_34", "/api/v1/dataset/34");
        this.logExistingResource("test_5.preserve.dataset_35", "/api/v1/dataset/35");
        this.logExistingResource("test_5.preserve.dashboard_21", "/api/v1/dashboard/21");
        this.logExistingResource("test_5.preserve.dashboard_22", "/api/v1/dashboard/22");
        this.logExistingResource("test_5.preserve.dashboard_19", "/api/v1/dashboard/19");
        this.logExistingResource("test_5.preserve.dashboard_17", "/api/v1/dashboard/17");
        this.logExistingResource("test_5.preserve.dashboard_18", "/api/v1/dashboard/18");
    }

    @Test
    public void test_6() {
        Console.log("CgoObservabilityIT.test_6.base_url", BASE_URL);
        Console.log(
                "CgoObservabilityIT.test_6.goal",
                "evaluate native Fact-kind filtering on existing illustrative Axis/graph datasets");
        Console.log(
                "CgoObservabilityIT.test_6.disclaimer",
                "uses datasets 31 and 33; SYNTHETIC/ILLUSTRATIVE only; no invented domain membership");

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }
        if (!this.preserveFactExplorerResources()) {
            return;
        }
        if (!this.preserveExistingGraphResources()) {
            return;
        }
        if (!this.preserveExpandedGraphResources()) {
            return;
        }
        if (!this.preserveHistoricalGraphResources()) {
            return;
        }
        if (!this.preserveChangeExplorerResources()) {
            return;
        }
        if (!this.preserveInventoryTrendResources()) {
            return;
        }

        this.inspectDatasetFilterFields(31, "axis_facts");
        this.inspectDatasetFilterFields(33, "expanded_graph");
        this.inspectDatasetFilterFields(36, "inventory_trend");
        this.logExistingResource("test_6.filter.dashboard_17", "/api/v1/dashboard/17");
        this.logExistingResource("test_6.filter.dashboard_19", "/api/v1/dashboard/19");

        if (!this.postColumnEqualsFilter(
                31,
                "kind",
                "Inventory",
                this.axisFactColumns(),
                "axis.kind.Inventory")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                31,
                "kind",
                "Product",
                this.axisFactColumns(),
                "axis.kind.Product")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                31,
                "kind",
                "Sales",
                this.axisFactColumns(),
                "axis.kind.Sales")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                31,
                "kind",
                "Warehouse",
                this.axisFactColumns(),
                "axis.kind.Warehouse")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                31,
                "kind",
                "Supplier",
                this.axisFactColumns(),
                "axis.kind.Supplier")) {
            return;
        }

        if (!this.postColumnEqualsFilter(
                33,
                "source_kind",
                "Inventory",
                this.graphEdgeColumns(),
                "graph.source_kind.Inventory")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                33,
                "source_kind",
                "Product",
                this.graphEdgeColumns(),
                "graph.source_kind.Product")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                33,
                "source_kind",
                "Sales",
                this.graphEdgeColumns(),
                "graph.source_kind.Sales")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                33,
                "source_kind",
                "Warehouse",
                this.graphEdgeColumns(),
                "graph.source_kind.Warehouse")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                33,
                "source_kind",
                "Supplier",
                this.graphEdgeColumns(),
                "graph.source_kind.Supplier")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                33,
                "target_kind",
                "Product",
                this.graphEdgeColumns(),
                "graph.target_kind.Product")) {
            return;
        }
        if (!this.postColumnEqualsFilter(
                33,
                "target_kind",
                "Warehouse",
                this.graphEdgeColumns(),
                "graph.target_kind.Warehouse")) {
            return;
        }

        Console.log(
                "CgoObservabilityIT.test_6.observation.domain",
                "no domain column on datasets 31/33/36; domain filter not posted");
        Console.log(
                "CgoObservabilityIT.test_6.observation.kind",
                "dataset 31 kind covers Inventory/Product/Sales; Warehouse/Supplier appear as source_kind/target_kind on dataset 33");

        Integer dashboardId = this.createKindFilterDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer axisChartId = this.createAxisKindTableChart(31, dashboardId.intValue());
        if (axisChartId == null) {
            return;
        }
        Integer graphChartId = this.createGraphKindTableChart(33, dashboardId.intValue());
        if (graphChartId == null) {
            return;
        }
        this.placeKindFilterChartsOnDashboard(
                dashboardId.intValue(),
                axisChartId.intValue(),
                graphChartId.intValue());
        this.putKindNativeFilters(
                dashboardId.intValue(),
                axisChartId.intValue(),
                graphChartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(axisChartId.intValue());
        this.readChart(graphChartId.intValue());
        this.logExistingResource("test_6.preserve.dataset_31", "/api/v1/dataset/31");
        this.logExistingResource("test_6.preserve.dataset_33", "/api/v1/dataset/33");
        this.logExistingResource("test_6.preserve.dataset_36", "/api/v1/dataset/36");
        this.logExistingResource("test_6.preserve.dashboard_17", "/api/v1/dashboard/17");
        this.logExistingResource("test_6.preserve.dashboard_19", "/api/v1/dashboard/19");
        this.logExistingResource("test_6.preserve.dashboard_23", "/api/v1/dashboard/23");
    }

    @Test
    public void test_7() {
        Console.log("CgoObservabilityIT.test_7.base_url", BASE_URL);
        Console.log(
                "CgoObservabilityIT.test_7.goal",
                "evaluate native Superset dynamic NOW refresh without inventing ingestion or mutating metadata");
        Console.log(
                "CgoObservabilityIT.test_7.disclaimer",
                "SYNTHETIC_PROTOTYPE_DATA / NOT_A_MOTION_RECORD only; not CGO NOW runtime");

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }
        if (!this.preserveFactExplorerResources()) {
            return;
        }
        if (!this.preserveExistingGraphResources()) {
            return;
        }
        if (!this.preserveExpandedGraphResources()) {
            return;
        }
        if (!this.preserveHistoricalGraphResources()) {
            return;
        }
        if (!this.preserveChangeExplorerResources()) {
            return;
        }
        if (!this.preserveInventoryTrendResources()) {
            return;
        }
        if (!this.preserveKindFilterResources()) {
            return;
        }

        this.inspectRefreshAndCacheContracts();
        this.inspectVirtualNowDatasetSql(31);
        this.postSqlLabSelect();
        this.postSqlLabInsertProbe();

        Console.log(
                "CgoObservabilityIT.blocker",
                "mutable NOW sample data needs a writable physical table or DML; examples database allow_dml=false; changing dataset SQL would be Superset metadata; creating a table would be an unapproved schema change");
        Console.log(
                "CgoObservabilityIT.test_7.dashboard.url",
                "not created; stopped before independent NOW dataset/chart/dashboard");
        Console.log(
                "CgoObservabilityIT.test_7.before_after",
                "no fact value mutation; no before/after chart/data pair");
    }

    private boolean preserveFactExplorerResources() {
        HttpResponse dataset = this.request("GET", "/api/v1/dataset/31", null, true);
        this.logResponse("preserve.dataset_31.get", dataset);
        if (dataset.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dataset/31");
            return false;
        }
        HttpResponse chart = this.request("GET", "/api/v1/chart/127", null, true);
        this.logResponse("preserve.chart_127.get", chart);
        if (chart.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/127");
            return false;
        }
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/17", null, true);
        this.logResponse("preserve.dashboard_17.get", dashboard);
        if (dashboard.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dashboard/17");
            return false;
        }
        return true;
    }

    private boolean preserveExistingGraphResources() {
        HttpResponse dataset = this.request("GET", "/api/v1/dataset/32", null, true);
        this.logResponse("preserve.dataset_32.get", dataset);
        if (dataset.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dataset/32");
            return false;
        }
        HttpResponse chart = this.request("GET", "/api/v1/chart/128", null, true);
        this.logResponse("preserve.chart_128.get", chart);
        if (chart.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/128");
            return false;
        }
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/18", null, true);
        this.logResponse("preserve.dashboard_18.get", dashboard);
        if (dashboard.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dashboard/18");
            return false;
        }
        return true;
    }

    private boolean preserveExpandedGraphResources() {
        HttpResponse dataset = this.request("GET", "/api/v1/dataset/33", null, true);
        this.logResponse("test_2.preserve.dataset_33.get", dataset);
        if (dataset.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dataset/33");
            return false;
        }
        HttpResponse circular = this.request("GET", "/api/v1/chart/129", null, true);
        this.logResponse("test_2.preserve.chart_129.get", circular);
        if (circular.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/129");
            return false;
        }
        HttpResponse force = this.request("GET", "/api/v1/chart/130", null, true);
        this.logResponse("test_2.preserve.chart_130.get", force);
        if (force.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/130");
            return false;
        }
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/19", null, true);
        this.logResponse("test_2.preserve.dashboard_19.get", dashboard);
        if (dashboard.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dashboard/19");
            return false;
        }
        return true;
    }

    private boolean preserveHistoricalGraphResources() {
        HttpResponse dataset = this.request("GET", "/api/v1/dataset/34", null, true);
        this.logResponse("test_4.preserve.dataset_34.get", dataset);
        if (dataset.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dataset/34");
            return false;
        }
        HttpResponse graph = this.request("GET", "/api/v1/chart/132", null, true);
        this.logResponse("test_4.preserve.chart_132.get", graph);
        if (graph.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/132");
            return false;
        }
        HttpResponse table = this.request("GET", "/api/v1/chart/133", null, true);
        this.logResponse("test_4.preserve.chart_133.get", table);
        if (table.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/133");
            return false;
        }
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/21", null, true);
        this.logResponse("test_4.preserve.dashboard_21.get", dashboard);
        if (dashboard.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dashboard/21");
            return false;
        }
        return true;
    }

    private boolean preserveChangeExplorerResources() {
        HttpResponse dataset = this.request("GET", "/api/v1/dataset/35", null, true);
        this.logResponse("test_5.preserve.dataset_35.get", dataset);
        if (dataset.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dataset/35");
            return false;
        }
        HttpResponse table = this.request("GET", "/api/v1/chart/134", null, true);
        this.logResponse("test_5.preserve.chart_134.get", table);
        if (table.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/134");
            return false;
        }
        HttpResponse graph = this.request("GET", "/api/v1/chart/135", null, true);
        this.logResponse("test_5.preserve.chart_135.get", graph);
        if (graph.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/135");
            return false;
        }
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/22", null, true);
        this.logResponse("test_5.preserve.dashboard_22.get", dashboard);
        if (dashboard.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dashboard/22");
            return false;
        }
        return true;
    }

    private boolean preserveInventoryTrendResources() {
        HttpResponse dataset = this.request("GET", "/api/v1/dataset/36", null, true);
        this.logResponse("test_6.preserve.dataset_36.get", dataset);
        if (dataset.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dataset/36");
            return false;
        }
        HttpResponse chart = this.request("GET", "/api/v1/chart/136", null, true);
        this.logResponse("test_6.preserve.chart_136.get", chart);
        if (chart.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/136");
            return false;
        }
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/23", null, true);
        this.logResponse("test_6.preserve.dashboard_23.get", dashboard);
        if (dashboard.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dashboard/23");
            return false;
        }
        return true;
    }

    private boolean preserveKindFilterResources() {
        HttpResponse dataset = this.request("GET", "/api/v1/dataset/31", null, true);
        this.logResponse("test_7.preserve.dataset_31.get", dataset);
        if (dataset.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dataset/31");
            return false;
        }
        HttpResponse chart = this.request("GET", "/api/v1/chart/137", null, true);
        this.logResponse("test_7.preserve.chart_137.get", chart);
        if (chart.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/137");
            return false;
        }
        HttpResponse chart138 = this.request("GET", "/api/v1/chart/138", null, true);
        this.logResponse("test_7.preserve.chart_138.get", chart138);
        if (chart138.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/138");
            return false;
        }
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/24", null, true);
        this.logResponse("test_7.preserve.dashboard_24.get", dashboard);
        if (dashboard.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dashboard/24");
            return false;
        }
        return true;
    }

    private void inspectRefreshAndCacheContracts() {
        HttpResponse database = this.request("GET", "/api/v1/database/1", null, true);
        this.logResponse("test_7.database.1", database);
        JsonObject dbJson = this.parseObject(database.body);
        if (dbJson != null && dbJson.get("result") != null && dbJson.get("result").isJsonObject()) {
            JsonObject result = dbJson.getAsJsonObject("result");
            this.logJsonFlag(result, "allow_dml");
            this.logJsonFlag(result, "allow_ctas");
            this.logJsonFlag(result, "allow_cvas");
            this.logJsonFlag(result, "allow_file_upload");
            this.logJsonFlag(result, "allow_run_async");
            if (result.get("extra") != null && !result.get("extra").isJsonNull()) {
                Console.log("CgoObservabilityIT.test_7.database.1.extra", result.get("extra").getAsString());
            }
        }
        HttpResponse chart = this.request("GET", "/api/v1/chart/127", null, true);
        this.logResponse("test_7.chart.127.cache", chart);
        JsonObject chartJson = this.parseObject(chart.body);
        if (chartJson != null && chartJson.get("result") != null && chartJson.get("result").isJsonObject()) {
            JsonObject result = chartJson.getAsJsonObject("result");
            if (result.get("cache_timeout") == null || result.get("cache_timeout").isJsonNull()) {
                Console.log("CgoObservabilityIT.test_7.chart.127.cache_timeout", "null");
            } else {
                Console.log(
                        "CgoObservabilityIT.test_7.chart.127.cache_timeout",
                        result.get("cache_timeout").toString());
            }
            if (result.get("query_context") != null && !result.get("query_context").isJsonNull()) {
                Console.log(
                        "CgoObservabilityIT.test_7.chart.127.query_context",
                        result.get("query_context").getAsString());
            }
        }
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/17", null, true);
        this.logResponse("test_7.dashboard.17.refresh", dashboard);
        JsonObject dashJson = this.parseObject(dashboard.body);
        if (dashJson != null && dashJson.get("result") != null && dashJson.get("result").isJsonObject()) {
            JsonObject result = dashJson.getAsJsonObject("result");
            if (result.get("json_metadata") == null || result.get("json_metadata").isJsonNull()) {
                Console.log("CgoObservabilityIT.test_7.dashboard.17.json_metadata", "null");
            } else {
                Console.log(
                        "CgoObservabilityIT.test_7.dashboard.17.json_metadata",
                        result.get("json_metadata").getAsString());
            }
            if (result.get("refresh_frequency") != null && !result.get("refresh_frequency").isJsonNull()) {
                Console.log(
                        "CgoObservabilityIT.test_7.dashboard.17.refresh_frequency",
                        result.get("refresh_frequency").toString());
            } else {
                Console.log("CgoObservabilityIT.test_7.dashboard.17.refresh_frequency", "absent");
            }
        }
        HttpResponse dash24 = this.request("GET", "/api/v1/dashboard/24", null, true);
        this.logResponse("test_7.dashboard.24.refresh", dash24);
        JsonObject dash24Json = this.parseObject(dash24.body);
        if (dash24Json != null
                && dash24Json.get("result") != null
                && dash24Json.get("result").isJsonObject()) {
            JsonObject result = dash24Json.getAsJsonObject("result");
            if (result.get("json_metadata") == null || result.get("json_metadata").isJsonNull()) {
                Console.log("CgoObservabilityIT.test_7.dashboard.24.json_metadata", "null");
            } else {
                Console.log(
                        "CgoObservabilityIT.test_7.dashboard.24.json_metadata",
                        result.get("json_metadata").getAsString());
            }
        }
        Console.log(
                "CgoObservabilityIT.test_7.observation.chart_data.force",
                "POST /api/v1/chart/data force=true bypasses cache; force=false may reuse cache");
        Console.log(
                "CgoObservabilityIT.test_7.observation.auto_refresh",
                "dashboard auto-refresh is UI json_metadata refresh_frequency; not a CGO NOW poll API");
    }

    private void logJsonFlag(JsonObject result, String name) {
        if (result.get(name) == null || result.get(name).isJsonNull()) {
            Console.log("CgoObservabilityIT.test_7.database.1." + name, "null");
            return;
        }
        Console.log("CgoObservabilityIT.test_7.database.1." + name, result.get(name).toString());
    }

    private void inspectVirtualNowDatasetSql(int datasetId) {
        HttpResponse response = this.request("GET", "/api/v1/dataset/" + datasetId, null, true);
        this.logResponse("test_7.dataset." + datasetId + ".sql", response);
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("sql") != null && !result.get("sql").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_7.dataset." + datasetId + ".sql_text",
                    result.get("sql").getAsString());
        }
        if (result.get("is_sqllab_view") != null && !result.get("is_sqllab_view").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_7.dataset." + datasetId + ".is_sqllab_view",
                    String.valueOf(result.get("is_sqllab_view").getAsBoolean()));
        }
        Console.log(
                "CgoObservabilityIT.test_7.observation.virtual_sql",
                "dataset " + datasetId + " is a virtual UNION ALL; changing a fact would PUT dataset.sql metadata");
    }

    private void postSqlLabSelect() {
        JsonObject body = new JsonObject();
        body.addProperty("database_id", Integer.valueOf(1));
        body.addProperty("schema", "public");
        body.addProperty("sql", "SELECT 1 AS probe");
        body.addProperty("runAsync", Boolean.FALSE);
        body.addProperty("json", Boolean.TRUE);
        Console.log("CgoObservabilityIT.test_7.sqllab.select.request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/sqllab/execute/", body.toString(), true);
        this.logResponse("test_7.sqllab.select", response);
    }

    private void postSqlLabInsertProbe() {
        JsonObject body = new JsonObject();
        body.addProperty("database_id", Integer.valueOf(1));
        body.addProperty("schema", "public");
        body.addProperty(
                "sql",
                "INSERT INTO illustrative_cgo_now_mutable_facts "
                        + "(evidence_source, record_kind, synthetic_flag, fact_id, kind, "
                        + "attribute_name, attribute_value) VALUES ("
                        + "'SYNTHETIC_PROTOTYPE', 'NOT_A_MOTION_RECORD', 'SYNTHETIC_PROTOTYPE_DATA', "
                        + "'Inventory:INV-ILLUSTRATIVE-001', 'Inventory', 'quantityOnHand', '500')");
        body.addProperty("runAsync", Boolean.FALSE);
        body.addProperty("json", Boolean.TRUE);
        Console.log("CgoObservabilityIT.test_7.sqllab.insert.request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/sqllab/execute/", body.toString(), true);
        this.logResponse("test_7.sqllab.insert", response);
        Console.log(
                "CgoObservabilityIT.test_7.observation.insert",
                "INSERT is a DML probe against a non-created table; not a schema change and not an invented ingestion API");
    }

    private void inspectDatasetFilterFields(int datasetId, String label) {
        HttpResponse response = this.request("GET", "/api/v1/dataset/" + datasetId, null, true);
        this.logResponse("test_6.dataset." + label, response);
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        boolean hasKind = false;
        boolean hasSourceKind = false;
        boolean hasTargetKind = false;
        boolean hasDomain = false;
        if (result.get("columns") != null && result.get("columns").isJsonArray()) {
            JsonArray columns = result.getAsJsonArray("columns");
            int i = 0;
            while (i < columns.size()) {
                JsonObject column = columns.get(i).getAsJsonObject();
                String name = "";
                if (column.get("column_name") != null && !column.get("column_name").isJsonNull()) {
                    name = column.get("column_name").getAsString();
                }
                String filterable = "absent";
                if (column.get("filterable") != null && !column.get("filterable").isJsonNull()) {
                    filterable = String.valueOf(column.get("filterable").getAsBoolean());
                }
                Console.log(
                        "CgoObservabilityIT.test_6." + label + ".column",
                        name + " filterable=" + filterable);
                if ("kind".equals(name)) {
                    hasKind = true;
                }
                if ("source_kind".equals(name)) {
                    hasSourceKind = true;
                }
                if ("target_kind".equals(name)) {
                    hasTargetKind = true;
                }
                if ("domain".equals(name) || "domain_id".equals(name) || "domain_name".equals(name)) {
                    hasDomain = true;
                }
                i = i + 1;
            }
        }
        Console.log("CgoObservabilityIT.test_6." + label + ".has_kind", String.valueOf(hasKind));
        Console.log(
                "CgoObservabilityIT.test_6." + label + ".has_source_kind",
                String.valueOf(hasSourceKind));
        Console.log(
                "CgoObservabilityIT.test_6." + label + ".has_target_kind",
                String.valueOf(hasTargetKind));
        Console.log("CgoObservabilityIT.test_6." + label + ".has_domain", String.valueOf(hasDomain));
    }

    private JsonArray axisFactColumns() {
        JsonArray columns = new JsonArray();
        columns.add("fact_id");
        columns.add("kind");
        columns.add("attribute_name");
        columns.add("attribute_value");
        columns.add("record_kind");
        return columns;
    }

    private JsonArray graphEdgeColumns() {
        JsonArray columns = new JsonArray();
        columns.add("source_kind");
        columns.add("source_fact_id");
        columns.add("target_kind");
        columns.add("target_fact_id");
        columns.add("relationship_kind");
        columns.add("record_kind");
        return columns;
    }

    private boolean postColumnEqualsFilter(
            int datasetId,
            String columnName,
            String value,
            JsonArray columns,
            String label) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonObject filter = new JsonObject();
        filter.addProperty("col", columnName);
        filter.addProperty("op", "==");
        filter.addProperty("val", value);
        JsonArray filters = new JsonArray();
        filters.add(filter);

        JsonObject query = new JsonObject();
        query.add("columns", columns);
        query.add("filters", filters);
        query.addProperty("row_limit", Integer.valueOf(100));

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("CgoObservabilityIT.test_6.filter." + label + ".request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_6.filter." + label, response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/data filter " + label);
            return false;
        }
        return true;
    }

    private Integer createKindFilterDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE CGO Fact-kind filter explorer (synthetic prototype)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("test_6.dashboard.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dashboard/ kind filter");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createAxisKindTableChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray allColumns = new JsonArray();
        allColumns.add("evidence_source");
        allColumns.add("record_kind");
        allColumns.add("fact_id");
        allColumns.add("kind");
        allColumns.add("attribute_name");
        allColumns.add("attribute_value");

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "table");
        params.addProperty("query_mode", "raw");
        params.add("groupby", new JsonArray());
        params.add("all_columns", allColumns);
        params.add("percent_metrics", new JsonArray());
        params.add("adhoc_filters", new JsonArray());
        params.addProperty("row_limit", Integer.valueOf(1000));
        params.addProperty("server_page_length", Integer.valueOf(20));
        params.addProperty("order_desc", Boolean.TRUE);

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE Axis facts by kind (synthetic prototype)");
        body.addProperty("viz_type", "table");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_6.chart.axis.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ axis kind table");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createGraphKindTableChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray allColumns = new JsonArray();
        allColumns.add("evidence_source");
        allColumns.add("record_kind");
        allColumns.add("source_kind");
        allColumns.add("source_fact_id");
        allColumns.add("target_kind");
        allColumns.add("target_fact_id");
        allColumns.add("relationship_kind");

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "table");
        params.addProperty("query_mode", "raw");
        params.add("groupby", new JsonArray());
        params.add("all_columns", allColumns);
        params.add("percent_metrics", new JsonArray());
        params.add("adhoc_filters", new JsonArray());
        params.addProperty("row_limit", Integer.valueOf(1000));
        params.addProperty("server_page_length", Integer.valueOf(20));
        params.addProperty("order_desc", Boolean.TRUE);

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE graph edges by source/target kind (synthetic prototype)");
        body.addProperty("viz_type", "table");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_6.chart.graph.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ graph kind table");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeKindFilterChartsOnDashboard(
            int dashboardId,
            int axisChartId,
            int graphChartId) {
        JsonObject axisMeta = new JsonObject();
        axisMeta.addProperty("chartId", Integer.valueOf(axisChartId));
        axisMeta.addProperty("height", Integer.valueOf(40));
        axisMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE Axis facts by kind (synthetic prototype)");
        axisMeta.addProperty("width", Integer.valueOf(12));
        JsonObject axisNode = new JsonObject();
        axisNode.add("children", new JsonArray());
        axisNode.addProperty("id", "CHART-kind-axis-facts");
        axisNode.add("meta", axisMeta);
        axisNode.addProperty("type", "CHART");

        JsonObject graphMeta = new JsonObject();
        graphMeta.addProperty("chartId", Integer.valueOf(graphChartId));
        graphMeta.addProperty("height", Integer.valueOf(40));
        graphMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE graph edges by source/target kind (synthetic prototype)");
        graphMeta.addProperty("width", Integer.valueOf(12));
        JsonObject graphNode = new JsonObject();
        graphNode.add("children", new JsonArray());
        graphNode.addProperty("id", "CHART-kind-graph-edges");
        graphNode.add("meta", graphMeta);
        graphNode.addProperty("type", "CHART");

        JsonArray row1Children = new JsonArray();
        row1Children.add("CHART-kind-axis-facts");
        JsonObject row1Meta = new JsonObject();
        row1Meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject row1 = new JsonObject();
        row1.add("children", row1Children);
        row1.addProperty("id", "ROW-kind-axis-facts");
        row1.add("meta", row1Meta);
        row1.addProperty("type", "ROW");

        JsonArray row2Children = new JsonArray();
        row2Children.add("CHART-kind-graph-edges");
        JsonObject row2Meta = new JsonObject();
        row2Meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject row2 = new JsonObject();
        row2.add("children", row2Children);
        row2.addProperty("id", "ROW-kind-graph-edges");
        row2.add("meta", row2Meta);
        row2.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-kind-axis-facts");
        gridChildren.add("ROW-kind-graph-edges");
        JsonObject gridNode = new JsonObject();
        gridNode.add("children", gridChildren);
        gridNode.addProperty("id", "GRID_ID");
        gridNode.addProperty("type", "GRID");

        JsonArray rootChildren = new JsonArray();
        rootChildren.add("GRID_ID");
        JsonObject rootNode = new JsonObject();
        rootNode.add("children", rootChildren);
        rootNode.addProperty("id", "ROOT_ID");
        rootNode.addProperty("type", "ROOT");

        JsonObject position = new JsonObject();
        position.add("CHART-kind-axis-facts", axisNode);
        position.add("CHART-kind-graph-edges", graphNode);
        position.add("ROW-kind-axis-facts", row1);
        position.add("ROW-kind-graph-edges", row2);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("test_6.dashboard.position", response);
    }

    private void putKindNativeFilters(int dashboardId, int axisChartId, int graphChartId) {
        JsonObject kindColumn = new JsonObject();
        kindColumn.addProperty("name", "kind");
        JsonObject kindTarget = new JsonObject();
        kindTarget.addProperty("datasetId", Integer.valueOf(31));
        kindTarget.add("column", kindColumn);
        JsonArray kindTargets = new JsonArray();
        kindTargets.add(kindTarget);
        JsonArray kindRoot = new JsonArray();
        kindRoot.add("ROOT_ID");
        JsonObject kindScope = new JsonObject();
        kindScope.add("rootPath", kindRoot);
        kindScope.add("excluded", new JsonArray());
        JsonObject kindControls = new JsonObject();
        kindControls.addProperty("enableEmptyFilter", Boolean.FALSE);
        kindControls.addProperty("multiSelect", Boolean.TRUE);
        kindControls.addProperty("inverseSelection", Boolean.FALSE);
        kindControls.addProperty("searchAllOptions", Boolean.FALSE);
        JsonObject kindFilter = new JsonObject();
        kindFilter.addProperty("id", "NATIVE_FILTER-fact-kind");
        kindFilter.addProperty("name", "Fact kind");
        kindFilter.addProperty("filterType", "filter_select");
        kindFilter.add("targets", kindTargets);
        kindFilter.add("defaultDataMask", new JsonObject());
        kindFilter.add("cascadeParentIds", new JsonArray());
        kindFilter.add("scope", kindScope);
        kindFilter.addProperty("type", "NATIVE_FILTER");
        kindFilter.add("controlValues", kindControls);
        kindFilter.addProperty("description", "filter Axis facts by existing kind column");

        JsonObject sourceColumn = new JsonObject();
        sourceColumn.addProperty("name", "source_kind");
        JsonObject sourceTarget = new JsonObject();
        sourceTarget.addProperty("datasetId", Integer.valueOf(33));
        sourceTarget.add("column", sourceColumn);
        JsonArray sourceTargets = new JsonArray();
        sourceTargets.add(sourceTarget);
        JsonArray sourceRoot = new JsonArray();
        sourceRoot.add("ROOT_ID");
        JsonObject sourceScope = new JsonObject();
        sourceScope.add("rootPath", sourceRoot);
        sourceScope.add("excluded", new JsonArray());
        JsonObject sourceControls = new JsonObject();
        sourceControls.addProperty("enableEmptyFilter", Boolean.FALSE);
        sourceControls.addProperty("multiSelect", Boolean.TRUE);
        sourceControls.addProperty("inverseSelection", Boolean.FALSE);
        sourceControls.addProperty("searchAllOptions", Boolean.FALSE);
        JsonObject sourceFilter = new JsonObject();
        sourceFilter.addProperty("id", "NATIVE_FILTER-source-kind");
        sourceFilter.addProperty("name", "Source kind");
        sourceFilter.addProperty("filterType", "filter_select");
        sourceFilter.add("targets", sourceTargets);
        sourceFilter.add("defaultDataMask", new JsonObject());
        sourceFilter.add("cascadeParentIds", new JsonArray());
        sourceFilter.add("scope", sourceScope);
        sourceFilter.addProperty("type", "NATIVE_FILTER");
        sourceFilter.add("controlValues", sourceControls);
        sourceFilter.addProperty("description", "filter graph edges by existing source_kind column");

        JsonArray nativeFilters = new JsonArray();
        nativeFilters.add(kindFilter);
        nativeFilters.add(sourceFilter);
        JsonObject metadata = new JsonObject();
        metadata.add("native_filter_configuration", nativeFilters);
        metadata.add("chart_configuration", new JsonObject());

        JsonObject body = new JsonObject();
        body.addProperty("json_metadata", metadata.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("test_6.dashboard.native_filters", response);
        Console.log(
                "CgoObservabilityIT.test_6.native_filters.axis_chart_id",
                String.valueOf(axisChartId));
        Console.log(
                "CgoObservabilityIT.test_6.native_filters.graph_chart_id",
                String.valueOf(graphChartId));
    }

    private void inspectNativeTimeseriesContract() {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/chart/?q=(filters:!((col:viz_type,opr:eq,value:echarts_timeseries_line)),page:0,page_size:5)",
                null,
                true);
        this.logResponse("test_5.timeseries.line.list", listed);
        HttpResponse bars = this.request(
                "GET",
                "/api/v1/chart/?q=(filters:!((col:viz_type,opr:eq,value:echarts_timeseries_bar)),page:0,page_size:5)",
                null,
                true);
        this.logResponse("test_5.timeseries.bar.list", bars);
        HttpResponse chart101 = this.request("GET", "/api/v1/chart/101", null, true);
        this.logResponse("test_5.timeseries.example.101", chart101);
        JsonObject json = this.parseObject(chart101.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("viz_type") != null && !result.get("viz_type").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_5.timeseries.example.101.viz_type",
                    result.get("viz_type").getAsString());
        }
        if (result.get("params") != null && !result.get("params").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_5.timeseries.example.101.params",
                    result.get("params").getAsString());
        }
    }

    private Integer createInventoryTrendDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_cgo_inventory_quantity_trend");
        body.addProperty("sql", INVENTORY_TREND_SQL);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("test_5.dataset.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dataset/ inventory trend");
            return null;
        }
        return this.readId(response.body);
    }

    private boolean ensureSumAttributeValueMetric(int datasetId, JsonObject dataset) {
        JsonArray metrics = null;
        if (dataset.get("metrics") != null && dataset.get("metrics").isJsonArray()) {
            metrics = dataset.getAsJsonArray("metrics");
        }
        if (metrics != null) {
            int i = 0;
            while (i < metrics.size()) {
                JsonObject metric = metrics.get(i).getAsJsonObject();
                if (metric.get("metric_name") != null
                        && "sum_attribute_value".equals(metric.get("metric_name").getAsString())) {
                    Console.log("CgoObservabilityIT.test_5.metric.sum_attribute_value", "already present");
                    return true;
                }
                i = i + 1;
            }
        }
        JsonArray putMetrics = new JsonArray();
        if (metrics != null) {
            int j = 0;
            while (j < metrics.size()) {
                JsonObject existing = metrics.get(j).getAsJsonObject();
                JsonObject putMetric = new JsonObject();
                if (existing.get("id") != null && !existing.get("id").isJsonNull()) {
                    putMetric.addProperty("id", Integer.valueOf(existing.get("id").getAsInt()));
                }
                if (existing.get("metric_name") != null && !existing.get("metric_name").isJsonNull()) {
                    putMetric.addProperty("metric_name", existing.get("metric_name").getAsString());
                }
                if (existing.get("expression") != null && !existing.get("expression").isJsonNull()) {
                    putMetric.addProperty("expression", existing.get("expression").getAsString());
                }
                putMetrics.add(putMetric);
                j = j + 1;
            }
        }
        JsonObject sumMetric = new JsonObject();
        sumMetric.addProperty("metric_name", "sum_attribute_value");
        sumMetric.addProperty("expression", "SUM(attribute_value)");
        putMetrics.add(sumMetric);
        JsonObject body = new JsonObject();
        body.add("metrics", putMetrics);
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dataset/" + datasetId,
                body.toString(),
                true);
        this.logResponse("test_5.dataset.add_metric", response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "PUT /api/v1/dataset/ add sum_attribute_value");
            return false;
        }
        return true;
    }

    private void logTrendDatasetColumns(JsonObject dataset) {
        if (dataset.get("main_dttm_col") != null && !dataset.get("main_dttm_col").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_5.main_dttm_col",
                    dataset.get("main_dttm_col").getAsString());
        } else {
            Console.log("CgoObservabilityIT.test_5.main_dttm_col", "null");
        }
        if (dataset.get("columns") != null && dataset.get("columns").isJsonArray()) {
            JsonArray columns = dataset.getAsJsonArray("columns");
            int i = 0;
            while (i < columns.size()) {
                JsonObject column = columns.get(i).getAsJsonObject();
                Console.log("CgoObservabilityIT.test_5.dataset.column", column.toString());
                i = i + 1;
            }
        }
        if (dataset.get("metrics") != null && dataset.get("metrics").isJsonArray()) {
            JsonArray metrics = dataset.getAsJsonArray("metrics");
            int k = 0;
            while (k < metrics.size()) {
                Console.log(
                        "CgoObservabilityIT.test_5.dataset.metric",
                        metrics.get(k).getAsJsonObject().toString());
                k = k + 1;
            }
        }
    }

    private boolean postTrendRows(int datasetId, String temporalRange, String label) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray columns = new JsonArray();
        columns.add("fact_id");
        columns.add("kind");
        columns.add("attribute_name");
        columns.add("attribute_value");
        columns.add("observed_at");
        columns.add("synthetic_flag");
        columns.add("record_kind");

        JsonObject query = new JsonObject();
        query.add("columns", columns);
        query.addProperty("row_limit", Integer.valueOf(100));
        if (!"No filter".equals(temporalRange)) {
            JsonObject temporalFilter = new JsonObject();
            temporalFilter.addProperty("col", "observed_at");
            temporalFilter.addProperty("op", "TEMPORAL_RANGE");
            temporalFilter.addProperty("val", temporalRange);
            JsonArray filters = new JsonArray();
            filters.add(temporalFilter);
            query.add("filters", filters);
            query.addProperty("time_range", temporalRange);
            query.addProperty("granularity", "observed_at");
        }

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("CgoObservabilityIT.test_5.rows." + label + ".request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_5.rows." + label, response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/data inventory trend rows " + label);
            return false;
        }
        return true;
    }

    private boolean postTrendChartData(int datasetId, String temporalRange, String label) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonObject extras = new JsonObject();
        extras.addProperty("time_grain_sqla", "P1D");

        JsonArray groupby = new JsonArray();
        groupby.add("fact_id");

        JsonArray metrics = new JsonArray();
        metrics.add("sum_attribute_value");

        JsonObject query = new JsonObject();
        query.addProperty("granularity", "observed_at");
        query.addProperty("is_timeseries", Boolean.TRUE);
        query.add("groupby", groupby);
        query.add("metrics", metrics);
        query.add("extras", extras);
        query.addProperty("row_limit", Integer.valueOf(10000));
        if (!"No filter".equals(temporalRange)) {
            JsonObject temporalFilter = new JsonObject();
            temporalFilter.addProperty("col", "observed_at");
            temporalFilter.addProperty("op", "TEMPORAL_RANGE");
            temporalFilter.addProperty("val", temporalRange);
            JsonArray filters = new JsonArray();
            filters.add(temporalFilter);
            query.add("filters", filters);
            query.addProperty("time_range", temporalRange);
        } else {
            query.addProperty("time_range", "No filter");
        }

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("CgoObservabilityIT.test_5.chart_data." + label + ".request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_5.chart_data." + label, response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/data inventory trend " + label);
            return false;
        }
        return true;
    }

    private Integer createInventoryTrendDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE CGO Inventory quantityOnHand trend (synthetic prototype)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("test_5.dashboard.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dashboard/ inventory trend");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createInventoryTrendChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray groupby = new JsonArray();
        groupby.add("fact_id");

        JsonArray metrics = new JsonArray();
        metrics.add("sum_attribute_value");

        JsonObject temporalFilter = new JsonObject();
        temporalFilter.addProperty("clause", "WHERE");
        temporalFilter.addProperty("subject", "observed_at");
        temporalFilter.addProperty("operator", "TEMPORAL_RANGE");
        temporalFilter.addProperty("comparator", TREND_FULL_RANGE);
        temporalFilter.addProperty("expressionType", "SIMPLE");
        JsonArray adhocFilters = new JsonArray();
        adhocFilters.add(temporalFilter);

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "echarts_timeseries_line");
        params.addProperty("x_axis", "observed_at");
        params.addProperty("time_grain_sqla", "P1D");
        params.add("metrics", metrics);
        params.add("groupby", groupby);
        params.add("adhoc_filters", adhocFilters);
        params.addProperty("time_range", TREND_FULL_RANGE);
        params.addProperty("granularity_sqla", "observed_at");
        params.addProperty("orientation", "vertical");
        params.addProperty("show_legend", Boolean.TRUE);
        params.addProperty("color_scheme", "supersetColors");
        params.addProperty("row_limit", Integer.valueOf(10000));
        params.addProperty("y_axis_format", "SMART_NUMBER");
        params.addProperty("rich_tooltip", Boolean.TRUE);

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE Inventory quantityOnHand over time (synthetic prototype)");
        body.addProperty("viz_type", "echarts_timeseries_line");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_5.chart.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ echarts_timeseries_line");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeInventoryTrendChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE Inventory quantityOnHand over time (synthetic prototype)");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-inventory-quantity-trend");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-inventory-quantity-trend");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-inventory-quantity-trend");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-inventory-quantity-trend");
        JsonObject gridNode = new JsonObject();
        gridNode.add("children", gridChildren);
        gridNode.addProperty("id", "GRID_ID");
        gridNode.addProperty("type", "GRID");

        JsonArray rootChildren = new JsonArray();
        rootChildren.add("GRID_ID");
        JsonObject rootNode = new JsonObject();
        rootNode.add("children", rootChildren);
        rootNode.addProperty("id", "ROOT_ID");
        rootNode.addProperty("type", "ROOT");

        JsonObject position = new JsonObject();
        position.add("CHART-inventory-quantity-trend", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-inventory-quantity-trend", rowNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("test_5.dashboard.position", response);
    }

    private void inspectNativeComparisonCapabilities() {
        HttpResponse graphCharts = this.request(
                "GET",
                "/api/v1/chart/?q=(filters:!((col:viz_type,opr:eq,value:graph_chart)),page:0,page_size:5)",
                null,
                true);
        this.logResponse("test_4.compare.graph_chart.list", graphCharts);
        HttpResponse timeseries = this.request(
                "GET",
                "/api/v1/chart/?q=(filters:!((col:viz_type,opr:eq,value:echarts_timeseries_bar)),page:0,page_size:5)",
                null,
                true);
        this.logResponse("test_4.compare.timeseries_bar.list", timeseries);
        HttpResponse chart101 = this.request("GET", "/api/v1/chart/101", null, true);
        this.logResponse("test_4.compare.example.101", chart101);
        JsonObject json = this.parseObject(chart101.body);
        if (json != null && json.get("result") != null && json.get("result").isJsonObject()) {
            JsonObject result = json.getAsJsonObject("result");
            if (result.get("params") != null && !result.get("params").isJsonNull()) {
                Console.log(
                        "CgoObservabilityIT.test_4.compare.example.101.params",
                        result.get("params").getAsString());
            }
            if (result.get("viz_type") != null && !result.get("viz_type").isJsonNull()) {
                Console.log(
                        "CgoObservabilityIT.test_4.compare.example.101.viz_type",
                        result.get("viz_type").getAsString());
            }
        }
        HttpResponse chart132 = this.request("GET", "/api/v1/chart/132", null, true);
        this.logResponse("test_4.compare.chart_132", chart132);
        JsonObject graphJson = this.parseObject(chart132.body);
        if (graphJson != null
                && graphJson.get("result") != null
                && graphJson.get("result").isJsonObject()) {
            JsonObject result = graphJson.getAsJsonObject("result");
            if (result.get("params") != null && !result.get("params").isJsonNull()) {
                Console.log(
                        "CgoObservabilityIT.test_4.compare.chart_132.params",
                        result.get("params").getAsString());
            }
        }
        Console.log(
                "CgoObservabilityIT.test_4.compare.graph_chart.diff_field",
                "absent; source/target/metric/layout only");
    }

    private Integer createChangeExplorerDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_cgo_historical_graph_change");
        body.addProperty("sql", HISTORICAL_CHANGE_SQL);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("test_4.dataset.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dataset/ change explorer");
            return null;
        }
        return this.readId(response.body);
    }

    private boolean postChangeExplorerRows(int datasetId) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray columns = new JsonArray();
        columns.add("observed_change");
        columns.add("observed_at");
        columns.add("source_kind");
        columns.add("source_fact_id");
        columns.add("target_kind");
        columns.add("target_fact_id");
        columns.add("relationship_kind");
        columns.add("synthetic_flag");
        columns.add("record_kind");

        JsonObject query = new JsonObject();
        query.add("columns", columns);
        query.addProperty("row_limit", Integer.valueOf(100));

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("CgoObservabilityIT.test_4.change.rows.request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_4.change.rows", response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/data change explorer rows");
            return false;
        }
        return true;
    }

    private Integer createChangeExplorerDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE CGO Change Explorer T1 vs T2 (synthetic prototype)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("test_4.dashboard.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dashboard/ change explorer");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createChangeExplorerTableChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray allColumns = new JsonArray();
        allColumns.add("observed_change");
        allColumns.add("observed_at");
        allColumns.add("evidence_source");
        allColumns.add("record_kind");
        allColumns.add("synthetic_flag");
        allColumns.add("source_kind");
        allColumns.add("source_fact_id");
        allColumns.add("target_kind");
        allColumns.add("target_fact_id");
        allColumns.add("relationship_kind");

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "table");
        params.addProperty("query_mode", "raw");
        params.add("groupby", new JsonArray());
        params.add("all_columns", allColumns);
        params.add("percent_metrics", new JsonArray());
        params.add("adhoc_filters", new JsonArray());
        params.addProperty("row_limit", Integer.valueOf(1000));
        params.addProperty("server_page_length", Integer.valueOf(20));
        params.addProperty("order_desc", Boolean.TRUE);

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE T1 vs T2 relationship change (synthetic prototype)");
        body.addProperty("viz_type", "table");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_4.chart.table.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ change explorer table");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createChangeExplorerGraphChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "graph_chart");
        params.addProperty("source", "source_fact_id");
        params.addProperty("target", "target_fact_id");
        params.addProperty("metric", "count");
        params.addProperty("source_category", "source_kind");
        params.addProperty("target_category", "target_kind");
        params.add("adhoc_filters", new JsonArray());
        params.addProperty("row_limit", Integer.valueOf(10000));
        params.addProperty("color_scheme", "supersetColors");
        params.addProperty("show_legend", Boolean.TRUE);
        params.addProperty("legendType", "scroll");
        params.addProperty("legendOrientation", "top");
        params.addProperty("layout", "circular");
        params.addProperty("edgeSymbol", "none,arrow");
        params.addProperty("roam", Boolean.TRUE);
        params.addProperty("selectedMode", "single");
        params.addProperty("baseNodeSize", Integer.valueOf(20));
        params.addProperty("baseEdgeWidth", Integer.valueOf(3));
        params.addProperty("edgeLength", Integer.valueOf(400));
        params.addProperty("gravity", Double.valueOf(0.3));
        params.addProperty("repulsion", Integer.valueOf(1000));
        params.addProperty("friction", Double.valueOf(0.2));

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE T1 vs T2 added edges graph (synthetic prototype)");
        body.addProperty("viz_type", "graph_chart");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_4.chart.graph.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ change explorer graph");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeChangeExplorerChartsOnDashboard(
            int dashboardId,
            int tableChartId,
            int graphChartId) {
        JsonObject tableMeta = new JsonObject();
        tableMeta.addProperty("chartId", Integer.valueOf(tableChartId));
        tableMeta.addProperty("height", Integer.valueOf(40));
        tableMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE T1 vs T2 relationship change (synthetic prototype)");
        tableMeta.addProperty("width", Integer.valueOf(12));
        JsonObject tableNode = new JsonObject();
        tableNode.add("children", new JsonArray());
        tableNode.addProperty("id", "CHART-change-explorer-table");
        tableNode.add("meta", tableMeta);
        tableNode.addProperty("type", "CHART");

        JsonObject graphMeta = new JsonObject();
        graphMeta.addProperty("chartId", Integer.valueOf(graphChartId));
        graphMeta.addProperty("height", Integer.valueOf(50));
        graphMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE T1 vs T2 added edges graph (synthetic prototype)");
        graphMeta.addProperty("width", Integer.valueOf(12));
        JsonObject graphNode = new JsonObject();
        graphNode.add("children", new JsonArray());
        graphNode.addProperty("id", "CHART-change-explorer-graph");
        graphNode.add("meta", graphMeta);
        graphNode.addProperty("type", "CHART");

        JsonArray row1Children = new JsonArray();
        row1Children.add("CHART-change-explorer-table");
        JsonObject row1Meta = new JsonObject();
        row1Meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject row1 = new JsonObject();
        row1.add("children", row1Children);
        row1.addProperty("id", "ROW-change-explorer-table");
        row1.add("meta", row1Meta);
        row1.addProperty("type", "ROW");

        JsonArray row2Children = new JsonArray();
        row2Children.add("CHART-change-explorer-graph");
        JsonObject row2Meta = new JsonObject();
        row2Meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject row2 = new JsonObject();
        row2.add("children", row2Children);
        row2.addProperty("id", "ROW-change-explorer-graph");
        row2.add("meta", row2Meta);
        row2.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-change-explorer-table");
        gridChildren.add("ROW-change-explorer-graph");
        JsonObject gridNode = new JsonObject();
        gridNode.add("children", gridChildren);
        gridNode.addProperty("id", "GRID_ID");
        gridNode.addProperty("type", "GRID");

        JsonArray rootChildren = new JsonArray();
        rootChildren.add("GRID_ID");
        JsonObject rootNode = new JsonObject();
        rootNode.add("children", rootChildren);
        rootNode.addProperty("id", "ROOT_ID");
        rootNode.addProperty("type", "ROOT");

        JsonObject position = new JsonObject();
        position.add("CHART-change-explorer-table", tableNode);
        position.add("CHART-change-explorer-graph", graphNode);
        position.add("ROW-change-explorer-table", row1);
        position.add("ROW-change-explorer-graph", row2);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("test_4.dashboard.position", response);
    }

    private void inspectNativeTemporalExample() {
        HttpResponse example = this.request("GET", "/api/v1/chart/103", null, true);
        this.logResponse("test_3.temporal.example.graph_103", example);
        HttpResponse timeseries = this.request(
                "GET",
                "/api/v1/chart/?q=(filters:!((col:viz_type,opr:eq,value:echarts_timeseries_bar)),page:0,page_size:3)",
                null,
                true);
        this.logResponse("test_3.temporal.timeseries_bar.list", timeseries);
        HttpResponse chart101 = this.request("GET", "/api/v1/chart/101", null, true);
        this.logResponse("test_3.temporal.example.101", chart101);
        JsonObject json = this.parseObject(chart101.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            Console.log(
                    "CgoObservabilityIT.test_3.temporal.example.101.params",
                    "absent");
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("params") != null && !result.get("params").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_3.temporal.example.101.params",
                    result.get("params").getAsString());
        }
        if (result.get("viz_type") != null && !result.get("viz_type").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_3.temporal.example.101.viz_type",
                    result.get("viz_type").getAsString());
        }
    }

    private void inspectNowGraphHasNoObservedAt() {
        HttpResponse dataset = this.request("GET", "/api/v1/dataset/33", null, true);
        this.logResponse("test_3.now_graph.dataset_33", dataset);
        JsonObject json = this.parseObject(dataset.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("main_dttm_col") != null && !result.get("main_dttm_col").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_3.now_graph.main_dttm_col",
                    result.get("main_dttm_col").getAsString());
        } else {
            Console.log("CgoObservabilityIT.test_3.now_graph.main_dttm_col", "null");
        }
        if (result.get("columns") == null || !result.get("columns").isJsonArray()) {
            return;
        }
        JsonArray columns = result.getAsJsonArray("columns");
        int i = 0;
        boolean hasObservedAt = false;
        while (i < columns.size()) {
            JsonObject column = columns.get(i).getAsJsonObject();
            if (column.get("column_name") != null
                    && "observed_at".equals(column.get("column_name").getAsString())) {
                hasObservedAt = true;
            }
            i = i + 1;
        }
        Console.log(
                "CgoObservabilityIT.test_3.now_graph.has_observed_at",
                String.valueOf(hasObservedAt));
    }

    private Integer createHistoricalGraphDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_cgo_historical_graph_states");
        body.addProperty("sql", HISTORICAL_GRAPH_SQL);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("test_3.dataset.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dataset/ historical graph");
            return null;
        }
        return this.readId(response.body);
    }

    private boolean ensureObservedAtIsTemporal(int datasetId, JsonObject dataset) {
        boolean observedAtIsDttm = false;
        JsonArray columns = null;
        if (dataset.get("columns") != null && dataset.get("columns").isJsonArray()) {
            columns = dataset.getAsJsonArray("columns");
        }
        if (columns != null) {
            int i = 0;
            while (i < columns.size()) {
                JsonObject column = columns.get(i).getAsJsonObject();
                if (column.get("column_name") != null
                        && "observed_at".equals(column.get("column_name").getAsString())) {
                    if (column.get("is_dttm") != null
                            && !column.get("is_dttm").isJsonNull()
                            && column.get("is_dttm").getAsBoolean()) {
                        observedAtIsDttm = true;
                    }
                }
                i = i + 1;
            }
        }
        Console.log(
                "CgoObservabilityIT.test_3.observed_at.is_dttm.before_put",
                String.valueOf(observedAtIsDttm));
        if (observedAtIsDttm) {
            return true;
        }
        if (columns == null) {
            Console.log("CgoObservabilityIT.blocker", "historical dataset columns missing");
            return false;
        }
        JsonArray putColumns = new JsonArray();
        int j = 0;
        while (j < columns.size()) {
            JsonObject existing = columns.get(j).getAsJsonObject();
            JsonObject putColumn = new JsonObject();
            putColumn.addProperty("column_name", existing.get("column_name").getAsString());
            if (existing.get("id") != null && !existing.get("id").isJsonNull()) {
                putColumn.addProperty("id", Integer.valueOf(existing.get("id").getAsInt()));
            }
            if (existing.get("type") != null && !existing.get("type").isJsonNull()) {
                putColumn.addProperty("type", existing.get("type").getAsString());
            }
            if (existing.get("filterable") != null && !existing.get("filterable").isJsonNull()) {
                putColumn.addProperty("filterable", existing.get("filterable").getAsBoolean());
            }
            if (existing.get("groupby") != null && !existing.get("groupby").isJsonNull()) {
                putColumn.addProperty("groupby", existing.get("groupby").getAsBoolean());
            }
            if ("observed_at".equals(existing.get("column_name").getAsString())) {
                putColumn.addProperty("is_dttm", Boolean.TRUE);
            } else if (existing.get("is_dttm") != null && !existing.get("is_dttm").isJsonNull()) {
                putColumn.addProperty("is_dttm", existing.get("is_dttm").getAsBoolean());
            }
            putColumns.add(putColumn);
            j = j + 1;
        }
        JsonObject body = new JsonObject();
        body.add("columns", putColumns);
        body.addProperty("main_dttm_col", "observed_at");
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dataset/" + datasetId,
                body.toString(),
                true);
        this.logResponse("test_3.dataset.mark_temporal", response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "PUT /api/v1/dataset/ historical mark observed_at temporal");
            return false;
        }
        return true;
    }

    private void logObservedAtColumn(JsonObject dataset) {
        if (dataset.get("main_dttm_col") != null && !dataset.get("main_dttm_col").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_3.main_dttm_col",
                    dataset.get("main_dttm_col").getAsString());
        } else {
            Console.log("CgoObservabilityIT.test_3.main_dttm_col", "null");
        }
        if (dataset.get("columns") == null || !dataset.get("columns").isJsonArray()) {
            return;
        }
        JsonArray columns = dataset.getAsJsonArray("columns");
        int i = 0;
        while (i < columns.size()) {
            JsonObject column = columns.get(i).getAsJsonObject();
            if (column.get("column_name") != null
                    && "observed_at".equals(column.get("column_name").getAsString())) {
                Console.log(
                        "CgoObservabilityIT.test_3.observed_at.column",
                        column.toString());
            }
            i = i + 1;
        }
    }

    private boolean postHistoricalRows(int datasetId, String temporalRange, String label) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray columns = new JsonArray();
        columns.add("observed_at");
        columns.add("historical_label");
        columns.add("source_fact_id");
        columns.add("target_fact_id");
        columns.add("relationship_kind");
        columns.add("synthetic_flag");
        columns.add("record_kind");

        JsonObject query = new JsonObject();
        query.add("columns", columns);
        query.addProperty("row_limit", Integer.valueOf(100));
        if (!"No filter".equals(temporalRange)) {
            JsonObject temporalFilter = new JsonObject();
            temporalFilter.addProperty("col", "observed_at");
            temporalFilter.addProperty("op", "TEMPORAL_RANGE");
            temporalFilter.addProperty("val", temporalRange);
            JsonArray filters = new JsonArray();
            filters.add(temporalFilter);
            query.add("filters", filters);
            query.addProperty("time_range", temporalRange);
            query.addProperty("granularity", "observed_at");
        }

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("CgoObservabilityIT.test_3.rows." + label + ".request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_3.rows." + label, response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/data historical rows " + label);
            return false;
        }
        return true;
    }

    private boolean postHistoricalGraphQuery(int datasetId, String temporalRange, String label) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray groupby = new JsonArray();
        groupby.add("source_fact_id");
        groupby.add("target_fact_id");

        JsonArray metrics = new JsonArray();
        metrics.add("count");

        JsonObject temporalFilter = new JsonObject();
        temporalFilter.addProperty("col", "observed_at");
        temporalFilter.addProperty("op", "TEMPORAL_RANGE");
        temporalFilter.addProperty("val", temporalRange);
        JsonArray filters = new JsonArray();
        filters.add(temporalFilter);

        JsonObject query = new JsonObject();
        query.add("groupby", groupby);
        query.add("metrics", metrics);
        query.add("filters", filters);
        query.addProperty("time_range", temporalRange);
        query.addProperty("granularity", "observed_at");
        query.addProperty("row_limit", Integer.valueOf(10000));

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("CgoObservabilityIT.test_3.graph." + label + ".request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_3.graph." + label, response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/data historical graph " + label);
            return false;
        }
        return true;
    }

    private Integer createHistoricalDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE CGO historical T1 graph (synthetic prototype; pinned 2026-01-15)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("test_3.dashboard.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dashboard/ historical");
            return null;
        }
        return this.readId(response.body);
    }

    private JsonObject historicalTemporalAdhocFilter() {
        JsonObject temporalFilter = new JsonObject();
        temporalFilter.addProperty("clause", "WHERE");
        temporalFilter.addProperty("subject", "observed_at");
        temporalFilter.addProperty("operator", "TEMPORAL_RANGE");
        temporalFilter.addProperty("comparator", HISTORICAL_T1_RANGE);
        temporalFilter.addProperty("expressionType", "SIMPLE");
        return temporalFilter;
    }

    private Integer createHistoricalGraphChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray adhocFilters = new JsonArray();
        adhocFilters.add(this.historicalTemporalAdhocFilter());

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "graph_chart");
        params.addProperty("source", "source_fact_id");
        params.addProperty("target", "target_fact_id");
        params.addProperty("metric", "count");
        params.addProperty("source_category", "source_kind");
        params.addProperty("target_category", "target_kind");
        params.add("adhoc_filters", adhocFilters);
        params.addProperty("time_range", HISTORICAL_T1_RANGE);
        params.addProperty("granularity_sqla", "observed_at");
        params.addProperty("row_limit", Integer.valueOf(10000));
        params.addProperty("color_scheme", "supersetColors");
        params.addProperty("show_legend", Boolean.TRUE);
        params.addProperty("legendType", "scroll");
        params.addProperty("legendOrientation", "top");
        params.addProperty("layout", "circular");
        params.addProperty("edgeSymbol", "none,arrow");
        params.addProperty("roam", Boolean.TRUE);
        params.addProperty("selectedMode", "single");
        params.addProperty("baseNodeSize", Integer.valueOf(20));
        params.addProperty("baseEdgeWidth", Integer.valueOf(3));
        params.addProperty("edgeLength", Integer.valueOf(400));
        params.addProperty("gravity", Double.valueOf(0.3));
        params.addProperty("repulsion", Integer.valueOf(1000));
        params.addProperty("friction", Double.valueOf(0.2));

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE historical graph T1 pinned 2026-01-15 (synthetic prototype)");
        body.addProperty("viz_type", "graph_chart");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_3.chart.graph.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ historical graph");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createHistoricalTableChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray allColumns = new JsonArray();
        allColumns.add("observed_at");
        allColumns.add("historical_label");
        allColumns.add("evidence_source");
        allColumns.add("record_kind");
        allColumns.add("synthetic_flag");
        allColumns.add("source_kind");
        allColumns.add("source_fact_id");
        allColumns.add("target_kind");
        allColumns.add("target_fact_id");
        allColumns.add("relationship_kind");

        JsonArray adhocFilters = new JsonArray();
        adhocFilters.add(this.historicalTemporalAdhocFilter());

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "table");
        params.addProperty("query_mode", "raw");
        params.add("groupby", new JsonArray());
        params.add("all_columns", allColumns);
        params.add("percent_metrics", new JsonArray());
        params.add("adhoc_filters", adhocFilters);
        params.addProperty("time_range", HISTORICAL_T1_RANGE);
        params.addProperty("granularity_sqla", "observed_at");
        params.addProperty("row_limit", Integer.valueOf(1000));
        params.addProperty("server_page_length", Integer.valueOf(20));
        params.addProperty("order_desc", Boolean.TRUE);

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE historical relationships T1 pinned 2026-01-15 (synthetic prototype)");
        body.addProperty("viz_type", "table");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_3.chart.table.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ historical table");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeHistoricalChartsOnDashboard(int dashboardId, int graphChartId, int tableChartId) {
        JsonObject graphMeta = new JsonObject();
        graphMeta.addProperty("chartId", Integer.valueOf(graphChartId));
        graphMeta.addProperty("height", Integer.valueOf(50));
        graphMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE historical graph T1 pinned 2026-01-15 (synthetic prototype)");
        graphMeta.addProperty("width", Integer.valueOf(12));
        JsonObject graphNode = new JsonObject();
        graphNode.add("children", new JsonArray());
        graphNode.addProperty("id", "CHART-historical-graph-t1");
        graphNode.add("meta", graphMeta);
        graphNode.addProperty("type", "CHART");

        JsonObject tableMeta = new JsonObject();
        tableMeta.addProperty("chartId", Integer.valueOf(tableChartId));
        tableMeta.addProperty("height", Integer.valueOf(50));
        tableMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE historical relationships T1 pinned 2026-01-15 (synthetic prototype)");
        tableMeta.addProperty("width", Integer.valueOf(12));
        JsonObject tableNode = new JsonObject();
        tableNode.add("children", new JsonArray());
        tableNode.addProperty("id", "CHART-historical-table-t1");
        tableNode.add("meta", tableMeta);
        tableNode.addProperty("type", "CHART");

        JsonArray row1Children = new JsonArray();
        row1Children.add("CHART-historical-graph-t1");
        JsonObject row1Meta = new JsonObject();
        row1Meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject row1 = new JsonObject();
        row1.add("children", row1Children);
        row1.addProperty("id", "ROW-historical-graph-t1");
        row1.add("meta", row1Meta);
        row1.addProperty("type", "ROW");

        JsonArray row2Children = new JsonArray();
        row2Children.add("CHART-historical-table-t1");
        JsonObject row2Meta = new JsonObject();
        row2Meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject row2 = new JsonObject();
        row2.add("children", row2Children);
        row2.addProperty("id", "ROW-historical-table-t1");
        row2.add("meta", row2Meta);
        row2.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-historical-graph-t1");
        gridChildren.add("ROW-historical-table-t1");
        JsonObject gridNode = new JsonObject();
        gridNode.add("children", gridChildren);
        gridNode.addProperty("id", "GRID_ID");
        gridNode.addProperty("type", "GRID");

        JsonArray rootChildren = new JsonArray();
        rootChildren.add("GRID_ID");
        JsonObject rootNode = new JsonObject();
        rootNode.add("children", rootChildren);
        rootNode.addProperty("id", "ROOT_ID");
        rootNode.addProperty("type", "ROOT");

        JsonObject position = new JsonObject();
        position.add("CHART-historical-graph-t1", graphNode);
        position.add("CHART-historical-table-t1", tableNode);
        position.add("ROW-historical-graph-t1", row1);
        position.add("ROW-historical-table-t1", row2);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("test_3.dashboard.position", response);
    }

    private void inspectChartRelationshipCapabilities(int chartId, String label) {
        HttpResponse chart = this.request("GET", "/api/v1/chart/" + chartId, null, true);
        this.logResponse("test_2.chart." + label, chart);
        JsonObject json = this.parseObject(chart.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("viz_type") != null && !result.get("viz_type").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_2.chart." + label + ".viz_type",
                    result.get("viz_type").getAsString());
        }
        if (result.get("params") != null && !result.get("params").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_2.chart." + label + ".params",
                    result.get("params").getAsString());
        }
        if (result.get("query_context") != null && !result.get("query_context").isJsonNull()) {
            Console.log(
                    "CgoObservabilityIT.test_2.chart." + label + ".query_context",
                    result.get("query_context").getAsString());
        }
        HttpResponse data = this.request("GET", "/api/v1/chart/" + chartId + "/data/", null, true);
        this.logResponse("test_2.chart." + label + ".data_get", data);
    }

    private void logDatasetColumns(JsonObject dataset) {
        if (dataset.get("columns") == null || !dataset.get("columns").isJsonArray()) {
            return;
        }
        JsonArray columns = dataset.getAsJsonArray("columns");
        int i = 0;
        while (i < columns.size()) {
            JsonObject column = columns.get(i).getAsJsonObject();
            String name = "";
            if (column.get("column_name") != null && !column.get("column_name").isJsonNull()) {
                name = column.get("column_name").getAsString();
            }
            String filterable = "absent";
            if (column.get("filterable") != null && !column.get("filterable").isJsonNull()) {
                filterable = String.valueOf(column.get("filterable").getAsBoolean());
            }
            Console.log(
                    "CgoObservabilityIT.test_2.dataset.column",
                    name + " filterable=" + filterable);
            i = i + 1;
        }
    }

    private void postRelationshipDetailRows(int datasetId) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray columns = new JsonArray();
        columns.add("source_kind");
        columns.add("source_fact_id");
        columns.add("target_kind");
        columns.add("target_fact_id");
        columns.add("relationship_kind");
        columns.add("synthetic_flag");
        columns.add("record_kind");

        JsonObject query = new JsonObject();
        query.add("columns", columns);
        query.addProperty("row_limit", Integer.valueOf(100));

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("CgoObservabilityIT.test_2.detail.request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_2.detail.results", response);
    }

    private void postRelationshipKindFilter(int datasetId, String relationshipKind) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray columns = new JsonArray();
        columns.add("source_fact_id");
        columns.add("target_fact_id");
        columns.add("relationship_kind");

        JsonObject filter = new JsonObject();
        filter.addProperty("col", "relationship_kind");
        filter.addProperty("op", "==");
        filter.addProperty("val", relationshipKind);
        JsonArray filters = new JsonArray();
        filters.add(filter);

        JsonObject query = new JsonObject();
        query.add("columns", columns);
        query.add("filters", filters);
        query.addProperty("row_limit", Integer.valueOf(100));

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log(
                "CgoObservabilityIT.test_2.filter." + relationshipKind + ".request",
                body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_2.filter." + relationshipKind + ".results", response);
    }

    private void postGraphQueryWithRelationshipFilter(int datasetId, String relationshipKind) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray groupby = new JsonArray();
        groupby.add("source_fact_id");
        groupby.add("target_fact_id");

        JsonArray metrics = new JsonArray();
        metrics.add("count");

        JsonObject filter = new JsonObject();
        filter.addProperty("col", "relationship_kind");
        filter.addProperty("op", "==");
        filter.addProperty("val", relationshipKind);
        JsonArray filters = new JsonArray();
        filters.add(filter);

        JsonObject query = new JsonObject();
        query.add("groupby", groupby);
        query.add("metrics", metrics);
        query.add("filters", filters);
        query.addProperty("row_limit", Integer.valueOf(10000));

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log(
                "CgoObservabilityIT.test_2.graph_filter." + relationshipKind + ".request",
                body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_2.graph_filter." + relationshipKind + ".results", response);
    }

    private Integer createRelationshipDetailDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE CGO NOW relationship detail (synthetic prototype)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("test_2.dashboard.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dashboard/ relationship detail");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createRelationshipDetailTableChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray allColumns = new JsonArray();
        allColumns.add("evidence_source");
        allColumns.add("record_kind");
        allColumns.add("synthetic_flag");
        allColumns.add("source_kind");
        allColumns.add("source_fact_id");
        allColumns.add("target_kind");
        allColumns.add("target_fact_id");
        allColumns.add("relationship_kind");

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "table");
        params.addProperty("query_mode", "raw");
        params.add("groupby", new JsonArray());
        params.add("all_columns", allColumns);
        params.add("percent_metrics", new JsonArray());
        params.add("adhoc_filters", new JsonArray());
        params.addProperty("row_limit", Integer.valueOf(1000));
        params.addProperty("server_page_length", Integer.valueOf(20));
        params.addProperty("order_desc", Boolean.TRUE);

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE relationship detail table (synthetic prototype)");
        body.addProperty("viz_type", "table");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_2.chart.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ relationship detail table");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeRelationshipDetailChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE relationship detail table (synthetic prototype)");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-illustrative-relationship-detail");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-illustrative-relationship-detail");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-illustrative-relationship-detail");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-illustrative-relationship-detail");
        JsonObject gridNode = new JsonObject();
        gridNode.add("children", gridChildren);
        gridNode.addProperty("id", "GRID_ID");
        gridNode.addProperty("type", "GRID");

        JsonArray rootChildren = new JsonArray();
        rootChildren.add("GRID_ID");
        JsonObject rootNode = new JsonObject();
        rootNode.add("children", rootChildren);
        rootNode.addProperty("id", "ROOT_ID");
        rootNode.addProperty("type", "ROOT");

        JsonObject position = new JsonObject();
        position.add("CHART-illustrative-relationship-detail", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-illustrative-relationship-detail", rowNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("test_2.dashboard.position", response);
    }

    private boolean inspectGraphChartSupport() {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/chart/?q=(filters:!((col:viz_type,opr:eq,value:graph_chart)),page:0,page_size:5)",
                null,
                true);
        this.logResponse("graph_chart.list", listed);
        HttpResponse example = this.request("GET", "/api/v1/chart/103", null, true);
        this.logResponse("graph_chart.example.103", example);
        if (example.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/chart/103 graph_chart example unavailable");
            return false;
        }
        JsonObject json = this.parseObject(example.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            Console.log("CgoObservabilityIT.blocker", "chart 103 missing result");
            return false;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("viz_type") == null
                || result.get("viz_type").isJsonNull()
                || !"graph_chart".equals(result.get("viz_type").getAsString())) {
            Console.log("CgoObservabilityIT.blocker", "chart 103 viz_type is not graph_chart");
            return false;
        }
        Console.log("CgoObservabilityIT.graph_chart.available", "true");
        if (result.get("params") != null && !result.get("params").isJsonNull()) {
            Console.log("CgoObservabilityIT.graph_chart.example.params", result.get("params").getAsString());
        }
        Console.log(
                "CgoObservabilityIT.graph_chart.layout.options",
                "force,circular");
        Console.log(
                "CgoObservabilityIT.graph_chart.layout.source",
                "Superset 6.0.0 plugin-chart-echarts Graph RadioButtonControl; example chart 103 uses circular");
        return true;
    }

    private Integer createExpandedGraphDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_cgo_now_axis_graph_expanded");
        body.addProperty("sql", EXPANDED_GRAPH_SQL);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("expanded.dataset.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dataset/ expanded graph");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createExpandedGraphDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE CGO NOW expanded synthetic graph (not Motion records)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("expanded.dashboard.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dashboard/ expanded graph");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createGraphChartWithLayout(
            int datasetId,
            int dashboardId,
            String layout,
            String label) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "graph_chart");
        params.addProperty("source", "source_fact_id");
        params.addProperty("target", "target_fact_id");
        params.addProperty("metric", "count");
        params.addProperty("source_category", "source_kind");
        params.addProperty("target_category", "target_kind");
        params.add("adhoc_filters", new JsonArray());
        params.addProperty("row_limit", Integer.valueOf(10000));
        params.addProperty("color_scheme", "supersetColors");
        params.addProperty("show_legend", Boolean.TRUE);
        params.addProperty("legendType", "scroll");
        params.addProperty("legendOrientation", "top");
        params.addProperty("layout", layout);
        params.addProperty("edgeSymbol", "none,arrow");
        params.addProperty("roam", Boolean.TRUE);
        params.addProperty("selectedMode", "single");
        params.addProperty("baseNodeSize", Integer.valueOf(20));
        params.addProperty("baseEdgeWidth", Integer.valueOf(3));
        params.addProperty("edgeLength", Integer.valueOf(400));
        params.addProperty("gravity", Double.valueOf(0.3));
        params.addProperty("repulsion", Integer.valueOf(1000));
        params.addProperty("friction", Double.valueOf(0.2));

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE expanded synthetic graph layout=" + layout);
        body.addProperty("viz_type", "graph_chart");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("expanded.chart.create." + label, response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ graph_chart layout=" + layout);
            return null;
        }
        return this.readId(response.body);
    }

    private void placeExpandedGraphChartsOnDashboard(
            int dashboardId,
            int circularChartId,
            int forceChartId) {
        JsonObject circularMeta = new JsonObject();
        circularMeta.addProperty("chartId", Integer.valueOf(circularChartId));
        circularMeta.addProperty("height", Integer.valueOf(50));
        circularMeta.addProperty("sliceName", "ILLUSTRATIVE expanded synthetic graph layout=circular");
        circularMeta.addProperty("width", Integer.valueOf(12));
        JsonObject circularNode = new JsonObject();
        circularNode.add("children", new JsonArray());
        circularNode.addProperty("id", "CHART-expanded-graph-circular");
        circularNode.add("meta", circularMeta);
        circularNode.addProperty("type", "CHART");

        JsonObject forceMeta = new JsonObject();
        forceMeta.addProperty("chartId", Integer.valueOf(forceChartId));
        forceMeta.addProperty("height", Integer.valueOf(50));
        forceMeta.addProperty("sliceName", "ILLUSTRATIVE expanded synthetic graph layout=force");
        forceMeta.addProperty("width", Integer.valueOf(12));
        JsonObject forceNode = new JsonObject();
        forceNode.add("children", new JsonArray());
        forceNode.addProperty("id", "CHART-expanded-graph-force");
        forceNode.add("meta", forceMeta);
        forceNode.addProperty("type", "CHART");

        JsonArray row1Children = new JsonArray();
        row1Children.add("CHART-expanded-graph-circular");
        JsonObject row1Meta = new JsonObject();
        row1Meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject row1 = new JsonObject();
        row1.add("children", row1Children);
        row1.addProperty("id", "ROW-expanded-graph-circular");
        row1.add("meta", row1Meta);
        row1.addProperty("type", "ROW");

        JsonArray row2Children = new JsonArray();
        row2Children.add("CHART-expanded-graph-force");
        JsonObject row2Meta = new JsonObject();
        row2Meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject row2 = new JsonObject();
        row2.add("children", row2Children);
        row2.addProperty("id", "ROW-expanded-graph-force");
        row2.add("meta", row2Meta);
        row2.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-expanded-graph-circular");
        gridChildren.add("ROW-expanded-graph-force");
        JsonObject gridNode = new JsonObject();
        gridNode.add("children", gridChildren);
        gridNode.addProperty("id", "GRID_ID");
        gridNode.addProperty("type", "GRID");

        JsonArray rootChildren = new JsonArray();
        rootChildren.add("GRID_ID");
        JsonObject rootNode = new JsonObject();
        rootNode.add("children", rootChildren);
        rootNode.addProperty("id", "ROOT_ID");
        rootNode.addProperty("type", "ROOT");

        JsonObject position = new JsonObject();
        position.add("CHART-expanded-graph-circular", circularNode);
        position.add("CHART-expanded-graph-force", forceNode);
        position.add("ROW-expanded-graph-circular", row1);
        position.add("ROW-expanded-graph-force", row2);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("expanded.dashboard.position", response);
    }

    private Integer createEdgeDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_cgo_now_axis_edges");
        body.addProperty("sql", EDGE_SQL);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("graph.dataset.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dataset/ graph edges");
            return null;
        }
        return this.readId(response.body);
    }

    private boolean postGraphChartData(int datasetId) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray groupby = new JsonArray();
        groupby.add("source_fact_id");
        groupby.add("target_fact_id");

        JsonArray metrics = new JsonArray();
        metrics.add("count");

        JsonObject query = new JsonObject();
        query.add("groupby", groupby);
        query.add("metrics", metrics);
        query.addProperty("row_limit", Integer.valueOf(10000));

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("CgoObservabilityIT.graph.chart_data.request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("graph.chart_data.results", response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/data graph results");
            return false;
        }
        return true;
    }

    private Integer createGraphDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE CGO NOW Axis graph (not Motion records)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("graph.dashboard.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dashboard/ graph");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createGraphChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "graph_chart");
        params.addProperty("source", "source_fact_id");
        params.addProperty("target", "target_fact_id");
        params.addProperty("metric", "count");
        params.add("adhoc_filters", new JsonArray());
        params.addProperty("row_limit", Integer.valueOf(10000));
        params.addProperty("color_scheme", "supersetColors");
        params.addProperty("show_legend", Boolean.TRUE);
        params.addProperty("legendType", "scroll");
        params.addProperty("legendOrientation", "top");
        params.addProperty("layout", "circular");
        params.addProperty("edgeSymbol", "none,arrow");
        params.addProperty("roam", Boolean.TRUE);
        params.addProperty("selectedMode", "single");
        params.addProperty("baseNodeSize", Integer.valueOf(20));
        params.addProperty("baseEdgeWidth", Integer.valueOf(3));
        params.addProperty("edgeLength", Integer.valueOf(400));
        params.addProperty("gravity", Double.valueOf(0.3));
        params.addProperty("repulsion", Integer.valueOf(1000));
        params.addProperty("friction", Double.valueOf(0.2));

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE CGO NOW Axis graph (not Motion records)");
        body.addProperty("viz_type", "graph_chart");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("graph.chart.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/ graph_chart");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeGraphChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE CGO NOW Axis graph (not Motion records)");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-illustrative-cgo-now-graph");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-illustrative-cgo-now-graph");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-illustrative-cgo-now-graph");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-illustrative-cgo-now-graph");
        JsonObject gridNode = new JsonObject();
        gridNode.add("children", gridChildren);
        gridNode.addProperty("id", "GRID_ID");
        gridNode.addProperty("type", "GRID");

        JsonArray rootChildren = new JsonArray();
        rootChildren.add("GRID_ID");
        JsonObject rootNode = new JsonObject();
        rootNode.add("children", rootChildren);
        rootNode.addProperty("id", "ROOT_ID");
        rootNode.addProperty("type", "ROOT");

        JsonObject position = new JsonObject();
        position.add("CHART-illustrative-cgo-now-graph", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-illustrative-cgo-now-graph", rowNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("graph.dashboard.position", response);
    }

    private Integer createAxisDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_cgo_now_axis_facts");
        body.addProperty("sql", AXIS_SQL);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("dataset.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dataset/");
            return null;
        }
        return this.readId(response.body);
    }

    private void refreshDataset(int datasetId) {
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dataset/" + datasetId + "/refresh",
                null,
                true);
        this.logResponse("dataset.refresh", response);
    }

    private JsonObject readDataset(int datasetId) {
        HttpResponse response = this.request("GET", "/api/v1/dataset/" + datasetId, null, true);
        this.logResponse("dataset.get", response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/dataset/{pk}");
            return null;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            Console.log("CgoObservabilityIT.blocker", "dataset get missing result");
            return null;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("id") != null && !result.get("id").isJsonNull()) {
            Console.log("CgoObservabilityIT.dataset.id", String.valueOf(result.get("id").getAsInt()));
        }
        return result;
    }

    private void postAxisRows(int datasetId) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray columns = new JsonArray();
        columns.add("fact_id");
        columns.add("kind");
        columns.add("attribute_name");
        columns.add("attribute_value");
        columns.add("now_state_label");
        columns.add("evidence_source");
        columns.add("record_kind");

        JsonObject query = new JsonObject();
        query.add("columns", columns);
        query.addProperty("row_limit", Integer.valueOf(100));

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("CgoObservabilityIT.chart_data.request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("chart_data.results", response);
    }

    private Integer createDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE CGO NOW Axis facts (not Motion records)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("dashboard.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/dashboard/");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createTableChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray allColumns = new JsonArray();
        allColumns.add("evidence_source");
        allColumns.add("record_kind");
        allColumns.add("now_state_label");
        allColumns.add("fact_id");
        allColumns.add("kind");
        allColumns.add("attribute_name");
        allColumns.add("attribute_value");

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "table");
        params.addProperty("query_mode", "raw");
        params.add("groupby", new JsonArray());
        params.add("all_columns", allColumns);
        params.add("percent_metrics", new JsonArray());
        params.add("adhoc_filters", new JsonArray());
        params.addProperty("row_limit", Integer.valueOf(1000));
        params.addProperty("server_page_length", Integer.valueOf(10));
        params.addProperty("order_desc", Boolean.TRUE);

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE CGO NOW Axis Fact Explorer (not Motion records)");
        body.addProperty("viz_type", "table");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("chart.create", response);
        if (response.status != 201) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/chart/");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE CGO NOW Axis Fact Explorer (not Motion records)");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-illustrative-cgo-now-axis");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-illustrative-cgo-now-axis");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-illustrative-cgo-now-axis");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-illustrative-cgo-now-axis");
        JsonObject gridNode = new JsonObject();
        gridNode.add("children", gridChildren);
        gridNode.addProperty("id", "GRID_ID");
        gridNode.addProperty("type", "GRID");

        JsonArray rootChildren = new JsonArray();
        rootChildren.add("GRID_ID");
        JsonObject rootNode = new JsonObject();
        rootNode.add("children", rootChildren);
        rootNode.addProperty("id", "ROOT_ID");
        rootNode.addProperty("type", "ROOT");

        JsonObject position = new JsonObject();
        position.add("CHART-illustrative-cgo-now-axis", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-illustrative-cgo-now-axis", rowNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("dashboard.position", response);
    }

    private boolean login() {
        JsonObject body = new JsonObject();
        body.addProperty("username", "admin");
        body.addProperty("password", "admin");
        body.addProperty("provider", "db");
        body.addProperty("refresh", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/security/login", body.toString(), false);
        this.logResponse("security.login", response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "POST /api/v1/security/login");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("access_token") == null || json.get("access_token").isJsonNull()) {
            Console.log("CgoObservabilityIT.blocker", "login response missing access_token");
            return false;
        }
        this.accessToken = json.get("access_token").getAsString();
        Console.log("CgoObservabilityIT.access_token.present", "true");
        return true;
    }

    private boolean loadCsrf() {
        HttpResponse response = this.request("GET", "/api/v1/security/csrf_token/", null, true);
        this.logResponse("security.csrf_token", response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/security/csrf_token/");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || json.get("result").isJsonNull()) {
            Console.log("CgoObservabilityIT.blocker", "csrf response missing result");
            return false;
        }
        this.csrfToken = json.get("result").getAsString();
        Console.log("CgoObservabilityIT.csrf.present", "true");
        return true;
    }

    private Integer readExamplesDatabaseId() {
        HttpResponse response = this.request("GET", "/api/v1/database/", null, true);
        this.logResponse("database.list", response);
        if (response.status != 200) {
            Console.log("CgoObservabilityIT.blocker", "GET /api/v1/database/");
            return null;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonArray()) {
            return null;
        }
        JsonArray results = json.getAsJsonArray("result");
        int i = 0;
        while (i < results.size()) {
            JsonObject row = results.get(i).getAsJsonObject();
            Console.log("CgoObservabilityIT.database.row", row.toString());
            if (row.get("id") != null
                    && !row.get("id").isJsonNull()
                    && row.get("database_name") != null
                    && "examples".equals(row.get("database_name").getAsString())) {
                return Integer.valueOf(row.get("id").getAsInt());
            }
            i = i + 1;
        }
        return null;
    }

    private void readDashboard(int dashboardId) {
        HttpResponse response = this.request("GET", "/api/v1/dashboard/" + dashboardId, null, true);
        this.logResponse("dashboard.get", response);
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("id") != null && !result.get("id").isJsonNull()) {
            Console.log("CgoObservabilityIT.dashboard.id", String.valueOf(result.get("id").getAsInt()));
        }
        if (result.get("url") != null && !result.get("url").isJsonNull()) {
            Console.log("CgoObservabilityIT.dashboard.path", result.get("url").getAsString());
            Console.log("CgoObservabilityIT.dashboard.url", BASE_URL + result.get("url").getAsString());
        } else {
            Console.log(
                    "CgoObservabilityIT.dashboard.url",
                    BASE_URL + "/superset/dashboard/" + dashboardId + "/");
        }
    }

    private void readChart(int chartId) {
        HttpResponse response = this.request("GET", "/api/v1/chart/" + chartId, null, true);
        this.logResponse("chart.get", response);
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("id") != null && !result.get("id").isJsonNull()) {
            Console.log("CgoObservabilityIT.chart.id", String.valueOf(result.get("id").getAsInt()));
        }
        if (result.get("url") != null && !result.get("url").isJsonNull()) {
            Console.log("CgoObservabilityIT.chart.url", BASE_URL + result.get("url").getAsString());
        }
    }

    private void logExistingResource(String label, String path) {
        HttpResponse response = this.request("GET", path, null, true);
        this.logResponse(label, response);
    }

    private Integer readId(String body) {
        JsonObject json = this.parseObject(body);
        if (json == null || json.get("id") == null || json.get("id").isJsonNull()) {
            Console.log("CgoObservabilityIT.blocker", "response missing id");
            return null;
        }
        int id = json.get("id").getAsInt();
        Console.log("CgoObservabilityIT.created.id", String.valueOf(id));
        return Integer.valueOf(id);
    }

    private void logResponse(String label, HttpResponse response) {
        Console.log("CgoObservabilityIT." + label + ".status", String.valueOf(response.status));
        Console.log("CgoObservabilityIT." + label + ".body", response.body);
        if (response.setCookie != null) {
            Console.log("CgoObservabilityIT." + label + ".set_cookie", response.setCookie);
        }
    }

    private JsonObject parseObject(String body) {
        if (body == null) {
            return null;
        }
        JsonElement element = JsonParser.parseString(body);
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        return element.getAsJsonObject();
    }

    private HttpResponse request(String method, String path, String jsonBody, boolean authed) {
        HttpResponse result = new HttpResponse();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Referer", BASE_URL + "/");
            if (authed && this.accessToken != null) {
                connection.setRequestProperty("Authorization", "Bearer " + this.accessToken);
            }
            if (authed && this.csrfToken != null) {
                connection.setRequestProperty("X-CSRFToken", this.csrfToken);
            }
            if (this.sessionCookie != null) {
                connection.setRequestProperty("Cookie", this.sessionCookie);
            }
            if (jsonBody != null) {
                byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
                OutputStream output = connection.getOutputStream();
                output.write(bytes);
                output.flush();
                output.close();
            }
            result.status = connection.getResponseCode();
            String setCookie = connection.getHeaderField("Set-Cookie");
            result.setCookie = setCookie;
            if (setCookie != null) {
                int cut = setCookie.indexOf(';');
                if (cut > 0) {
                    this.sessionCookie = setCookie.substring(0, cut);
                } else {
                    this.sessionCookie = setCookie;
                }
            }
            InputStream stream;
            if (result.status >= 400) {
                stream = connection.getErrorStream();
            } else {
                stream = connection.getInputStream();
            }
            result.body = this.readStream(stream);
        } catch (Exception e) {
            result.status = -1;
            result.body = String.valueOf(e);
            Console.log("CgoObservabilityIT.request.exception", String.valueOf(e));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    private String readStream(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n = stream.read(chunk);
        while (n >= 0) {
            buffer.write(chunk, 0, n);
            n = stream.read(chunk);
        }
        stream.close();
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private static class HttpResponse {
        private int status;
        private String body;
        private String setCookie;
    }
}
