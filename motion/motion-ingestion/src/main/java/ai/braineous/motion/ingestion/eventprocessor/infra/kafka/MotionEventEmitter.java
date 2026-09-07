package ai.braineous.motion.ingestion.eventprocessor.infra.kafka;

import io.braineous.motion.core.model.MotionEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class MotionEventEmitter {

    @Inject
    @Channel("motion_event")
    Emitter<String> emitter;

    public void emit(MotionEvent motionEvent) {
        String motionEventJson = motionEvent.toJson();
        CompletionStage<Void> sendCompletion = emitter.send(motionEventJson);

        try {
            sendCompletion.toCompletableFuture().join();
        } catch (CompletionException completionException) {
            Throwable cause = completionException.getCause();

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }

            if (cause instanceof Error) {
                throw (Error) cause;
            }

            throw new RuntimeException(cause);
        }
    }
}
