/*
 * Copyright (c) 2023 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.server.apis;

import static io.airbyte.commons.auth.AuthRoleConstants.AUTHENTICATED_USER;

import io.airbyte.api.generated.OpenapiApi;
import io.airbyte.commons.server.handlers.OpenApiConfigHandler;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.io.File;

@SuppressWarnings("MissingJavadocType")
@Controller("/api/v1/openapi")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class OpenapiApiController implements OpenapiApi {

  private final OpenApiConfigHandler openApiConfigHandler;

  public OpenapiApiController(final OpenApiConfigHandler openApiConfigHandler) {
    this.openApiConfigHandler = openApiConfigHandler;
  }

  @Get(produces = "text/plain")
  @Secured({AUTHENTICATED_USER})
  @ExecuteOn(TaskExecutors.IO)
  @Override
  public File getOpenApiSpec() {
    return ApiHelper.execute(openApiConfigHandler::getFile);
  }

}
