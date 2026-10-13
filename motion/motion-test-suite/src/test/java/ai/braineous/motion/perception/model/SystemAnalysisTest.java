package ai.braineous.motion.perception.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SystemAnalysisTest {

    @Test
    public void test_1() {
        SystemAnalysis analysis = new SystemAnalysis();

        Assertions.assertNull(analysis.getPerception());
    }

    @Test
    public void test_2() {
        SystemAnalysis analysis = new SystemAnalysis();
        Perception perception = new Perception();

        analysis.setPerception(perception);

        Assertions.assertSame(perception, analysis.getPerception());
    }

    @Test
    public void test_3() {
        SystemAnalysis analysis = new SystemAnalysis();
        Perception perception = new Perception();

        analysis.setPerception(perception);

        String representation = analysis.toString();
        Assertions.assertTrue(representation.contains("SystemAnalysis"));
        Assertions.assertTrue(representation.contains("perception="));
    }

    @Test
    public void test_4() {
        SystemAnalysis analysis = new SystemAnalysis();

        Assertions.assertNull(analysis.getAnalysisId());

        analysis.setAnalysisId("analysis-1");

        Assertions.assertEquals("analysis-1", analysis.getAnalysisId());

        String representation = analysis.toString();
        Assertions.assertTrue(representation.contains("perception="));
        Assertions.assertTrue(representation.contains("analysisId="));
        Assertions.assertTrue(representation.contains("createdAt="));
        Assertions.assertTrue(representation.contains("cognitiveQuery="));
    }

    @Test
    public void test_5() {
        SystemAnalysis analysis = new SystemAnalysis();

        Assertions.assertNull(analysis.getCreatedAt());

        analysis.setCreatedAt("2026-10-12T12:20:00Z");

        Assertions.assertEquals("2026-10-12T12:20:00Z", analysis.getCreatedAt());

        String representation = analysis.toString();
        Assertions.assertTrue(representation.contains("perception="));
        Assertions.assertTrue(representation.contains("analysisId="));
        Assertions.assertTrue(representation.contains("createdAt="));
        Assertions.assertTrue(representation.contains("cognitiveQuery="));
    }

    @Test
    public void test_6() {
        SystemAnalysis analysis = new SystemAnalysis();
        String payV1Sql =
                "select decision, reason, code "
                        + "from llm "
                        + "where factId = 'PaymentRequest:PAY-1001' "
                        + "and relatedFactIds = 'CustomerAccount:CUST-2001,"
                        + "PaymentMethod:PM-3001,"
                        + "RiskProfile:RISK-4001,"
                        + "MerchantPolicy:POL-5001' "
                        + "control intent = 'decide_payment_capture', "
                        + "action = 'determine', "
                        + "subject = 'primary_payment_request', "
                        + "decision = 'allow_capture', "
                        + "basis = 'related_system_facts', "
                        + "goal = 'decision'";

        Assertions.assertNull(analysis.getCognitiveQuery());

        analysis.setCognitiveQuery(payV1Sql);

        Assertions.assertEquals(payV1Sql, analysis.getCognitiveQuery());

        String representation = analysis.toString();
        Assertions.assertTrue(representation.contains("perception="));
        Assertions.assertTrue(representation.contains("analysisId="));
        Assertions.assertTrue(representation.contains("createdAt="));
        Assertions.assertTrue(representation.contains("cognitiveQuery="));
        Assertions.assertTrue(representation.contains(payV1Sql));
    }
}
