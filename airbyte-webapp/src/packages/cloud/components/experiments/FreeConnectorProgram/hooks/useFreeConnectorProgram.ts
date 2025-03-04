import { useState } from "react";
import { useIntl } from "react-intl";
import { useQuery } from "react-query";
import { useSearchParams } from "react-router-dom";
import { useEffectOnce } from "react-use";

import { ToastType } from "components/ui/Toast";

import { MissingConfigError, useConfig } from "config";
import { pollUntil } from "core/request/pollUntil";
import { useAppMonitoringService } from "hooks/services/AppMonitoringService";
import { useExperiment } from "hooks/services/Experiment";
import { useNotificationService } from "hooks/services/Notification";
import { useDefaultRequestMiddlewares } from "services/useDefaultRequestMiddlewares";
import { useCurrentWorkspaceId } from "services/workspaces/WorkspacesService";

import { webBackendGetFreeConnectorProgramInfoForWorkspace } from "../lib/api";

export const STRIPE_SUCCESS_QUERY = "fcpEnrollmentSuccess";

export const useFreeConnectorProgram = () => {
  const workspaceId = useCurrentWorkspaceId();
  const { cloudApiUrl } = useConfig();
  if (!cloudApiUrl) {
    throw new MissingConfigError("Missing required configuration cloudApiUrl");
  }
  const config = { apiUrl: cloudApiUrl };
  const middlewares = useDefaultRequestMiddlewares();
  const requestOptions = { config, middlewares };
  const freeConnectorProgramEnabled = useExperiment("workspace.freeConnectorsProgram.visible", false);
  const [searchParams, setSearchParams] = useSearchParams();
  const [userDidEnroll, setUserDidEnroll] = useState(false);
  const { formatMessage } = useIntl();
  const { registerNotification } = useNotificationService();
  const { trackError } = useAppMonitoringService();

  const removeStripeSuccessQuery = () => {
    const { [STRIPE_SUCCESS_QUERY]: _, ...unrelatedSearchParams } = Object.fromEntries(searchParams);
    setSearchParams(unrelatedSearchParams, { replace: true });
  };

  useEffectOnce(() => {
    if (searchParams.has(STRIPE_SUCCESS_QUERY)) {
      pollUntil(
        () => webBackendGetFreeConnectorProgramInfoForWorkspace({ workspaceId }, requestOptions),
        ({ hasPaymentAccountSaved }) => hasPaymentAccountSaved,
        { intervalMs: 1000, maxTimeoutMs: 10000 }
      ).then((maybeFcpInfo) => {
        if (maybeFcpInfo) {
          removeStripeSuccessQuery();
          setUserDidEnroll(true);
          registerNotification({
            id: "fcp/enrollment-success",
            text: formatMessage({ id: "freeConnectorProgram.enroll.success" }),
            type: ToastType.SUCCESS,
          });
        } else {
          trackError(new Error("Unable to confirm Free Connector Program enrollment before timeout"), { workspaceId });
          registerNotification({
            id: "fcp/enrollment-failure",
            text: formatMessage({ id: "freeConnectorProgram.enroll.failure" }),
            type: ToastType.ERROR,
          });
        }
      });
    }
  });

  const enrollmentStatusQuery = useQuery(["freeConnectorProgramInfo", workspaceId], () =>
    webBackendGetFreeConnectorProgramInfoForWorkspace({ workspaceId }, requestOptions).then(
      ({ hasEligibleConnector, hasPaymentAccountSaved }) => {
        const userIsEligibleToEnroll = !hasPaymentAccountSaved && hasEligibleConnector;

        return {
          showEnrollmentUi: freeConnectorProgramEnabled && userIsEligibleToEnroll,
          isEnrolled: freeConnectorProgramEnabled && hasPaymentAccountSaved,
        };
      }
    )
  );

  return {
    enrollmentStatusQuery,
    userDidEnroll,
  };
};
