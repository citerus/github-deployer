package se.citerus.model;

import java.util.List;
import java.util.Map;

public record DeploymentRequest(String action, Deployment deployment, Map<String, ?> enterprise, Map<String, ?> installation,
                                Map<String, ?> organization, Map<String, ?> repository, Map<String, ?> sender, Workflow workflow,
                                WorkflowRun workflow_run) {
}

