package ai.braineous.motion.mox;

public final class TmExecutionOutcomes {

    public static final String TITLE = "TM SUCCESS vs FAILURE";

    public static final String MONGO_URI = "mongodb://localhost:27017";

    public static final String SUPERSET_URL = "http://localhost:8099";

    public static final String SUPERSET_MONGO_URI =
            "mongodb://host.docker.internal:27017/cgo?mode=superset";

    public static final String DATABASE_NAME = "cgo-tm-execution-results";

    public static final int DATABASE_ID = 2;

    public static final int DATASET_ID = 4;

    public static final int CHART_ID = 2;

    public static final int DASHBOARD_ID = 2;

    public static final String CHART_NAME = "UC1 TM SUCCESS vs FAILURE (ok)";

    public static final String DASHBOARD_TITLE = "UC1 TM SUCCESS vs FAILURE";

    public static final String VIZ_TYPE = "echarts_timeseries_bar";

    public static final String METRIC_NAME = "tm_ok_count";

    public static final String METRIC_EXPRESSION = "SUM(\"count\")";

    public static final String OUTCOME_SQL =
            "CASE WHEN ok THEN 'SUCCESS' ELSE 'FAILURE' END";

    public static final String STANDALONE = "3";

    public static final String DASHBOARD_CSS =
            "\n.header-controls { display: none !important; }\n"
                    + "#slice_2-controls { display: none !important; }\n"
                    + ".header-title a { pointer-events: none; cursor: default; }\n";

    public static final String DASHBOARD_POSITION_JSON =
            "{\"DASHBOARD_VERSION_KEY\": \"v2\","
                    + " \"ROOT_ID\": {\"children\": [\"GRID_ID\"], \"id\": \"ROOT_ID\", \"type\": \"ROOT\"},"
                    + " \"GRID_ID\": {\"children\": [\"ROW-sf\"], \"id\": \"GRID_ID\", \"type\": \"GRID\"},"
                    + " \"ROW-sf\": {\"children\": [\"CHART-sf\"], \"id\": \"ROW-sf\","
                    + " \"meta\": {\"background\": \"BACKGROUND_TRANSPARENT\"}, \"type\": \"ROW\"},"
                    + " \"CHART-sf\": {\"children\": [], \"id\": \"CHART-sf\","
                    + " \"meta\": {\"chartId\": 2, \"height\": 50,"
                    + " \"sliceName\": \"UC1 TM SUCCESS vs FAILURE (ok)\","
                    + " \"width\": 12, \"hideChartControls\": true}, \"type\": \"CHART\"}}";

    private TmExecutionOutcomes() {
    }

    public static String dashboardUrl() {
        return SUPERSET_URL
                + "/superset/dashboard/"
                + DASHBOARD_ID
                + "/?standalone="
                + STANDALONE;
    }
}
