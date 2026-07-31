package com.mycomp.csmessage.config.secrets;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.config.SdkConfig;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.util.InfisicalException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

@Slf4j
public class InfisicalSecretsEnvironmentPostProcessor implements EnvironmentPostProcessor {

  private static final String CLIENT_ID_ENV = "INFISICAL_CLIENT_ID";
  private static final String CLIENT_SECRET_ENV = "INFISICAL_CLIENT_SECRET";
  private static final String PROJECT_ID_ENV = "INFISICAL_PROJECT_ID";
  private static final String ENV_SLUG_ENV = "INFISICAL_ENVIRONMENT";

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    String clientId = environment.getProperty(CLIENT_ID_ENV);
    String clientSecret = environment.getProperty(CLIENT_SECRET_ENV);
    String projectId = environment.getProperty(PROJECT_ID_ENV);

    if (clientId == null || clientSecret == null || projectId == null) {
      if (isProduction(environment)) {
        throw new IllegalStateException(
            "Infisical bootstrap credentials missing but production profile is active — refusing"
                + " to start with local secrets.");
      }
      log.info(
          "Infisical bootstrap credentials not set ({} / {} / {}). Falling back to local env vars.",
          CLIENT_ID_ENV,
          CLIENT_SECRET_ENV,
          PROJECT_ID_ENV);
      return;
    }

    String envSlug = environment.getProperty(ENV_SLUG_ENV, "dev");

    Map<String, Object> secrets;
    try {
      secrets = fetchSecrets(clientId, clientSecret, projectId, envSlug);
    } catch (InfisicalException | RuntimeException e) {
      if (isProduction(environment)) {
        throw new IllegalStateException(
            "Infisical unreachable and production profile is active — refusing to start with local"
                + " secrets.",
            e);
      }
      log.warn("Infisical unavailable ({}). Falling back to local env vars.", e.getMessage());
      return;
    }

    environment.getPropertySources().addFirst(new MapPropertySource("infisical", secrets));
    log.info("Loaded {} secrets from Infisical (env={}).", secrets.size(), envSlug);
  }

  private Map<String, Object> fetchSecrets(
      String clientId, String clientSecret, String projectId, String envSlug)
      throws InfisicalException {
    InfisicalSdk sdk = new InfisicalSdk(new SdkConfig.Builder().build());
    sdk.Auth().UniversalAuthLogin(clientId, clientSecret);

    List<Secret> fetched =
        sdk.Secrets().ListSecrets(projectId, envSlug, "/", false, false, false, false);

    Map<String, Object> secrets = new LinkedHashMap<>();
    for (Secret secret : fetched) {
      secrets.put(secret.getSecretKey(), secret.getSecretValue());
    }
    return secrets;
  }

  private boolean isProduction(ConfigurableEnvironment environment) {
    for (String profile : environment.getActiveProfiles()) {
      if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
        return true;
      }
    }
    String prop = environment.getProperty("spring.profiles.active");
    return prop != null && (prop.contains("prod") || prop.contains("production"));
  }
}
