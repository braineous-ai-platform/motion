package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import io.braineous.motion.core.model.MotionFrame;
import jakarta.enterprise.inject.spi.CDI;

public class SinkProcessor {

    private final OperationalViewMaterializer operationalViewMaterializer;
    private final OperationalSink operationalSink;

    public SinkProcessor() {
        this.operationalViewMaterializer =
                new OperationalViewMaterializer();

        this.operationalSink =
                CDI.current()
                        .select(MongoOperationalSink.class)
                        .get();
    }

    SinkProcessor(
            OperationalViewMaterializer operationalViewMaterializer,
            OperationalSink operationalSink) {

        this.operationalViewMaterializer =
                operationalViewMaterializer;

        this.operationalSink =
                operationalSink;
    }

    public void process(MotionFrame motionFrame) {

        if (motionFrame == null) {
            return;
        }

        OperationalView operationalView =
                operationalViewMaterializer.materialize(
                        motionFrame);

        if (operationalView == null) {
            return;
        }

        operationalSink.write(
                operationalView);
    }
}
