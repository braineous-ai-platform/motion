package ai.braineous.motion.mox;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class TmExecutionOutcomesView {

    public static final String NAME = "tm_observability_ok_counts";

    public static final String SOURCE_DATABASE = "cgo";

    public static final String SOURCE_COLLECTION = "tm_observability";

    public static final String QUERYSET_RESOURCE =
            "/ai/braineous/motion/mox/tm-execution-outcomes.sql";

    private TmExecutionOutcomesView() {
    }

    public static String queryset() {
        return read(QUERYSET_RESOURCE);
    }

    private static String read(String resource) {
        InputStream stream = TmExecutionOutcomesView.class.getResourceAsStream(resource);
        if (stream == null) {
            throw new IllegalStateException("missing resource " + resource);
        }
        try {
            byte[] bytes = stream.readAllBytes();
            stream.close();
            return new String(bytes, StandardCharsets.UTF_8).trim();
        } catch (Exception exception) {
            throw new IllegalStateException("failed to read " + resource, exception);
        }
    }
}
