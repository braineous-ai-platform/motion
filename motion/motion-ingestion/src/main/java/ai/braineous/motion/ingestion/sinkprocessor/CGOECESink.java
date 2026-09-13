package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.cgo.api.LLMBridge;
import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.cgo.api.RelationshipProvider;
import ai.braineous.rag.prompt.services.cgo.causal.CausalLLMBridge;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.braineous.motion.core.model.MotionFrame;

import java.util.ArrayList;
import java.util.List;

public class CGOECESink {

    private LLMBridge llmBridge = new CausalLLMBridge();

    public GraphView write(MotionFrame motionFrame) {

        try {
            if (motionFrame == null) {
                return null;
            }

            String payload =
                    "[" + motionFrame.toJson() + "]";

            FactExtractor factExtractor =
                    new FactExtractor() {

                        @Override
                        public List<Fact> extract(String json) {

                            JsonArray jsonArray =
                                    JsonParser.parseString(json)
                                            .getAsJsonArray();

                            if (jsonArray.isEmpty()) {
                                return new ArrayList<Fact>();
                            }

                            String motionFrameJson =
                                    jsonArray.get(0).toString();

                            MotionFactExtractor extractor =
                                    new MotionFactExtractor();

                            return extractor.extract(
                                    motionFrameJson
                            );
                        }
                    };

            RelationshipProvider relationshipProvider =
                    new MotionRelationshipProvider();

            LLMContext context = new LLMContext();

            context.build(
                    "motion_frame",
                    payload,
                    factExtractor,
                    relationshipProvider,
                    null
            );

            return this.llmBridge.submit(context);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
