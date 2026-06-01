package ai.braineous.motion.ingestion.timeprocessor.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MotionFrameRoutingKeyTest {

    @Test
    public void test_1() {

        MotionFrameRoutingKey routingKey = new MotionFrameRoutingKey();

        routingKey.setRoutingKey("customer-1001");
        routingKey.setSubjectId("customer-1001");
        routingKey.setSubjectType("CUSTOMER");
        routingKey.setFrameType("CUSTOMER_ACTIVITY");

        Assertions.assertEquals("customer-1001", routingKey.getRoutingKey());
        Assertions.assertEquals("customer-1001", routingKey.getSubjectId());
        Assertions.assertEquals("CUSTOMER", routingKey.getSubjectType());
        Assertions.assertEquals("CUSTOMER_ACTIVITY", routingKey.getFrameType());

        Console.log("routingKey", routingKey);

        Assertions.assertNotNull(routingKey.toString());
        Assertions.assertTrue(routingKey.toString().contains("customer-1001"));
        Assertions.assertTrue(routingKey.toString().contains("CUSTOMER"));
        Assertions.assertTrue(routingKey.toString().contains("CUSTOMER_ACTIVITY"));
    }
}