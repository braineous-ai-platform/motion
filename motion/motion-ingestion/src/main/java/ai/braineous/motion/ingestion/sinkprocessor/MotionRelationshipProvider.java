package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.cgo.api.RelationshipProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MotionRelationshipProvider implements RelationshipProvider {

    @Override
    public List<Relationship> provideRelationships(List<Fact> facts) {

        List<Relationship> relationships = new ArrayList<Relationship>();

        if (facts == null || facts.isEmpty()) {
            return relationships;
        }

        Fact motionFrame = null;
        List<Fact> motionEvents = new ArrayList<Fact>();

        for (Fact fact : facts) {

            if (fact == null) {
                continue;
            }

            if (fact.getId() == null || fact.getText() == null) {
                continue;
            }

            JsonObject factJson;

            try {
                factJson = JsonParser.parseString(
                        fact.getText()
                ).getAsJsonObject();
            } catch (Exception e) {
                continue;
            }

            if (!factJson.has("kind")) {
                continue;
            }

            String kind = factJson.get("kind").getAsString();

            if ("MotionFrame".equals(kind)
                    && fact.getId().startsWith("MotionFrame:")) {

                motionFrame = fact;
                continue;
            }

            if ("MotionEvent".equals(kind)
                    && fact.getId().startsWith("MotionEvent:")) {

                motionEvents.add(fact);
            }
        }

        if (motionFrame == null) {
            return relationships;
        }

        if (motionEvents.isEmpty()) {
            return relationships;
        }

        Set<String> seen = new HashSet<String>();

        for (Fact motionEvent : motionEvents) {

            String fromId = motionFrame.getId();
            String toId = motionEvent.getId();

            if (fromId.equals(toId)) {
                continue;
            }

            String key = fromId + "->" + toId;

            if (!seen.add(key)) {
                continue;
            }

            Edge edge = new Edge();

            edge.setId("Edge:" + key);
            edge.setFromFactId(fromId);
            edge.setToFactId(toId);
            edge.setMode("relational");

            Fact fromRef = new Fact(
                    fromId,
                    motionFrame.getText()
            );

            Fact toRef = new Fact(
                    toId,
                    motionEvent.getText()
            );

            relationships.add(
                    new Relationship(
                            fromRef,
                            toRef,
                            edge
                    )
            );
        }

        return relationships;
    }
}
