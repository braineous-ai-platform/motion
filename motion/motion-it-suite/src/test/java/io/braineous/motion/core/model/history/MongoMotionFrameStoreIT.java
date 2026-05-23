package io.braineous.motion.core.model.history;

import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import io.braineous.motion.core.model.MotionFrame;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class MongoMotionFrameStoreIT {

    @Inject
    MongoClient mongoClient;

    @Test
    void addRecord_and_findByFrameId_and_getAll_roundtrip() {
        Console.log("IT", "MongoMotionFrameStoreIT.addRecord_and_findByFrameId_and_getAll_roundtrip");

        String dbName = "motion_it";
        String collection = "motion_frame_roundtrip";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        MotionFrameRecord record = newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1");

        store.addRecord(record);

        MotionFrameRecord byFrameId = store.findByFrameId("frame-1");
        List<MotionFrameRecord> all = store.getAll();

        assertNotNull(byFrameId);
        assertEquals(1, all.size());
        assertEquals("record-1", byFrameId.getRecordId());
        assertEquals("frame-1", byFrameId.getFrameId());
        assertNotNull(byFrameId.getMotionFrame());
        assertEquals("frame-1", byFrameId.getMotionFrame().getFrameId());

        Console.log("record", byFrameId.toString());
    }

    @Test
    void addRecord_nullRecord_isNoop() {
        Console.log("IT", "MongoMotionFrameStoreIT.addRecord_nullRecord_isNoop");

        String dbName = "motion_it";
        String collection = "motion_frame_null_noop";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(null);

        assertEquals(0, store.getAll().size());
    }

    @Test
    void findByFrameId_nullFrameId_returnsNull() {
        Console.log("IT", "MongoMotionFrameStoreIT.findByFrameId_nullFrameId_returnsNull");

        String dbName = "motion_it";
        String collection = "motion_frame_find_null_frameid";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1"));

        assertNull(store.findByFrameId(null));
    }

    @Test
    void findByFrameId_blankFrameId_returnsNull() {
        Console.log("IT", "MongoMotionFrameStoreIT.findByFrameId_blankFrameId_returnsNull");

        String dbName = "motion_it";
        String collection = "motion_frame_find_blank_frameid";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1"));

        assertNull(store.findByFrameId("   "));
    }

    @Test
    void findByFrameId_trimmedFrameId_findsRecord() {
        Console.log("IT", "MongoMotionFrameStoreIT.findByFrameId_trimmedFrameId_findsRecord");

        String dbName = "motion_it";
        String collection = "motion_frame_find_trimmed_frameid";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1"));

        MotionFrameRecord record = store.findByFrameId("  frame-1  ");

        assertNotNull(record);
        assertEquals("frame-1", record.getFrameId());
    }

    @Test
    void addRecord_multipleRecords_accumulates() {
        Console.log("IT", "MongoMotionFrameStoreIT.addRecord_multipleRecords_accumulates");

        String dbName = "motion_it";
        String collection = "motion_frame_multiple_records";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1"));
        store.addRecord(newRecord("record-2", "context-1", "ORDER-1001", "ORDER", "frame-2"));
        store.addRecord(newRecord("record-3", "context-1", "ORDER-1001", "ORDER", "frame-3"));

        assertEquals(3, store.getAll().size());
    }

    @Test
    void findBySubject_mixedSubjects_isolatedResults() {
        Console.log("IT", "MongoMotionFrameStoreIT.findBySubject_mixedSubjects_isolatedResults");

        String dbName = "motion_it";
        String collection = "motion_frame_mixed_subjects";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1"));
        store.addRecord(newRecord("record-2", "context-1", "ORDER-1001", "ORDER", "frame-2"));
        store.addRecord(newRecord("record-3", "context-2", "ORDER-2001", "ORDER", "frame-3"));

        List<MotionFrameRecord> order1001 = store.findBySubject("ORDER-1001", "ORDER");
        List<MotionFrameRecord> order2001 = store.findBySubject("ORDER-2001", "ORDER");
        List<MotionFrameRecord> missing = store.findBySubject("ORDER-9999", "ORDER");

        assertEquals(2, order1001.size());
        assertEquals(1, order2001.size());
        assertEquals(0, missing.size());
    }

    @Test
    void findBySubject_nullOrBlank_returnsEmpty() {
        Console.log("IT", "MongoMotionFrameStoreIT.findBySubject_nullOrBlank_returnsEmpty");

        String dbName = "motion_it";
        String collection = "motion_frame_subject_null_blank";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1"));

        assertEquals(0, store.findBySubject(null, "ORDER").size());
        assertEquals(0, store.findBySubject("ORDER-1001", null).size());
        assertEquals(0, store.findBySubject("   ", "ORDER").size());
        assertEquals(0, store.findBySubject("ORDER-1001", "   ").size());
    }

    @Test
    void findByContextId_mixedContexts_isolatedResults() {
        Console.log("IT", "MongoMotionFrameStoreIT.findByContextId_mixedContexts_isolatedResults");

        String dbName = "motion_it";
        String collection = "motion_frame_mixed_contexts";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1"));
        store.addRecord(newRecord("record-2", "context-1", "ORDER-1001", "ORDER", "frame-2"));
        store.addRecord(newRecord("record-3", "context-2", "ORDER-2001", "ORDER", "frame-3"));

        List<MotionFrameRecord> context1 = store.findByContextId("context-1");
        List<MotionFrameRecord> context2 = store.findByContextId("context-2");
        List<MotionFrameRecord> missing = store.findByContextId("context-9999");

        assertEquals(2, context1.size());
        assertEquals(1, context2.size());
        assertEquals(0, missing.size());
    }

    @Test
    void findByContextId_nullOrBlank_returnsEmpty() {
        Console.log("IT", "MongoMotionFrameStoreIT.findByContextId_nullOrBlank_returnsEmpty");

        String dbName = "motion_it";
        String collection = "motion_frame_context_null_blank";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1"));

        assertEquals(0, store.findByContextId(null).size());
        assertEquals(0, store.findByContextId("   ").size());
    }

    @Test
    void clear_emptiesCollection() {
        Console.log("IT", "MongoMotionFrameStoreIT.clear_emptiesCollection");

        String dbName = "motion_it";
        String collection = "motion_frame_clear_empties";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1"));
        store.addRecord(newRecord("record-2", "context-1", "ORDER-1001", "ORDER", "frame-2"));

        assertEquals(2, store.getAll().size());

        store.clear();

        assertEquals(0, store.getAll().size());
    }

    @Test
    void ctor_invalidArgs_failFast() {
        Console.log("IT", "MongoMotionFrameStoreIT.ctor_invalidArgs_failFast");

        assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        new MongoMotionFrameStore(null, "db", "collection");
                    }
                }
        );

        assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        new MongoMotionFrameStore(mongoClient, "   ", "collection");
                    }
                }
        );

        assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        new MongoMotionFrameStore(mongoClient, "db", "   ");
                    }
                }
        );
    }

    @Test
    void roundtrip_preservesNestedMotionFrame() {
        Console.log("IT", "MongoMotionFrameStoreIT.roundtrip_preservesNestedMotionFrame");

        String dbName = "motion_it";
        String collection = "motion_frame_preserve_nested_frame";

        MongoMotionFrameStore store = new MongoMotionFrameStore(mongoClient, dbName, collection);
        store.clear();

        MotionFrameRecord record = newRecord("record-1", "context-1", "ORDER-1001", "ORDER", "frame-1");

        store.addRecord(record);

        MotionFrameRecord out = store.findByFrameId("frame-1");

        assertNotNull(out);
        assertNotNull(out.getMotionFrame());
        assertEquals("frame-1", out.getMotionFrame().getFrameId());
        assertEquals("ORDER_OPERATION_FRAME", out.getMotionFrame().getFrameType());
        assertEquals("2026-05-22T10:00:00Z", out.getMotionFrame().getWindowStart());
        assertEquals("2026-05-22T10:05:00Z", out.getMotionFrame().getWindowEnd());
        assertEquals("1", out.getMotionFrame().getSequence());
        assertEquals("OPEN", out.getMotionFrame().getStatus());
        assertNotNull(out.getMotionFrame().getMotionEvents());
        assertEquals(0, out.getMotionFrame().getMotionEvents().size());

        Console.log("record", out.toString());
    }

    private MotionFrameRecord newRecord(
            String recordId,
            String contextId,
            String subjectId,
            String subjectType,
            String frameId) {

        MotionFrame frame = new MotionFrame();
        frame.setFrameId(frameId);
        frame.setFrameType("ORDER_OPERATION_FRAME");
        frame.setWindowStart("2026-05-22T10:00:00Z");
        frame.setWindowEnd("2026-05-22T10:05:00Z");
        frame.setSequence("1");
        frame.setStatus("OPEN");
        frame.setMetadataJson("{\"runtime\":\"motion\"}");

        MotionFrameRecord record = new MotionFrameRecord();
        record.setRecordId(recordId);
        record.setContextId(contextId);
        record.setContextType("ORDER_EVOLVING_CONTEXT");
        record.setSubjectId(subjectId);
        record.setSubjectType(subjectType);
        record.setFrameId(frameId);
        record.setFrameType("ORDER_OPERATION_FRAME");
        record.setWindowStart("2026-05-22T10:00:00Z");
        record.setWindowEnd("2026-05-22T10:05:00Z");
        record.setSequence("1");
        record.setMotionFrame(frame);
        record.setCreatedAt("2026-05-22T10:06:00Z");
        record.setUpdatedAt("2026-05-22T10:07:00Z");

        return record;
    }
}