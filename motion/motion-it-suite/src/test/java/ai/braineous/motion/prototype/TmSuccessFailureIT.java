package ai.braineous.motion.prototype;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@QuarkusTest
public class TmSuccessFailureIT {

    private static final String SUPERSET_URL = "http://localhost:8099";

    private static final String MONGO_DATABASE = "motion";

    private static final String ANALYSIS_COLLECTION = "analysis_store";

    private static final String MUTATION_COLLECTION = "mutation_store";

    @Inject
    MongoClient mongoClient;

    private String accessToken;

    private String csrfToken;

    private String sessionCookie;

    @Test
    public void test_1() {
        Console.log(
                "TmSuccessFailureIT.test_1.goal",
                "observe TM-authoritative SUCCESS vs FAILURE from real persisted Motion Mongo through native Superset 6.0.0 + PyMongoSQL");
        Console.log(
                "TmSuccessFailureIT.test_1.disclaimer",
                "read-only Mongo; disposable Superset 8099 only; no original 8088 writes; no assertions; no synthetic TM rows");
        Console.log(
                "TmSuccessFailureIT.tm.contract",
                "AnalysisOrchestrator persists TxExecutionResult.toJson() as SystemAnalysis.cognitiveResultJson into motion.analysis_store; TxExecutionResult.isApproved() is gateResult.isOk(); MutationOrchestrator does not persist");

        SourceVerification source = this.verifyPersistedTmOutcomes();
        Console.log(
                "TmSuccessFailureIT.source.verified",
                String.valueOf(source.verified));
        Console.log(
                "TmSuccessFailureIT.source.analysis_store.count",
                String.valueOf(source.analysisCount));
        Console.log(
                "TmSuccessFailureIT.source.success_count",
                String.valueOf(source.successCount));
        Console.log(
                "TmSuccessFailureIT.source.failure_count",
                String.valueOf(source.failureCount));
        Console.log(
                "TmSuccessFailureIT.source.ok_true_count",
                String.valueOf(source.okTrueCount));
        Console.log(
                "TmSuccessFailureIT.source.ok_false_count",
                String.valueOf(source.okFalseCount));
        Console.log(
                "TmSuccessFailureIT.source.exact_values",
                source.exactValues);

        if (!source.verified) {
            Console.log(
                    "TmSuccessFailureIT.blocker",
                    source.blocker);
            Console.log(
                    "TmSuccessFailureIT.step_2",
                    "skipped; TM SUCCESS/FAILURE is not persisted and queryable in live Mongo");
            return;
        }

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }

        Integer databaseId = this.reuseOrRegisterMongoDatabase();
        if (databaseId == null) {
            return;
        }

        Integer datasetId = this.createAnalysisDataset(databaseId.intValue());
        if (datasetId == null) {
            return;
        }
        this.clearDatasetSchema(datasetId.intValue());
        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }

        Integer dashboardId = this.createDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer chartId = this.createOutcomeChart(
                datasetId.intValue(),
                dataset,
                dashboardId.intValue(),
                source.groupbyColumn);
        if (chartId == null) {
            return;
        }
        this.placeChartOnDashboard(dashboardId.intValue(), chartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(chartId.intValue());
        this.postChartData(chartId.intValue(), datasetId.intValue(), source.groupbyColumn);
        Console.log(
                "TmSuccessFailureIT.browser.url",
                SUPERSET_URL + "/superset/dashboard/" + dashboardId.intValue() + "/");
    }

    private SourceVerification verifyPersistedTmOutcomes() {
        SourceVerification source = new SourceVerification();
        MongoDatabase database = this.mongoClient.getDatabase(MONGO_DATABASE);

        MongoCursor<String> collections = database.listCollectionNames().iterator();
        while (collections.hasNext()) {
            String collectionName = collections.next();
            long count = database.getCollection(collectionName).countDocuments();
            Console.log(
                    "TmSuccessFailureIT.mongo.collection",
                    MONGO_DATABASE + "." + collectionName + " count=" + count);
        }

        MongoCollection<Document> analysisStore =
                database.getCollection(ANALYSIS_COLLECTION);
        source.analysisCount = analysisStore.countDocuments();
        Console.log(
                "TmSuccessFailureIT.analysis_store.count",
                String.valueOf(source.analysisCount));

        boolean mutationExists = this.collectionExists(database, MUTATION_COLLECTION);
        if (mutationExists) {
            long mutationCount = database.getCollection(MUTATION_COLLECTION).countDocuments();
            Console.log(
                    "TmSuccessFailureIT.mutation_store.count",
                    String.valueOf(mutationCount));
        } else {
            Console.log(
                    "TmSuccessFailureIT.mutation_store",
                    "collection absent; MutationOrchestrator does not persist TxExecutionResult");
        }

        this.logNonTmStatusFields(database);

        if (source.analysisCount == 0) {
            source.verified = false;
            source.exactValues = "none; analysis_store is empty";
            source.blocker =
                    "motion.analysis_store count=0; TM-authoritative TxExecutionResult / cognitiveResultJson is not persisted in live Mongo; operational_view.state.status and motion_happening_store.status are OPEN frame state, not TM SUCCESS/FAILURE; replaySignal.replayLevel FAILURE_RECOVERY is not TM outcome";
            return source;
        }

        MongoCursor<Document> cursor = analysisStore.find().iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            this.observeAnalysisDocument(document, source);
        }

        Console.log(
                "TmSuccessFailureIT.tm.groupby_column",
                source.groupbyColumn);
        if (source.successCount + source.failureCount > 0) {
            source.verified = true;
            source.exactValues =
                    "SUCCESS="
                            + source.successCount
                            + " FAILURE="
                            + source.failureCount;
            return source;
        }
        if (source.okTrueCount + source.okFalseCount > 0) {
            source.verified = false;
            source.exactValues =
                    "gateResult.ok true="
                            + source.okTrueCount
                            + " false="
                            + source.okFalseCount
                            + "; literals SUCCESS/FAILURE not present";
            source.blocker =
                    "analysis_store has TxExecutionResult JSON with gateResult.ok boolean; TM does not persist SUCCESS/FAILURE string values on those records; Use Case #1 will not map ok to SUCCESS/FAILURE";
            return source;
        }

        source.verified = false;
        source.exactValues = "no SUCCESS/FAILURE literals and no gateResult.ok on stored cognitiveResultJson";
        source.blocker =
                "motion.analysis_store has documents but no queryable TM SUCCESS/FAILURE values and no gateResult.ok";
        return source;
    }

    private void observeAnalysisDocument(Document document, SourceVerification source) {
        Console.log(
                "TmSuccessFailureIT.analysis.doc.keys",
                String.valueOf(document.keySet()));
        Object cognitive = document.get("cognitiveResultJson");
        if (cognitive == null) {
            Console.log(
                    "TmSuccessFailureIT.analysis.doc.cognitiveResultJson",
                    "absent");
            return;
        }
        Console.log(
                "TmSuccessFailureIT.analysis.doc.cognitiveResultJson.type",
                cognitive.getClass().getName());
        String jsonText;
        if (cognitive instanceof Document) {
            jsonText = ((Document) cognitive).toJson();
        } else {
            jsonText = String.valueOf(cognitive);
        }
        Console.log(
                "TmSuccessFailureIT.analysis.doc.cognitiveResultJson",
                jsonText);

        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(jsonText);
        } catch (Exception e) {
            Console.log(
                    "TmSuccessFailureIT.analysis.doc.parse.exception",
                    String.valueOf(e));
            return;
        }
        if (parsed == null || !parsed.isJsonObject()) {
            Console.log(
                    "TmSuccessFailureIT.analysis.doc.parse",
                    "cognitiveResultJson is not a JSON object");
            return;
        }
        JsonObject tx = parsed.getAsJsonObject();
        String status = this.readString(tx, "status");
        Console.log("TmSuccessFailureIT.analysis.doc.tx.status", status);
        if ("SUCCESS".equals(status)) {
            source.successCount = source.successCount + 1;
            source.groupbyColumn = "status";
        } else if ("FAILURE".equals(status)) {
            source.failureCount = source.failureCount + 1;
            source.groupbyColumn = "status";
        }

        JsonObject gate = null;
        if (tx.get("gateResult") != null && tx.get("gateResult").isJsonObject()) {
            gate = tx.getAsJsonObject("gateResult");
        }
        if (gate != null && gate.get("ok") != null && gate.get("ok").isJsonPrimitive()) {
            boolean ok = gate.get("ok").getAsBoolean();
            Console.log(
                    "TmSuccessFailureIT.analysis.doc.gateResult.ok",
                    String.valueOf(ok));
            if (ok) {
                source.okTrueCount = source.okTrueCount + 1;
            } else {
                source.okFalseCount = source.okFalseCount + 1;
            }
        }
    }

    private void logNonTmStatusFields(MongoDatabase database) {
        if (this.collectionExists(database, "operational_view")) {
            MongoCollection<Document> views = database.getCollection("operational_view");
            Console.log(
                    "TmSuccessFailureIT.operational_view.count",
                    String.valueOf(views.countDocuments()));
            Document sample = views.find().first();
            if (sample != null) {
                Console.log(
                        "TmSuccessFailureIT.operational_view.sample.keys",
                        String.valueOf(sample.keySet()));
                Object state = sample.get("state");
                Console.log(
                        "TmSuccessFailureIT.operational_view.sample.state",
                        String.valueOf(state));
            }
            Console.log(
                    "TmSuccessFailureIT.operational_view.not_tm",
                    "state.status OPEN is Motion frame status, not TxExecutionResult SUCCESS/FAILURE");
        }
        if (this.collectionExists(database, "motion_happening_store")) {
            MongoCollection<Document> happenings =
                    database.getCollection("motion_happening_store");
            Console.log(
                    "TmSuccessFailureIT.motion_happening_store.count",
                    String.valueOf(happenings.countDocuments()));
            Document sample = happenings.find().first();
            if (sample != null) {
                Console.log(
                        "TmSuccessFailureIT.motion_happening_store.sample.status",
                        String.valueOf(sample.get("status")));
            }
            Console.log(
                    "TmSuccessFailureIT.motion_happening_store.not_tm",
                    "status OPEN and nested replaySignal.replayLevel FAILURE_RECOVERY are not TM-authoritative transaction outcomes");
        }
    }

    private boolean collectionExists(MongoDatabase database, String name) {
        MongoCursor<String> collections = database.listCollectionNames().iterator();
        while (collections.hasNext()) {
            if (name.equals(collections.next())) {
                return true;
            }
        }
        return false;
    }

    private Integer reuseOrRegisterMongoDatabase() {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/database/?q=(page:0,page_size:50)",
                null,
                true);
        this.logResponse("database.list", listed);
        JsonObject json = this.parseObject(listed.body);
        if (json != null && json.get("result") != null && json.get("result").isJsonArray()) {
            JsonArray rows = json.getAsJsonArray("result");
            int i = 0;
            while (i < rows.size()) {
                JsonObject row = rows.get(i).getAsJsonObject();
                String backend = this.readString(row, "backend");
                String uri = this.readString(row, "sqlalchemy_uri");
                Console.log(
                        "TmSuccessFailureIT.database.existing",
                        "id="
                                + this.readString(row, "id")
                                + " name="
                                + this.readString(row, "database_name")
                                + " backend="
                                + backend
                                + " sqlalchemy_uri="
                                + uri);
                if ("mongodb".equals(backend) && row.get("id") != null) {
                    int id = row.get("id").getAsInt();
                    Console.log(
                            "TmSuccessFailureIT.database.reuse",
                            String.valueOf(id));
                    return Integer.valueOf(id);
                }
                i = i + 1;
            }
        }

        JsonObject testBody = new JsonObject();
        testBody.addProperty("database_name", "motion-mongo-gate2b");
        testBody.addProperty("sqlalchemy_uri", "mongodb://motion-mongo-1:27017/motion");
        HttpResponse test = this.request(
                "POST",
                "/api/v1/database/test_connection/",
                testBody.toString(),
                true);
        this.logResponse("database.test_connection", test);
        if (test.status != 200) {
            Console.log(
                    "TmSuccessFailureIT.blocker",
                    "POST /api/v1/database/test_connection/");
            return null;
        }

        JsonObject extra = new JsonObject();
        extra.addProperty("engine_params", "{}");
        JsonObject create = new JsonObject();
        create.addProperty("database_name", "motion-mongo-uc1");
        create.addProperty("sqlalchemy_uri", "mongodb://motion-mongo-1:27017/motion");
        create.addProperty("expose_in_sqllab", Boolean.TRUE);
        HttpResponse created = this.request(
                "POST",
                "/api/v1/database/",
                create.toString(),
                true);
        this.logResponse("database.create", created);
        if (created.status != 201) {
            Console.log("TmSuccessFailureIT.blocker", "POST /api/v1/database/");
            return null;
        }
        return this.readId(created.body);
    }

    private Integer createAnalysisDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("table_name", ANALYSIS_COLLECTION);
        body.addProperty("schema", "");
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("dataset.create", response);
        if (response.status != 201) {
            Console.log("TmSuccessFailureIT.blocker", "POST /api/v1/dataset/");
            return null;
        }
        return this.readId(response.body);
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
            Console.log("TmSuccessFailureIT.blocker", "GET /api/v1/dataset/{pk}");
            return null;
        }
        return json.getAsJsonObject("result");
    }

    private Integer createDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty("dashboard_title", "UC1 TM SUCCESS vs FAILURE (Mongo)");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("dashboard.create", response);
        if (response.status != 201) {
            Console.log("TmSuccessFailureIT.blocker", "POST /api/v1/dashboard/");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createOutcomeChart(
            int datasetId,
            JsonObject dataset,
            int dashboardId,
            String groupbyColumn) {
        if (groupbyColumn == null || groupbyColumn.length() == 0) {
            Console.log(
                    "TmSuccessFailureIT.blocker",
                    "no TM SUCCESS/FAILURE column for native groupby");
            return null;
        }
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty("slice_name", "UC1 TM SUCCESS vs FAILURE");
        body.addProperty("viz_type", "pie");
        body.add("dashboards", dashboards);
        body.addProperty(
                "params",
                this.pieParams(datasetId, dataset, groupbyColumn).toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("chart.create", response);
        if (response.status != 201) {
            Console.log("TmSuccessFailureIT.blocker", "POST /api/v1/chart/");
            return null;
        }
        return this.readId(response.body);
    }

    private JsonObject pieParams(int datasetId, JsonObject dataset, String groupbyColumn) {
        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "pie");
        params.addProperty("row_limit", Integer.valueOf(10000));
        JsonArray groupby = new JsonArray();
        groupby.add(groupbyColumn);
        params.add("groupby", groupby);
        JsonArray metrics = new JsonArray();
        metrics.add("count");
        params.addProperty("metric", "count");
        params.add("metrics", metrics);
        params.add("adhoc_filters", new JsonArray());
        return params;
    }

    private void placeChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty("sliceName", "UC1 TM SUCCESS vs FAILURE");
        chartMeta.addProperty("width", Integer.valueOf(12));

        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-uc1-tm");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");

        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-uc1-tm");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-uc1-tm");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-uc1-tm");
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
        position.add("CHART-uc1-tm", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-uc1-tm", rowNode);
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

    private void postChartData(int chartId, int datasetId, String groupbyColumn) {
        JsonObject query = new JsonObject();
        query.addProperty("datasource", datasetId + "__table");
        query.addProperty("viz_type", "pie");
        JsonArray columns = new JsonArray();
        columns.add(groupbyColumn);
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

    private boolean login() {
        JsonObject body = new JsonObject();
        body.addProperty("username", "admin");
        body.addProperty("password", "admin");
        body.addProperty("provider", "db");
        body.addProperty("refresh", Boolean.TRUE);
        HttpResponse response = this.request(
                "POST",
                "/api/v1/security/login",
                body.toString(),
                false);
        this.logResponse("security.login", response);
        if (response.status != 200) {
            Console.log("TmSuccessFailureIT.blocker", "POST /api/v1/security/login");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("access_token") == null || json.get("access_token").isJsonNull()) {
            Console.log("TmSuccessFailureIT.blocker", "login response missing access_token");
            return false;
        }
        this.accessToken = json.get("access_token").getAsString();
        Console.log("TmSuccessFailureIT.access_token.present", "true");
        return true;
    }

    private boolean loadCsrf() {
        HttpResponse response = this.request("GET", "/api/v1/security/csrf_token/", null, true);
        this.logResponse("security.csrf_token", response);
        if (response.status != 200) {
            Console.log("TmSuccessFailureIT.blocker", "GET /api/v1/security/csrf_token/");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || json.get("result").isJsonNull()) {
            Console.log("TmSuccessFailureIT.blocker", "csrf response missing result");
            return false;
        }
        this.csrfToken = json.get("result").getAsString();
        Console.log("TmSuccessFailureIT.csrf.present", "true");
        return true;
    }

    private Integer readId(String body) {
        JsonObject json = this.parseObject(body);
        if (json == null || json.get("id") == null || json.get("id").isJsonNull()) {
            return null;
        }
        return Integer.valueOf(json.get("id").getAsInt());
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
        JsonElement element = JsonParser.parseString(body);
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        return element.getAsJsonObject();
    }

    private void logResponse(String label, HttpResponse response) {
        Console.log("TmSuccessFailureIT." + label + ".status", String.valueOf(response.status));
        Console.log("TmSuccessFailureIT." + label + ".body", response.body);
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
            Console.log("TmSuccessFailureIT.request.exception", String.valueOf(e));
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

    private static class SourceVerification {
        private boolean verified;
        private long analysisCount;
        private long successCount;
        private long failureCount;
        private long okTrueCount;
        private long okFalseCount;
        private String exactValues = "";
        private String blocker = "";
        private String groupbyColumn = "";
    }

    private static class HttpResponse {
        private int status;
        private String body;
    }
}
