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
public class UnifiedObservabilityIT {

    private static final String BASE_URL = "http://localhost:8088";

    private String accessToken;

    private String csrfToken;

    private String sessionCookie;

    @Test
    public void test_1() {
        Console.log("UnifiedObservabilityIT.base_url", BASE_URL);
        Console.log(
                "UnifiedObservabilityIT.test_1.goal",
                "compose existing TM and CGO illustrative charts on one native landing dashboard");
        Console.log(
                "UnifiedObservabilityIT.test_1.disclaimer",
                "illustrative charts only; not Motion runtime observability");

        if (!this.login()) {
            return;
        }
        if (!this.loadCsrf()) {
            return;
        }

        if (!this.preserveSourceCharts()) {
            return;
        }
        this.inspectDashboardListContract();
        this.inspectDetailDashboardNavigation();

        String name122 = this.readChartSliceName(122);
        String name125 = this.readChartSliceName(125);
        String name124 = this.readChartSliceName(124);
        String name130 = this.readChartSliceName(130);
        String name136 = this.readChartSliceName(136);
        String name134 = this.readChartSliceName(134);
        if (name122 == null
                || name125 == null
                || name124 == null
                || name130 == null
                || name136 == null
                || name134 == null) {
            Console.log("UnifiedObservabilityIT.blocker", "missing slice_name on one of 122/125/124/130/136/134");
            return;
        }

        this.logChartDashboards("before", 122);
        this.logChartDashboards("before", 125);
        this.logChartDashboards("before", 124);
        this.logChartDashboards("before", 130);
        this.logChartDashboards("before", 136);
        this.logChartDashboards("before", 134);

        Integer dashboardId = this.findOrCreateLandingDashboard();
        if (dashboardId == null) {
            return;
        }
        this.placeLandingLayout(
                dashboardId.intValue(),
                name122,
                name125,
                name124,
                name130,
                name136,
                name134);
        this.getDashboardCharts(dashboardId.intValue());
        this.readLandingDashboard(dashboardId.intValue());

        this.logChartDashboards("after", 122);
        this.logChartDashboards("after", 125);
        this.logChartDashboards("after", 124);
        this.logChartDashboards("after", 130);
        this.logChartDashboards("after", 136);
        this.logChartDashboards("after", 134);

        Console.log(
                "UnifiedObservabilityIT.test_1.observation.navigation",
                "REST has no dashboard-to-dashboard link resource; markdown hrefs to /superset/dashboard/{12-24}/ are native layout, not new charts");
        Console.log(
                "UnifiedObservabilityIT.test_1.observation.compose",
                "only POST/PUT the new dashboard; datasets/charts/dashboards 12-24 were GET-only");
        Console.log(
                "UnifiedObservabilityIT.test_1.observation.slices",
                "chart-dashboard M2M is filled by PUT json_metadata.positions (DashboardDAO.set_dash_metadata), not PUT /api/v1/chart");

        this.logExistingResource("preserve.dashboard_12", "/api/v1/dashboard/12");
        this.logExistingResource("preserve.dashboard_17", "/api/v1/dashboard/17");
        this.logExistingResource("preserve.dashboard_24", "/api/v1/dashboard/24");
    }

    private boolean preserveSourceCharts() {
        int[] ids = new int[] {122, 125, 124, 130, 136, 134};
        int i = 0;
        while (i < ids.length) {
            HttpResponse response = this.request("GET", "/api/v1/chart/" + ids[i], null, true);
            this.logResponse("preserve.chart_" + ids[i] + ".get", response);
            if (response.status != 200) {
                Console.log("UnifiedObservabilityIT.blocker", "GET /api/v1/chart/" + ids[i]);
                return false;
            }
            i = i + 1;
        }
        return true;
    }

    private void inspectDashboardListContract() {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/dashboard/?q=(page:0,page_size:5)",
                null,
                true);
        this.logResponse("dashboard.list", listed);
        HttpResponse info = this.request("GET", "/api/v1/dashboard/_info", null, true);
        this.logResponse("dashboard.info", info);
    }

    private void inspectDetailDashboardNavigation() {
        int id = 12;
        while (id <= 24) {
            HttpResponse response = this.request("GET", "/api/v1/dashboard/" + id, null, true);
            this.logResponse("nav.dashboard_" + id, response);
            if (response.status == 200) {
                JsonObject json = this.parseObject(response.body);
                if (json != null && json.get("result") != null && json.get("result").isJsonObject()) {
                    JsonObject result = json.getAsJsonObject("result");
                    String title = "";
                    String path = "";
                    if (result.get("dashboard_title") != null && !result.get("dashboard_title").isJsonNull()) {
                        title = result.get("dashboard_title").getAsString();
                    }
                    if (result.get("url") != null && !result.get("url").isJsonNull()) {
                        path = result.get("url").getAsString();
                    }
                    Console.log(
                            "UnifiedObservabilityIT.nav.dashboard_" + id + ".summary",
                            title + " " + BASE_URL + path);
                }
            }
            HttpResponse related = this.request(
                    "GET",
                    "/api/v1/dashboard/" + id + "/related",
                    null,
                    true);
            this.logResponse("nav.dashboard_" + id + ".related", related);
            id = id + 1;
        }
        Console.log(
                "UnifiedObservabilityIT.nav.contract",
                "GET /api/v1/dashboard/{id} returns url; GET related is inspected; no native parent-landing field");
    }

    private String readChartSliceName(int chartId) {
        HttpResponse response = this.request("GET", "/api/v1/chart/" + chartId, null, true);
        this.logResponse("chart." + chartId + ".slice", response);
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return null;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("slice_name") == null || result.get("slice_name").isJsonNull()) {
            return null;
        }
        String name = result.get("slice_name").getAsString();
        Console.log("UnifiedObservabilityIT.chart." + chartId + ".slice_name", name);
        return name;
    }

    private void logChartDashboards(String phase, int chartId) {
        HttpResponse response = this.request("GET", "/api/v1/chart/" + chartId, null, true);
        this.logResponse("chart." + chartId + "." + phase, response);
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("dashboards") != null) {
            Console.log(
                    "UnifiedObservabilityIT.chart." + chartId + "." + phase + ".dashboards",
                    result.get("dashboards").toString());
        }
    }

    private Integer findOrCreateLandingDashboard() {
        HttpResponse listed = this.request(
                "GET",
                "/api/v1/dashboard/?q=(page:0,page_size:50)",
                null,
                true);
        this.logResponse("dashboard.find", listed);
        JsonObject listedJson = this.parseObject(listed.body);
        if (listedJson != null && listedJson.get("result") != null && listedJson.get("result").isJsonArray()) {
            JsonArray rows = listedJson.getAsJsonArray("result");
            int i = 0;
            while (i < rows.size()) {
                JsonElement element = rows.get(i);
                if (element != null && element.isJsonObject()) {
                    JsonObject row = element.getAsJsonObject();
                    if (row.get("dashboard_title") != null
                            && !row.get("dashboard_title").isJsonNull()
                            && this.landingTitle().equals(row.get("dashboard_title").getAsString())
                            && row.get("id") != null
                            && !row.get("id").isJsonNull()) {
                        int existingId = row.get("id").getAsInt();
                        Console.log(
                                "UnifiedObservabilityIT.dashboard.reuse.id",
                                String.valueOf(existingId));
                        return Integer.valueOf(existingId);
                    }
                }
                i = i + 1;
            }
        }
        JsonObject body = new JsonObject();
        body.addProperty("dashboard_title", this.landingTitle());
        body.addProperty("published", Boolean.TRUE);
        HttpResponse response = this.request("POST", "/api/v1/dashboard/", body.toString(), true);
        this.logResponse("dashboard.create", response);
        if (response.status != 201) {
            Console.log("UnifiedObservabilityIT.blocker", "POST /api/v1/dashboard/ landing");
            return null;
        }
        return this.readId(response.body);
    }

    private String landingTitle() {
        return "ILLUSTRATIVE Motion unified observability landing (not runtime)";
    }

    private void placeLandingLayout(
            int dashboardId,
            String name122,
            String name125,
            String name124,
            String name130,
            String name136,
            String name134) {
        JsonObject markdownMeta = new JsonObject();
        markdownMeta.addProperty("code", this.navigationMarkdown());
        markdownMeta.addProperty("height", Integer.valueOf(46));
        markdownMeta.addProperty("width", Integer.valueOf(12));
        JsonObject markdown = new JsonObject();
        markdown.add("children", new JsonArray());
        markdown.addProperty("id", "MARKDOWN-landing-nav");
        markdown.add("meta", markdownMeta);
        markdown.addProperty("type", "MARKDOWN");

        JsonObject tmHeader = this.headerNode("HEADER-tm", "Transaction Memory");
        JsonObject cgoHeader = this.headerNode("HEADER-cgo", "CGO NOW / History");

        JsonObject chart122 = this.chartNode("CHART-tm-122", 122, name122, 40);
        JsonObject chart125 = this.chartNode("CHART-tm-125", 125, name125, 40);
        JsonObject chart124 = this.chartNode("CHART-tm-124", 124, name124, 40);
        JsonObject chart130 = this.chartNode("CHART-cgo-130", 130, name130, 50);
        JsonObject chart136 = this.chartNode("CHART-cgo-136", 136, name136, 50);
        JsonObject chart134 = this.chartNode("CHART-cgo-134", 134, name134, 40);

        JsonObject rowNav = this.rowNode("ROW-nav", "MARKDOWN-landing-nav");
        JsonObject rowTmHeader = this.rowNode("ROW-tm-header", "HEADER-tm");
        JsonObject row122 = this.rowNode("ROW-tm-122", "CHART-tm-122");
        JsonObject row125 = this.rowNode("ROW-tm-125", "CHART-tm-125");
        JsonObject row124 = this.rowNode("ROW-tm-124", "CHART-tm-124");
        JsonObject rowCgoHeader = this.rowNode("ROW-cgo-header", "HEADER-cgo");
        JsonObject row130 = this.rowNode("ROW-cgo-130", "CHART-cgo-130");
        JsonObject row136 = this.rowNode("ROW-cgo-136", "CHART-cgo-136");
        JsonObject row134 = this.rowNode("ROW-cgo-134", "CHART-cgo-134");

        JsonArray gridChildren = new JsonArray();
        gridChildren.add("ROW-nav");
        gridChildren.add("ROW-tm-header");
        gridChildren.add("ROW-tm-122");
        gridChildren.add("ROW-tm-125");
        gridChildren.add("ROW-tm-124");
        gridChildren.add("ROW-cgo-header");
        gridChildren.add("ROW-cgo-130");
        gridChildren.add("ROW-cgo-136");
        gridChildren.add("ROW-cgo-134");
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
        position.add("MARKDOWN-landing-nav", markdown);
        position.add("HEADER-tm", tmHeader);
        position.add("HEADER-cgo", cgoHeader);
        position.add("CHART-tm-122", chart122);
        position.add("CHART-tm-125", chart125);
        position.add("CHART-tm-124", chart124);
        position.add("CHART-cgo-130", chart130);
        position.add("CHART-cgo-136", chart136);
        position.add("CHART-cgo-134", chart134);
        position.add("ROW-nav", rowNav);
        position.add("ROW-tm-header", rowTmHeader);
        position.add("ROW-tm-122", row122);
        position.add("ROW-tm-125", row125);
        position.add("ROW-tm-124", row124);
        position.add("ROW-cgo-header", rowCgoHeader);
        position.add("ROW-cgo-130", row130);
        position.add("ROW-cgo-136", row136);
        position.add("ROW-cgo-134", row134);
        position.add("GRID_ID", gridNode);
        position.add("ROOT_ID", rootNode);
        position.addProperty("DASHBOARD_VERSION_KEY", "v2");

        Console.log("UnifiedObservabilityIT.position_json.request", position.toString());
        JsonObject metadata = new JsonObject();
        metadata.add("positions", position);
        JsonObject body = new JsonObject();
        body.addProperty("position_json", position.toString());
        body.addProperty("json_metadata", metadata.toString());
        HttpResponse response = this.request(
                "PUT",
                "/api/v1/dashboard/" + dashboardId,
                body.toString(),
                true);
        this.logResponse("dashboard.position", response);
        if (response.status != 200) {
            Console.log("UnifiedObservabilityIT.blocker", "PUT /api/v1/dashboard/{id} position_json");
        }
    }

    private String navigationMarkdown() {
        return "## Illustrative landing (not Motion records)\n\n"
                + "**Transaction Memory**\n"
                + "- [12 SUCCESS vs FAILURE](/superset/dashboard/12/)\n"
                + "- [13 drill-down](/superset/dashboard/13/)\n"
                + "- [14 execution evidence](/superset/dashboard/14/)\n"
                + "- [15 timeseries](/superset/dashboard/15/)\n"
                + "- [16 SystemMutation](/superset/dashboard/16/)\n\n"
                + "**CGO**\n"
                + "- [17 Fact Explorer](/superset/dashboard/17/)\n"
                + "- [18 one-edge graph](/superset/dashboard/18/)\n"
                + "- [19 expanded graph](/superset/dashboard/19/)\n"
                + "- [20 relationship detail](/superset/dashboard/20/)\n"
                + "- [21 historical T1](/superset/dashboard/21/)\n"
                + "- [22 Change Explorer](/superset/dashboard/22/)\n"
                + "- [23 quantityOnHand trend](/superset/dashboard/23/)\n"
                + "- [24 Fact-kind filters](/superset/dashboard/24/)\n";
    }

    private JsonObject headerNode(String id, String text) {
        JsonObject meta = new JsonObject();
        meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        meta.addProperty("headerSize", "MEDIUM_HEADER");
        meta.addProperty("text", text);
        JsonObject node = new JsonObject();
        node.add("children", new JsonArray());
        node.addProperty("id", id);
        node.add("meta", meta);
        node.addProperty("type", "HEADER");
        return node;
    }

    private JsonObject chartNode(String id, int chartId, String sliceName, int height) {
        JsonObject meta = new JsonObject();
        meta.addProperty("chartId", Integer.valueOf(chartId));
        meta.addProperty("height", Integer.valueOf(height));
        meta.addProperty("sliceName", sliceName);
        meta.addProperty("width", Integer.valueOf(12));
        JsonObject node = new JsonObject();
        node.add("children", new JsonArray());
        node.addProperty("id", id);
        node.add("meta", meta);
        node.addProperty("type", "CHART");
        return node;
    }

    private JsonObject rowNode(String id, String childId) {
        JsonArray children = new JsonArray();
        children.add(childId);
        JsonObject meta = new JsonObject();
        meta.addProperty("background", "BACKGROUND_TRANSPARENT");
        JsonObject node = new JsonObject();
        node.add("children", children);
        node.addProperty("id", id);
        node.add("meta", meta);
        node.addProperty("type", "ROW");
        return node;
    }

    private void getDashboardCharts(int dashboardId) {
        HttpResponse response = this.request(
                "GET",
                "/api/v1/dashboard/" + dashboardId + "/charts",
                null,
                true);
        this.logResponse("dashboard.charts", response);
    }

    private void readLandingDashboard(int dashboardId) {
        HttpResponse response = this.request("GET", "/api/v1/dashboard/" + dashboardId, null, true);
        this.logResponse("dashboard.get", response);
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || !json.get("result").isJsonObject()) {
            return;
        }
        JsonObject result = json.getAsJsonObject("result");
        if (result.get("id") != null && !result.get("id").isJsonNull()) {
            Console.log("UnifiedObservabilityIT.dashboard.id", String.valueOf(result.get("id").getAsInt()));
        }
        if (result.get("charts") != null) {
            Console.log("UnifiedObservabilityIT.dashboard.charts", result.get("charts").toString());
        }
        if (result.get("position_json") != null && !result.get("position_json").isJsonNull()) {
            Console.log(
                    "UnifiedObservabilityIT.dashboard.position_json",
                    result.get("position_json").getAsString());
        }
        if (result.get("url") != null && !result.get("url").isJsonNull()) {
            Console.log("UnifiedObservabilityIT.dashboard.path", result.get("url").getAsString());
            Console.log("UnifiedObservabilityIT.dashboard.url", BASE_URL + result.get("url").getAsString());
        } else {
            Console.log(
                    "UnifiedObservabilityIT.dashboard.url",
                    BASE_URL + "/superset/dashboard/" + dashboardId + "/");
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
            Console.log("UnifiedObservabilityIT.blocker", "POST /api/v1/security/login");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("access_token") == null || json.get("access_token").isJsonNull()) {
            Console.log("UnifiedObservabilityIT.blocker", "login response missing access_token");
            return false;
        }
        this.accessToken = json.get("access_token").getAsString();
        Console.log("UnifiedObservabilityIT.access_token.present", "true");
        return true;
    }

    private boolean loadCsrf() {
        HttpResponse response = this.request("GET", "/api/v1/security/csrf_token/", null, true);
        this.logResponse("security.csrf_token", response);
        if (response.status != 200) {
            Console.log("UnifiedObservabilityIT.blocker", "GET /api/v1/security/csrf_token/");
            return false;
        }
        JsonObject json = this.parseObject(response.body);
        if (json == null || json.get("result") == null || json.get("result").isJsonNull()) {
            Console.log("UnifiedObservabilityIT.blocker", "csrf response missing result");
            return false;
        }
        this.csrfToken = json.get("result").getAsString();
        Console.log("UnifiedObservabilityIT.csrf.present", "true");
        return true;
    }

    private void logExistingResource(String label, String path) {
        HttpResponse response = this.request("GET", path, null, true);
        this.logResponse(label, response);
    }

    private Integer readId(String body) {
        JsonObject json = this.parseObject(body);
        if (json == null || json.get("id") == null || json.get("id").isJsonNull()) {
            Console.log("UnifiedObservabilityIT.blocker", "response missing id");
            return null;
        }
        int id = json.get("id").getAsInt();
        Console.log("UnifiedObservabilityIT.created.id", String.valueOf(id));
        return Integer.valueOf(id);
    }

    private void logResponse(String label, HttpResponse response) {
        Console.log("UnifiedObservabilityIT." + label + ".status", String.valueOf(response.status));
        Console.log("UnifiedObservabilityIT." + label + ".body", response.body);
        if (response.setCookie != null) {
            Console.log("UnifiedObservabilityIT." + label + ".set_cookie", response.setCookie);
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
            Console.log("UnifiedObservabilityIT.request.exception", String.valueOf(e));
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
