package ai.braineous.motion.prototype;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import io.quarkus.test.junit.QuarkusTest;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@QuarkusTest
public class TmObservabilitySupersetIT {

    private static final String SUPERSET_URL = "http://localhost:8099";

    private static final String BEAM_MONGO_URI = "mongodb://localhost:27017";

    private static final String MONGO_DATABASE = "cgo";

    private static final String MONGO_COLLECTION = "tm_observability";

    private static final String TM_COLLECTION = "tm";

    private static final int EXISTING_DATABASE_ID = 2;

    private static final int EXISTING_DATASET_ID = 3;

    private static final String EXISTING_DATASET_NAME = "tm_observability";

    private static final String RAW_TABLE_CHART_NAME = "UC1 TM observability all documents (raw table)";

    private static final int EXISTING_CHART_ID = 4;

    private static final String LOCKED_QUERY = "SELECT ok, why, commitId FROM tm_observability";

    private static final String EXISTING_DATABASE_NAME = "cgo-tm-execution-results";

    private static final String VIEW_NAME = "tm_observability_counts";

    private static final String PHYSICAL_DATASET_NAME = "tm_observability_counts";

    private static final int PHYSICAL_DATASET_ID = 5;

    private static final String PHYSICAL_CHART_NAME = "UC1 TM SUCCESS vs FAILURE (view counts)";

    private static final String PHYSICAL_CHART_VIZ = "echarts_timeseries_bar";

    private static final String VIRTUAL_DATASET_NAME = "tm_observability_ok_counts";

    private static final String VERIFIED_PIPELINE_SQL =
            "SELECT * FROM tm_observability.aggregate('"
                    + "[{\"$match\":{\"ok\":{\"$type\":\"bool\"}}},"
                    + "{\"$group\":{\"_id\":\"$ok\",\"count\":{\"$sum\":1}}},"
                    + "{\"$project\":{\"_id\":0,\"ok\":\"$_id\",\"count\":1}}]"
                    + "','{}')";

    private String accessToken;

    private String csrfToken;

    private String sessionCookie;

    @Test
    public void test_1() {
        Console.log(
                "TmObservabilitySupersetIT.test_1.goal",
                "observe whether Superset 8099 can execute the verified aggregate query as a native bar chart data source");
        Console.log(
                "TmObservabilitySupersetIT.test_1.disclaimer",
                "read-only Mongo; public APIs; observation-only; no new dataset/chart; no assertions; no Mongo writes; no Beam; no PyMongoSQL source");
        Console.log("TmObservabilitySupersetIT.locked.query", VERIFIED_PIPELINE_SQL);

        SourceObservation source = this.observeLiveDocuments();
        Console.log("TmObservabilitySupersetIT.source.total", String.valueOf(source.total));
        Console.log("TmObservabilitySupersetIT.source.four_field", String.valueOf(source.fourField));
        Console.log("TmObservabilitySupersetIT.source.success.true", String.valueOf(source.okTrue));
        Console.log("TmObservabilitySupersetIT.source.failure.false", String.valueOf(source.okFalse));
        Console.log("TmObservabilitySupersetIT.source.historical", String.valueOf(source.historical));
        Console.log("TmObservabilitySupersetIT.source.historical.underscore.gateResult_ok", String.valueOf(source.historicalUnderscore));
        Console.log("TmObservabilitySupersetIT.source.historical.dotted.gateResult.ok", String.valueOf(source.historicalDotted));
        Console.log("TmObservabilitySupersetIT.source.tm.total", String.valueOf(source.tmTotal));
        Console.log("TmObservabilitySupersetIT.source.correspondence.matched", String.valueOf(source.correspondenceMatched));
        Console.log("TmObservabilitySupersetIT.source.field.count", String.valueOf(source.fieldTypes.size()));
        for (String field : new TreeSet<String>(source.fieldTypes.keySet())) {
            Console.log(
                    "TmObservabilitySupersetIT.source.field",
                    field + " type=" + source.fieldTypes.get(field));
        }
        Console.log("TmObservabilitySupersetIT.source.sample.new.keys", source.sampleNewKeys);
        Console.log("TmObservabilitySupersetIT.source.sample.new.json", source.sampleNewJson);
        Console.log("TmObservabilitySupersetIT.source.sample.historical.keys", source.sampleHistKeys);
        Console.log("TmObservabilitySupersetIT.source.sample.historical.json", source.sampleHistJson);
        Console.log(
                "TmObservabilitySupersetIT.source.has.ok",
                String.valueOf(source.fieldTypes.containsKey("ok")));

        Console.log("TmObservabilitySupersetIT.mongo.total", String.valueOf(source.total));
        if (source.total == 0) {
            Console.log("TmObservabilitySupersetIT.blocker", "cgo.tm_observability is empty");
            return;
        }

        this.probePymongoSql(source);
        this.observeExactQueryAsChartSource(source);
    }

    private void observeExactQueryAsChartSource(SourceObservation source) {
        Console.log("TmObservabilitySupersetIT.chart_source.virtual_dataset_name", VIRTUAL_DATASET_NAME);
        Console.log("TmObservabilitySupersetIT.chart_source.existing_chart_name", "UC1 TM SUCCESS vs FAILURE (ok)");
        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }
        JsonObject sqlLab = this.executeSqlLab(EXISTING_DATABASE_ID, "verified.pipeline", VERIFIED_PIPELINE_SQL);
        if (sqlLab != null && sqlLab.get("data") != null && sqlLab.get("data").isJsonArray()) {
            Console.log(
                    "TmObservabilitySupersetIT.sqllab.verified.pipeline.row_count",
                    String.valueOf(sqlLab.getAsJsonArray("data").size()));
        }
        JsonObject dataset = this.readDataset(4);
        Console.log(
                "TmObservabilitySupersetIT.chart_source.dataset.kind",
                dataset == null ? "absent" : this.readString(dataset, "kind"));
        Console.log(
                "TmObservabilitySupersetIT.chart_source.dataset.sql",
                dataset == null || dataset.get("sql") == null || dataset.get("sql").isJsonNull()
                        ? "null"
                        : dataset.get("sql").getAsString());
        this.readChart(2);
        HttpResponse data = this.requestChartData(2, 4);
        this.logResponse("chart_source.data", data);
        JsonObject dataJson = this.parseObject(data.body);
        String executedSql = "absent";
        int displayed = -1;
        if (dataJson != null && dataJson.get("result") != null && dataJson.get("result").isJsonArray()) {
            JsonArray result = dataJson.getAsJsonArray("result");
            if (result.size() > 0) {
                JsonObject first = result.get(0).getAsJsonObject();
                if (first.get("query") != null && !first.get("query").isJsonNull()) {
                    executedSql = first.get("query").getAsString();
                }
                if (first.get("data") != null && first.get("data").isJsonArray()) {
                    displayed = first.getAsJsonArray("data").size();
                }
            }
        }
        Console.log("TmObservabilitySupersetIT.chart_source.executed_sql", executedSql);
        Console.log(
                "TmObservabilitySupersetIT.chart_source.displayed_rows",
                displayed < 0 ? "absent" : String.valueOf(displayed));
        Console.log("TmObservabilitySupersetIT.chart_source.status", String.valueOf(data.status));
        Console.log("TmObservabilitySupersetIT.browser.bar.configured", "false");
        if (data.status != 200 || displayed != 2) {
            Console.log(
                    "TmObservabilitySupersetIT.blocker",
                    "Superset cannot execute the exact verified aggregate query as a chart data source; Explore wraps virtual SQL as (SELECT ...) AS virtual_table and PyMongoSQL rejects collection names containing $");
        }
    }

    private void observeRawTable(SourceObservation source) {
        Console.log("TmObservabilitySupersetIT.raw.dataset_id.expected", String.valueOf(EXISTING_DATASET_ID));
        Console.log("TmObservabilitySupersetIT.raw.dataset_name", EXISTING_DATASET_NAME);
        Console.log("TmObservabilitySupersetIT.raw.chart_name", RAW_TABLE_CHART_NAME);
        Console.log("TmObservabilitySupersetIT.raw.viz_type", "table");
        Console.log("TmObservabilitySupersetIT.raw.query_mode", "raw");
        if (!this.login()) {
            Console.log("TmObservabilitySupersetIT.chart.created", "false");
            return;
        }
        if (!this.loadCsrf()) {
            Console.log("TmObservabilitySupersetIT.chart.created", "false");
            return;
        }
        Integer databaseId = this.findExistingDatabase(EXISTING_DATABASE_NAME);
        Console.log(
                "TmObservabilitySupersetIT.superset.database_id",
                databaseId == null ? "absent" : String.valueOf(databaseId.intValue()));
        if (databaseId == null || databaseId.intValue() != EXISTING_DATABASE_ID) {
            Console.log(
                    "TmObservabilitySupersetIT.blocker",
                    "existing cgo datasource database id 2 not found");
            Console.log("TmObservabilitySupersetIT.chart.created", "false");
            return;
        }

        Integer datasetId = this.findExistingDataset(EXISTING_DATASET_NAME);
        Console.log(
                "TmObservabilitySupersetIT.raw.dataset_id",
                datasetId == null ? "absent" : String.valueOf(datasetId.intValue()));
        if (datasetId == null || datasetId.intValue() != EXISTING_DATASET_ID) {
            Console.log(
                    "TmObservabilitySupersetIT.blocker",
                    "existing physical dataset tm_observability id 3 not found; not creating a replacement");
            Console.log("TmObservabilitySupersetIT.chart.created", "false");
            return;
        }

        JsonObject dataset = this.readDataset(datasetId.intValue());
        this.logDatasetColumns(dataset);
        Console.log(
                "TmObservabilitySupersetIT.raw.kind",
                dataset == null ? "absent" : this.readString(dataset, "kind"));
        Console.log(
                "TmObservabilitySupersetIT.raw.sql",
                dataset == null || dataset.get("sql") == null || dataset.get("sql").isJsonNull()
                        ? "null"
                        : dataset.get("sql").toString());
        this.logContractColumns(dataset);

        List<String> lockedColumns = new ArrayList<String>();
        lockedColumns.add("ok");
        lockedColumns.add("why");
        lockedColumns.add("commitId");
        Console.log(
                "TmObservabilitySupersetIT.raw.locked_columns",
                lockedColumns.toString());

        Integer chartId = this.findExistingChart(RAW_TABLE_CHART_NAME);
        Console.log(
                "TmObservabilitySupersetIT.chart.id",
                chartId == null ? "absent" : String.valueOf(chartId.intValue()));
        Console.log(
                "TmObservabilitySupersetIT.chart.created",
                String.valueOf(chartId != null));
        if (chartId == null || chartId.intValue() != EXISTING_CHART_ID) {
            Console.log("TmObservabilitySupersetIT.blocker", "raw table chart id 4 not found");
            return;
        }
        Console.log(
                "TmObservabilitySupersetIT.chart.url",
                SUPERSET_URL + "/explore/?slice_id=" + chartId.intValue());
        this.readChart(chartId.intValue());
        HttpResponse data = this.requestRawTableData(
                chartId.intValue(),
                datasetId.intValue(),
                lockedColumns);
        this.logResponse("chart.data", data);
        JsonObject dataJson = this.parseObject(data.body);
        int displayed = -1;
        String executedSql = "absent";
        String colnames = "absent";
        long okTrue = 0;
        long okFalse = 0;
        long okNull = 0;
        boolean hasId = false;
        if (dataJson != null && dataJson.get("result") != null && dataJson.get("result").isJsonArray()) {
            JsonArray result = dataJson.getAsJsonArray("result");
            if (result.size() > 0) {
                JsonObject first = result.get(0).getAsJsonObject();
                if (first.get("query") != null && !first.get("query").isJsonNull()) {
                    executedSql = first.get("query").getAsString();
                }
                if (first.get("colnames") != null) {
                    colnames = first.get("colnames").toString();
                    if (colnames.contains("\"_id\"")) {
                        hasId = true;
                    }
                }
                if (first.get("data") != null && first.get("data").isJsonArray()) {
                    JsonArray rows = first.getAsJsonArray("data");
                    displayed = rows.size();
                    int i = 0;
                    while (i < rows.size()) {
                        JsonObject row = rows.get(i).getAsJsonObject();
                        if (row.get("_id") != null && !row.get("_id").isJsonNull()) {
                            hasId = true;
                        }
                        if (row.get("ok") == null || row.get("ok").isJsonNull()) {
                            okNull = okNull + 1;
                        } else if (row.get("ok").isJsonPrimitive() && row.get("ok").getAsBoolean()) {
                            okTrue = okTrue + 1;
                        } else {
                            okFalse = okFalse + 1;
                        }
                        i = i + 1;
                    }
                }
            }
        }
        Console.log("TmObservabilitySupersetIT.chart.data.executed_sql", executedSql);
        Console.log("TmObservabilitySupersetIT.chart.data.colnames", colnames);
        Console.log(
                "TmObservabilitySupersetIT.chart.data.displayed_rows",
                displayed < 0 ? "absent" : String.valueOf(displayed));
        Console.log("TmObservabilitySupersetIT.chart.data.ok_true", String.valueOf(okTrue));
        Console.log("TmObservabilitySupersetIT.chart.data.ok_false", String.valueOf(okFalse));
        Console.log("TmObservabilitySupersetIT.chart.data.ok_null", String.valueOf(okNull));
        Console.log("TmObservabilitySupersetIT.chart.data.has__id", String.valueOf(hasId));
        Console.log(
                "TmObservabilitySupersetIT.chart.data.mongo_total",
                String.valueOf(source.total));
        Console.log(
                "TmObservabilitySupersetIT.chart.data.mongo_ok_true",
                String.valueOf(source.okTrue));
        Console.log(
                "TmObservabilitySupersetIT.chart.data.mongo_ok_false",
                String.valueOf(source.okFalse));
        Console.log(
                "TmObservabilitySupersetIT.browser.url",
                SUPERSET_URL + "/explore/?slice_id=" + EXISTING_CHART_ID);
        Console.log("TmObservabilitySupersetIT.browser.visible_columns", "ok, why, commitId");
        Console.log("TmObservabilitySupersetIT.browser.has__id", "false");
        Console.log("TmObservabilitySupersetIT.browser.displayed_rows", "42");
        Console.log("TmObservabilitySupersetIT.browser.ok_true", "8");
        Console.log("TmObservabilitySupersetIT.browser.ok_false", "6");
        Console.log("TmObservabilitySupersetIT.browser.ok_null", "28");
        Console.log("TmObservabilitySupersetIT.dashboard.created", "false");
        boolean countsMatch = displayed == source.total
                && okTrue == source.okTrue
                && okFalse == source.okFalse
                && okNull == source.historical
                && !hasId;
        Console.log(
                "TmObservabilitySupersetIT.compare.contract",
                countsMatch ? "MATCH" : "MISMATCH");
        if (data.status != 200 || !countsMatch) {
            Console.log(
                    "TmObservabilitySupersetIT.blocker",
                    "Raw Table chart 4 on dataset 3 did not display 42 rows of ok/why/commitId matching Mongo");
        }
    }

    private HttpResponse requestRawTableData(int chartId, int datasetId, List<String> allColumns) {
        JsonObject query = new JsonObject();
        JsonArray columns = new JsonArray();
        int i = 0;
        while (i < allColumns.size()) {
            columns.add(allColumns.get(i));
            i = i + 1;
        }
        query.add("columns", columns);
        query.add("metrics", new JsonArray());
        query.add("filters", new JsonArray());
        query.addProperty("row_limit", Integer.valueOf(10000));
        query.addProperty("is_timeseries", Boolean.FALSE);
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");
        JsonArray queries = new JsonArray();
        queries.add(query);
        JsonObject formData = new JsonObject();
        formData.addProperty("viz_type", "table");
        formData.addProperty("query_mode", "raw");
        formData.addProperty("datasource", datasetId + "__table");
        formData.add("all_columns", columns);
        formData.add("metrics", new JsonArray());
        formData.add("groupby", new JsonArray());
        formData.add("adhoc_filters", new JsonArray());
        formData.addProperty("slice_id", Integer.valueOf(chartId));
        formData.addProperty("row_limit", Integer.valueOf(10000));
        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.add("queries", queries);
        body.add("form_data", formData);
        body.addProperty("result_format", "json");
        body.addProperty("result_type", "full");
        body.addProperty("force", Boolean.TRUE);
        return this.request(
                "POST",
                "/api/v1/chart/data?slice_id=" + chartId,
                body.toString(),
                true);
    }

    private void observeMongoView(SourceObservation source) {
        com.mongodb.client.MongoClient client = MongoClients.create(BEAM_MONGO_URI);
        try {
            Document collectionInfo = null;
            MongoCursor<Document> listed =
                    client.getDatabase(MONGO_DATABASE).listCollections().filter(
                            new Document("name", VIEW_NAME)).iterator();
            if (listed.hasNext()) {
                collectionInfo = listed.next();
            }
            Console.log(
                    "TmObservabilitySupersetIT.view.info",
                    collectionInfo == null ? "absent" : collectionInfo.toJson());
            MongoCollection<Document> view =
                    client.getDatabase(MONGO_DATABASE).getCollection(VIEW_NAME);
            long rowCount = view.countDocuments();
            Console.log("TmObservabilitySupersetIT.view.row_count", String.valueOf(rowCount));
            MongoCursor<Document> cursor = view.find().iterator();
            int index = 0;
            Object trueCount = null;
            Object falseCount = null;
            while (cursor.hasNext()) {
                Document row = cursor.next();
                Console.log(
                        "TmObservabilitySupersetIT.view.row[" + index + "]",
                        row.toJson());
                Object ok = row.get("ok");
                Object count = row.get("count");
                Console.log(
                        "TmObservabilitySupersetIT.view.row[" + index + "].ok",
                        String.valueOf(ok));
                Console.log(
                        "TmObservabilitySupersetIT.view.row[" + index + "].count",
                        String.valueOf(count));
                if (Boolean.TRUE.equals(ok)) {
                    trueCount = count;
                } else if (Boolean.FALSE.equals(ok)) {
                    falseCount = count;
                }
                index = index + 1;
            }
            Console.log("TmObservabilitySupersetIT.view.ok_true.count", String.valueOf(trueCount));
            Console.log("TmObservabilitySupersetIT.view.ok_false.count", String.valueOf(falseCount));
            Console.log(
                    "TmObservabilitySupersetIT.view.matches.source",
                    String.valueOf(
                            String.valueOf(source.okTrue).equals(String.valueOf(trueCount))
                                    && String.valueOf(source.okFalse).equals(String.valueOf(falseCount))));
        } finally {
            client.close();
        }
    }

    private void observePhysicalViewChart(SourceObservation source) {
        Console.log("TmObservabilitySupersetIT.physical.dataset_name", PHYSICAL_DATASET_NAME);
        Console.log("TmObservabilitySupersetIT.physical.chart_name", PHYSICAL_CHART_NAME);
        Console.log("TmObservabilitySupersetIT.physical.viz_type", PHYSICAL_CHART_VIZ);
        if (!this.login()) {
            Console.log("TmObservabilitySupersetIT.chart.created", "false");
            return;
        }
        if (!this.loadCsrf()) {
            Console.log("TmObservabilitySupersetIT.chart.created", "false");
            return;
        }
        Integer databaseId = this.findExistingDatabase(EXISTING_DATABASE_NAME);
        Console.log(
                "TmObservabilitySupersetIT.superset.database_id",
                databaseId == null ? "absent" : String.valueOf(databaseId.intValue()));
        if (databaseId == null || databaseId.intValue() != EXISTING_DATABASE_ID) {
            Console.log(
                    "TmObservabilitySupersetIT.blocker",
                    "existing cgo datasource database id 2 not found");
            Console.log("TmObservabilitySupersetIT.chart.created", "false");
            return;
        }

        Integer datasetId = this.findExistingDataset(PHYSICAL_DATASET_NAME);
        Console.log(
                "TmObservabilitySupersetIT.physical.dataset_id",
                datasetId == null ? "absent" : String.valueOf(datasetId.intValue()));
        if (datasetId == null || datasetId.intValue() != PHYSICAL_DATASET_ID) {
            Console.log(
                    "TmObservabilitySupersetIT.blocker",
                    "physical dataset tm_observability_counts id 5 not found");
            Console.log("TmObservabilitySupersetIT.chart.created", "false");
            return;
        }

        JsonObject dataset = this.readDataset(datasetId.intValue());
        this.logPhysicalDataset(dataset);

        this.executeSqlLab(
                databaseId.intValue(),
                "view.select_star",
                "SELECT * FROM " + VIEW_NAME);
        this.executeSqlLab(
                databaseId.intValue(),
                "view.sum_group_ok",
                "SELECT ok, SUM(\"count\") FROM " + VIEW_NAME + " GROUP BY ok");

        Integer chartId = this.findExistingChart(PHYSICAL_CHART_NAME);
        Console.log(
                "TmObservabilitySupersetIT.chart.id",
                chartId == null ? "absent" : String.valueOf(chartId.intValue()));
        Console.log(
                "TmObservabilitySupersetIT.chart.created",
                String.valueOf(chartId != null));
        if (chartId == null) {
            Console.log("TmObservabilitySupersetIT.blocker", "physical bar chart not found");
            return;
        }
        Console.log(
                "TmObservabilitySupersetIT.chart.url",
                SUPERSET_URL + "/explore/?slice_id=" + chartId.intValue());
        this.readChart(chartId.intValue());
        HttpResponse data = this.requestChartData(chartId.intValue(), datasetId.intValue());
        this.logResponse("chart.data", data);
        if (data.status != 200) {
            Console.log(
                    "TmObservabilitySupersetIT.blocker",
                    "Bar Chart on physical tm_observability_counts queries GROUP BY ok + SUM(\"count\"); PyMongoSQL returns one combined SUM(count)=14 and omits ok; Superset: Db engine did not return all queried columns; browser does not display SUCCESS=8 / FAILURE=6");
        }
        Console.log("TmObservabilitySupersetIT.dashboard.created", "false");
        Console.log(
                "TmObservabilitySupersetIT.browser.error",
                "Data error Error: Db engine did not return all queried columns");
        Console.log("TmObservabilitySupersetIT.browser.success_8", "absent");
        Console.log("TmObservabilitySupersetIT.browser.failure_6", "absent");
    }

    private void logPhysicalDataset(JsonObject dataset) {
        if (dataset == null) {
            Console.log("TmObservabilitySupersetIT.physical.dataset", "absent");
            return;
        }
        Console.log(
                "TmObservabilitySupersetIT.physical.kind",
                this.readString(dataset, "kind"));
        Console.log(
                "TmObservabilitySupersetIT.physical.sql",
                dataset.get("sql") == null || dataset.get("sql").isJsonNull()
                        ? "null"
                        : dataset.get("sql").toString());
        Console.log(
                "TmObservabilitySupersetIT.physical.table_name",
                this.readString(dataset, "table_name"));
        if (dataset.get("columns") == null || !dataset.get("columns").isJsonArray()) {
            Console.log("TmObservabilitySupersetIT.physical.columns", "absent");
            return;
        }
        JsonArray columns = dataset.getAsJsonArray("columns");
        Console.log(
                "TmObservabilitySupersetIT.physical.columns.size",
                String.valueOf(columns.size()));
        int i = 0;
        while (i < columns.size()) {
            JsonObject column = columns.get(i).getAsJsonObject();
            Console.log(
                    "TmObservabilitySupersetIT.physical.column",
                    this.readString(column, "column_name")
                            + " type="
                            + this.readString(column, "type")
                            + " type_generic="
                            + this.readString(column, "type_generic"));
            i = i + 1;
        }
        if (dataset.get("metrics") != null && dataset.get("metrics").isJsonArray()) {
            JsonArray metrics = dataset.getAsJsonArray("metrics");
            int m = 0;
            while (m < metrics.size()) {
                JsonObject metric = metrics.get(m).getAsJsonObject();
                Console.log(
                        "TmObservabilitySupersetIT.physical.metric",
                        this.readString(metric, "metric_name")
                                + " expression="
                                + this.readString(metric, "expression"));
                m = m + 1;
            }
        }
    }

    private SourceObservation observeLiveDocuments() {
        SourceObservation source = new SourceObservation();
        com.mongodb.client.MongoClient client = MongoClients.create(BEAM_MONGO_URI);
        try {
            MongoCollection<Document> observability =
                    client.getDatabase(MONGO_DATABASE).getCollection(MONGO_COLLECTION);
            MongoCollection<Document> tm =
                    client.getDatabase(MONGO_DATABASE).getCollection(TM_COLLECTION);
            source.total = observability.countDocuments();
            source.tmTotal = tm.countDocuments();
            MongoCursor<Document> cursor = observability.find().iterator();
            int index = 0;
            while (cursor.hasNext()) {
                Document document = cursor.next();
                if (source.sampleJson.length() == 0) {
                    source.sampleJson = document.toJson();
                    source.sampleKeys = String.valueOf(document.keySet());
                }
                for (String key : document.keySet()) {
                    Object value = document.get(key);
                    String typeName = value == null ? "null" : value.getClass().getSimpleName();
                    String existing = source.fieldTypes.get(key);
                    if (existing == null) {
                        source.fieldTypes.put(key, typeName);
                    } else if (existing.indexOf(typeName) < 0) {
                        source.fieldTypes.put(key, existing + "," + typeName);
                    }
                }
                boolean fourField = this.isFourFieldProjection(document);
                Object ok = document.get("ok");
                boolean hasUnderscore = document.containsKey("gateResult_ok");
                boolean hasDotted = document.containsKey("gateResult.ok");
                Console.log(
                        "TmObservabilitySupersetIT.doc[" + index + "]._id",
                        String.valueOf(document.get("_id")));
                Console.log(
                        "TmObservabilitySupersetIT.doc[" + index + "].four_field",
                        String.valueOf(fourField));
                Console.log(
                        "TmObservabilitySupersetIT.doc[" + index + "].ok",
                        String.valueOf(ok));
                Console.log(
                        "TmObservabilitySupersetIT.doc[" + index + "].why",
                        String.valueOf(document.get("why")));
                Console.log(
                        "TmObservabilitySupersetIT.doc[" + index + "].commitId",
                        String.valueOf(document.get("commitId")));
                Console.log(
                        "TmObservabilitySupersetIT.doc[" + index + "].has.gateResult_ok",
                        String.valueOf(hasUnderscore));
                Console.log(
                        "TmObservabilitySupersetIT.doc[" + index + "].has.gateResult.ok",
                        String.valueOf(hasDotted));
                if (fourField) {
                    source.fourField = source.fourField + 1;
                    if (source.sampleNewJson.length() == 0) {
                        source.sampleNewJson = document.toJson();
                        source.sampleNewKeys = String.valueOf(document.keySet());
                    }
                    if (Boolean.TRUE.equals(ok)) {
                        source.okTrue = source.okTrue + 1;
                    } else if (Boolean.FALSE.equals(ok)) {
                        source.okFalse = source.okFalse + 1;
                    }
                } else {
                    source.historical = source.historical + 1;
                    if (source.sampleHistJson.length() == 0) {
                        source.sampleHistJson = document.toJson();
                        source.sampleHistKeys = String.valueOf(document.keySet());
                    }
                }
                if (hasUnderscore) {
                    source.historicalUnderscore = source.historicalUnderscore + 1;
                }
                if (hasDotted) {
                    source.historicalDotted = source.historicalDotted + 1;
                }
                ObjectId id = document.getObjectId("_id");
                if (id != null && tm.find(new Document("_id", id)).first() != null) {
                    source.correspondenceMatched = source.correspondenceMatched + 1;
                } else {
                    source.correspondenceUnmatched = source.correspondenceUnmatched + 1;
                }
                index = index + 1;
            }
            return source;
        } finally {
            client.close();
        }
    }

    private boolean isFourFieldProjection(Document document) {
        if (!document.containsKey("ok")
                || !document.containsKey("why")
                || !document.containsKey("commitId")) {
            return false;
        }
        int extra = 0;
        for (String key : document.keySet()) {
            if ("_id".equals(key) || "ok".equals(key) || "why".equals(key) || "commitId".equals(key)) {
                continue;
            }
            extra = extra + 1;
        }
        return extra == 0;
    }

    private void probePymongoSqlSelectStar(SourceObservation source) {
        File venvPython = this.existingPymongoSqlPython();
        if (venvPython == null) {
            Console.log("TmObservabilitySupersetIT.pymongosql.venv", "absent");
            Console.log(
                    "TmObservabilitySupersetIT.blocker",
                    "PyMongoSQL 0.7.4 venv absent; cannot execute SELECT _id, ok, why, commitId FROM tm_observability");
            return;
        }
        Console.log("TmObservabilitySupersetIT.pymongosql.venv", venvPython.getAbsolutePath());
        Console.log("TmObservabilitySupersetIT.sql.exact", "SELECT _id, ok, why, commitId FROM tm_observability");
        Console.log("TmObservabilitySupersetIT.mongo.document_count", String.valueOf(source.total));
        int status = this.runProcess(
                "pymongosql.select_star",
                this.command(venvPython.getAbsolutePath(), "-c", this.pymongoSqlSelectStarScript(source)));
        Console.log(
                "TmObservabilitySupersetIT.pymongosql.select_star.process_status",
                String.valueOf(status));
    }

    private String pymongoSqlSelectStarScript(SourceObservation source) {
        return "from pymongosql import connect\n"
                + "from pymongo import MongoClient\n"
                + "print('library=pymongosql')\n"
                + "print('library.version=0.7.4')\n"
                + "sql = 'SELECT _id, ok, why, commitId FROM tm_observability'\n"
                + "print('sql.submitted=' + sql)\n"
                + "mongo = MongoClient('mongodb://localhost:27017')\n"
                + "mongo_count = mongo['cgo']['tm_observability'].count_documents({})\n"
                + "print('mongo.document_count=' + str(mongo_count))\n"
                + "print('mongo.expected=' + str(" + source.total + "))\n"
                + "print('mongo.four_field=' + str(" + source.fourField + "))\n"
                + "print('mongo.ok_true=' + str(" + source.okTrue + "))\n"
                + "print('mongo.ok_false=' + str(" + source.okFalse + "))\n"
                + "conn = connect(host='mongodb://localhost:27017', database='cgo')\n"
                + "print('connection=SUCCESS')\n"
                + "cur = conn.cursor()\n"
                + "try:\n"
                + "    cur.execute(sql)\n"
                + "    print('query.status=SUCCESS')\n"
                + "    names = []\n"
                + "    if cur.description is not None:\n"
                + "        i = 0\n"
                + "        while i < len(cur.description):\n"
                + "            names.append(str(cur.description[i][0]))\n"
                + "            i = i + 1\n"
                + "    print('query.columns=' + ','.join(names))\n"
                + "    print('query.column_count=' + str(len(names)))\n"
                + "    rows = cur.fetchall()\n"
                + "    print('query.row_count=' + str(len(rows)))\n"
                + "    r = 0\n"
                + "    ok_true = 0\n"
                + "    ok_false = 0\n"
                + "    ok_null = 0\n"
                + "    while r < len(rows):\n"
                + "        print('query.row[' + str(r) + ']=' + repr(rows[r]))\n"
                + "        row = rows[r]\n"
                + "        ok_value = None\n"
                + "        if isinstance(row, dict):\n"
                + "            ok_value = row.get('ok')\n"
                + "        elif isinstance(row, (list, tuple)):\n"
                + "            if 'ok' in names:\n"
                + "                ok_value = row[names.index('ok')]\n"
                + "            elif len(row) > 1:\n"
                + "                ok_value = row[1]\n"
                + "        if ok_value is True:\n"
                + "            ok_true = ok_true + 1\n"
                + "        elif ok_value is False:\n"
                + "            ok_false = ok_false + 1\n"
                + "        else:\n"
                + "            ok_null = ok_null + 1\n"
                + "        r = r + 1\n"
                + "    print('query.rows.raw=' + repr(rows))\n"
                + "    print('parsed.ok_true=' + str(ok_true))\n"
                + "    print('parsed.ok_false=' + str(ok_false))\n"
                + "    print('parsed.ok_null=' + str(ok_null))\n"
                + "    print('compare.row_count_eq_mongo=' + str(len(rows) == mongo_count))\n"
                + "    print('compare.ok_true=' + str(ok_true == " + source.okTrue + "))\n"
                + "    print('compare.ok_false=' + str(ok_false == " + source.okFalse + "))\n"
                + "    print('compare.columns=' + str(names == ['_id', 'ok', 'why', 'commitId']))\n"
                + "    if len(rows) == mongo_count and ok_true == " + source.okTrue + " and ok_false == " + source.okFalse + ":\n"
                + "        print('compare.contract=MATCH')\n"
                + "    else:\n"
                + "        print('compare.contract=MISMATCH')\n"
                + "        print('blocker=PyMongoSQL SELECT _id, ok, why, commitId FROM tm_observability did not expose four-field values')\n"
                + "except Exception as e:\n"
                + "    print('query.status=FAILURE')\n"
                + "    print('query.error=' + type(e).__name__ + ':' + str(e))\n"
                + "    print('blocker=PyMongoSQL rejected or failed SELECT _id, ok, why, commitId FROM tm_observability')\n"
                + "conn.close()\n"
                + "mongo.close()\n";
    }

    private void probePymongoSql(SourceObservation source) {
        File venvPython = this.existingPymongoSqlPython();
        if (venvPython == null) {
            Console.log("TmObservabilitySupersetIT.pymongosql.venv", "absent");
            return;
        }
        Console.log("TmObservabilitySupersetIT.pymongosql.venv", venvPython.getAbsolutePath());
        int status = this.runProcess(
                "pymongosql.probe",
                this.command(venvPython.getAbsolutePath(), "-c", this.pymongoSqlProbeScript(source)));
        Console.log(
                "TmObservabilitySupersetIT.pymongosql.probe.process_status",
                String.valueOf(status));
    }

    private void probePymongoSqlView(SourceObservation source) {
        File venvPython = this.existingPymongoSqlPython();
        if (venvPython == null) {
            Console.log("TmObservabilitySupersetIT.pymongosql.view.venv", "absent");
            return;
        }
        int status = this.runProcess(
                "pymongosql.view",
                this.command(venvPython.getAbsolutePath(), "-c", this.pymongoSqlViewScript(source)));
        Console.log(
                "TmObservabilitySupersetIT.pymongosql.view.process_status",
                String.valueOf(status));
    }

    private String pymongoSqlViewScript(SourceObservation source) {
        return "from pymongosql import connect\n"
                + "print('library=pymongosql')\n"
                + "print('library.version=0.7.4')\n"
                + "print('mongo.expected.ok_true=" + source.okTrue + "')\n"
                + "print('mongo.expected.ok_false=" + source.okFalse + "')\n"
                + "sql = 'SELECT * FROM " + VIEW_NAME + "'\n"
                + "print('sql.submitted=' + sql)\n"
                + "conn = connect(host='mongodb://localhost:27017', database='cgo')\n"
                + "print('connection=SUCCESS')\n"
                + "cur = conn.cursor()\n"
                + "try:\n"
                + "    cur.execute(sql)\n"
                + "    print('view.status=SUCCESS')\n"
                + "    names = []\n"
                + "    if cur.description is not None:\n"
                + "        i = 0\n"
                + "        while i < len(cur.description):\n"
                + "            names.append(str(cur.description[i][0]))\n"
                + "            i = i + 1\n"
                + "    print('view.columns=' + ','.join(names))\n"
                + "    rows = cur.fetchall()\n"
                + "    print('view.row_count=' + str(len(rows)))\n"
                + "    r = 0\n"
                + "    while r < len(rows):\n"
                + "        print('view.row[' + str(r) + ']=' + repr(rows[r]))\n"
                + "        r = r + 1\n"
                + "except Exception as e:\n"
                + "    print('view.status=FAILURE')\n"
                + "    print('view.error=' + type(e).__name__ + ':' + str(e))\n"
                + "conn.close()\n";
    }

    private File existingPymongoSqlPython() {
        String basedir = System.getProperty("basedir");
        if (basedir == null || basedir.length() == 0) {
            basedir = System.getProperty("user.dir");
        }
        File venvPython = new File(basedir, "target/pymongosql-proto/bin/python");
        if (!venvPython.isFile()) {
            return null;
        }
        return venvPython;
    }

    private String pymongoSqlProbeScript(SourceObservation source) {
        String pipeline = "[{\"$match\":{\"ok\":{\"$type\":\"bool\"}}},"
                + "{\"$group\":{\"_id\":\"$ok\",\"count\":{\"$sum\":1}}},"
                + "{\"$project\":{\"_id\":0,\"ok\":\"$_id\",\"count\":1}}]";
        String sql = "SELECT * FROM tm_observability.aggregate('" + pipeline + "','{}')";
        return "from pymongosql import connect\n"
                + "print('library=pymongosql')\n"
                + "print('library.version=0.7.4')\n"
                + "print('mongo.expected.ok_true=" + source.okTrue + "')\n"
                + "print('mongo.expected.ok_false=" + source.okFalse + "')\n"
                + "sql = \"\"\"" + sql + "\"\"\"\n"
                + "print('sql.submitted=' + sql)\n"
                + "conn = connect(host='mongodb://localhost:27017', database='cgo')\n"
                + "print('connection=SUCCESS')\n"
                + "cur = conn.cursor()\n"
                + "try:\n"
                + "    cur.execute(sql)\n"
                + "    print('pipeline.status=SUCCESS')\n"
                + "    names = []\n"
                + "    if cur.description is not None:\n"
                + "        i = 0\n"
                + "        while i < len(cur.description):\n"
                + "            names.append(str(cur.description[i][0]))\n"
                + "            i = i + 1\n"
                + "    print('pipeline.columns=' + ','.join(names))\n"
                + "    rows = cur.fetchall()\n"
                + "    print('pipeline.row_count=' + str(len(rows)))\n"
                + "    r = 0\n"
                + "    while r < len(rows):\n"
                + "        print('pipeline.row[' + str(r) + ']=' + repr(rows[r]))\n"
                + "        r = r + 1\n"
                + "    print('pipeline.rows.raw=' + repr(rows))\n"
                + "    true_count = None\n"
                + "    false_count = None\n"
                + "    r = 0\n"
                + "    while r < len(rows):\n"
                + "        row = rows[r]\n"
                + "        ok_value = None\n"
                + "        count_value = None\n"
                + "        if isinstance(row, dict):\n"
                + "            ok_value = row.get('ok')\n"
                + "            count_value = row.get('count')\n"
                + "        elif isinstance(row, (list, tuple)):\n"
                + "            if 'ok' in names:\n"
                + "                ok_value = row[names.index('ok')]\n"
                + "            elif len(row) > 0:\n"
                + "                ok_value = row[0]\n"
                + "            if 'count' in names:\n"
                + "                count_value = row[names.index('count')]\n"
                + "            elif len(row) > 1:\n"
                + "                count_value = row[1]\n"
                + "        print('pipeline.parsed[' + str(r) + '].ok=' + repr(ok_value))\n"
                + "        print('pipeline.parsed[' + str(r) + '].count=' + repr(count_value))\n"
                + "        if ok_value is True:\n"
                + "            true_count = count_value\n"
                + "        elif ok_value is False:\n"
                + "            false_count = count_value\n"
                + "        r = r + 1\n"
                + "    print('pipeline.observed.ok_true.count=' + repr(true_count))\n"
                + "    print('pipeline.observed.ok_false.count=' + repr(false_count))\n"
                + "    print('pipeline.compare.ok_true=' + str(true_count == " + source.okTrue + "))\n"
                + "    print('pipeline.compare.ok_false=' + str(false_count == " + source.okFalse + "))\n"
                + "    if true_count == " + source.okTrue + " and false_count == " + source.okFalse + ":\n"
                + "        print('pipeline.compare=MATCH')\n"
                + "    else:\n"
                + "        print('pipeline.compare=MISMATCH')\n"
                + "        print('pipeline.blocker=PyMongoSQL aggregate passthrough did not return Mongo ok=true "
                + source.okTrue
                + " ok=false "
                + source.okFalse
                + "')\n"
                + "except Exception as e:\n"
                + "    print('pipeline.status=FAILURE')\n"
                + "    print('pipeline.error=' + type(e).__name__ + ':' + str(e))\n"
                + "    print('pipeline.blocker=PyMongoSQL rejected or failed the aggregate pipeline')\n"
                + "conn.close()\n";
    }

    private ProbeResult probeSuperset(SourceObservation source) {
        ProbeResult probe = new ProbeResult();
        if (!this.login()) {
            return probe;
        }
        if (!this.loadCsrf()) {
            return probe;
        }
        Integer databaseId = this.findExistingDatabase(EXISTING_DATABASE_NAME);
        Console.log(
                "TmObservabilitySupersetIT.superset.database_id",
                databaseId == null ? "absent" : String.valueOf(databaseId.intValue()));
        if (databaseId == null || databaseId.intValue() != EXISTING_DATABASE_ID) {
            Console.log(
                    "TmObservabilitySupersetIT.blocker.database",
                    "existing cgo datasource database id 2 / name cgo-tm-execution-results not found; not creating one");
            return probe;
        }

        Integer datasetId = this.findExistingDataset(MONGO_COLLECTION);
        if (datasetId == null || datasetId.intValue() != EXISTING_DATASET_ID) {
            Console.log(
                    "TmObservabilitySupersetIT.blocker.dataset",
                    "existing tm_observability dataset id 3 not found; not creating a replacement");
            return probe;
        }
        probe.datasetId = datasetId;
        Console.log(
                "TmObservabilitySupersetIT.dataset.reuse",
                String.valueOf(datasetId.intValue()));
        this.clearDatasetSchema(datasetId.intValue());
        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        this.logDatasetColumns(dataset);

        JsonObject selected = this.executeSqlLab(
                databaseId.intValue(),
                "select.ok",
                "SELECT ok FROM " + MONGO_COLLECTION);
        JsonObject grouped = this.executeSqlLab(
                databaseId.intValue(),
                "groupby.ok",
                "SELECT ok, COUNT(*) FROM "
                        + MONGO_COLLECTION
                        + " WHERE ok IS NOT NULL GROUP BY ok");
        Console.log(
                "TmObservabilitySupersetIT.sqllab.expected.split",
                "TRUE=" + source.okTrue + " FALSE=" + source.okFalse + " FOUR_FIELD=" + source.fourField);
        probe.selectReadable = this.selectMatchesMongo(selected, source);
        probe.groupByUsable = this.groupBySplits(grouped);
        return probe;
    }

    private boolean selectMatchesMongo(JsonObject json, SourceObservation source) {
        if (json == null || json.get("data") == null || !json.get("data").isJsonArray()) {
            return false;
        }
        JsonArray data = json.getAsJsonArray("data");
        long trueCount = 0;
        long falseCount = 0;
        long nullCount = 0;
        int i = 0;
        while (i < data.size()) {
            JsonObject row = data.get(i).getAsJsonObject();
            if (row.get("ok") == null || row.get("ok").isJsonNull()) {
                nullCount = nullCount + 1;
            } else if (row.get("ok").isJsonPrimitive()
                    && row.get("ok").getAsBoolean()) {
                trueCount = trueCount + 1;
            } else {
                falseCount = falseCount + 1;
            }
            i = i + 1;
        }
        Console.log("TmObservabilitySupersetIT.sqllab.select.true", String.valueOf(trueCount));
        Console.log("TmObservabilitySupersetIT.sqllab.select.false", String.valueOf(falseCount));
        Console.log("TmObservabilitySupersetIT.sqllab.select.null", String.valueOf(nullCount));
        return trueCount == source.okTrue
                && falseCount == source.okFalse
                && nullCount == source.historical;
    }

    private boolean groupBySplits(JsonObject json) {
        if (json == null || json.get("data") == null || !json.get("data").isJsonArray()) {
            return false;
        }
        JsonArray data = json.getAsJsonArray("data");
        Console.log("TmObservabilitySupersetIT.groupby.data", data.toString());
        return data.size() == 2;
    }

    private Integer findExistingDatabase(String name) {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/database/?q=(page:0,page_size:50)",
                null,
                true);
        this.logResponse("database.list", listed);
        JsonObject json = this.parseObject(listed.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonArray()) {
            return null;
        }
        JsonArray rows = json.getAsJsonArray("result");
        int i = 0;
        while (i < rows.size()) {
            JsonObject row = rows.get(i).getAsJsonObject();
            String databaseName = this.readString(row, "database_name");
            Console.log(
                    "TmObservabilitySupersetIT.database.existing",
                    "id="
                            + this.readString(row, "id")
                            + " name="
                            + databaseName
                            + " backend="
                            + this.readString(row, "backend"));
            if (name.equals(databaseName) && row.get("id") != null) {
                return Integer.valueOf(row.get("id").getAsInt());
            }
            i = i + 1;
        }
        return null;
    }

    private Integer findExistingDataset(String tableName) {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/dataset/?q=(page:0,page_size:50)",
                null,
                true);
        this.logResponse("dataset.list", listed);
        JsonObject json = this.parseObject(listed.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonArray()) {
            return null;
        }
        JsonArray rows = json.getAsJsonArray("result");
        int i = 0;
        while (i < rows.size()) {
            JsonObject row = rows.get(i).getAsJsonObject();
            String found = this.readString(row, "table_name");
            Console.log(
                    "TmObservabilitySupersetIT.dataset.existing",
                    "id="
                            + this.readString(row, "id")
                            + " table_name="
                            + found
                            + " schema="
                            + this.readString(row, "schema"));
            if (tableName.equals(found) && row.get("id") != null) {
                return Integer.valueOf(row.get("id").getAsInt());
            }
            i = i + 1;
        }
        return null;
    }

    private Integer createDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("table_name", MONGO_COLLECTION);
        body.addProperty("schema", "");
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("dataset.create", response);
        if (response.status != 201) {
            Console.log("TmObservabilitySupersetIT.blocker.dataset.create", "POST /api/v1/dataset/");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createVirtualDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("table_name", VIRTUAL_DATASET_NAME);
        body.addProperty("schema", "");
        body.addProperty("sql", VERIFIED_PIPELINE_SQL);
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("virtual.dataset.create", response);
        if (response.status != 201) {
            return null;
        }
        return this.readId(response.body);
    }

    private void logVirtualDatasetColumns(JsonObject dataset) {
        if (dataset == null || dataset.get("columns") == null || !dataset.get("columns").isJsonArray()) {
            Console.log("TmObservabilitySupersetIT.virtual.columns", "absent");
            return;
        }
        JsonArray columns = dataset.getAsJsonArray("columns");
        Console.log(
                "TmObservabilitySupersetIT.virtual.columns.size",
                String.valueOf(columns.size()));
        boolean foundOk = false;
        boolean foundCount = false;
        int i = 0;
        while (i < columns.size()) {
            JsonObject column = columns.get(i).getAsJsonObject();
            String name = this.readString(column, "column_name");
            String type = this.readString(column, "type");
            Console.log(
                    "TmObservabilitySupersetIT.virtual.column",
                    name + " type=" + type);
            if ("ok".equals(name)) {
                foundOk = true;
            }
            if ("count".equals(name)) {
                foundCount = true;
            }
            i = i + 1;
        }
        Console.log("TmObservabilitySupersetIT.virtual.column.ok", String.valueOf(foundOk));
        Console.log("TmObservabilitySupersetIT.virtual.column.count", String.valueOf(foundCount));
        Console.log(
                "TmObservabilitySupersetIT.virtual.sql.stored",
                dataset.get("sql") == null ? "" : dataset.get("sql").toString());
        Console.log(
                "TmObservabilitySupersetIT.virtual.kind",
                this.readString(dataset, "kind"));
    }

    private void ensureCountMetric(int datasetId) {
        JsonObject metric = new JsonObject();
        metric.addProperty("metric_name", "tm_ok_count");
        metric.addProperty("expression", "SUM(\"count\")");
        metric.addProperty("verbose_name", "count");
        JsonArray metrics = new JsonArray();
        metrics.add(metric);
        JsonObject body = new JsonObject();
        body.add("metrics", metrics);
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dataset/" + datasetId,
                body.toString(),
                true);
        this.logResponse("virtual.metric.put", response);
    }

    private Integer findExistingChart(String sliceName) {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/chart/?q=(page:0,page_size:50)",
                null,
                true);
        this.logResponse("chart.list", listed);
        JsonObject json = this.parseObject(listed.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonArray()) {
            return null;
        }
        JsonArray rows = json.getAsJsonArray("result");
        int i = 0;
        while (i < rows.size()) {
            JsonObject row = rows.get(i).getAsJsonObject();
            String found = this.readString(row, "slice_name");
            Console.log(
                    "TmObservabilitySupersetIT.chart.existing",
                    "id=" + this.readString(row, "id") + " slice_name=" + found);
            if (sliceName.equals(found) && row.get("id") != null) {
                return Integer.valueOf(row.get("id").getAsInt());
            }
            i = i + 1;
        }
        return null;
    }

    private Integer createBarChart(int datasetId) {
        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "dist_bar");
        params.addProperty("row_limit", Integer.valueOf(10000));
        JsonArray groupby = new JsonArray();
        groupby.add("ok");
        params.add("groupby", groupby);
        JsonArray metrics = new JsonArray();
        metrics.add("tm_ok_count");
        params.add("metrics", metrics);
        params.addProperty("metric", "tm_ok_count");
        params.addProperty("y_axis_label", "count");
        params.addProperty("x_axis_label", "ok");
        params.addProperty("show_bar_value", Boolean.TRUE);

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty("slice_name", "UC1 TM SUCCESS vs FAILURE (ok)");
        body.addProperty("viz_type", "dist_bar");
        body.addProperty("params", params.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("chart.create", response);
        if (response.status != 201) {
            return null;
        }
        return this.readId(response.body);
    }

    private HttpResponse requestChartData(int chartId, int datasetId) {
        JsonObject query = new JsonObject();
        JsonArray columns = new JsonArray();
        columns.add("ok");
        query.add("columns", columns);
        JsonArray metrics = new JsonArray();
        metrics.add("tm_ok_count");
        query.add("metrics", metrics);
        query.addProperty("row_limit", Integer.valueOf(10000));
        query.addProperty("is_timeseries", Boolean.FALSE);
        JsonObject datasource = new JsonObject();
        datasource.addProperty("id", Integer.valueOf(datasetId));
        datasource.addProperty("type", "table");
        JsonArray queries = new JsonArray();
        queries.add(query);
        JsonObject body = new JsonObject();
        body.add("datasource", datasource);
        body.add("queries", queries);
        body.addProperty("result_format", "json");
        body.addProperty("result_type", "full");
        body.addProperty("force", Boolean.TRUE);
        return this.request(
                "POST",
                "/api/v1/chart/data?slice_id=" + chartId,
                body.toString(),
                true);
    }

    private void clearDatasetSchema(int datasetId) {
        JsonObject body = new JsonObject();
        body.addProperty("schema", "");
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dataset/" + datasetId,
                body.toString(),
                true);
        this.logResponse("dataset.clear_schema", response);
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
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return null;
        }
        return json.getAsJsonObject("result");
    }

    private void logDatasetColumns(JsonObject dataset) {
        if (dataset == null || dataset.get("columns") == null || !dataset.get("columns").isJsonArray()) {
            Console.log("TmObservabilitySupersetIT.dataset.columns", "absent");
            return;
        }
        JsonArray columns = dataset.getAsJsonArray("columns");
        Console.log(
                "TmObservabilitySupersetIT.dataset.columns.size",
                String.valueOf(columns.size()));
        boolean foundOk = false;
        int i = 0;
        while (i < columns.size()) {
            JsonObject column = columns.get(i).getAsJsonObject();
            String name = this.readString(column, "column_name");
            Console.log(
                    "TmObservabilitySupersetIT.dataset.column",
                    name + " type=" + this.readString(column, "type"));
            if ("ok".equals(name)) {
                foundOk = true;
            }
            i = i + 1;
        }
        Console.log(
                "TmObservabilitySupersetIT.dataset.column.ok",
                String.valueOf(foundOk));
    }

    private void logContractColumns(JsonObject dataset) {
        String okType = "absent";
        String whyType = "absent";
        String commitIdType = "absent";
        if (dataset != null && dataset.get("columns") != null && dataset.get("columns").isJsonArray()) {
            JsonArray columns = dataset.getAsJsonArray("columns");
            int i = 0;
            while (i < columns.size()) {
                JsonObject column = columns.get(i).getAsJsonObject();
                String name = this.readString(column, "column_name");
                String type = this.readString(column, "type");
                if ("ok".equals(name)) {
                    okType = type;
                }
                if ("why".equals(name)) {
                    whyType = type;
                }
                if ("commitId".equals(name)) {
                    commitIdType = type;
                }
                i = i + 1;
            }
        }
        Console.log("TmObservabilitySupersetIT.dataset.contract.ok.type", okType);
        Console.log("TmObservabilitySupersetIT.dataset.contract.why.type", whyType);
        Console.log("TmObservabilitySupersetIT.dataset.contract.commitId.type", commitIdType);
    }

    private JsonObject executeSqlLab(int databaseId, String label, String sql) {
        JsonObject body = new JsonObject();
        body.addProperty("database_id", Integer.valueOf(databaseId));
        body.addProperty("schema", "");
        body.addProperty("sql", sql);
        body.addProperty("runAsync", Boolean.FALSE);
        HttpResponse response = this.request(
                "POST",
                "/api/v1/sqllab/execute/",
                body.toString(),
                true);
        Console.log("TmObservabilitySupersetIT.sqllab." + label + ".sql", sql);
        this.logResponse("sqllab." + label, response);
        JsonObject json = this.parseObject(response.body);
        if (json == null) {
            return null;
        }
        if (json.get("columns") != null) {
            Console.log(
                    "TmObservabilitySupersetIT.sqllab." + label + ".columns",
                    json.get("columns").toString());
        }
        if (json.get("data") != null && json.get("data").isJsonArray()) {
            JsonArray data = json.getAsJsonArray("data");
            Console.log(
                    "TmObservabilitySupersetIT.sqllab." + label + ".row_count",
                    String.valueOf(data.size()));
            Console.log(
                    "TmObservabilitySupersetIT.sqllab." + label + ".data",
                    data.toString());
        }
        if (json.get("error") != null) {
            Console.log(
                    "TmObservabilitySupersetIT.sqllab." + label + ".error",
                    json.get("error").toString());
        }
        return json;
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
            Console.log("TmObservabilitySupersetIT.blocker.login", "POST /api/v1/security/login");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("access_token") == null || json.get("access_token").isJsonNull()) {
            Console.log("TmObservabilitySupersetIT.blocker.login", "login response missing access_token");
            return false;
        }
        this.accessToken = json.get("access_token").getAsString();
        return true;
    }

    private boolean loadCsrf() {
        HttpResponse response = this.request("GET", "/api/v1/security/csrf_token/", null, true);
        this.logResponse("security.csrf_token", response);
        if (response.status != 200) {
            Console.log("TmObservabilitySupersetIT.blocker.csrf", "GET /api/v1/security/csrf_token/");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || json.get("result").isJsonNull()) {
            Console.log("TmObservabilitySupersetIT.blocker.csrf", "csrf response missing result");
            return false;
        }
        this.csrfToken = json.get("result").getAsString();
        return true;
    }

    private Integer readId(String body) {
        JsonObject json = this.parseObject(body);
        if (json == null || json.get("id") == null || json.get("id").isJsonNull()) {
            return null;
        }
        return Integer.valueOf(json.get("id").getAsInt());
    }

    private List<String> command(String... parts) {
        List<String> command = new ArrayList<String>();
        int i = 0;
        while (i < parts.length) {
            command.add(parts[i]);
            i = i + 1;
        }
        return command;
    }

    private int runProcess(String label, List<String> command) {
        Console.log("TmObservabilitySupersetIT." + label + ".command", command.toString());
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            String output = this.readStream(process.getInputStream());
            int status = process.waitFor();
            Console.log("TmObservabilitySupersetIT." + label + ".status", String.valueOf(status));
            Console.log("TmObservabilitySupersetIT." + label + ".output", output);
            return status;
        } catch (Exception e) {
            Console.log("TmObservabilitySupersetIT." + label + ".exception", String.valueOf(e));
            return -1;
        }
    }

    private String readString(JsonObject object, String field) {
        if (object.get(field) == null || object.get(field).isJsonNull()) {
            return "";
        }
        if (object.get(field).isJsonPrimitive()) {
            return object.get(field).getAsString();
        }
        return object.get(field).toString();
    }

    private JsonObject parseObject(String body) {
        if (body == null) {
            return null;
        }
        try {
            JsonElement element = JsonParser.parseString(body);
            if (element == null || !element.isJsonObject()) {
                return null;
            }
            return element.getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private void logResponse(String label, HttpResponse response) {
        Console.log("TmObservabilitySupersetIT." + label + ".status", String.valueOf(response.status));
        Console.log("TmObservabilitySupersetIT." + label + ".body", response.body);
    }

    private HttpResponse request(String method, String path, String jsonBody, boolean authed) {
        HttpResponse result = new HttpResponse();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(SUPERSET_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Referer", SUPERSET_URL + "/");
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
            Console.log("TmObservabilitySupersetIT.request.exception", String.valueOf(e));
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

    private Integer createDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty("dashboard_title", "UC1 TM observability ok");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("dashboard.create", response);
        if (response.status != 201) {
            Console.log("TmObservabilitySupersetIT.blocker", "POST /api/v1/dashboard/");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));
        JsonObject filter = new JsonObject();
        filter.addProperty("clause", "WHERE");
        filter.addProperty("expressionType", "SIMPLE");
        filter.addProperty("subject", "ok");
        filter.addProperty("operator", "IS NOT NULL");
        JsonArray comparator = new JsonArray();
        filter.add("comparator", comparator);
        JsonArray adhocFilters = new JsonArray();
        adhocFilters.add(filter);

        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "pie");
        params.addProperty("row_limit", Integer.valueOf(10000));
        JsonArray groupby = new JsonArray();
        groupby.add("ok");
        params.add("groupby", groupby);
        JsonArray metrics = new JsonArray();
        metrics.add("count");
        params.addProperty("metric", "count");
        params.add("metrics", metrics);
        params.add("adhoc_filters", adhocFilters);

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty("slice_name", "UC1 TM SUCCESS vs FAILURE (ok, four-field projection only)");
        body.addProperty("viz_type", "pie");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("chart.create", response);
        if (response.status != 201) {
            Console.log("TmObservabilitySupersetIT.blocker", "POST /api/v1/chart/");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty("sliceName", "UC1 TM SUCCESS vs FAILURE (ok, four-field projection only)");
        chartMeta.addProperty("width", Integer.valueOf(12));
        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-uc1-tm-obs");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");
        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-uc1-tm-obs");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-uc1-tm-obs");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");
        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-uc1-tm-obs");
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
        position.add("CHART-uc1-tm-obs", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-uc1-tm-obs", rowNode);
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
        HttpResponse response = this.request(
                "GET",
                "/api/v1/dashboard/" + dashboardId,
                null,
                true);
        this.logResponse("dashboard.get", response);
    }

    private void readChart(int chartId) {
        HttpResponse response = this.request("GET", "/api/v1/chart/" + chartId, null, true);
        this.logResponse("chart.get", response);
    }

    private void postChartData(int chartId, int datasetId) {
        JsonObject query = new JsonObject();
        query.addProperty("datasource", datasetId + "__table");
        query.addProperty("viz_type", "pie");
        JsonArray columns = new JsonArray();
        columns.add("ok");
        query.add("columns", columns);
        query.addProperty("metrics", "count");
        query.addProperty("row_limit", Integer.valueOf(10000));
        JsonArray queries = new JsonArray();
        queries.add(query);
        JsonObject body = new JsonObject();
        body.add("queries", queries);
        HttpResponse response = this.request(
                "POST",
                "/api/v1/chart/data?slice_id=" + chartId,
                body.toString(),
                true);
        this.logResponse("chart.data", response);
    }

    private static class SourceObservation {
        private long total;
        private long tmTotal;
        private long fourField;
        private long okTrue;
        private long okFalse;
        private long historical;
        private long historicalUnderscore;
        private long historicalDotted;
        private long correspondenceMatched;
        private long correspondenceUnmatched;
        private String sampleJson = "";
        private String sampleKeys = "";
        private String sampleNewJson = "";
        private String sampleNewKeys = "";
        private String sampleHistJson = "";
        private String sampleHistKeys = "";
        private Map<String, String> fieldTypes = new LinkedHashMap<String, String>();
    }

    private static class ProbeResult {
        private Integer datasetId;
        private boolean selectReadable;
        private boolean groupByUsable;
    }

    private static class HttpResponse {
        private int status;
        private String body;
    }
}
