package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.CognitiveQueryArtifact;
import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
import ai.braineous.motion.perception.model.DevxDefinition;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DevxResourceTest {

    @Test
    public void test_1() {
        String json = "{"
                + "\"analysis\":["
                + "{\"name\":\"payment_analysis\",\"queries\":["
                + "{\"name\":\"analyze_payment\",\"cognitiveQuery\":\"select analysis_decision from llm\"},"
                + "{\"name\":\"evaluate_risk\",\"cognitiveQuery\":\"select risk_decision from llm\"}"
                + "]},"
                + "{\"name\":\"payment_summary\",\"queries\":["
                + "{\"name\":\"summarize_analysis\",\"cognitiveQuery\":\"select summary from llm\"}"
                + "]}"
                + "],"
                + "\"mutation\":["
                + "{\"name\":\"payment_mutation\",\"queries\":["
                + "{\"name\":\"select_action\",\"cognitiveQuery\":\"select mutation_decision from llm\"}"
                + "]},"
                + "{\"name\":\"payment_confirmation\",\"queries\":["
                + "{\"name\":\"confirm_action\",\"cognitiveQuery\":\"select confirmation from llm\"},"
                + "{\"name\":\"record_action\",\"cognitiveQuery\":\"select record from llm\"}"
                + "]}"
                + "]"
                + "}";

        RecordingDevxQueryOrchestrator orchestrator = new RecordingDevxQueryOrchestrator();
        DevxResource resource = resource(orchestrator);
        Response response = resource.publish(json);

        Assertions.assertEquals(1, orchestrator.publishCount);
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertNull(response.getEntity());
        Assertions.assertNull(orchestrator.analysisGroupStore);
        Assertions.assertNull(orchestrator.analysisQueryStore);
        Assertions.assertNull(orchestrator.mutationGroupStore);
        Assertions.assertNull(orchestrator.mutationQueryStore);

        DevxDefinition definition = orchestrator.definition;
        List<CognitiveTransactionGroup> analysis = definition.getAnalysis();
        Assertions.assertEquals(2, analysis.size());
        CognitiveTransactionGroup paymentAnalysis = analysis.get(0);
        Assertions.assertEquals("payment_analysis", paymentAnalysis.getName());
        List<CognitiveQueryArtifact> analysisQueries = paymentAnalysis.getQueries();
        Assertions.assertEquals(2, analysisQueries.size());
        Assertions.assertEquals("analyze_payment", analysisQueries.get(0).getName());
        Assertions.assertEquals(
                "select analysis_decision from llm",
                analysisQueries.get(0).getCognitiveQuery());
        Assertions.assertEquals("evaluate_risk", analysisQueries.get(1).getName());
        Assertions.assertEquals(
                "select risk_decision from llm",
                analysisQueries.get(1).getCognitiveQuery());
        CognitiveTransactionGroup paymentSummary = analysis.get(1);
        Assertions.assertEquals("payment_summary", paymentSummary.getName());
        Assertions.assertEquals(1, paymentSummary.getQueries().size());
        Assertions.assertEquals(
                "summarize_analysis",
                paymentSummary.getQueries().get(0).getName());
        Assertions.assertEquals(
                "select summary from llm",
                paymentSummary.getQueries().get(0).getCognitiveQuery());

        List<CognitiveTransactionGroup> mutation = definition.getMutation();
        Assertions.assertEquals(2, mutation.size());
        CognitiveTransactionGroup paymentMutation = mutation.get(0);
        Assertions.assertEquals("payment_mutation", paymentMutation.getName());
        Assertions.assertEquals(1, paymentMutation.getQueries().size());
        Assertions.assertEquals(
                "select_action",
                paymentMutation.getQueries().get(0).getName());
        Assertions.assertEquals(
                "select mutation_decision from llm",
                paymentMutation.getQueries().get(0).getCognitiveQuery());
        CognitiveTransactionGroup paymentConfirmation = mutation.get(1);
        Assertions.assertEquals("payment_confirmation", paymentConfirmation.getName());
        List<CognitiveQueryArtifact> mutationQueries = paymentConfirmation.getQueries();
        Assertions.assertEquals(2, mutationQueries.size());
        Assertions.assertEquals("confirm_action", mutationQueries.get(0).getName());
        Assertions.assertEquals(
                "select confirmation from llm",
                mutationQueries.get(0).getCognitiveQuery());
        Assertions.assertEquals("record_action", mutationQueries.get(1).getName());
        Assertions.assertEquals(
                "select record from llm",
                mutationQueries.get(1).getCognitiveQuery());
    }

    @Test
    public void test_2() {
        String json = "{"
                + "\"analysis\":["
                + "{\"name\":\"payment_analysis\",\"queries\":["
                + "{\"name\":\"analyze_payment\",\"cognitiveQuery\":\"select analysis_decision from llm\"},"
                + "{\"name\":\"evaluate_risk\",\"cognitiveQuery\":\"select risk_decision from llm\"}"
                + "]}"
                + "]"
                + "}";

        RecordingDevxQueryOrchestrator orchestrator = new RecordingDevxQueryOrchestrator();
        Response response = resource(orchestrator).publish(json);

        Assertions.assertEquals(1, orchestrator.publishCount);
        Assertions.assertEquals(200, response.getStatus());
        DevxDefinition definition = orchestrator.definition;
        Assertions.assertNull(definition.getMutation());
        Assertions.assertEquals(1, definition.getAnalysis().size());
        CognitiveTransactionGroup paymentAnalysis = definition.getAnalysis().get(0);
        Assertions.assertEquals("payment_analysis", paymentAnalysis.getName());
        Assertions.assertEquals(2, paymentAnalysis.getQueries().size());
        Assertions.assertEquals(
                "analyze_payment",
                paymentAnalysis.getQueries().get(0).getName());
        Assertions.assertEquals(
                "select analysis_decision from llm",
                paymentAnalysis.getQueries().get(0).getCognitiveQuery());
        Assertions.assertEquals(
                "evaluate_risk",
                paymentAnalysis.getQueries().get(1).getName());
        Assertions.assertEquals(
                "select risk_decision from llm",
                paymentAnalysis.getQueries().get(1).getCognitiveQuery());
        Assertions.assertNull(orchestrator.analysisGroupStore);
        Assertions.assertNull(orchestrator.mutationQueryStore);
    }

    @Test
    public void test_3() {
        String json = "{"
                + "\"mutation\":["
                + "{\"name\":\"payment_mutation\",\"queries\":["
                + "{\"name\":\"select_action\",\"cognitiveQuery\":\"select mutation_decision from llm\"}"
                + "]},"
                + "{\"name\":\"payment_confirmation\",\"queries\":["
                + "{\"name\":\"record_action\",\"cognitiveQuery\":\"select record from llm\"}"
                + "]}"
                + "]"
                + "}";

        RecordingDevxQueryOrchestrator orchestrator = new RecordingDevxQueryOrchestrator();
        Response response = resource(orchestrator).publish(json);

        Assertions.assertEquals(1, orchestrator.publishCount);
        Assertions.assertEquals(200, response.getStatus());
        DevxDefinition definition = orchestrator.definition;
        Assertions.assertNull(definition.getAnalysis());
        Assertions.assertEquals(2, definition.getMutation().size());
        CognitiveTransactionGroup paymentMutation = definition.getMutation().get(0);
        Assertions.assertEquals("payment_mutation", paymentMutation.getName());
        Assertions.assertEquals(1, paymentMutation.getQueries().size());
        Assertions.assertEquals(
                "select_action",
                paymentMutation.getQueries().get(0).getName());
        Assertions.assertEquals(
                "select mutation_decision from llm",
                paymentMutation.getQueries().get(0).getCognitiveQuery());
        CognitiveTransactionGroup paymentConfirmation = definition.getMutation().get(1);
        Assertions.assertEquals("payment_confirmation", paymentConfirmation.getName());
        Assertions.assertEquals(
                "record_action",
                paymentConfirmation.getQueries().get(0).getName());
        Assertions.assertEquals(
                "select record from llm",
                paymentConfirmation.getQueries().get(0).getCognitiveQuery());
        Assertions.assertNull(orchestrator.mutationGroupStore);
        Assertions.assertNull(orchestrator.analysisQueryStore);
    }

    private DevxResource resource(RecordingDevxQueryOrchestrator orchestrator) {
        DevxResource resource = new DevxResource();
        resource.devxQueryOrchestrator = orchestrator;
        return resource;
    }

    static class RecordingDevxQueryOrchestrator extends DevxQueryOrchestrator {

        int publishCount;
        DevxDefinition definition;

        @Override
        public void publish(DevxDefinition definition) {
            this.publishCount = this.publishCount + 1;
            this.definition = definition;
        }
    }
}
