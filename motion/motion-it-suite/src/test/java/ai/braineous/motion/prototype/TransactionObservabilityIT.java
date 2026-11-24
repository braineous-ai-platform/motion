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
public class TransactionObservabilityIT {

    private static final String BASE_URL = "http://localhost:8088";

    private static final String SAMPLE_SQL =
            "SELECT 'Run 1' AS run_label, 'SUCCESS' AS outcome, 48 AS tm_count "
                    + "UNION ALL SELECT 'Run 1', 'FAILURE', 2 "
                    + "UNION ALL SELECT 'Run 2', 'SUCCESS', 46 "
                    + "UNION ALL SELECT 'Run 2', 'FAILURE', 4 "
                    + "UNION ALL SELECT 'Run 3', 'SUCCESS', 49 "
                    + "UNION ALL SELECT 'Run 3', 'FAILURE', 1";

    private String accessToken;

    private String csrfToken;

    private String sessionCookie;

    @Test
    public void test_1() {
        Console.log("TransactionObservabilityIT.base_url", BASE_URL);
        Console.log("TransactionObservabilityIT.sample.sql", SAMPLE_SQL);

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }

        Integer databaseId = this.readExamplesDatabaseId();
        if (databaseId == null) {
            Console.log(
                    "TransactionObservabilityIT.blocker",
                    "no examples database id from GET /api/v1/database/");
            return;
        }

        Integer datasetId = this.createVirtualDataset(databaseId.intValue());
        if (datasetId == null) {
            return;
        }

        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }

        Integer dashboardId = this.createDashboard();
        if (dashboardId == null) {
            return;
        }

        Integer chartId = this.createChart(datasetId.intValue(), dataset, dashboardId.intValue());
        if (chartId == null) {
            return;
        }

        this.placeChartOnDashboard(dashboardId.intValue(), chartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(chartId.intValue());
    }

    @Test
    public void test_2() {
        Console.log("TransactionObservabilityIT.test_2.goal",
                "evaluate native drill-down from chart 122 FAILURE bars to transaction-level records");
        Console.log("TransactionObservabilityIT.test_2.preserve.dataset", "26");
        Console.log("TransactionObservabilityIT.test_2.preserve.chart", "122");
        Console.log("TransactionObservabilityIT.test_2.preserve.dashboard", "12");

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }

        if (!this.inspectPreservedResources()) {
            return;
        }

        this.getDatasetDrillInfo(26);
        this.getDatasetRelatedObjects(26);
        this.getDatabaseDrillFlag(1);
        this.getDashboardCharts(12);
        this.getChartStoredData(122);
        this.getExploreSlice(122);
        this.getOutcomeColumnValues(26);
        this.postChartDataDrillDetail(
                26,
                "run_label",
                "outcome",
                "tm_count",
                "existing_summary");

        Console.log(
                "TransactionObservabilityIT.test_2.observation.grain",
                "dataset 26 drill_info columns are run_label, outcome, tm_count only; native drill_detail can return those aggregated FAILURE rows, not individual transaction executions");
        Console.log(
                "TransactionObservabilityIT.test_2.observation.click_api",
                "OpenAPI has no endpoint that simulates selecting a bar on dashboard 12; PUT /api/v1/dashboard/12/filters would alter dashboard 12 and is skipped");

        Integer evidenceDatasetId = this.createIllustrativeEvidenceDataset();
        if (evidenceDatasetId == null) {
            return;
        }
        this.refreshDataset(evidenceDatasetId.intValue());
        JsonObject evidenceDataset = this.readDataset(evidenceDatasetId.intValue());
        if (evidenceDataset == null) {
            return;
        }

        this.postChartDataDrillDetail(
                evidenceDatasetId.intValue(),
                "run_label",
                "outcome",
                "evidence_id",
                "illustrative_evidence");

        Integer evidenceDashboardId = this.createEvidenceDashboard();
        if (evidenceDashboardId == null) {
            return;
        }
        Integer evidenceChartId = this.createEvidenceTableChart(
                evidenceDatasetId.intValue(),
                evidenceDashboardId.intValue());
        if (evidenceChartId == null) {
            return;
        }
        this.placeEvidenceChartOnDashboard(
                evidenceDashboardId.intValue(),
                evidenceChartId.intValue());
        this.readDashboard(evidenceDashboardId.intValue());
        this.readChart(evidenceChartId.intValue());
        this.confirmPreservedResourcesUnchanged();
    }

    @Test
    public void test_3() {
        Console.log(
                "TransactionObservabilityIT.test_3.goal",
                "evaluate whether native Superset table viz can display complete individual illustrative cognitive transaction evidence");
        Console.log(
                "TransactionObservabilityIT.test_3.disclaimer",
                "ILLUSTRATIVE rows only; record_kind=NOT_A_MOTION_RECORD; not sourced from Motion execution");

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }

        this.logExistingResource("test_3.existing.chart_122", "/api/v1/chart/122");
        this.logExistingResource("test_3.existing.dataset_26", "/api/v1/dataset/26");
        this.logExistingResource("test_3.existing.dashboard_12", "/api/v1/dashboard/12");
        this.logExistingResource("test_3.existing.chart_123", "/api/v1/chart/123");
        this.logExistingResource("test_3.existing.dataset_27", "/api/v1/dataset/27");
        this.logExistingResource("test_3.existing.dashboard_13", "/api/v1/dashboard/13");

        Integer datasetId = this.createCompleteEvidenceDataset();
        if (datasetId == null) {
            return;
        }
        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        this.logExistingResource(
                "test_3.dataset.drill_info",
                "/api/v1/dataset/" + datasetId.intValue() + "/drill_info/");
        this.postCompleteEvidenceRows(datasetId.intValue());

        Integer dashboardId = this.createCompleteEvidenceDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer chartId = this.createCompleteEvidenceTableChart(
                datasetId.intValue(),
                dashboardId.intValue());
        if (chartId == null) {
            return;
        }
        this.placeCompleteEvidenceChartOnDashboard(
                dashboardId.intValue(),
                chartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(chartId.intValue());
        this.confirmEarlierResourcesUntouched();
    }

    @Test
    public void test_4() {
        Console.log(
                "TransactionObservabilityIT.test_4.goal",
                "evaluate native time-series viz and temporal filtering for illustrative TM SUCCESS/FAILURE");
        Console.log(
                "TransactionObservabilityIT.test_4.disclaimer",
                "ILLUSTRATIVE rows only; record_kind=NOT_A_MOTION_RECORD; not sourced from Motion execution");

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }

        this.logExistingResource("test_4.existing.chart_122", "/api/v1/chart/122");
        this.logExistingResource("test_4.existing.dashboard_12", "/api/v1/dashboard/12");
        this.logExistingResource("test_4.existing.dataset_26", "/api/v1/dataset/26");
        this.logExistingResource("test_4.existing.chart_123", "/api/v1/chart/123");
        this.logExistingResource("test_4.existing.dashboard_13", "/api/v1/dashboard/13");
        this.logExistingResource("test_4.existing.dataset_27", "/api/v1/dataset/27");
        this.logExistingResource("test_4.existing.chart_124", "/api/v1/chart/124");
        this.logExistingResource("test_4.existing.dashboard_14", "/api/v1/dashboard/14");
        this.logExistingResource("test_4.existing.dataset_28", "/api/v1/dataset/28");
        this.logExistingResource("test_4.contract.chart_101", "/api/v1/chart/101");

        Integer datasetId = this.createTimeseriesDataset();
        if (datasetId == null) {
            return;
        }
        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        this.ensureEventTsIsTemporal(datasetId.intValue(), dataset);
        dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        this.logEventTsColumn(dataset);

        this.postTimeseriesChartData(datasetId.intValue(), "No filter", "unfiltered");
        this.postTimeseriesChartData(
                datasetId.intValue(),
                "2026-01-01T00:00:00 : 2026-02-01T00:00:00",
                "january");

        Integer dashboardId = this.createTimeseriesDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer chartId = this.createTimeseriesChart(datasetId.intValue(), dashboardId.intValue());
        if (chartId == null) {
            return;
        }
        this.placeTimeseriesChartOnDashboard(dashboardId.intValue(), chartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(chartId.intValue());
        this.confirmTest4EarlierResourcesUntouched();
    }

    @Test
    public void test_5() {
        Console.log(
                "TransactionObservabilityIT.test_5.goal",
                "evaluate native table viz of illustrative SystemMutation records and publication evidence");
        Console.log(
                "TransactionObservabilityIT.test_5.disclaimer",
                "ILLUSTRATIVE rows only; record_kind=NOT_A_MOTION_RECORD; not sourced from Motion execution or Kafka");

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }

        this.logExistingResource("test_5.existing.dataset_26", "/api/v1/dataset/26");
        this.logExistingResource("test_5.existing.chart_122", "/api/v1/chart/122");
        this.logExistingResource("test_5.existing.dashboard_12", "/api/v1/dashboard/12");
        this.logExistingResource("test_5.existing.dataset_27", "/api/v1/dataset/27");
        this.logExistingResource("test_5.existing.chart_123", "/api/v1/chart/123");
        this.logExistingResource("test_5.existing.dashboard_13", "/api/v1/dashboard/13");
        this.logExistingResource("test_5.existing.dataset_28", "/api/v1/dataset/28");
        this.logExistingResource("test_5.existing.chart_124", "/api/v1/chart/124");
        this.logExistingResource("test_5.existing.dashboard_14", "/api/v1/dashboard/14");
        this.logExistingResource("test_5.existing.dataset_29", "/api/v1/dataset/29");
        this.logExistingResource("test_5.existing.chart_125", "/api/v1/chart/125");
        this.logExistingResource("test_5.existing.dashboard_15", "/api/v1/dashboard/15");

        Integer datasetId = this.createMutationEvidenceDataset();
        if (datasetId == null) {
            return;
        }
        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        this.postMutationRows(datasetId.intValue(), null, "all");
        this.postMutationRows(datasetId.intValue(), "FAILURE", "failure");
        this.postMutationRows(datasetId.intValue(), "SUCCESS", "success");

        Integer dashboardId = this.createMutationEvidenceDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer chartId = this.createMutationEvidenceTableChart(
                datasetId.intValue(),
                dashboardId.intValue());
        if (chartId == null) {
            return;
        }
        this.placeMutationEvidenceChartOnDashboard(
                dashboardId.intValue(),
                chartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(chartId.intValue());
        this.confirmTest5EarlierResourcesUntouched();
    }

    private Integer createTimeseriesDataset() {
        String sql =
                "SELECT 'ILLUSTRATIVE' AS evidence_source, "
                        + "'NOT_A_MOTION_RECORD' AS record_kind, "
                        + "'ILLUSTRATIVE-TX-TS-001' AS transaction_id, "
                        + "TIMESTAMP '2026-01-05 10:00:00' AS event_ts, "
                        + "'SUCCESS' AS tm_outcome "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-TX-TS-002', TIMESTAMP '2026-01-12 11:00:00', 'FAILURE' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-TX-TS-003', TIMESTAMP '2026-01-20 09:00:00', 'SUCCESS' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-TX-TS-004', TIMESTAMP '2026-02-03 14:00:00', 'SUCCESS' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-TX-TS-005', TIMESTAMP '2026-02-14 16:00:00', 'FAILURE' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-TX-TS-006', TIMESTAMP '2026-02-28 08:00:00', 'SUCCESS' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-TX-TS-007', TIMESTAMP '2026-03-08 13:00:00', 'FAILURE' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-TX-TS-008', TIMESTAMP '2026-03-22 15:00:00', 'SUCCESS'";

        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(1));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_tm_timeseries");
        body.addProperty("sql", sql);
        Console.log("TransactionObservabilityIT.test_4.dataset.sql", sql);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("test_4.dataset.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dataset/ timeseries");
            return null;
        }
        return this.readId(response.body);
    }

    private void ensureEventTsIsTemporal(int datasetId, JsonObject dataset) {
        boolean eventTsIsDttm = false;
        JsonArray columns = null;
        if (dataset.get("columns") != null && dataset.get("columns").isJsonArray()) {
            columns = dataset.getAsJsonArray("columns");
        }
        if (columns != null) {
            int i = 0;
            while (i < columns.size()) {
                JsonObject column = columns.get(i).getAsJsonObject();
                if (column.get("column_name") != null
                        && "event_ts".equals(column.get("column_name").getAsString())) {
                    if (column.get("is_dttm") != null
                            && !column.get("is_dttm").isJsonNull()
                            && column.get("is_dttm").getAsBoolean()) {
                        eventTsIsDttm = true;
                    }
                }
                i = i + 1;
            }
        }
        Console.log(
                "TransactionObservabilityIT.test_4.event_ts.is_dttm.before_put",
                String.valueOf(eventTsIsDttm));
        if (eventTsIsDttm) {
            return;
        }
        if (columns == null) {
            Console.log("TransactionObservabilityIT.blocker", "dataset  columns missing; cannot mark event_ts temporal");
            return;
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
            if ("event_ts".equals(existing.get("column_name").getAsString())) {
                putColumn.addProperty("is_dttm", Boolean.TRUE);
            } else if (existing.get("is_dttm") != null && !existing.get("is_dttm").isJsonNull()) {
                putColumn.addProperty("is_dttm", existing.get("is_dttm").getAsBoolean());
            }
            putColumns.add(putColumn);
            j = j + 1;
        }
        JsonObject body = new JsonObject();
        body.add("columns", putColumns);
        body.addProperty("main_dttm_col", "event_ts");
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dataset/" + datasetId,
                body.toString(),
                true);
        this.logResponse("test_4.dataset.mark_temporal", response);
    }

    private void logEventTsColumn(JsonObject dataset) {
        if (dataset.get("main_dttm_col") != null && !dataset.get("main_dttm_col").isJsonNull()) {
            Console.log(
                    "TransactionObservabilityIT.test_4.main_dttm_col",
                    dataset.get("main_dttm_col").getAsString());
        }
        if (dataset.get("columns") == null || !dataset.get("columns").isJsonArray()) {
            return;
        }
        JsonArray columns = dataset.getAsJsonArray("columns");
        int i = 0;
        while (i < columns.size()) {
            JsonObject column = columns.get(i).getAsJsonObject();
            Console.log(
                    "TransactionObservabilityIT.test_4.dataset.column",
                    column.toString());
            i = i + 1;
        }
    }

    private void postTimeseriesChartData(int datasetId, String temporalRange, String label) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonObject extras = new JsonObject();
        extras.addProperty("time_grain_sqla", "P1M");

        JsonArray groupby = new JsonArray();
        groupby.add("tm_outcome");

        JsonArray metrics = new JsonArray();
        metrics.add("count");

        JsonObject temporalFilter = new JsonObject();
        temporalFilter.addProperty("col", "event_ts");
        temporalFilter.addProperty("op", "TEMPORAL_RANGE");
        temporalFilter.addProperty("val", temporalRange);

        JsonArray filters = new JsonArray();
        filters.add(temporalFilter);

        JsonObject query = new JsonObject();
        query.addProperty("granularity", "event_ts");
        query.addProperty("is_timeseries", Boolean.TRUE);
        query.add("groupby", groupby);
        query.add("metrics", metrics);
        query.add("filters", filters);
        query.add("extras", extras);
        query.addProperty("time_range", temporalRange);
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
                "TransactionObservabilityIT.test_4.chart_data." + label + ".request",
                body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_4.chart_data." + label, response);
    }

    private Integer createTimeseriesDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE TM SUCCESS/FAILURE time series (not Motion records)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("test_4.dashboard.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dashboard/ timeseries");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createTimeseriesChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray groupby = new JsonArray();
        groupby.add("tm_outcome");

        JsonArray metrics = new JsonArray();
        metrics.add("count");

        JsonObject temporalFilter = new JsonObject();
        temporalFilter.addProperty("clause", "WHERE");
        temporalFilter.addProperty("subject", "event_ts");
        temporalFilter.addProperty("operator", "TEMPORAL_RANGE");
        temporalFilter.addProperty("comparator", "No filter");
        temporalFilter.addProperty("expressionType", "SIMPLE");

        JsonArray adhocFilters = new JsonArray();
        adhocFilters.add(temporalFilter);

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "echarts_timeseries_bar");
        params.addProperty("x_axis", "event_ts");
        params.addProperty("time_grain_sqla", "P1M");
        params.add("metrics", metrics);
        params.add("groupby", groupby);
        params.add("adhoc_filters", adhocFilters);
        params.addProperty("orientation", "vertical");
        params.addProperty("show_legend", Boolean.TRUE);
        params.addProperty("color_scheme", "supersetColors");
        params.addProperty("row_limit", Integer.valueOf(10000));
        params.addProperty("y_axis_format", "SMART_NUMBER");

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty(
                "slice_name",
                "ILLUSTRATIVE TM SUCCESS vs FAILURE over time (not Motion records)");
        body.addProperty("viz_type", "echarts_timeseries_bar");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_4.chart.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/chart/ timeseries");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeTimeseriesChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE TM SUCCESS vs FAILURE over time (not Motion records)");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-illustrative-tm-timeseries");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-illustrative-tm-timeseries");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-illustrative-tm-timeseries");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-illustrative-tm-timeseries");
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
        position.add("CHART-illustrative-tm-timeseries", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-illustrative-tm-timeseries", rowNode);
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

    private void confirmTest4EarlierResourcesUntouched() {
        this.logExistingResource("test_4.preserve.dataset_26.related", "/api/v1/dataset/26/related_objects");
        this.logExistingResource("test_4.preserve.dataset_27.related", "/api/v1/dataset/27/related_objects");
        this.logExistingResource("test_4.preserve.dataset_28.related", "/api/v1/dataset/28/related_objects");
        this.logExistingResource("test_4.preserve.dashboard_12", "/api/v1/dashboard/12");
        this.logExistingResource("test_4.preserve.dashboard_13", "/api/v1/dashboard/13");
        this.logExistingResource("test_4.preserve.dashboard_14", "/api/v1/dashboard/14");
    }

    private Integer createMutationEvidenceDataset() {
        String sql =
                "SELECT 'ILLUSTRATIVE' AS evidence_source, "
                        + "'NOT_A_MOTION_RECORD' AS record_kind, "
                        + "'ILLUSTRATIVE-MUT-001' AS mutation_id, "
                        + "'ILLUSTRATIVE-TX-SUCCESS-001' AS source_transaction_id, "
                        + "'pay_v1' AS mutation_query_name, "
                        + "'SUCCESS' AS tm_outcome, "
                        + "'{\"mutationId\":\"ILLUSTRATIVE-MUT-001\",\"source\":\"ILLUSTRATIVE\",\"kind\":\"SystemMutation\"}' AS mutation_json, "
                        + "'illustrative_mutation_event' AS publication_topic, "
                        + "'published' AS publication_status, "
                        + "'ILLUSTRATIVE Kafka-like send of mutation JSON; not a Motion Emitter payload' AS publication_evidence "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-MUT-002', "
                        + "'ILLUSTRATIVE-TX-FAILURE-001', "
                        + "'pay_v1', "
                        + "'FAILURE', "
                        + "'{\"mutationId\":\"ILLUSTRATIVE-MUT-002\",\"source\":\"ILLUSTRATIVE\",\"kind\":\"SystemMutation\",\"tm\":\"FAILURE\"}', "
                        + "'illustrative_mutation_event', "
                        + "'published', "
                        + "'ILLUSTRATIVE publication evidence for FAILURE mutation; not a Motion record' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-MUT-003', "
                        + "'ILLUSTRATIVE-TX-SUCCESS-002', "
                        + "'jit_decision_v14', "
                        + "'SUCCESS', "
                        + "'{\"mutationId\":\"ILLUSTRATIVE-MUT-003\",\"source\":\"ILLUSTRATIVE\",\"kind\":\"SystemMutation\"}', "
                        + "'', "
                        + "'not_published', "
                        + "''";

        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(1));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_system_mutation_evidence");
        body.addProperty("sql", sql);
        Console.log("TransactionObservabilityIT.test_5.dataset.sql", sql);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("test_5.dataset.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dataset/ mutation evidence");
            return null;
        }
        return this.readId(response.body);
    }

    private void postMutationRows(int datasetId, String tmOutcomeFilter, String label) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray columns = new JsonArray();
        columns.add("mutation_id");
        columns.add("source_transaction_id");
        columns.add("mutation_query_name");
        columns.add("tm_outcome");
        columns.add("mutation_json");
        columns.add("publication_topic");
        columns.add("publication_status");
        columns.add("publication_evidence");
        columns.add("evidence_source");
        columns.add("record_kind");

        JsonObject query = new JsonObject();
        query.add("columns", columns);
        query.addProperty("row_limit", Integer.valueOf(100));
        if (tmOutcomeFilter != null) {
            JsonObject filter = new JsonObject();
            filter.addProperty("col", "tm_outcome");
            filter.addProperty("op", "==");
            filter.addProperty("val", tmOutcomeFilter);
            JsonArray filters = new JsonArray();
            filters.add(filter);
            query.add("filters", filters);
        }

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "results");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("TransactionObservabilityIT.test_5.chart_data." + label + ".request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_5.chart_data." + label, response);
    }

    private Integer createMutationEvidenceDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE SystemMutation evidence (not Motion records)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("test_5.dashboard.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dashboard/ mutation evidence");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createMutationEvidenceTableChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray allColumns = new JsonArray();
        allColumns.add("evidence_source");
        allColumns.add("record_kind");
        allColumns.add("mutation_id");
        allColumns.add("source_transaction_id");
        allColumns.add("mutation_query_name");
        allColumns.add("tm_outcome");
        allColumns.add("mutation_json");
        allColumns.add("publication_topic");
        allColumns.add("publication_status");
        allColumns.add("publication_evidence");

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
                "ILLUSTRATIVE SystemMutation records (not Motion records)");
        body.addProperty("viz_type", "table");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_5.chart.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/chart/ mutation evidence");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeMutationEvidenceChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE SystemMutation records (not Motion records)");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-illustrative-system-mutation");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-illustrative-system-mutation");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-illustrative-system-mutation");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-illustrative-system-mutation");
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
        position.add("CHART-illustrative-system-mutation", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-illustrative-system-mutation", rowNode);
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

    private void confirmTest5EarlierResourcesUntouched() {
        this.logExistingResource("test_5.preserve.dataset_26.related", "/api/v1/dataset/26/related_objects");
        this.logExistingResource("test_5.preserve.dataset_27.related", "/api/v1/dataset/27/related_objects");
        this.logExistingResource("test_5.preserve.dataset_28.related", "/api/v1/dataset/28/related_objects");
        this.logExistingResource("test_5.preserve.dataset_29.related", "/api/v1/dataset/29/related_objects");
        this.logExistingResource("test_5.preserve.dashboard_12", "/api/v1/dashboard/12");
        this.logExistingResource("test_5.preserve.dashboard_15", "/api/v1/dashboard/15");
    }

    private void logExistingResource(String label, String path) {
        HttpResponse response = this.request("GET", path, null, true);
        this.logResponse(label, response);
    }

    private Integer createCompleteEvidenceDataset() {
        String sql =
                "SELECT 'ILLUSTRATIVE' AS evidence_source, "
                        + "'NOT_A_MOTION_RECORD' AS record_kind, "
                        + "'ILLUSTRATIVE-TX-SUCCESS-001' AS transaction_id, "
                        + "'jit_decision_v14' AS query_name, "
                        + "'SUCCESS' AS tm_outcome, "
                        + "'Inventory:INV-001' AS fact_axis, "
                        + "'Sales:SALE-001,Vendor:VENDOR-001,Product:PROD-SHAMPOO-250,Location:LOC-001' AS related_facts, "
                        + "'{\"source\":\"ILLUSTRATIVE\",\"kind\":\"Nexus Context\",\"facts\":[\"Inventory:INV-001\"]}' AS nexus_context, "
                        + "'success' AS cognitive_result, "
                        + "'' AS tm_rejection_evidence "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', "
                        + "'ILLUSTRATIVE-TX-FAILURE-001', "
                        + "'jit_decision_v14', "
                        + "'FAILURE', "
                        + "'Inventory:INV-001', "
                        + "'Sales:SALE-002,Vendor:VENDOR-001,Product:PROD-SHAMPOO-250,Location:LOC-001', "
                        + "'{\"source\":\"ILLUSTRATIVE\",\"kind\":\"Nexus Context\",\"facts\":[\"Inventory:INV-001\",\"Sales:SALE-002\"]}', "
                        + "'fail', "
                        + "'WHY_CODE_RESULT_NOT_SUCCESS; illustrative rejection only; not a Motion TxExecutionResult'";

        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(1));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_complete_tm_evidence");
        body.addProperty("sql", sql);

        Console.log("TransactionObservabilityIT.test_3.dataset.sql", sql);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("test_3.dataset.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dataset/ complete evidence");
            return null;
        }
        return this.readId(response.body);
    }

    private void postCompleteEvidenceRows(int datasetId) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray columns = new JsonArray();
        columns.add("transaction_id");
        columns.add("query_name");
        columns.add("tm_outcome");
        columns.add("fact_axis");
        columns.add("related_facts");
        columns.add("nexus_context");
        columns.add("cognitive_result");
        columns.add("tm_rejection_evidence");
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

        Console.log("TransactionObservabilityIT.test_3.chart_data.request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_3.chart_data.results", response);
    }

    private Integer createCompleteEvidenceDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty(
                "dashboard_title",
                "ILLUSTRATIVE complete TM evidence (not Motion records)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("test_3.dashboard.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dashboard/ complete evidence");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createCompleteEvidenceTableChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray allColumns = new JsonArray();
        allColumns.add("evidence_source");
        allColumns.add("record_kind");
        allColumns.add("transaction_id");
        allColumns.add("query_name");
        allColumns.add("tm_outcome");
        allColumns.add("fact_axis");
        allColumns.add("related_facts");
        allColumns.add("nexus_context");
        allColumns.add("cognitive_result");
        allColumns.add("tm_rejection_evidence");

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
                "ILLUSTRATIVE complete TM evidence (not Motion records)");
        body.addProperty("viz_type", "table");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_3.chart.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/chart/ complete evidence");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeCompleteEvidenceChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty(
                "sliceName",
                "ILLUSTRATIVE complete TM evidence (not Motion records)");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-illustrative-complete-tm");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-illustrative-complete-tm");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-illustrative-complete-tm");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-illustrative-complete-tm");
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
        position.add("CHART-illustrative-complete-tm", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-illustrative-complete-tm", rowNode);
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

    private void confirmEarlierResourcesUntouched() {
        this.logExistingResource("test_3.preserve.chart_122", "/api/v1/chart/122");
        this.logExistingResource("test_3.preserve.dashboard_12", "/api/v1/dashboard/12");
        this.logExistingResource("test_3.preserve.dataset_26.related", "/api/v1/dataset/26/related_objects");
        this.logExistingResource("test_3.preserve.chart_123", "/api/v1/chart/123");
        this.logExistingResource("test_3.preserve.dashboard_13", "/api/v1/dashboard/13");
        this.logExistingResource("test_3.preserve.dataset_27.related", "/api/v1/dataset/27/related_objects");
    }

    private boolean inspectPreservedResources() {
        HttpResponse chart = this.request("GET", "/api/v1/chart/122", null, true);
        this.logResponse("test_2.chart_122.get", chart);
        if (chart.status != 200) {
            Console.log("TransactionObservabilityIT.blocker", "GET /api/v1/chart/122");
            return false;
        }
        HttpResponse dataset = this.request("GET", "/api/v1/dataset/26", null, true);
        this.logResponse("test_2.dataset_26.get", dataset);
        if (dataset.status != 200) {
            Console.log("TransactionObservabilityIT.blocker", "GET /api/v1/dataset/26");
            return false;
        }
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/12", null, true);
        this.logResponse("test_2.dashboard_12.get", dashboard);
        if (dashboard.status != 200) {
            Console.log("TransactionObservabilityIT.blocker", "GET /api/v1/dashboard/12");
            return false;
        }
        return true;
    }

    private void confirmPreservedResourcesUnchanged() {
        HttpResponse chart = this.request("GET", "/api/v1/chart/122", null, true);
        this.logResponse("test_2.preserve.chart_122", chart);
        HttpResponse dashboard = this.request("GET", "/api/v1/dashboard/12", null, true);
        this.logResponse("test_2.preserve.dashboard_12", dashboard);
        HttpResponse related = this.request("GET", "/api/v1/dataset/26/related_objects", null, true);
        this.logResponse("test_2.preserve.dataset_26.related", related);
    }

    private void getDatasetDrillInfo(int datasetId) {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/dataset/" + datasetId + "/drill_info/",
                null,
                true);
        this.logResponse("test_2.dataset." + datasetId + ".drill_info", response);
    }

    private void getDatasetRelatedObjects(int datasetId) {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/dataset/" + datasetId + "/related_objects",
                null,
                true);
        this.logResponse("test_2.dataset." + datasetId + ".related_objects", response);
    }

    private void getDatabaseDrillFlag(int databaseId) {
        HttpResponse response = this.request("GET", "/api/v1/database/" + databaseId, null, true);
        this.logResponse("test_2.database." + databaseId + ".get", response);
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("disable_drill_to_detail") == null || result.get("disable_drill_to_detail").isJsonNull()) {
            Console.log(
                    "TransactionObservabilityIT.test_2.database.disable_drill_to_detail",
                    "null");
        } else {
            Console.log(
                    "TransactionObservabilityIT.test_2.database.disable_drill_to_detail",
                    String.valueOf(result.get("disable_drill_to_detail").getAsBoolean()));
        }
    }

    private void getDashboardCharts(int dashboardId) {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/dashboard/" + dashboardId + "/charts",
                null,
                true);
        this.logResponse("test_2.dashboard." + dashboardId + ".charts", response);
    }

    private void getChartStoredData(int chartId) {
        HttpResponse full = this.request("GET", "/api/v1/chart/" + chartId + "/data/", null, true);
        this.logResponse("test_2.chart." + chartId + ".data", full);
        HttpResponse drill = this.request(
                "GET",
                "/api/v1/chart/" + chartId + "/data/?type=drill_detail",
                null,
                true);
        this.logResponse("test_2.chart." + chartId + ".data.drill_detail", drill);
    }

    private void getExploreSlice(int chartId) {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/explore/?slice_id=" + chartId,
                null,
                true);
        this.logResponse("test_2.explore.slice_" + chartId, response);
    }

    private void getOutcomeColumnValues(int datasetId) {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/datasource/table/" + datasetId + "/column/outcome/values/",
                null,
                true);
        this.logResponse("test_2.dataset." + datasetId + ".outcome.values", response);
    }

    private void postChartDataDrillDetail(
            int datasetId,
            String columnA,
            String columnB,
            String columnC,
            String label) {
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");

        JsonArray columns = new JsonArray();
        columns.add(columnA);
        columns.add(columnB);
        columns.add(columnC);

        JsonObject filter = new JsonObject();
        filter.addProperty("col", "outcome");
        filter.addProperty("op", "==");
        filter.addProperty("val", "FAILURE");

        JsonArray filters = new JsonArray();
        filters.add(filter);

        JsonObject query = new JsonObject();
        query.addProperty("result_type", "drill_detail");
        query.add("columns", columns);
        query.add("filters", filters);
        query.addProperty("row_limit", Integer.valueOf(100));

        JsonArray queries = new JsonArray();
        queries.add(query);

        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.addProperty("result_type", "drill_detail");
        body.addProperty("result_format", "json");
        body.addProperty("force", Boolean.TRUE);
        body.add("queries", queries);

        Console.log("TransactionObservabilityIT.test_2.chart_data." + label + ".request", body.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/data", body.toString(), true);
        this.logResponse("test_2.chart_data." + label, response);
    }

    private Integer createIllustrativeEvidenceDataset() {
        Console.log(
                "TransactionObservabilityIT.test_2.evidence.disclaimer",
                "ILLUSTRATIVE rows only; record_kind=NOT_A_MOTION_RECORD; not sourced from Motion execution");
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(1));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "illustrative_failed_tm_evidence");
        body.addProperty(
                "sql",
                "SELECT 'ILLUSTRATIVE' AS evidence_source, 'NOT_A_MOTION_RECORD' AS record_kind, "
                        + "'Run 1' AS run_label, 'FAILURE' AS outcome, "
                        + "'ILLUSTRATIVE-TX-R1-01' AS evidence_id, "
                        + "'illustrative_timeout' AS why_code, "
                        + "'synthetic failure row 1 of 2 for Run 1' AS detail "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'Run 1', 'FAILURE', "
                        + "'ILLUSTRATIVE-TX-R1-02', 'illustrative_validation', "
                        + "'synthetic failure row 2 of 2 for Run 1' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'Run 2', 'FAILURE', "
                        + "'ILLUSTRATIVE-TX-R2-01', 'illustrative_timeout', "
                        + "'synthetic failure row 1 of 4 for Run 2' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'Run 2', 'FAILURE', "
                        + "'ILLUSTRATIVE-TX-R2-02', 'illustrative_noise', "
                        + "'synthetic failure row 2 of 4 for Run 2' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'Run 2', 'FAILURE', "
                        + "'ILLUSTRATIVE-TX-R2-03', 'illustrative_timeout', "
                        + "'synthetic failure row 3 of 4 for Run 2' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'Run 2', 'FAILURE', "
                        + "'ILLUSTRATIVE-TX-R2-04', 'illustrative_denied', "
                        + "'synthetic failure row 4 of 4 for Run 2' "
                        + "UNION ALL SELECT 'ILLUSTRATIVE', 'NOT_A_MOTION_RECORD', 'Run 3', 'FAILURE', "
                        + "'ILLUSTRATIVE-TX-R3-01', 'illustrative_timeout', "
                        + "'synthetic failure row 1 of 1 for Run 3'");
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("test_2.evidence.dataset.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dataset/ illustrative evidence");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createEvidenceDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty("dashboard_title", "ILLUSTRATIVE failed TM evidence (not Motion records)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("test_2.evidence.dashboard.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dashboard/ illustrative evidence");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createEvidenceTableChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonArray allColumns = new JsonArray();
        allColumns.add("evidence_source");
        allColumns.add("record_kind");
        allColumns.add("run_label");
        allColumns.add("outcome");
        allColumns.add("evidence_id");
        allColumns.add("why_code");
        allColumns.add("detail");

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
        body.addProperty("slice_name", "ILLUSTRATIVE failed TM evidence (not Motion records)");
        body.addProperty("viz_type", "table");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("test_2.evidence.chart.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/chart/ illustrative evidence");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeEvidenceChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty("sliceName", "ILLUSTRATIVE failed TM evidence (not Motion records)");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-illustrative-failed-tm");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-illustrative-failed-tm");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-illustrative-failed-tm");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-illustrative-failed-tm");
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
        position.add("CHART-illustrative-failed-tm", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-illustrative-failed-tm", rowNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("test_2.evidence.dashboard.position", response);
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
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/security/login");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("access_token") == null || json.get("access_token").isJsonNull()) {
            Console.log("TransactionObservabilityIT.blocker", "login response missing access_token");
            return false;
        }
        this.accessToken = json.get("access_token").getAsString();
        Console.log("TransactionObservabilityIT.access_token.present", "true");
        return true;
    }

    private boolean loadCsrf() {
        HttpResponse response = this.request("GET", "/api/v1/security/csrf_token/", null, true);
        this.logResponse("security.csrf_token", response);
        if (response.status != 200) {
            Console.log("TransactionObservabilityIT.blocker", "GET /api/v1/security/csrf_token/");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || json.get("result").isJsonNull()) {
            Console.log("TransactionObservabilityIT.blocker", "csrf response missing result");
            return false;
        }
        this.csrfToken = json.get("result").getAsString();
        Console.log("TransactionObservabilityIT.csrf.present", "true");
        return true;
    }

    private Integer readExamplesDatabaseId() {
        HttpResponse response = this.request("GET", "/api/v1/database/", null, true);
        this.logResponse("database.list", response);
        if (response.status != 200) {
            Console.log("TransactionObservabilityIT.blocker", "GET /api/v1/database/");
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
            Console.log(
                    "TransactionObservabilityIT.database.row",
                    row.toString());
            if (row.get("id") != null && !row.get("id").isJsonNull()) {
                if (row.get("database_name") != null
                        && "examples".equals(row.get("database_name").getAsString())) {
                    return Integer.valueOf(row.get("id").getAsInt());
                }
            }
            i = i + 1;
        }
        if (results.size() == 1) {
            JsonObject only = results.get(0).getAsJsonObject();
            if (only.get("id") != null && !only.get("id").isJsonNull()) {
                return Integer.valueOf(only.get("id").getAsInt());
            }
        }
        return null;
    }

    private Integer createVirtualDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("schema", "public");
        body.addProperty("table_name", "tm_success_failure_runs");
        body.addProperty("sql", SAMPLE_SQL);

        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("dataset.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dataset/");
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
            Console.log("TransactionObservabilityIT.blocker", "GET /api/v1/dataset/{pk}");
            return null;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            Console.log("TransactionObservabilityIT.blocker", "dataset get missing result");
            return null;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("id") != null && !result.get("id").isJsonNull()) {
            Console.log(
                    "TransactionObservabilityIT.dataset.id",
                    String.valueOf(result.get("id").getAsInt()));
        }
        return result;
    }

    private Integer createDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty("dashboard_title", "TM SUCCESS vs FAILURE");
        body.addProperty("published", Boolean.TRUE);

        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("dashboard.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/dashboard/");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createChart(int datasetId, JsonObject dataset, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty("slice_name", "TM SUCCESS vs FAILURE");
        body.addProperty("viz_type", "echarts_timeseries_bar");
        body.add("dashboards", dashboards);
        body.addProperty("params", this.chartParams(datasetId, dataset).toString());

        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("chart.create", response);
        if (response.status != 201) {
            Console.log("TransactionObservabilityIT.blocker", "POST /api/v1/chart/");
            return null;
        }
        return this.readId(response.body);
    }

    private JsonObject chartParams(int datasetId, JsonObject dataset) {
        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "echarts_timeseries_bar");
        params.addProperty("x_axis", "run_label");
        params.add("adhoc_filters", new JsonArray());
        params.addProperty("orientation", "vertical");
        params.addProperty("show_legend", Boolean.TRUE);
        params.addProperty("color_scheme", "supersetColors");
        params.addProperty("y_axis_format", "SMART_NUMBER");
        params.addProperty("row_limit", Integer.valueOf(10000));
        params.addProperty("time_range", "No filter");

        JsonArray groupby = new JsonArray();
        groupby.add("outcome");
        params.add("groupby", groupby);

        JsonArray metrics = new JsonArray();
        metrics.add(this.sumMetric(dataset, "tm_count", "TM count"));
        params.add("metrics", metrics);
        return params;
    }

    private JsonObject sumMetric(JsonObject dataset, String columnName, String label) {
        JsonObject column = new JsonObject();
        column.addProperty("column_name", columnName);
        JsonArray columns = null;
        if (dataset.get("columns") != null && dataset.get("columns").isJsonArray()) {
            columns = dataset.getAsJsonArray("columns");
        }
        if (columns != null) {
            int i = 0;
            while (i < columns.size()) {
                JsonObject existing = columns.get(i).getAsJsonObject();
                if (existing.get("column_name") != null
                        && columnName.equals(existing.get("column_name").getAsString())) {
                    if (existing.get("type") != null && !existing.get("type").isJsonNull()) {
                        column.addProperty("type", existing.get("type").getAsString());
                    }
                    if (existing.get("id") != null && !existing.get("id").isJsonNull()) {
                        column.addProperty("id", Integer.valueOf(existing.get("id").getAsInt()));
                    }
                    if (existing.get("filterable") != null && !existing.get("filterable").isJsonNull()) {
                        column.addProperty("filterable", existing.get("filterable").getAsBoolean());
                    }
                    if (existing.get("groupby") != null && !existing.get("groupby").isJsonNull()) {
                        column.addProperty("groupby", existing.get("groupby").getAsBoolean());
                    }
                    if (existing.get("is_dttm") != null && !existing.get("is_dttm").isJsonNull()) {
                        column.addProperty("is_dttm", existing.get("is_dttm").getAsBoolean());
                    }
                }
                i = i + 1;
            }
        }

        JsonObject metric = new JsonObject();
        metric.addProperty("aggregate", "SUM");
        metric.add("column", column);
        metric.addProperty("expressionType", "SIMPLE");
        metric.addProperty("label", label);
        return metric;
    }

    private void placeChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty("sliceName", "TM SUCCESS vs FAILURE");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-tm-success-failure");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-tm-success-failure");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-tm-success-failure");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-tm-success-failure");
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
        position.add("CHART-tm-success-failure", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-tm-success-failure", rowNode);
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

    private void readDashboard(int dashboardId) {
        HttpResponse response = this.request("GET", "/api/v1/dashboard/" + dashboardId, null, true);
        this.logResponse("dashboard.get", response);
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("id") != null && !result.get("id").isJsonNull()) {
            Console.log(
                    "TransactionObservabilityIT.dashboard.id",
                    String.valueOf(result.get("id").getAsInt()));
        }
        if (result.get("url") != null && !result.get("url").isJsonNull()) {
            Console.log(
                    "TransactionObservabilityIT.dashboard.path",
                    result.get("url").getAsString());
            Console.log(
                    "TransactionObservabilityIT.dashboard.url",
                    BASE_URL + result.get("url").getAsString());
        } else {
            Console.log(
                    "TransactionObservabilityIT.dashboard.url",
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
            Console.log(
                    "TransactionObservabilityIT.chart.id",
                    String.valueOf(result.get("id").getAsInt()));
        }
        if (result.get("url") != null && !result.get("url").isJsonNull()) {
            Console.log(
                    "TransactionObservabilityIT.chart.url",
                    BASE_URL + result.get("url").getAsString());
        }
    }

    private Integer readId(String body) {
        JsonObject json = this.parseObject(body);
        if (json == null || json.get("id") == null || json.get("id").isJsonNull()) {
            Console.log("TransactionObservabilityIT.blocker", "response missing id");
            return null;
        }
        int id = json.get("id").getAsInt();
        Console.log("TransactionObservabilityIT.created.id", String.valueOf(id));
        return Integer.valueOf(id);
    }

    private void logResponse(String label, HttpResponse response) {
        Console.log("TransactionObservabilityIT." + label + ".status", String.valueOf(response.status));
        Console.log("TransactionObservabilityIT." + label + ".body", response.body);
        if (response.setCookie != null) {
            Console.log("TransactionObservabilityIT." + label + ".set_cookie", response.setCookie);
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
            Console.log("TransactionObservabilityIT.request.exception", String.valueOf(e));
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
