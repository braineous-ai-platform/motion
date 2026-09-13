package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import io.braineous.motion.core.model.MotionFrame;
import jakarta.enterprise.inject.spi.CDI;

public class SinkProcessor {

    private final OperationalViewMaterializer operationalViewMaterializer;
    private final OperationalSink operationalSink;
    private final CGOECESink cgoECESink;

    public SinkProcessor() {
        this.operationalViewMaterializer =
                new OperationalViewMaterializer();

        this.operationalSink =
                CDI.current()
                        .select(MongoOperationalSink.class)
                        .get();

        this.cgoECESink =
                new CGOECESink();
    }

    SinkProcessor(
            OperationalViewMaterializer operationalViewMaterializer,
            OperationalSink operationalSink) {

        this.operationalViewMaterializer =
                operationalViewMaterializer;

        this.operationalSink =
                operationalSink;

        this.cgoECESink =
                new CGOECESink();
    }

    public void process(MotionFrame motionFrame) {

        if (motionFrame == null) {
            return;
        }

        OperationalView operationalView =
                operationalViewMaterializer.materialize(
                        motionFrame);

        if (operationalView != null) {
            operationalSink.write(
                    operationalView);
        }

        cgoECESink.write(
                motionFrame);
    }
}
