package ai.braineous.motion.mox;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TmExecutionOutcomesIT {

    private String accessToken;

    private String csrfToken;

    private String sessionCookie;

    @Test
    public void test_1() {
        Console.log("TmExecutionOutcomesIT.test_1.goal", TmExecutionOutcomes.TITLE);
        Console.log("TmExecutionOutcomesIT.view.name", TmExecutionOutcomesView.NAME);
        Console.log("TmExecutionOutcomesIT.view.source",
                TmExecutionOutcomesView.SOURCE_DATABASE + "." + TmExecutionOutcomesView.SOURCE_COLLECTION);
        Console.log("TmExecutionOutcomesIT.queryset", TmExecutionOutcomesView.queryset());
        Console.log("TmExecutionOutcomesIT.superset", TmExecutionOutcomes.SUPERSET_URL);
        Console.log("TmExecutionOutcomesIT.dashboard.url", TmExecutionOutcomes.dashboardUrl());

        MongoCounts mongo = this.countMongo();
        Console.log("TmExecutionOutcomesIT.mongo.success", String.valueOf(mongo.success));
        Console.log("TmExecutionOutcomesIT.mongo.failure", String.valueOf(mongo.failure));
        Console.log("TmExecutionOutcomesIT.mongo.total", String.valueOf(mongo.total));
        Assertions.assertTrue(
                mongo.success + mongo.failure > 0,
                "cgo.tm_observability has no boolean ok values");

        Assertions.assertTrue(this.login(), "POST /api/v1/security/login");
        Assertions.assertTrue(this.loadCsrf(), "GET /api/v1/security/csrf_token/");

        this.verifyDatabase();
        this.provisionView();
        MongoCounts querysetCounts = this.executeQueryset();
        Console.log("TmExecutionOutcomesIT.queryset.success", String.valueOf(querysetCounts.success));
        Console.log("TmExecutionOutcomesIT.queryset.failure", String.valueOf(querysetCounts.failure));
        Assertions.assertEquals(mongo.success, querysetCounts.success, "queryset SUCCESS");
        Assertions.assertEquals(mongo.failure, querysetCounts.failure, "queryset FAILURE");

        this.verifyChart();
        MongoCounts chartCounts = this.executeChart();
        Console.log("TmExecutionOutcomesIT.chart.success", String.valueOf(chartCounts.success));
        Console.log("TmExecutionOutcomesIT.chart.failure", String.valueOf(chartCounts.failure));
        Assertions.assertEquals(mongo.success, chartCounts.success, "chart SUCCESS");
        Assertions.assertEquals(mongo.failure, chartCounts.failure, "chart FAILURE");

        this.verifyDashboard();
        Console.log("TmExecutionOutcomesIT.browser.url", TmExecutionOutcomes.dashboardUrl());
        Console.log(
                "TmExecutionOutcomesIT.observed",
                "SUCCESS=" + mongo.success + " FAILURE=" + mongo.failure);
    }

    private MongoCounts countMongo() {
        MongoCounts counts = new MongoCounts();
        MongoClient client = MongoClients.create(TmExecutionOutcomes.MONGO_URI);
        try {
            MongoCollection<Document> collection = client
                    .getDatabase(TmExecutionOutcomesView.SOURCE_DATABASE)
                    .getCollection(TmExecutionOutcomesView.SOURCE_COLLECTION);
            counts.total = collection.countDocuments();
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Object ok = cursor.next().get("ok");
                if (Boolean.TRUE.equals(ok)) {
                    counts.success = counts.success + 1;
                } else if (Boolean.FALSE.equals(ok)) {
                    counts.failure = counts.failure + 1;
                }
            }
            return counts;
        } finally {
            client.close();
        }
    }

    private MongoCounts executeQueryset() {
        HttpResponse response = this.request(
                "POST",
                "/api/v1/sqllab/execute/",
                this.sqlLabBody(TmExecutionOutcomesView.queryset()).toString(),
                true);
        this.logResponse("sqllab.queryset", response);
        Assertions.assertEquals(200, response.status, "SQL Lab queryset");
        JsonObject json = this.parseObject(response.body);
        Assertions.assertNotNull(json, "SQL Lab queryset body");
        JsonArray data = json.getAsJsonArray("data");
        Assertions.assertNotNull(data, "SQL Lab queryset data");
        MongoCounts counts = new MongoCounts();
        int i = 0;
        while (i < data.size()) {
            JsonObject row = data.get(i).getAsJsonObject();
            this.addRow(counts, row.get("ok"), row.get("count"));
            i = i + 1;
        }
        return counts;
    }

    private void verifyDatabase() {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/database/" + TmExecutionOutcomes.DATABASE_ID,
                null,
                true);
        this.logResponse("database.get", response);
        Assertions.assertEquals(200, response.status, "GET /api/v1/database/2");
        JsonObject json = this.parseObject(response.body);
        JsonObject result = json.getAsJsonObject("result");
        Assertions.assertEquals(
                TmExecutionOutcomes.DATABASE_NAME,
                this.readString(result, "database_name"),
                "database 2 name");
        Assertions.assertEquals("mongodb", this.readString(result, "backend"), "database 2 backend");
    }

    private void provisionView() {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/dataset/" + TmExecutionOutcomes.DATASET_ID,
                null,
                true);
        this.logResponse("dataset.get", response);
        Assertions.assertEquals(200, response.status, "GET /api/v1/dataset/4");
        JsonObject result = this.parseObject(response.body).getAsJsonObject("result");
        Assertions.assertEquals(
                TmExecutionOutcomesView.NAME,
                this.readString(result, "table_name"),
                "dataset 4 table_name");
        String sql = result.get("sql") == null || result.get("sql").isJsonNull()
                ? ""
                : result.get("sql").getAsString().trim();
        Console.log("TmExecutionOutcomesIT.dataset.sql", sql);
        if (!TmExecutionOutcomesView.queryset().equals(sql)) {
            JsonObject body = new JsonObject();
            body.addProperty("sql", TmExecutionOutcomesView.queryset());
            HttpResponse updated = this.request(
                    "PUT",
                    "/api/v1/dataset/" + TmExecutionOutcomes.DATASET_ID,
                    body.toString(),
                    true);
            this.logResponse("dataset.provision", updated);
            Assertions.assertTrue(
                    updated.status == 200 || updated.status == 201,
                    "PUT /api/v1/dataset/4 queryset");
        }
    }

    private void verifyChart() {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/chart/" + TmExecutionOutcomes.CHART_ID,
                null,
                true);
        this.logResponse("chart.get", response);
        Assertions.assertEquals(200, response.status, "GET /api/v1/chart/2");
        JsonObject result = this.parseObject(response.body).getAsJsonObject("result");
        Assertions.assertEquals(
                TmExecutionOutcomes.CHART_NAME,
                this.readString(result, "slice_name"),
                "chart 2 name");
        Assertions.assertEquals(
                TmExecutionOutcomes.VIZ_TYPE,
                this.readString(result, "viz_type"),
                "chart 2 viz");
        String params = this.readString(result, "params");
        Assertions.assertTrue(
                params.indexOf(TmExecutionOutcomes.OUTCOME_SQL) >= 0,
                "chart 2 CASE SUCCESS/FAILURE");
        Assertions.assertTrue(
                params.indexOf(TmExecutionOutcomes.METRIC_NAME) >= 0,
                "chart 2 metric tm_ok_count");
    }

    private MongoCounts executeChart() {
        HttpResponse chart = this.request(
                "GET",
                "/api/v1/chart/" + TmExecutionOutcomes.CHART_ID,
                null,
                true);
        JsonObject chartJson = this.parseObject(chart.body).getAsJsonObject("result");
        String queryContext = this.readString(chartJson, "query_context");
        Assertions.assertTrue(queryContext.length() > 0, "chart 2 query_context");
        HttpResponse response = this.request(
                "POST",
                "/api/v1/chart/data?slice_id=" + TmExecutionOutcomes.CHART_ID,
                queryContext,
                true);
        this.logResponse("chart.data", response);
        Assertions.assertEquals(200, response.status, "POST /api/v1/chart/data chart 2");
        JsonObject json = this.parseObject(response.body);
        JsonArray result = json.getAsJsonArray("result");
        Assertions.assertTrue(result.size() > 0, "chart 2 result");
        JsonObject first = result.get(0).getAsJsonObject();
        Console.log("TmExecutionOutcomesIT.chart.executed_sql", this.readString(first, "query"));
        JsonArray data = first.getAsJsonArray("data");
        Assertions.assertNotNull(data, "chart 2 data");
        MongoCounts counts = new MongoCounts();
        int i = 0;
        while (i < data.size()) {
            JsonObject row = data.get(i).getAsJsonObject();
            this.addChartRow(counts, row);
            i = i + 1;
        }
        return counts;
    }

    private void verifyDashboard() {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/dashboard/" + TmExecutionOutcomes.DASHBOARD_ID,
                null,
                true);
        this.logResponse("dashboard.get", response);
        Assertions.assertEquals(200, response.status, "GET /api/v1/dashboard/2");
        JsonObject result = this.parseObject(response.body).getAsJsonObject("result");
        Assertions.assertEquals(
                TmExecutionOutcomes.DASHBOARD_TITLE,
                this.readString(result, "dashboard_title"),
                "dashboard 2 title");
        Assertions.assertTrue(
                result.get("published") != null && result.get("published").getAsBoolean(),
                "dashboard 2 published");
        String position = this.readString(result, "position_json");
        Assertions.assertTrue(
                position.indexOf("\"chartId\": 2") >= 0 || position.indexOf("\"chartId\":2") >= 0,
                "dashboard 2 hosts chart 2");
        String url = this.readString(result, "url");
        Console.log("TmExecutionOutcomesIT.dashboard.api.url", url);
        Console.log("TmExecutionOutcomesIT.dashboard.standalone", TmExecutionOutcomes.STANDALONE);
    }

    private JsonObject sqlLabBody(String sql) {
        JsonObject body = new JsonObject();
        body.addProperty("database_id", Integer.valueOf(TmExecutionOutcomes.DATABASE_ID));
        body.addProperty("schema", "");
        body.addProperty("sql", sql);
        body.addProperty("runAsync", Boolean.FALSE);
        return body;
    }

    private void addRow(MongoCounts counts, JsonElement ok, JsonElement count) {
        long value = this.asLong(count);
        if (ok != null && ok.isJsonPrimitive() && ok.getAsBoolean()) {
            counts.success = value;
        } else if (ok != null && ok.isJsonPrimitive() && !ok.getAsBoolean()) {
            counts.failure = value;
        }
    }

    private void addChartRow(MongoCounts counts, JsonObject row) {
        String result = this.readString(row, "result");
        String outcome = this.readString(row, "outcome");
        if ("SUCCESS".equals(result) || "SUCCESS".equals(outcome) || row.get("SUCCESS") != null) {
            long value = this.firstNumber(row, "SUCCESS", "tm_ok_count", "count");
            if ("SUCCESS".equals(result) || "SUCCESS".equals(outcome) || value > 0) {
                if (value > 0) {
                    counts.success = value;
                }
            }
        }
        if ("FAILURE".equals(result) || "FAILURE".equals(outcome) || row.get("FAILURE") != null) {
            long value = this.firstNumber(row, "FAILURE", "tm_ok_count", "count");
            if ("FAILURE".equals(result) || "FAILURE".equals(outcome) || value > 0) {
                if (value > 0) {
                    counts.failure = value;
                }
            }
        }
    }

    private long firstNumber(JsonObject row, String... fields) {
        int i = 0;
        while (i < fields.length) {
            JsonElement element = row.get(fields[i]);
            if (element != null && !element.isJsonNull() && element.isJsonPrimitive()) {
                return this.asLong(element);
            }
            i = i + 1;
        }
        return 0;
    }

    private long asLong(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return 0;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return Math.round(element.getAsDouble());
        }
        return Long.parseLong(element.getAsString());
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
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("access_token") == null || json.get("access_token").isJsonNull()) {
            return false;
        }
        this.accessToken = json.get("access_token").getAsString();
        return true;
    }

    private boolean loadCsrf() {
        HttpResponse response = this.request("GET", "/api/v1/security/csrf_token/", null, true);
        this.logResponse("security.csrf_token", response);
        if (response.status != 200) {
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || json.get("result").isJsonNull()) {
            return false;
        }
        this.csrfToken = json.get("result").getAsString();
        return true;
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
        Console.log("TmExecutionOutcomesIT." + label + ".status", String.valueOf(response.status));
        Console.log("TmExecutionOutcomesIT." + label + ".body", response.body);
    }

    private HttpResponse request(String method, String path, String jsonBody, boolean authed) {
        HttpResponse result = new HttpResponse();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(TmExecutionOutcomes.SUPERSET_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Referer", TmExecutionOutcomes.SUPERSET_URL + "/");
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
        } catch (Exception exception) {
            result.status = -1;
            result.body = String.valueOf(exception);
            Console.log("TmExecutionOutcomesIT.request.exception", String.valueOf(exception));
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

    private static class MongoCounts {
        private long total;
        private long success;
        private long failure;
    }

    private static class HttpResponse {
        private int status;
        private String body;
    }
}
