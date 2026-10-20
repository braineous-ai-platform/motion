package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.DevxDefinition;
import io.braineous.motion.core.model.MotionBaseModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/devx")
public class DevxResource {

    @Inject
    DevxQueryOrchestrator devxQueryOrchestrator;

    public DevxResource() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response publish(String json) {
        DevxDefinition definition = MotionBaseModel.fromJson(
                json,
                DevxDefinition.class);
        this.devxQueryOrchestrator.publish(definition);
        return Response.ok().build();
    }
}
