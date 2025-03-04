/*
 * Copyright (c) 2023 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.server.apis;

import static io.airbyte.commons.auth.AuthRoleConstants.ADMIN;
import static io.airbyte.commons.auth.AuthRoleConstants.EDITOR;
import static io.airbyte.commons.auth.AuthRoleConstants.READER;

import io.airbyte.api.generated.JobsApi;
import io.airbyte.api.model.generated.AttemptNormalizationStatusReadList;
import io.airbyte.api.model.generated.ConnectionIdRequestBody;
import io.airbyte.api.model.generated.JobDebugInfoRead;
import io.airbyte.api.model.generated.JobIdRequestBody;
import io.airbyte.api.model.generated.JobInfoLightRead;
import io.airbyte.api.model.generated.JobInfoRead;
import io.airbyte.api.model.generated.JobListRequestBody;
import io.airbyte.api.model.generated.JobOptionalRead;
import io.airbyte.api.model.generated.JobReadList;
import io.airbyte.commons.auth.SecuredWorkspace;
import io.airbyte.commons.server.handlers.JobHistoryHandler;
import io.airbyte.commons.server.handlers.SchedulerHandler;
import io.micronaut.context.annotation.Context;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@SuppressWarnings("MissingJavadocType")
@Controller("/api/v1/jobs")
@Context
@Secured(SecurityRule.IS_AUTHENTICATED)
public class JobsApiController implements JobsApi {

  private final JobHistoryHandler jobHistoryHandler;
  private final SchedulerHandler schedulerHandler;

  public JobsApiController(final JobHistoryHandler jobHistoryHandler, final SchedulerHandler schedulerHandler) {
    this.jobHistoryHandler = jobHistoryHandler;
    this.schedulerHandler = schedulerHandler;
  }

  @Post("/cancel")
  @Secured({EDITOR})
  @SecuredWorkspace
  @ExecuteOn(TaskExecutors.IO)
  @Override
  public JobInfoRead cancelJob(final JobIdRequestBody jobIdRequestBody) {
    return ApiHelper.execute(() -> schedulerHandler.cancelJob(jobIdRequestBody));
  }

  @Post("/get_normalization_status")
  @Secured({ADMIN})
  @ExecuteOn(TaskExecutors.IO)
  @Override
  public AttemptNormalizationStatusReadList getAttemptNormalizationStatusesForJob(final JobIdRequestBody jobIdRequestBody) {
    return ApiHelper.execute(() -> jobHistoryHandler.getAttemptNormalizationStatuses(jobIdRequestBody));
  }

  @Post("/get_debug_info")
  @Secured({READER})
  @SecuredWorkspace
  @ExecuteOn(TaskExecutors.IO)
  @Override
  public JobDebugInfoRead getJobDebugInfo(final JobIdRequestBody jobIdRequestBody) {
    return ApiHelper.execute(() -> jobHistoryHandler.getJobDebugInfo(jobIdRequestBody));
  }

  @Post("/get")
  @Secured({READER})
  @SecuredWorkspace
  @ExecuteOn(TaskExecutors.IO)
  @Override
  public JobInfoRead getJobInfo(final JobIdRequestBody jobIdRequestBody) {
    return ApiHelper.execute(() -> jobHistoryHandler.getJobInfo(jobIdRequestBody));
  }

  @Post("/get_light")
  @Secured({READER})
  @SecuredWorkspace
  @ExecuteOn(TaskExecutors.IO)
  @Override
  public JobInfoLightRead getJobInfoLight(final JobIdRequestBody jobIdRequestBody) {
    return ApiHelper.execute(() -> jobHistoryHandler.getJobInfoLight(jobIdRequestBody));
  }

  @Post("/get_last_replication_job")
  @Secured({READER})
  @ExecuteOn(TaskExecutors.IO)
  @Override
  public JobOptionalRead getLastReplicationJob(final ConnectionIdRequestBody connectionIdRequestBody) {
    return ApiHelper.execute(() -> jobHistoryHandler.getLastReplicationJob(connectionIdRequestBody));
  }

  @Post("/list")
  @Secured({READER})
  @SecuredWorkspace
  @ExecuteOn(TaskExecutors.IO)
  @Override
  public JobReadList listJobsFor(final JobListRequestBody jobListRequestBody) {
    return ApiHelper.execute(() -> jobHistoryHandler.listJobsFor(jobListRequestBody));
  }

}
