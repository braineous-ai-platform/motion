package ai.braineous.motion.ingestion.timeprocessor.store;

import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionTimeWindow;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class MongoMotionHappeningStoreIT {

    @Inject
    MongoClient mongoClient;

    @Test
    void test_1_addRecord_and_findByRoutingKey_and_getAll_roundtrip() {

        String dbName = "motion_it";
        String collection = "motion_happening_roundtrip";
        MongoMotionHappeningStore store = newStore(dbName, collection);

        store.clear();

        MotionHappeningRecord record =
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1");

        store.addRecord(record);

        MotionHappeningRecord byRoutingKey =
                store.findByRoutingKey(
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME");

        List<MotionHappeningRecord> all =
                store.getAll();

        assertNotNull(byRoutingKey);
        assertEquals(1, all.size());

        assertEquals(
                "record-1",
                byRoutingKey.getRecordId());

        assertEquals(
                "frame-1",
                byRoutingKey.getFrameId());

        assertNotNull(byRoutingKey.getMotionFrame());

        Console.log(
                "record",
                byRoutingKey.toString());
    }

    @Test
    void test_2_addRecord_nullRecord_isNoop() {

        String dbName = "motion_it";
        String collection = "motion_happening_null_noop";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(null);

        assertEquals(
                0,
                store.getAll().size());
    }

    @Test
    void test_3_findByRoutingKey_nullRoutingKey_returnsNull() {

        String dbName = "motion_it";
        String collection = "motion_happening_find_null_routing";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        assertNull(
                store.findByRoutingKey(null));
    }

    @Test
    void test_4_findByRoutingKey_blankRoutingKey_returnsNull() {

        String dbName = "motion_it";
        String collection = "motion_happening_find_blank_routing";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        assertNull(
                store.findByRoutingKey("   "));
    }

    @Test
    void test_5_findByRoutingKey_trimmedRoutingKey_findsRecord() {

        String dbName = "motion_it";
        String collection = "motion_happening_find_trimmed_routing";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        MotionHappeningRecord record =
                store.findByRoutingKey(
                        "  ORDER-1001:ORDER:ORDER_OPERATION_FRAME  ");

        assertNotNull(record);

        assertEquals(
                "frame-1",
                record.getFrameId());

        Console.log(
                "record",
                record.toString());
    }

    @Test
    void test_6_findByFrameId_nullFrameId_returnsNull() {

        String dbName = "motion_it";
        String collection = "motion_happening_find_null_frame";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        assertNull(
                store.findByFrameId(null));
    }

    @Test
    void test_7_findByFrameId_blankFrameId_returnsNull() {

        String dbName = "motion_it";
        String collection = "motion_happening_find_blank_frame";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        assertNull(
                store.findByFrameId("   "));
    }

    @Test
    void test_8_findByFrameId_trimmedFrameId_findsRecord() {

        String dbName = "motion_it";
        String collection = "motion_happening_find_trimmed_frame";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        MotionHappeningRecord record =
                store.findByFrameId("  frame-1  ");

        assertNotNull(record);

        assertEquals(
                "frame-1",
                record.getFrameId());

        Console.log(
                "record",
                record.toString());
    }

    @Test
    void test_9_addRecord_multipleRecords_accumulates() {

        String dbName = "motion_it";
        String collection = "motion_happening_multiple_records";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        store.addRecord(
                newRecord(
                        "record-2",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-2"));

        store.addRecord(
                newRecord(
                        "record-3",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-3"));

        List<MotionHappeningRecord> all =
                store.getAll();

        Console.log(
                "all-size",
                String.valueOf(all.size()));

        assertEquals(
                3,
                all.size());
    }

    @Test
    void test_10_findBySubject_mixedSubjects_isolatedResults() {

        String dbName = "motion_it";
        String collection = "motion_happening_mixed_subjects";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        store.addRecord(
                newRecord(
                        "record-2",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-2"));

        store.addRecord(
                newRecord(
                        "record-3",
                        "ORDER-2001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-2001",
                        "ORDER",
                        "frame-3"));

        List<MotionHappeningRecord> order1001 =
                store.findBySubject(
                        "ORDER-1001",
                        "ORDER");

        List<MotionHappeningRecord> order2001 =
                store.findBySubject(
                        "ORDER-2001",
                        "ORDER");

        List<MotionHappeningRecord> missing =
                store.findBySubject(
                        "ORDER-9999",
                        "ORDER");

        Console.log(
                "order1001-size",
                String.valueOf(order1001.size()));

        Console.log(
                "order2001-size",
                String.valueOf(order2001.size()));

        assertEquals(2, order1001.size());
        assertEquals(1, order2001.size());
        assertEquals(0, missing.size());
    }

    @Test
    void test_11_findBySubject_nullOrBlank_returnsEmpty() {

        String dbName = "motion_it";
        String collection = "motion_happening_subject_null_blank";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        assertEquals(
                0,
                store.findBySubject(
                        null,
                        "ORDER").size());

        assertEquals(
                0,
                store.findBySubject(
                        "ORDER-1001",
                        null).size());

        assertEquals(
                0,
                store.findBySubject(
                        "   ",
                        "ORDER").size());

        assertEquals(
                0,
                store.findBySubject(
                        "ORDER-1001",
                        "   ").size());
    }

    @Test
    void test_12_clear_emptiesCollection() {

        String dbName = "motion_it";
        String collection = "motion_happening_clear";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        store.addRecord(
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1"));

        store.addRecord(
                newRecord(
                        "record-2",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-2"));

        assertEquals(
                2,
                store.getAll().size());

        store.clear();

        assertEquals(
                0,
                store.getAll().size());
    }



    @Test
    void test_14_roundtrip_preservesNestedMotionFrame() {

        String dbName = "motion_it";
        String collection = "motion_happening_nested_frame";
        MongoMotionHappeningStore store = newStore(dbName, collection);


        store.clear();

        MotionHappeningRecord record =
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-1");

        store.addRecord(record);

        MotionHappeningRecord out =
                store.findByRoutingKey(
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME");

        assertNotNull(out);
        assertNotNull(out.getMotionFrame());

        assertEquals(
                "frame-1",
                out.getMotionFrame().getFrameId());

        assertEquals(
                "ORDER_OPERATION_FRAME",
                out.getMotionFrame().getFrameType());

        assertEquals(
                "2026-05-22T10:00:00Z",
                out.getMotionFrame().getTimeWindow().getWindowStart());

        assertEquals(
                "2026-05-22T10:05:00Z",
                out.getMotionFrame().getTimeWindow().getWindowEnd());

        assertEquals(
                "1",
                out.getMotionFrame().getSequence());

        assertEquals(
                "OPEN",
                out.getMotionFrame().getStatus());

        assertNotNull(
                out.getMotionFrame().getMotionEvents());

        assertEquals(
                0,
                out.getMotionFrame().getMotionEvents().size());

        Console.log(
                "record",
                out.toString());
    }

    private MongoMotionHappeningStore newStore(
            String dbName,
            String collectionName) {

        MongoMotionHappeningStore store =
                new MongoMotionHappeningStore();

        store.mongoClient = mongoClient;

        // Direct store ITs own isolated collections that runtime consumers cannot mutate.
        setField(store, "dbName", dbName);
        setField(store, "collectionName", collectionName);

        return store;
    }

    private void setField(
            MongoMotionHappeningStore store,
            String fieldName,
            String value) {

        try {
            Field field =
                    MongoMotionHappeningStore.class.getDeclaredField(fieldName);

            field.setAccessible(true);
            field.set(store, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Unable to configure isolated Mongo store",
                    exception);
        }
    }

    private MotionHappeningRecord newRecord(
            String recordId,
            String routingKey,
            String subjectId,
            String subjectType,
            String frameId) {

        MotionFrame frame = new MotionFrame();
        frame.setFrameId(frameId);
        frame.setFrameType("ORDER_OPERATION_FRAME");
        MotionTimeWindow timeWindow = new MotionTimeWindow();
        timeWindow.setWindowStart("2026-05-22T10:00:00Z");
        timeWindow.setWindowEnd("2026-05-22T10:05:00Z");
        frame.setTimeWindow(timeWindow);
        frame.setSequence("1");
        frame.setStatus("OPEN");
        frame.setMetadataJson("{\"runtime\":\"motion\"}");

        MotionHappeningRecord record =
                new MotionHappeningRecord();

        record.setRecordId(recordId);
        record.setRoutingKey(routingKey);

        record.setSubjectId(subjectId);
        record.setSubjectType(subjectType);

        record.setFrameId(frameId);
        record.setFrameType("ORDER_OPERATION_FRAME");

        record.setWindowStart("2026-05-22T10:00:00Z");
        record.setWindowEnd("2026-05-22T10:05:00Z");

        record.setSequence("1");
        record.setStatus("OPEN");

        record.setMotionFrame(frame);

        record.setCreatedAt("2026-05-22T10:06:00Z");
        record.setUpdatedAt("2026-05-22T10:07:00Z");

        return record;
    }
}
