package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionTimeWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MotionFactExtractor implements FactExtractor {

    private final Gson gson = new Gson();

    @Override
    public List<Fact> extract(String json) {
        List<Fact> facts = new ArrayList<Fact>();

        if (json == null || json.trim().isEmpty()) {
            return facts;
        }

        MotionFrame motionFrame;

        try {
            motionFrame = this.gson.fromJson(json, MotionFrame.class);
        } catch (Exception e) {
            return facts;
        }

        if (motionFrame == null) {
            return facts;
        }

        this.addMotionFrameFact(facts, motionFrame);
        this.addMotionEventFacts(facts, motionFrame);

        return facts;
    }

    private void addMotionFrameFact(List<Fact> facts,
                                    MotionFrame motionFrame) {

        String frameId = motionFrame.getFrameId();

        if (frameId == null || frameId.trim().isEmpty()) {
            return;
        }

        String factId = "MotionFrame:" + frameId;

        JsonObject factJson = new JsonObject();

        factJson.addProperty("id", factId);
        factJson.addProperty("kind", "MotionFrame");
        factJson.addProperty("mode", "atomic");
        factJson.addProperty("frameId", motionFrame.getFrameId());
        factJson.addProperty("frameType", motionFrame.getFrameType());

        MotionTimeWindow timeWindow = motionFrame.getTimeWindow();

        if (timeWindow != null) {
            JsonObject timeWindowJson = new JsonObject();

            timeWindowJson.addProperty(
                    "windowStart",
                    timeWindow.getWindowStart()
            );

            timeWindowJson.addProperty(
                    "windowEnd",
                    timeWindow.getWindowEnd()
            );

            factJson.add("timeWindow", timeWindowJson);
        } else {
            factJson.add("timeWindow", null);
        }

        factJson.addProperty("sequence", motionFrame.getSequence());
        factJson.addProperty("status", motionFrame.getStatus());
        factJson.addProperty("metadataJson", motionFrame.getMetadataJson());

        Fact fact = new Fact(factId, factJson.toString());
        fact.setMode("atomic");

        facts.add(fact);
    }

    private void addMotionEventFacts(List<Fact> facts,
                                     MotionFrame motionFrame) {

        List<MotionEvent> motionEvents = motionFrame.getMotionEvents();

        if (motionEvents == null) {
            return;
        }

        for (MotionEvent motionEvent : motionEvents) {

            if (motionEvent == null) {
                continue;
            }

            String eventId = motionEvent.getEventId();

            if (eventId == null || eventId.trim().isEmpty()) {
                continue;
            }

            String factId = "MotionEvent:" + eventId;

            JsonObject factJson = new JsonObject();
            factJson.addProperty("id", factId);
            factJson.addProperty("kind", "MotionEvent");
            factJson.addProperty("mode", "atomic");

            JsonObject motionEventJson =
                    JsonParser.parseString(motionEvent.toJson()).getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : motionEventJson.entrySet()) {
                factJson.add(entry.getKey(), entry.getValue());
            }

            Fact fact = new Fact(
                    factId,
                    factJson.toString()
            );

            fact.setMode("atomic");

            facts.add(fact);
        }
    }
}
