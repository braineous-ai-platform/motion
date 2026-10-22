package ai.braineous.motion.it.jit;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionEnvelope;
import ai.braineous.motion.ingestion.eventprocessor.model.MotionResponseResult;
import ai.braineous.motion.ingestion.eventprocessor.model.RawEvent;
import ai.braineous.motion.ingestion.eventprocessor.orchestrator.MotionIngestionOrchestrator;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningRecord;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningStore;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
public class JitSalesEventIT {

    private static final String ENVELOPE_ID = "SALE-002";

    private static final String CORRELATION_ID = "PROD-SHAMPOO-250";

    private static final String EVENT_TYPE = "SALE";

    private static final String SOURCE_TYPE = "SALES";

    private static final String RECEIVED_AT = "2026-10-21T12:00:00Z";

    private static final String SALES_PAYLOAD =
            "{\"saleId\":\"SALE-002\",\"productId\":\"PROD-SHAMPOO-250\",\"quantitySold\":1}";

    @Inject
    MotionIngestionOrchestrator ingestionOrchestrator;

    @Inject
    MotionHappeningStore happeningStore;

    @Test
    public void test_1() {
        RawEvent rawEvent = new RawEvent();
        rawEvent.setRawEventId(ENVELOPE_ID);
        rawEvent.setEventType(EVENT_TYPE);
        rawEvent.setSourceType(SOURCE_TYPE);
        rawEvent.setReceivedAt(RECEIVED_AT);
        rawEvent.setPayloadJson(SALES_PAYLOAD);

        MotionEnvelope envelope = new MotionEnvelope();
        envelope.setEnvelopeId(ENVELOPE_ID);
        envelope.setCorrelationId(CORRELATION_ID);
        envelope.setReceivedAt(RECEIVED_AT);
        envelope.setRawEvent(rawEvent);

        Console.log("JitSalesEventIT.envelope", envelope.toJson());
        Console.log("JitSalesEventIT.payload", rawEvent.getPayloadJson());

        MotionResponseResult response =
                this.ingestionOrchestrator.ingest(envelope);

        if (response == null) {
            Console.log("JitSalesEventIT.response", "null");
        } else {
            Console.log("JitSalesEventIT.response", response.toJson());
            this.logMotionEvent(response.getMotionEventJson());
        }

        this.logHappening();
    }

    private void logMotionEvent(String motionEventJson) {
        if (motionEventJson == null) {
            Console.log("JitSalesEventIT.motionEvent", "null");
            return;
        }

        Console.log("JitSalesEventIT.motionEvent", motionEventJson);

        MotionEvent motionEvent =
                MotionEvent.fromJson(motionEventJson, MotionEvent.class);

        Console.log(
                "JitSalesEventIT.motionEvent.eventId",
                shown(motionEvent.getEventId()));
        Console.log(
                "JitSalesEventIT.motionEvent.eventType",
                shown(motionEvent.getEventType()));
        Console.log(
                "JitSalesEventIT.motionEvent.operation",
                shown(motionEvent.getOperation()));
        Console.log(
                "JitSalesEventIT.motionEvent.subjectId",
                shown(motionEvent.getSubjectId()));
        Console.log(
                "JitSalesEventIT.motionEvent.subjectType",
                shown(motionEvent.getSubjectType()));
        Console.log(
                "JitSalesEventIT.motionEvent.originTime",
                shown(motionEvent.getOriginTime()));
        Console.log(
                "JitSalesEventIT.motionEvent.receivedAt",
                shown(motionEvent.getReceivedAt()));
        Console.log(
                "JitSalesEventIT.motionEvent.payloadJson",
                shown(motionEvent.getPayloadJson()));
        if (motionEvent.getReplaySignal() == null) {
            Console.log("JitSalesEventIT.motionEvent.replaySignal", "null");
        } else {
            Console.log(
                    "JitSalesEventIT.motionEvent.replaySignal",
                    motionEvent.getReplaySignal().toJson());
        }
    }

    private void logHappening() {
        List<MotionHappeningRecord> records =
                this.happeningStore.findBySubject(
                        CORRELATION_ID,
                        SOURCE_TYPE);

        if (records == null) {
            Console.log("JitSalesEventIT.happening", "null");
            Console.log("JitSalesEventIT.motionFrame", "null");
            Console.log("JitSalesEventIT.operationalView", "null");
            return;
        }

        Console.log(
                "JitSalesEventIT.happening.count",
                String.valueOf(records.size()));

        if (records.isEmpty()) {
            Console.log("JitSalesEventIT.motionFrame", "null");
            Console.log("JitSalesEventIT.operationalView", "null");
            return;
        }

        for (int i = 0; i < records.size(); i++) {
            MotionHappeningRecord record = records.get(i);
            Console.log(
                    "JitSalesEventIT.happening.record",
                    record.toJson());

            MotionFrame motionFrame = record.getMotionFrame();
            if (motionFrame == null) {
                Console.log("JitSalesEventIT.motionFrame", "null");
            } else {
                Console.log(
                        "JitSalesEventIT.motionFrame",
                        motionFrame.toJson());
            }
        }

        Console.log("JitSalesEventIT.operationalView", "null");
    }

    private String shown(String value) {
        if (value == null) {
            return "null";
        }
        return value;
    }
}
