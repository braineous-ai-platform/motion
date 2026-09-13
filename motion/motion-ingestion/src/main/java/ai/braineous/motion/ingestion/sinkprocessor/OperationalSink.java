package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;

public interface OperationalSink {

    void write(OperationalView operationalView);
}
