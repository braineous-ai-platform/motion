package ai.braineous.motion.prototype;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.junit.QuarkusTest;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@QuarkusTest
public class TmExecutionResultsSupersetIT {

    private static final String SUPERSET_URL = "http://localhost:8099";

    private static final String BEAM_MONGO_URI = "mongodb://localhost:27017";

    private static final String SUPERSET_MONGO_URI = "mongodb://host.docker.internal:27017/cgo";

    private static final String MONGO_DATABASE = "cgo";

    private static final String MONGO_COLLECTION = "tm_execution_results";

    private String accessToken;

    private String csrfToken;

    private String sessionCookie;

    @Test
    public void test_1() {
        Console.log(
                "TmExecutionResultsSupersetIT.test_1.goal",
                "observe Beam TM SUCCESS/FAILURE from live cgo.tm_execution_results through disposable Superset 6.0.0 + PyMongoSQL on 8099");
        Console.log(
                "TmExecutionResultsSupersetIT.test_1.disclaimer",
                "read-only Mongo; no 8088 writes; no assertions; gateResult.ok is the only TM authority");
        Console.log(
                "TmExecutionResultsSupersetIT.source.uri",
                BEAM_MONGO_URI + "/" + MONGO_DATABASE + "." + MONGO_COLLECTION);
        Console.log(
                "TmExecutionResultsSupersetIT.superset.uri",
                SUPERSET_MONGO_URI);

        SourceVerification source = this.verifyBeamRecords();
        Console.log(
                "TmExecutionResultsSupersetIT.source.verified",
                String.valueOf(source.verified));
        Console.log(
                "TmExecutionResultsSupersetIT.source.total",
                String.valueOf(source.total));
        Console.log(
                "TmExecutionResultsSupersetIT.source.success_true",
                String.valueOf(source.okTrue));
        Console.log(
                "TmExecutionResultsSupersetIT.source.failure_false",
                String.valueOf(source.okFalse));
        Console.log(
                "TmExecutionResultsSupersetIT.source.missing_ok",
                String.valueOf(source.missingOk));
        Console.log(
                "TmExecutionResultsSupersetIT.source.sample",
                source.sample);

        if (!source.verified) {
            Console.log("TmExecutionResultsSupersetIT.blocker", source.blocker);
            return;
        }

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }

        this.inspectExistingDatabases();
        Integer databaseId = this.reuseOrRegisterCgoDatabase();
        if (databaseId == null) {
            return;
        }

        Integer datasetId = this.createDataset(databaseId.intValue());
        if (datasetId == null) {
            return;
        }
        this.clearDatasetSchema(datasetId.intValue());
        this.refreshDataset(datasetId.intValue());
        JsonObject dataset = this.readDataset(datasetId.intValue());
        if (dataset == null) {
            return;
        }
        this.logDatasetColumns(dataset);

        boolean grouped = this.probeNativeGroupBy(databaseId.intValue(), source.okTrue, source.okFalse);
        if (!grouped) {
            Console.log(
                    "TmExecutionResultsSupersetIT.blocker",
                    "nested boolean gateResult.ok can be SELECTed and filtered (TRUE="
                            + source.okTrue
                            + " FALSE="
                            + source.okFalse
                            + ") but native GROUP BY gateResult.ok does not split SUCCESS/FAILURE counts; dataset columns expose gateResult as a nested object, not gateResult.ok; no bridge and no Mongo rewrite authorized");
            Console.log(
                    "TmExecutionResultsSupersetIT.dataset_id",
                    String.valueOf(datasetId.intValue()));
            Console.log(
                    "TmExecutionResultsSupersetIT.database_id",
                    String.valueOf(databaseId.intValue()));
            return;
        }

        Integer dashboardId = this.createDashboard();
        if (dashboardId == null) {
            return;
        }
        Integer chartId = this.createChart(datasetId.intValue(), dashboardId.intValue());
        if (chartId == null) {
            return;
        }
        this.placeChartOnDashboard(dashboardId.intValue(), chartId.intValue());
        this.readDashboard(dashboardId.intValue());
        this.readChart(chartId.intValue());
        this.postChartData(chartId.intValue(), datasetId.intValue());
        Console.log(
                "TmExecutionResultsSupersetIT.browser.url",
                SUPERSET_URL + "/superset/dashboard/" + dashboardId.intValue() + "/");
    }

    private SourceVerification verifyBeamRecords() {
        SourceVerification source = new SourceVerification();
        com.mongodb.client.MongoClient client = MongoClients.create(BEAM_MONGO_URI);
        try {
            MongoDatabase database = client.getDatabase(MONGO_DATABASE);
            MongoCursor<String> names = database.listCollectionNames().iterator();
            while (names.hasNext()) {
                String name = names.next();
                long count = database.getCollection(name).countDocuments();
                Console.log(
                        "TmExecutionResultsSupersetIT.mongo.collection",
                        MONGO_DATABASE + "." + name + " count=" + count);
            }

            MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION);
            source.total = collection.countDocuments();
            MongoCursor<Document> cursor = collection.find().iterator();
            int index = 0;
            while (cursor.hasNext()) {
                Document document = cursor.next();
                Console.log(
                        "TmExecutionResultsSupersetIT.doc[" + index + "].keys",
                        String.valueOf(document.keySet()));
                Object gate = document.get("gateResult");
                Console.log(
                        "TmExecutionResultsSupersetIT.doc[" + index + "].gateResult",
                        String.valueOf(gate));
                Console.log(
                        "TmExecutionResultsSupersetIT.doc[" + index + "].status",
                        String.valueOf(document.get("status")));
                Console.log(
                        "TmExecutionResultsSupersetIT.doc[" + index + "].description",
                        String.valueOf(document.get("description")));
                Boolean ok = this.readGateOk(gate);
                if (ok == null) {
                    source.missingOk = source.missingOk + 1;
                } else if (ok.booleanValue()) {
                    source.okTrue = source.okTrue + 1;
                } else {
                    source.okFalse = source.okFalse + 1;
                }
                if (index == 0) {
                    source.sample = document.toJson();
                }
                index = index + 1;
            }

            if (source.total == 0) {
                source.verified = false;
                source.blocker = "cgo.tm_execution_results is empty on mongodb://localhost:27017";
                return source;
            }
            if (source.missingOk > 0) {
                source.verified = false;
                source.blocker =
                        "cgo.tm_execution_results has documents without gateResult.ok; cannot classify SUCCESS/FAILURE";
                return source;
            }
            source.verified = true;
            return source;
        } finally {
            client.close();
        }
    }

    private Boolean readGateOk(Object gate) {
        if (!(gate instanceof Document)) {
            return null;
        }
        Document gateDoc = (Document) gate;
        if (!gateDoc.containsKey("ok")) {
            return null;
        }
        Object ok = gateDoc.get("ok");
        if (ok instanceof Boolean) {
            return (Boolean) ok;
        }
        return null;
    }

    private void inspectExistingDatabases() {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/database/?q=(page:0,page_size:50)",
                null,
                true);
        this.logResponse("database.list", listed);
    }

    private Integer reuseOrRegisterCgoDatabase() {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/database/?q=(page:0,page_size:50)",
                null,
                true);
        JsonObject json = this.parseObject(listed.body);
        if (json != null && json.get("result") != null && json.get("result").isJsonArray()) {
            JsonArray rows = json.getAsJsonArray("result");
            int i = 0;
            while (i < rows.size()) {
                JsonObject row = rows.get(i).getAsJsonObject();
                String name = this.readString(row, "database_name");
                Console.log(
                        "TmExecutionResultsSupersetIT.database.existing",
                        "id="
                                + this.readString(row, "id")
                                + " name="
                                + name
                                + " backend="
                                + this.readString(row, "backend"));
                if ("cgo-tm-execution-results".equals(name) && row.get("id") != null) {
                    int id = row.get("id").getAsInt();
                    Console.log(
                            "TmExecutionResultsSupersetIT.database.reuse",
                            String.valueOf(id));
                    return Integer.valueOf(id);
                }
                i = i + 1;
            }
        }

        JsonObject testBody = new JsonObject();
        testBody.addProperty("database_name", "cgo-tm-execution-results");
        testBody.addProperty("sqlalchemy_uri", SUPERSET_MONGO_URI);
        HttpResponse test = this.request(
                "POST",
                "/api/v1/database/test_connection/",
                testBody.toString(),
                true);
        this.logResponse("database.test_connection", test);
        if (test.status != 200) {
            Console.log(
                    "TmExecutionResultsSupersetIT.blocker",
                    "POST /api/v1/database/test_connection/ sqlalchemy_uri=" + SUPERSET_MONGO_URI);
            return null;
        }

        JsonObject create = new JsonObject();
        create.addProperty("database_name", "cgo-tm-execution-results");
        create.addProperty("sqlalchemy_uri", SUPERSET_MONGO_URI);
        create.addProperty("expose_in_sqllab", Boolean.TRUE);
        HttpResponse created = this.request("POST", "/api/v1/database/", create.toString(), true);
        this.logResponse("database.create", created);
        if (created.status != 201) {
            Console.log("TmExecutionResultsSupersetIT.blocker", "POST /api/v1/database/");
            return null;
        }
        return this.readId(created.body);
    }

    private Integer createDataset(int databaseId) {
        JsonObject body = new JsonObject();
        body.addProperty("database", Integer.valueOf(databaseId));
        body.addProperty("table_name", MONGO_COLLECTION);
        body.addProperty("schema", "");
        HttpResponse response = this.request("POST", "/api/v1/dataset/", body.toString(), true);
        this.logResponse("dataset.create", response);
        if (response.status != 201) {
            Console.log("TmExecutionResultsSupersetIT.blocker", "POST /api/v1/dataset/");
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
            Console.log("TmExecutionResultsSupersetIT.blocker", "GET /api/v1/dataset/{pk}");
            return null;
        }
        return json.getAsJsonObject("result");
    }

    private void logDatasetColumns(JsonObject dataset) {
        if (dataset.get("columns") == null || !dataset.get("columns").isJsonArray()) {
            Console.log("TmExecutionResultsSupersetIT.dataset.columns", "absent");
            return;
        }
        JsonArray columns = dataset.getAsJsonArray("columns");
        Console.log(
                "TmExecutionResultsSupersetIT.dataset.columns.size",
                String.valueOf(columns.size()));
        int i = 0;
        while (i < columns.size()) {
            JsonObject column = columns.get(i).getAsJsonObject();
            Console.log(
                    "TmExecutionResultsSupersetIT.dataset.column",
                    this.readString(column, "column_name")
                            + " type="
                            + this.readString(column, "type"));
            i = i + 1;
        }
        Console.log(
                "TmExecutionResultsSupersetIT.dataset.schema",
                this.readString(dataset, "schema"));
    }

    private boolean probeNativeGroupBy(int databaseId, long okTrue, long okFalse) {
        JsonObject body = new JsonObject();
        body.addProperty("database_id", Integer.valueOf(databaseId));
        body.addProperty("schema", "");
        body.addProperty(
                "sql",
                "SELECT gateResult.ok, COUNT(*) FROM " + MONGO_COLLECTION + " GROUP BY gateResult.ok");
        body.addProperty("runAsync", Boolean.FALSE);
        HttpResponse response = this.request(
                "POST",
                "/api/v1/sqllab/execute/",
                body.toString(),
                true);
        this.logResponse("sqllab.groupby.gateResult.ok", response);
        JsonObject json = this.parseObject(response.body);
        if (json == null) {
            return false;
        }
        JsonArray data = null;
        if (json.get("data") != null && json.get("data").isJsonArray()) {
            data = json.getAsJsonArray("data");
        }
        Console.log(
                "TmExecutionResultsSupersetIT.sqllab.groupby.row_count",
                data == null ? "null" : String.valueOf(data.size()));
        if (data == null || data.size() != 2) {
            return false;
        }
        Console.log(
                "TmExecutionResultsSupersetIT.sqllab.groupby.expected",
                "TRUE=" + okTrue + " FALSE=" + okFalse);
        return true;
    }

    private Integer createDashboard() {
        JsonObject body = new JsonObject();
        body.addProperty("dashboard_title", "UC1 Beam TM SUCCESS vs FAILURE");
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("dashboard.create", response);
        if (response.status != 201) {
            Console.log("TmExecutionResultsSupersetIT.blocker", "POST /api/v1/dashboard/");
            return null;
        }
        return this.readId(response.body);
    }

    private Integer createChart(int datasetId, int dashboardId) {
        JsonArray dashboards = new JsonArray();
        dashboards.add(Integer.valueOf(dashboardId));
        JsonObject params = new JsonObject();
        params.addProperty("datasource", datasetId + "__table");
        params.addProperty("viz_type", "pie");
        params.addProperty("row_limit", Integer.valueOf(10000));
        JsonArray groupby = new JsonArray();
        groupby.add("gateResult.ok");
        params.add("groupby", groupby);
        JsonArray metrics = new JsonArray();
        metrics.add("count");
        params.addProperty("metric", "count");
        params.add("metrics", metrics);
        params.add("adhoc_filters", new JsonArray());

        JsonObject body = new JsonObject();
        body.addProperty("datasource_id", Integer.valueOf(datasetId));
        body.addProperty("datasource_type", "table");
        body.addProperty("slice_name", "UC1 Beam TM gateResult.ok");
        body.addProperty("viz_type", "pie");
        body.add("dashboards", dashboards);
        body.addProperty("params", params.toString());
        HttpResponse response = this.request("POST", "/api/v1/chart/", body.toString(), true);
        this.logResponse("chart.create", response);
        if (response.status != 201) {
            Console.log("TmExecutionResultsSupersetIT.blocker", "POST /api/v1/chart/");
            return null;
        }
        return this.readId(response.body);
    }

    private void placeChartOnDashboard(int dashboardId, int chartId) {
        JsonObject chartMeta = new JsonObject();
        chartMeta.addProperty("chartId", Integer.valueOf(chartId));
        chartMeta.addProperty("height", Integer.valueOf(50));
        chartMeta.addProperty("sliceName", "UC1 Beam TM gateResult.ok");
        chartMeta.addProperty("width", Integer.valueOf(12));
        JsonObject chartNode = new JsonObject();
        chartNode.add("children", new JsonArray());
        chartNode.addProperty("id", "CHART-uc1-beam-tm");
        chartNode.add("meta", chartMeta);
        chartNode.addProperty("type", "CHART");
        JsonArray rowChildren = new JsonArray();
        rowChildren.add("CHART-uc1-beam-tm");
        JsonObject rowMeta = new JsonObject();
        rowMeta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject rowNode = new JsonObject();
        rowNode.add("children", rowChildren);
        rowNode.addProperty("id", "ROW-uc1-beam-tm");
        rowNode.add("meta", rowMeta);
        rowNode.addProperty("type", "ROW");
        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-uc1-beam-tm");
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
        position.add("CHART-uc1-beam-tm", chartNode);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.add("ROW-uc1-beam-tm", rowNode);
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
        columns.add("gateResult.ok");
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
        HttpResponse response = this.request("POST", "/api/v1/security/login", body.toString(), false);
        this.logResponse("security.login", response);
        if (response.status != 200) {
            Console.log("TmExecutionResultsSupersetIT.blocker", "POST /api/v1/security/login");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("access_token") == null || json.get("access_token").isJsonNull()) {
            Console.log("TmExecutionResultsSupersetIT.blocker", "login response missing access_token");
            return false;
        }
        this.accessToken = json.get("access_token").getAsString();
        Console.log("TmExecutionResultsSupersetIT.access_token.present", "true");
        return true;
    }

    private boolean loadCsrf() {
        HttpResponse response = this.request("GET", "/api/v1/security/csrf_token/", null, true);
        this.logResponse("security.csrf_token", response);
        if (response.status != 200) {
            Console.log("TmExecutionResultsSupersetIT.blocker", "GET /api/v1/security/csrf_token/");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || json.get("result").isJsonNull()) {
            Console.log("TmExecutionResultsSupersetIT.blocker", "csrf response missing result");
            return false;
        }
        this.csrfToken = json.get("result").getAsString();
        Console.log("TmExecutionResultsSupersetIT.csrf.present", "true");
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
        Console.log("TmExecutionResultsSupersetIT." + label + ".status", String.valueOf(response.status));
        Console.log("TmExecutionResultsSupersetIT." + label + ".body", response.body);
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
            Console.log("TmExecutionResultsSupersetIT.request.exception", String.valueOf(e));
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
        private long total;
        private long okTrue;
        private long okFalse;
        private long missingOk;
        private String sample = "";
        private String blocker = "";
    }

    private static class HttpResponse {
        private int status;
        private String body;
    }
}
