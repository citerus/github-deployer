package se.citerus;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogsHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(LogsHandler.class);

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String deploymentId = ctx.pathParam("deploymentId");
        LOG.info("Got logs request for deployment id {}", deploymentId);
        ctx.status(HttpStatus.NOT_FOUND); // TODO implement this method
    }
}
