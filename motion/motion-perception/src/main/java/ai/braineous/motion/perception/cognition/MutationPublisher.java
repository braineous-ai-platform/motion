package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.SystemMutation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class MutationPublisher {

    @Inject
    @Channel("mutation_out")
    Emitter<String> emitter;

    public MutationPublisher() {
    }

    public void publish(SystemMutation systemMutation) {
        String payload = systemMutation.toJson();
        this.emitter.send(payload);
    }
}
