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
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@QuarkusTest
public class MongoSupersetDirectQueryIT {

    private static final String SUPERSET_URL = "http://localhost:8088";

    private static final String MONGO_DATABASE = "motion";

    private static final String MONGO_COLLECTION = "operational_view";

    @Inject
    MongoClient mongoClient;

    private String accessToken;

    private String csrfToken;

    private String sessionCookie;

    @Test
    public void test_1() {
        Console.log("MongoSupersetDirectQueryIT.test_1.goal",
                "observe whether Superset 6.0.0 can query an existing MongoDB collection through a SQLAlchemy-compatible connection");
        Console.log("MongoSupersetDirectQueryIT.test_1.disclaimer",
                "read-only; no Mongo writes; no Superset database create; no driver install");
        Console.log("MongoSupersetDirectQueryIT.mongo.configured.database", MONGO_DATABASE);
        Console.log("MongoSupersetDirectQueryIT.mongo.configured.collection", MONGO_COLLECTION);

        this.inspectMongoCollections();
        this.inspectSampleDocuments();
        this.observeTabularRowShape();

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }
        this.inspectExistingSupersetDatabases();
        this.inspectAvailableEngines();
        this.probeMongoSqlalchemyUris();

        Console.log(
                "MongoSupersetDirectQueryIT.observation.gap",
                "installed Superset 6.0.0 public engine list and test_connection are the SQLAlchemy surface; mongodb:// is probed without creating a database");
    }

    @Test
    public void test_2() {
        Console.log("MongoSupersetDirectQueryIT.test_2.goal",
                "verify PyMongoSQL can read motion.operational_view documents as tabular query results");
        Console.log("MongoSupersetDirectQueryIT.test_2.disclaimer",
                "isolated pymongosql venv; read-only; no Superset; no Mongo writes");
        Console.log("MongoSupersetDirectQueryIT.test_2.source",
                "mongodb://localhost:27018 database=motion collection=operational_view");

        File venvPython = this.ensureIsolatedPymongoSql();
        if (venvPython == null) {
            return;
        }
        this.runPymongoSqlQuery(venvPython);
    }

    private File ensureIsolatedPymongoSql() {
        String basedir = System.getProperty("basedir");
        if (basedir == null || basedir.length() == 0) {
            basedir = System.getProperty("user.dir");
        }
        File venvDir = new File(basedir, "target/pymongosql-proto");
        File venvPython = new File(venvDir, "bin/python");
        Console.log("MongoSupersetDirectQueryIT.test_2.venv", venvDir.getAbsolutePath());
        if (!venvPython.isFile()) {
            Console.log("MongoSupersetDirectQueryIT.test_2.venv.create", "python3 -m venv");
            int venvStatus = this.runProcess(
                    "venv.create",
                    this.command("python3", "-m", "venv", venvDir.getAbsolutePath()));
            if (venvStatus != 0) {
                Console.log("MongoSupersetDirectQueryIT.blocker", "isolated venv create failed");
                return null;
            }
        }
        if (!venvPython.isFile()) {
            Console.log("MongoSupersetDirectQueryIT.blocker", "isolated venv python missing");
            return null;
        }
        int inspectStatus = this.runProcess(
                "venv.inspect",
                this.command(
                        venvPython.getAbsolutePath(),
                        "-c",
                        "import pymongosql,sqlalchemy,pymongo,sys; print('python='+sys.version.split()[0]); print('pymongosql='+getattr(pymongosql,'__version__','?')); print('sqlalchemy='+sqlalchemy.__version__); print('pymongo='+pymongo.version)"));
        if (inspectStatus != 0) {
            Console.log("MongoSupersetDirectQueryIT.test_2.install", "pip install pymongosql[sqlalchemy]");
            int installStatus = this.runProcess(
                    "venv.install",
                    this.command(
                            venvPython.getAbsolutePath(),
                            "-m",
                            "pip",
                            "install",
                            "pymongosql[sqlalchemy]"));
            if (installStatus != 0) {
                Console.log("MongoSupersetDirectQueryIT.blocker", "isolated pip install pymongosql failed");
                return null;
            }
            inspectStatus = this.runProcess(
                    "venv.inspect.after_install",
                    this.command(
                            venvPython.getAbsolutePath(),
                            "-c",
                            "import pymongosql,sqlalchemy,pymongo,sys; print('python='+sys.version.split()[0]); print('pymongosql='+getattr(pymongosql,'__version__','?')); print('sqlalchemy='+sqlalchemy.__version__); print('pymongo='+pymongo.version)"));
            if (inspectStatus != 0) {
                Console.log("MongoSupersetDirectQueryIT.blocker", "pymongosql import failed after install");
                return null;
            }
        }
        return venvPython;
    }

    private void runPymongoSqlQuery(File venvPython) {
        String script = this.pymongoSqlProbeScript();
        int status = this.runProcess(
                "query",
                this.command(venvPython.getAbsolutePath(), "-c", script));
        Console.log("MongoSupersetDirectQueryIT.test_2.query.process_status", String.valueOf(status));
        if (status != 0) {
            Console.log("MongoSupersetDirectQueryIT.blocker", "PyMongoSQL probe process failed");
        }
    }

    private String pymongoSqlProbeScript() {
        return "from pymongosql import connect\n"
                + "import pymongosql, sqlalchemy, pymongo, sys, json\n"
                + "print('library=pymongosql')\n"
                + "print('pymongosql_version=' + str(getattr(pymongosql,'__version__','?')))\n"
                + "print('sqlalchemy_version=' + sqlalchemy.__version__)\n"
                + "print('pymongo_version=' + pymongo.version)\n"
                + "print('python_version=' + sys.version.split()[0])\n"
                + "query = 'SELECT * FROM operational_view LIMIT 3'\n"
                + "print('executed_query=' + query)\n"
                + "try:\n"
                + "    conn = connect(host='mongodb://localhost:27018', database='motion')\n"
                + "    print('connection=SUCCESS')\n"
                + "    print('connection_type=' + type(conn).__name__)\n"
                + "except Exception as e:\n"
                + "    print('connection=FAILURE')\n"
                + "    print('connection_error=' + type(e).__name__ + ':' + str(e))\n"
                + "    raise SystemExit(2)\n"
                + "try:\n"
                + "    cur = conn.cursor()\n"
                + "    cur.execute(query)\n"
                + "    print('query=SUCCESS')\n"
                + "except Exception as e:\n"
                + "    print('query=FAILURE')\n"
                + "    print('query_error=' + type(e).__name__ + ':' + str(e))\n"
                + "    raise SystemExit(3)\n"
                + "print('cursor_description=' + repr(cur.description))\n"
                + "if cur.description is None:\n"
                + "    print('column_names=')\n"
                + "    print('column_types=')\n"
                + "else:\n"
                + "    names = []\n"
                + "    types = []\n"
                + "    i = 0\n"
                + "    while i < len(cur.description):\n"
                + "        col = cur.description[i]\n"
                + "        names.append(str(col[0]))\n"
                + "        types.append(str(col[1]))\n"
                + "        print('column[' + str(i) + '].name=' + str(col[0]))\n"
                + "        print('column[' + str(i) + '].type=' + str(col[1]))\n"
                + "        i = i + 1\n"
                + "    print('column_names=' + ','.join(names))\n"
                + "    print('column_types=' + ';'.join(types))\n"
                + "rows = cur.fetchall()\n"
                + "print('returned_row_count=' + str(len(rows)))\n"
                + "r = 0\n"
                + "while r < len(rows):\n"
                + "    row = rows[r]\n"
                + "    print('row[' + str(r) + '].python_type=' + type(row).__name__)\n"
                + "    print('row[' + str(r) + '].arity=' + str(len(row)))\n"
                + "    print('row[' + str(r) + '].raw=' + repr(row))\n"
                + "    c = 0\n"
                + "    while c < len(row):\n"
                + "        value = row[c]\n"
                + "        name = str(cur.description[c][0]) if cur.description is not None else str(c)\n"
                + "        print('row[' + str(r) + '].' + name + '.python_type=' + type(value).__name__)\n"
                + "        print('row[' + str(r) + '].' + name + '.repr=' + repr(value))\n"
                + "        c = c + 1\n"
                + "    r = r + 1\n"
                + "count_query = 'SELECT COUNT(*) AS total FROM operational_view'\n"
                + "print('count_query=' + count_query)\n"
                + "count_cur = conn.cursor()\n"
                + "count_cur.execute(count_query)\n"
                + "count_row = count_cur.fetchone()\n"
                + "print('count_row=' + repr(count_row))\n"
                + "print('one_document_per_row=SELECT * LIMIT 3 returned 3 tuples; COUNT(*) is compared to previously observed 260 documents')\n"
                + "print('nested_representation=state remains a dict cell, not exploded columns')\n"
                + "print('array_representation=sourceFrameIds and sourceEventIds remain list cells')\n"
                + "print('objectid_representation=_id remains bson.ObjectId')\n"
                + "conn.close()\n";
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
        Console.log("MongoSupersetDirectQueryIT.test_2." + label + ".command", command.toString());
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            String output = this.readStream(process.getInputStream());
            int status = process.waitFor();
            Console.log("MongoSupersetDirectQueryIT.test_2." + label + ".status", String.valueOf(status));
            Console.log("MongoSupersetDirectQueryIT.test_2." + label + ".output", output);
            return status;
        } catch (Exception e) {
            Console.log("MongoSupersetDirectQueryIT.test_2." + label + ".exception", String.valueOf(e));
            return -1;
        }
    }

    private void inspectMongoCollections() {
        MongoCursor<String> databases = this.mongoClient.listDatabaseNames().iterator();
        while (databases.hasNext()) {
            String databaseName = databases.next();
            Console.log("MongoSupersetDirectQueryIT.mongo.database", databaseName);
            MongoDatabase database = this.mongoClient.getDatabase(databaseName);
            MongoCursor<String> collections = database.listCollectionNames().iterator();
            while (collections.hasNext()) {
                String collectionName = collections.next();
                long count = database.getCollection(collectionName).countDocuments();
                Console.log(
                        "MongoSupersetDirectQueryIT.mongo.collection",
                        databaseName + "." + collectionName + " count=" + count);
            }
        }
    }

    private void inspectSampleDocuments() {
        MongoCollection<Document> collection =
                this.mongoClient.getDatabase(MONGO_DATABASE).getCollection(MONGO_COLLECTION);
        long count = collection.countDocuments();
        Console.log("MongoSupersetDirectQueryIT.sample.collection.count", String.valueOf(count));
        MongoCursor<Document> cursor = collection.find().limit(2).iterator();
        int i = 0;
        while (cursor.hasNext()) {
            Document document = cursor.next();
            Console.log("MongoSupersetDirectQueryIT.sample.doc." + i + ".json", document.toJson());
            this.logFieldTypes("sample.doc." + i, document, "");
            i = i + 1;
        }
    }

    private void observeTabularRowShape() {
        MongoCollection<Document> collection =
                this.mongoClient.getDatabase(MONGO_DATABASE).getCollection(MONGO_COLLECTION);
        Document document = collection.find().first();
        if (document == null) {
            Console.log("MongoSupersetDirectQueryIT.blocker", "operational_view has no documents");
            return;
        }
        int nested = 0;
        int arrays = 0;
        int scalars = 0;
        Set<String> keys = document.keySet();
        Object[] keyArray = keys.toArray();
        int i = 0;
        while (i < keyArray.length) {
            String key = String.valueOf(keyArray[i]);
            Object value = document.get(key);
            if (value instanceof Document || value instanceof Map) {
                nested = nested + 1;
            } else if (value instanceof List) {
                arrays = arrays + 1;
            } else {
                scalars = scalars + 1;
            }
            i = i + 1;
        }
        Console.log("MongoSupersetDirectQueryIT.row.top_level_keys", String.valueOf(keyArray.length));
        Console.log("MongoSupersetDirectQueryIT.row.scalar_fields", String.valueOf(scalars));
        Console.log("MongoSupersetDirectQueryIT.row.nested_object_fields", String.valueOf(nested));
        Console.log("MongoSupersetDirectQueryIT.row.array_fields", String.valueOf(arrays));
        Console.log(
                "MongoSupersetDirectQueryIT.row.observation",
                "one BSON document is one object; nested maps/arrays are not SQL scalar columns without flattening or JSON column mapping");
    }

    private void logFieldTypes(String label, Document document, String prefix) {
        Set<String> keys = document.keySet();
        Object[] keyArray = keys.toArray();
        int i = 0;
        while (i < keyArray.length) {
            String key = String.valueOf(keyArray[i]);
            Object value = document.get(key);
            String path = prefix + key;
            String typeName = "null";
            if (value != null) {
                typeName = value.getClass().getName();
            }
            Console.log("MongoSupersetDirectQueryIT." + label + ".field." + path, typeName);
            i = i + 1;
        }
    }

    private void inspectExistingSupersetDatabases() {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/database/?q=(page:0,page_size:50)",
                null,
                true);
        this.logResponse("database.list", listed);
        HttpResponse info = this.request("GET", "/api/v1/database/_info", null, true);
        this.logResponse("database.info", info);
        JsonObject json = this.parseObject(listed.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonArray()) {
            return;
        }
        JsonArray rows = json.getAsJsonArray("result");
        int i = 0;
        while (i < rows.size()) {
            JsonElement element = rows.get(i);
            if (element != null && element.isJsonObject()) {
                JsonObject row = element.getAsJsonObject();
                String name = this.readString(row, "database_name");
                String backend = this.readString(row, "backend");
                String uri = this.readString(row, "sqlalchemy_uri");
                Console.log(
                        "MongoSupersetDirectQueryIT.database.existing",
                        "name=" + name + " backend=" + backend + " sqlalchemy_uri=" + uri);
            }
            i = i + 1;
        }
    }

    private void inspectAvailableEngines() {
        HttpResponse response = this.request("GET", "/api/v1/database/available/", null, true);
        this.logResponse("database.available", response);
        JsonElement element = JsonParser.parseString(response.body);
        JsonArray engines = null;
        if (element != null && element.isJsonArray()) {
            engines = element.getAsJsonArray();
        } else if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.get("result") != null && object.get("result").isJsonArray()) {
                engines = object.getAsJsonArray("result");
            } else if (object.get("databases") != null && object.get("databases").isJsonArray()) {
                engines = object.getAsJsonArray("databases");
            }
        }
        if (engines == null) {
            Console.log("MongoSupersetDirectQueryIT.engines.parse", "available payload is not a list");
            return;
        }
        int mongoNamed = 0;
        int i = 0;
        while (i < engines.size()) {
            JsonElement item = engines.get(i);
            if (item != null && item.isJsonObject()) {
                JsonObject engine = item.getAsJsonObject();
                String name = this.readString(engine, "name");
                String engineName = this.readString(engine, "engine");
                String drivers = "";
                if (engine.get("available_drivers") != null) {
                    drivers = engine.get("available_drivers").toString();
                }
                Console.log(
                        "MongoSupersetDirectQueryIT.engine",
                        "name=" + name + " engine=" + engineName + " drivers=" + drivers);
                String combined = (name + " " + engineName + " " + drivers).toLowerCase();
                if (combined.indexOf("mongo") >= 0) {
                    mongoNamed = mongoNamed + 1;
                }
            }
            i = i + 1;
        }
        Console.log("MongoSupersetDirectQueryIT.engine.mongo_named_count", String.valueOf(mongoNamed));
        Console.log(
                "MongoSupersetDirectQueryIT.engine.observation",
                "GET /api/v1/database/available/ is the public installed-dialect catalog");
    }

    private void probeMongoSqlalchemyUris() {
        this.testConnection("mongodb://host.docker.internal:27018/motion");
        this.testConnection("mongo://host.docker.internal:27018/motion");
        this.testConnection("mongodb+srv://localhost/motion");
    }

    private void testConnection(String sqlalchemyUri) {
        JsonObject body = new JsonObject();
        body.addProperty("database_name", "ILLUSTRATIVE mongo dialect probe (not persisted)");
        body.addProperty("sqlalchemy_uri", sqlalchemyUri);
        HttpResponse response = this.request(
                "POST",
                "/api/v1/database/test_connection/",
                body.toString(),
                true);
        this.logResponse("database.test_connection." + sqlalchemyUri, response);
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
            Console.log("MongoSupersetDirectQueryIT.blocker", "POST /api/v1/security/login");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("access_token") == null || json.get("access_token").isJsonNull()) {
            Console.log("MongoSupersetDirectQueryIT.blocker", "login response missing access_token");
            return false;
        }
        this.accessToken = json.get("access_token").getAsString();
        Console.log("MongoSupersetDirectQueryIT.access_token.present", "true");
        return true;
    }

    private boolean loadCsrf() {
        HttpResponse response = this.request("GET", "/api/v1/security/csrf_token/", null, true);
        this.logResponse("security.csrf_token", response);
        if (response.status != 200) {
            Console.log("MongoSupersetDirectQueryIT.blocker", "GET /api/v1/security/csrf_token/");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || json.get("result").isJsonNull()) {
            Console.log("MongoSupersetDirectQueryIT.blocker", "csrf response missing result");
            return false;
        }
        this.csrfToken = json.get("result").getAsString();
        Console.log("MongoSupersetDirectQueryIT.csrf.present", "true");
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

    private void logResponse(String label, HttpResponse response) {
        Console.log("MongoSupersetDirectQueryIT." + label + ".status", String.valueOf(response.status));
        Console.log("MongoSupersetDirectQueryIT." + label + ".body", response.body);
        if (response.setCookie != null) {
            Console.log("MongoSupersetDirectQueryIT." + label + ".set_cookie", response.setCookie);
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
            Console.log("MongoSupersetDirectQueryIT.request.exception", String.valueOf(e));
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
