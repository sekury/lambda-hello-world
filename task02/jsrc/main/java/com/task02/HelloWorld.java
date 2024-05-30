package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import java.util.Map;
import java.util.function.Function;

@LambdaHandler(
    lambdaName = "hello_world",
    roleName = "hello_world-role",
    layers = {"sdk-layer"},
    runtime = DeploymentRuntime.JAVA11,
    architecture = Architecture.ARM64,
    logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
    layerName = "sdk-layer",
    libraries = {"lib/gson-2.11.0.jar"},
    runtime = DeploymentRuntime.JAVA11,
    architectures = {Architecture.ARM64},
    artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
    authType = AuthType.NONE,
    invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, String> {

    private static final String HELLO_MESSAGE = "Hello from Lambda";
    private static final String BAD_REQUEST_MESSAGE =
        "Bad request syntax or unsupported method. Request path: %s. HTTP method: %s";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Route, Function<APIGatewayV2HTTPEvent, String>> routeMap =
        Map.of(new Route("GET", "/hello"), this::handleGetHelloEvent);

    @Override
    public String handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        return routeMap.getOrDefault(new Route(getMethod(event), getPath(event)), this::handleBadRequest).apply(event);
    }

    private String handleGetHelloEvent(APIGatewayV2HTTPEvent event) {
        return buildResponse(200, HELLO_MESSAGE);
    }

    private String handleBadRequest(APIGatewayV2HTTPEvent event) {
        return buildResponse(400, String.format(BAD_REQUEST_MESSAGE, getPath(event), getMethod(event)));
    }

    private String buildResponse(int statusCode, String message) {
        return gson.toJson(new ResponseBody(statusCode, message));
    }

    private String getMethod(APIGatewayV2HTTPEvent event) {
        return event.getRequestContext().getHttp().getMethod();
    }

    private String getPath(APIGatewayV2HTTPEvent event) {
        return event.getRequestContext().getHttp().getPath();
    }
}
