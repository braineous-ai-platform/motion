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
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class TmLiteralDottedColumnIT {

    private static final String SUPERSET_URL = "http://localhost:8099";

    private static final String BEAM_MONGO_URI = "mongodb://localhost:27017";

    private static final String MONGO_DATABASE = "cgo";

    private static final String MONGO_COLLECTION = "tm_execution_results";

    private String accessToken;

    private String csrfToken;

    private String sessionCookie;

    @Test
    public void test_1() {
        Console.log(
                "TmLiteralDottedColumnIT.test_1.goal",
                "observe whether PyMongoSQL 0.7.4 and Superset 6.0.0 can expose and GROUP BY a literal flattened Mongo field name gateResult.ok, distinct from nested gateResult.ok");
        Console.log(
                "TmLiteralDottedColumnIT.test_1.disclaimer",
                "read-only cgo.tm_execution_results; no Mongo writes; no new collection/database/service; no 8088; no assertions; no nested-path GROUP BY substitute");

        SourceObservation source = this.readLiveDocumentsAndBuildLiteralRepresentation();
        Console.log("TmLiteralDottedColumnIT.source.total", String.valueOf(source.total));
        Console.log("TmLiteralDottedColumnIT.source.nested_ok_true", String.valueOf(source.okTrue));
        Console.log("TmLiteralDottedColumnIT.source.nested_ok_false", String.valueOf(source.okFalse));
        Console.log("TmLiteralDottedColumnIT.source.missing_ok", String.valueOf(source.missingOk));
        Console.log("TmLiteralDottedColumnIT.source.literal_key_docs", String.valueOf(source.literalKeyDocs));
        Console.log("TmLiteralDottedColumnIT.source.sample_A_nested_json", source.sampleA);
        Console.log("TmLiteralDottedColumnIT.source.sample_B_literal_json", source.sampleB);
        Console.log("TmLiteralDottedColumnIT.source.sample_A_keys", source.sampleAKeys);
        Console.log("TmLiteralDottedColumnIT.source.sample_B_keys", source.sampleBKeys);
        Console.log(
                "TmLiteralDottedColumnIT.representation.B",
                "in-memory org.bson.Document with literal keys gateResult.ok and gateResult.why copied from live nested gateResult values; not written");
        Console.log(
                "TmLiteralDottedColumnIT.distinction",
                "A nested {gateResult:{ok,why}} is not equivalent to B literal {\"gateResult.ok\":..., \"gateResult.why\":...}");

        if (source.total == 0) {
            Console.log(
                    "TmLiteralDottedColumnIT.blocker",
                    "cgo.tm_execution_results is empty on mongodb://localhost:27017; no live values for the literal representation");
            return;
        }
        if (source.missingOk > 0) {
            Console.log(
                    "TmLiteralDottedColumnIT.blocker",
                    "live documents missing nested gateResult.ok; cannot copy real SUCCESS/FAILURE values into the literal representation");
            return;
        }
        if (source.literalKeyDocs > 0) {
            Console.log(
                    "TmLiteralDottedColumnIT.live.literal_keys_present",
                    "unexpected: live production documents already contain top-level gateResult.ok; experiment would query those read-only");
        } else {
            Console.log(
                    "TmLiteralDottedColumnIT.live.literal_keys_present",
                    "false; live documents are shape A only");
        }

        this.probePymongoSqlAgainstLiveCollection();
        this.probeSupersetDiscoveryWithoutCreates();

        Console.log(
                "TmLiteralDottedColumnIT.pymongosql.literal_B.executed",
                "false");
        Console.log(
                "TmLiteralDottedColumnIT.chart.created",
                "false");
        Console.log(
                "TmLiteralDottedColumnIT.blocker",
                "exact literal-field B {\"gateResult.ok\": <live ok>, \"gateResult.why\": <live why>} exists only as an in-memory test representation; PyMongoSQL 0.7.4 connect() wraps MongoClient and has no in-memory collection API; live cgo.tm_execution_results has nested gateResult only; inserting B or creating a collection/view would be a Mongo write/new infrastructure, which this experiment forbids; quoted identifier SQL against live documents is nested-path A, not literal-key B; nested-path GROUP BY is not substituted");
    }

    private SourceObservation readLiveDocumentsAndBuildLiteralRepresentation() {
        SourceObservation source = new SourceObservation();
        com.mongodb.client.MongoClient client = MongoClients.create(BEAM_MONGO_URI);
        try {
            MongoCollection<Document> collection =
                    client.getDatabase(MONGO_DATABASE).getCollection(MONGO_COLLECTION);
            source.total = collection.countDocuments();
            MongoCursor<Document> cursor = collection.find().iterator();
            int index = 0;
            while (cursor.hasNext()) {
                Document nested = cursor.next();
                boolean hasLiteralOk = nested.containsKey("gateResult.ok");
                boolean hasLiteralWhy = nested.containsKey("gateResult.why");
                if (hasLiteralOk || hasLiteralWhy) {
                    source.literalKeyDocs = source.literalKeyDocs + 1;
                }
                Object gate = nested.get("gateResult");
                Boolean ok = this.readGateOk(gate);
                String why = this.readGateWhy(gate);
                if (ok == null) {
                    source.missingOk = source.missingOk + 1;
                } else if (ok.booleanValue()) {
                    source.okTrue = source.okTrue + 1;
                } else {
                    source.okFalse = source.okFalse + 1;
                }
                Document literal = new Document();
                if (ok != null) {
                    literal.put("gateResult.ok", ok);
                }
                if (why != null) {
                    literal.put("gateResult.why", why);
                }
                Console.log(
                        "TmLiteralDottedColumnIT.doc[" + index + "].A.keys",
                        String.valueOf(nested.keySet()));
                Console.log(
                        "TmLiteralDottedColumnIT.doc[" + index + "].A.gateResult",
                        String.valueOf(gate));
                Console.log(
                        "TmLiteralDottedColumnIT.doc[" + index + "].A.contains_literal_ok",
                        String.valueOf(hasLiteralOk));
                Console.log(
                        "TmLiteralDottedColumnIT.doc[" + index + "].B.json",
                        literal.toJson());
                Console.log(
                        "TmLiteralDottedColumnIT.doc[" + index + "].B.keys",
                        String.valueOf(literal.keySet()));
                if (index == 0) {
                    source.sampleA = nested.toJson();
                    source.sampleAKeys = String.valueOf(nested.keySet());
                    source.sampleB = literal.toJson();
                    source.sampleBKeys = String.valueOf(literal.keySet());
                }
                index = index + 1;
            }
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

    private String readGateWhy(Object gate) {
        if (!(gate instanceof Document)) {
            return null;
        }
        Document gateDoc = (Document) gate;
        if (!gateDoc.containsKey("why")) {
            return null;
        }
        Object why = gateDoc.get("why");
        if (why == null) {
            return null;
        }
        return String.valueOf(why);
    }

    private void probePymongoSqlAgainstLiveCollection() {
        File venvPython = this.existingPymongoSqlPython();
        if (venvPython == null) {
            Console.log(
                    "TmLiteralDottedColumnIT.pymongosql.venv",
                    "absent; not creating a new venv for this experiment");
            return;
        }
        Console.log(
                "TmLiteralDottedColumnIT.pymongosql.venv",
                venvPython.getAbsolutePath());
        int status = this.runProcess(
                "pymongosql.probe",
                this.command(venvPython.getAbsolutePath(), "-c", this.pymongoSqlProbeScript()));
        Console.log(
                "TmLiteralDottedColumnIT.pymongosql.probe.process_status",
                String.valueOf(status));
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

    private String pymongoSqlProbeScript() {
        return "from pymongosql import connect\n"
                + "import pymongosql, pymongo, inspect, sys\n"
                + "print('library=pymongosql')\n"
                + "print('pymongosql_version=' + str(getattr(pymongosql,'__version__','?')))\n"
                + "print('pymongo_version=' + pymongo.version)\n"
                + "print('python_version=' + sys.version.split()[0])\n"
                + "print('connect_signature=' + str(inspect.signature(pymongosql.connect)))\n"
                + "print('in_memory_collection_api=absent; connect wraps MongoClient')\n"
                + "client = pymongo.MongoClient('mongodb://localhost:27017')\n"
                + "col = client['cgo']['tm_execution_results']\n"
                + "literal_ok = 0\n"
                + "literal_why = 0\n"
                + "nested = 0\n"
                + "for d in col.find():\n"
                + "    if 'gateResult.ok' in d:\n"
                + "        literal_ok = literal_ok + 1\n"
                + "    if 'gateResult.why' in d:\n"
                + "        literal_why = literal_why + 1\n"
                + "    if isinstance(d.get('gateResult'), dict):\n"
                + "        nested = nested + 1\n"
                + "print('live.literal_gateResult.ok_keys=' + str(literal_ok))\n"
                + "print('live.literal_gateResult.why_keys=' + str(literal_why))\n"
                + "print('live.nested_gateResult_docs=' + str(nested))\n"
                + "print('live.count=' + str(col.count_documents({})))\n"
                + "client.close()\n"
                + "conn = connect(host='mongodb://localhost:27017', database='cgo')\n"
                + "print('connection=SUCCESS')\n"
                + "queries = []\n"
                + "queries.append('SELECT \"gateResult.ok\" FROM tm_execution_results')\n"
                + "queries.append('SELECT \"gateResult.ok\" FROM tm_execution_results WHERE \"gateResult.ok\" = true')\n"
                + "queries.append('SELECT \"gateResult.ok\" FROM tm_execution_results WHERE \"gateResult.ok\" = false')\n"
                + "queries.append('SELECT \"gateResult.ok\", COUNT(*) FROM tm_execution_results GROUP BY \"gateResult.ok\"')\n"
                + "queries.append('SELECT `gateResult.ok` FROM tm_execution_results')\n"
                + "q = 0\n"
                + "while q < len(queries):\n"
                + "    sql = queries[q]\n"
                + "    print('sql[' + str(q) + ']=' + sql)\n"
                + "    print('sql[' + str(q) + '].note=quoted identifier against live nested documents (shape A), not in-memory literal-key B')\n"
                + "    cur = conn.cursor()\n"
                + "    try:\n"
                + "        cur.execute(sql)\n"
                + "        print('sql[' + str(q) + '].status=SUCCESS')\n"
                + "        if cur.description is None:\n"
                + "            print('sql[' + str(q) + '].columns=')\n"
                + "        else:\n"
                + "            names = []\n"
                + "            i = 0\n"
                + "            while i < len(cur.description):\n"
                + "                names.append(str(cur.description[i][0]))\n"
                + "                print('sql[' + str(q) + '].column[' + str(i) + ']=' + str(cur.description[i][0]))\n"
                + "                i = i + 1\n"
                + "            print('sql[' + str(q) + '].columns=' + ','.join(names))\n"
                + "        rows = cur.fetchall()\n"
                + "        print('sql[' + str(q) + '].row_count=' + str(len(rows)))\n"
                + "        r = 0\n"
                + "        while r < len(rows):\n"
                + "            print('sql[' + str(q) + '].row[' + str(r) + ']=' + repr(rows[r]))\n"
                + "            r = r + 1\n"
                + "    except Exception as e:\n"
                + "        print('sql[' + str(q) + '].status=FAILURE')\n"
                + "        print('sql[' + str(q) + '].error=' + type(e).__name__ + ':' + str(e))\n"
                + "    q = q + 1\n"
                + "conn.close()\n"
                + "print('literal_B_query=NOT_EXECUTED')\n"
                + "print('reason=no Mongo documents with literal keys; writes forbidden')\n";
    }

    private void probeSupersetDiscoveryWithoutCreates() {
        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }
        Integer databaseId = this.findExistingDatabase("cgo-tm-execution-results");
        Console.log(
                "TmLiteralDottedColumnIT.superset.database_id",
                databaseId == null ? "absent" : String.valueOf(databaseId.intValue()));
        Integer datasetId = this.findExistingDataset("tm_execution_results");
        Console.log(
                "TmLiteralDottedColumnIT.superset.dataset_id",
                datasetId == null ? "absent" : String.valueOf(datasetId.intValue()));
        if (datasetId != null) {
            JsonObject dataset = this.readDataset(datasetId.intValue());
            this.logDatasetColumns(dataset);
        } else {
            Console.log(
                    "TmLiteralDottedColumnIT.superset.dataset",
                    "not creating a dataset; none found to inspect");
        }
        if (databaseId != null) {
            this.executeSqlLab(
                    databaseId.intValue(),
                    "quoted.select",
                    "SELECT \"gateResult.ok\" FROM " + MONGO_COLLECTION);
            this.executeSqlLab(
                    databaseId.intValue(),
                    "quoted.where.true",
                    "SELECT \"gateResult.ok\" FROM " + MONGO_COLLECTION + " WHERE \"gateResult.ok\" = true");
            this.executeSqlLab(
                    databaseId.intValue(),
                    "quoted.where.false",
                    "SELECT \"gateResult.ok\" FROM " + MONGO_COLLECTION + " WHERE \"gateResult.ok\" = false");
            this.executeSqlLab(
                    databaseId.intValue(),
                    "quoted.groupby",
                    "SELECT \"gateResult.ok\", COUNT(*) FROM " + MONGO_COLLECTION + " GROUP BY \"gateResult.ok\"");
            Console.log(
                    "TmLiteralDottedColumnIT.sqllab.note",
                    "quoted SQL Lab statements target live nested documents (shape A); they are not GROUP BY of literal-key B");
        } else {
            Console.log(
                    "TmLiteralDottedColumnIT.superset.database",
                    "not creating a Superset database; none found to reuse");
        }
        Console.log(
                "TmLiteralDottedColumnIT.superset.chart",
                "skipped; literal-key B was not grouped");
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
                    "TmLiteralDottedColumnIT.database.existing",
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
                    "TmLiteralDottedColumnIT.dataset.existing",
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
            Console.log("TmLiteralDottedColumnIT.dataset.columns", "absent");
            return;
        }
        JsonArray columns = dataset.getAsJsonArray("columns");
        Console.log(
                "TmLiteralDottedColumnIT.dataset.columns.size",
                String.valueOf(columns.size()));
        boolean foundLiteralOk = false;
        boolean foundLiteralWhy = false;
        boolean foundNestedGate = false;
        int i = 0;
        while (i < columns.size()) {
            JsonObject column = columns.get(i).getAsJsonObject();
            String name = this.readString(column, "column_name");
            Console.log(
                    "TmLiteralDottedColumnIT.dataset.column",
                    name + " type=" + this.readString(column, "type"));
            if ("gateResult.ok".equals(name)) {
                foundLiteralOk = true;
            }
            if ("gateResult.why".equals(name)) {
                foundLiteralWhy = true;
            }
            if ("gateResult".equals(name)) {
                foundNestedGate = true;
            }
            i = i + 1;
        }
        Console.log(
                "TmLiteralDottedColumnIT.dataset.column.gateResult.ok",
                String.valueOf(foundLiteralOk));
        Console.log(
                "TmLiteralDottedColumnIT.dataset.column.gateResult.why",
                String.valueOf(foundLiteralWhy));
        Console.log(
                "TmLiteralDottedColumnIT.dataset.column.gateResult",
                String.valueOf(foundNestedGate));
    }

    private void executeSqlLab(int databaseId, String label, String sql) {
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
        Console.log("TmLiteralDottedColumnIT.sqllab." + label + ".sql", sql);
        this.logResponse("sqllab." + label, response);
        JsonObject json = this.parseObject(response.body);
        if (json == null) {
            return;
        }
        if (json.get("columns") != null) {
            Console.log(
                    "TmLiteralDottedColumnIT.sqllab." + label + ".columns",
                    json.get("columns").toString());
        }
        if (json.get("data") != null && json.get("data").isJsonArray()) {
            JsonArray data = json.getAsJsonArray("data");
            Console.log(
                    "TmLiteralDottedColumnIT.sqllab." + label + ".row_count",
                    String.valueOf(data.size()));
            Console.log(
                    "TmLiteralDottedColumnIT.sqllab." + label + ".data",
                    data.toString());
        }
        if (json.get("error") != null) {
            Console.log(
                    "TmLiteralDottedColumnIT.sqllab." + label + ".error",
                    json.get("error").toString());
        }
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
            Console.log("TmLiteralDottedColumnIT.blocker.login", "POST /api/v1/security/login");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("access_token") == null || json.get("access_token").isJsonNull()) {
            Console.log("TmLiteralDottedColumnIT.blocker.login", "login response missing access_token");
            return false;
        }
        this.accessToken = json.get("access_token").getAsString();
        return true;
    }

    private boolean loadCsrf() {
        HttpResponse response = this.request("GET", "/api/v1/security/csrf_token/", null, true);
        this.logResponse("security.csrf_token", response);
        if (response.status != 200) {
            Console.log("TmLiteralDottedColumnIT.blocker.csrf", "GET /api/v1/security/csrf_token/");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || json.get("result").isJsonNull()) {
            Console.log("TmLiteralDottedColumnIT.blocker.csrf", "csrf response missing result");
            return false;
        }
        this.csrfToken = json.get("result").getAsString();
        return true;
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
        Console.log("TmLiteralDottedColumnIT." + label + ".command", command.toString());
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            String output = this.readStream(process.getInputStream());
            int status = process.waitFor();
            Console.log("TmLiteralDottedColumnIT." + label + ".status", String.valueOf(status));
            Console.log("TmLiteralDottedColumnIT." + label + ".output", output);
            return status;
        } catch (Exception e) {
            Console.log("TmLiteralDottedColumnIT." + label + ".exception", String.valueOf(e));
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
        Console.log("TmLiteralDottedColumnIT." + label + ".status", String.valueOf(response.status));
        Console.log("TmLiteralDottedColumnIT." + label + ".body", response.body);
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
            Console.log("TmLiteralDottedColumnIT.request.exception", String.valueOf(e));
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

    private static class SourceObservation {
        private long total;
        private long okTrue;
        private long okFalse;
        private long missingOk;
        private long literalKeyDocs;
        private String sampleA = "";
        private String sampleB = "";
        private String sampleAKeys = "";
        private String sampleBKeys = "";
    }

    private static class HttpResponse {
        private int status;
        private String body;
    }
}
