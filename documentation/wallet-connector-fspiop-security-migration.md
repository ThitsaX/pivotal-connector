# Wallet Connector FSPIOP Security Configuration

This guide covers the wallet changes for using JWS, OAuth, and mutual TLS. These features can be enabled independently.

| `FSPIOP_USE_JWS` | `KEY_PROVIDER` | Behavior |
| --- | --- | --- |
| `false` | Not needed | Callbacks are sent unsigned. |
| `true` | `vault-kv` | The private key is loaded from Vault. |
| `true` | `pkcs11` | The private key stays in AWS CloudHSM. |

`KEY_PROVIDER` is case-sensitive. Use exactly `vault-kv` or `pkcs11`.

## 1. JWS disabled

```yaml
env:
  FSPIOP_USE_JWS: "false"
```

`FSPIOP_USE_JWS` also defaults to `false` when it is not configured. No other JWS, Vault, or CloudHSM variables are needed.

The application starts normally and shows:

```text
FSPIOP JWS signing is disabled; outbound callbacks will be sent unsigned.
```

## 2. JWS enabled with Vault KV

Use this option when Vault stores the RSA private key.

```yaml
env:
  CONNECTOR_ID: payeefsp
  FSPIOP_USE_JWS: "true"
  KEY_PROVIDER: vault-kv
  VAULT_URL: https://vault.example.com
  VAULT_ROLE: pivotal-connector-payeefsp
  VAULT_KUBERNETES_AUTH_PATH: kubernetes
  VAULT_KV_MOUNT: secret
  VAULT_JWS_KEY_PATH_PREFIX: pivotal/jwskey # gitleaks:allow
```

Required variables:

- `CONNECTOR_ID`
- `FSPIOP_USE_JWS=true`
- `KEY_PROVIDER=vault-kv`
- `VAULT_URL`
- `VAULT_ROLE`

Optional variables with defaults:

- `VAULT_KUBERNETES_AUTH_PATH`: default `kubernetes`
- `VAULT_KV_MOUNT`: default `secret`
- `VAULT_JWS_KEY_PATH_PREFIX`: default `pivotal/jwskey`
- `VAULT_SERVICE_ACCOUNT_TOKEN_PATH`: default `/var/run/secrets/kubernetes.io/serviceaccount/token`

The connector reads the key from:

```text
<VAULT_JWS_KEY_PATH_PREFIX>/<CONNECTOR_ID>
```

The Vault secret must contain:

```json
{
  "privateKey": "<PKCS8 PEM RSA private key>"
}
```

Successful startup shows:

```text
Loaded the JWS signing key for 'payeefsp' from Vault path 'pivotal/jwskey/payeefsp'
```

If the secret is missing, `privateKey` is blank, or the key is invalid, startup fails. The connector does not send unsigned callbacks.

## 3. JWS enabled with AWS CloudHSM

Use this option when the private key must remain inside CloudHSM.

```yaml
env:
  CONNECTOR_ID: payeefsp
  FSPIOP_USE_JWS: "true"
  KEY_PROVIDER: pkcs11
  VAULT_URL: https://vault.example.com
  VAULT_ROLE: pivotal-connector-payeefsp
  VAULT_KUBERNETES_AUTH_PATH: kubernetes
  VAULT_KV_MOUNT: secret
  HSM_CRED_PATH: pivotal/hsmcred/payeefsp # gitleaks:allow
  KEY_REF_PATH_PREFIX: pivotal/keyref # gitleaks:allow
  CLOUDHSM_IP: <CloudHSM cluster IP>
  # Use true only when this environment intentionally has a single HSM.
  CLOUDHSM_DISABLE_KEY_AVAILABILITY_CHECK: "true"
```

Required variables:

- `CONNECTOR_ID`
- `FSPIOP_USE_JWS=true`
- `KEY_PROVIDER=pkcs11`
- `VAULT_URL`
- `VAULT_ROLE`
- `HSM_CRED_PATH`
- `KEY_REF_PATH_PREFIX`
- `CLOUDHSM_IP`

Optional CloudHSM variable:

- `CLOUDHSM_DISABLE_KEY_AVAILABILITY_CHECK`: default `false`. Set it to `true` only for an intentional single-HSM environment. For a normal multi-HSM cluster, leave it unset or set it to `"false"`.

The Vault secret at `HSM_CRED_PATH` must contain:

```json
{
  "username": "<CloudHSM crypto-user name>",
  "password": "<CloudHSM crypto-user password>"
}
```

The connector reads the key reference from:

```text
<KEY_REF_PATH_PREFIX>/<CONNECTOR_ID>
```

That Vault secret must contain:

```json
{
  "keyRef": "<CloudHSM RSA private-key label>"
}
```

Successful startup shows:

```text
CloudHSM client configured for <CloudHSM cluster IP>.
Signing for 'payeefsp' through CloudHSM, key reference '<key label>'
```

If the credentials or key reference are missing or blank, or the HSM key cannot be opened, startup fails. The connector does not send unsigned callbacks.

## 4. Required wallet code changes

CloudHSM support requires changes in both `Dockerfile` and `docker-entrypoint.sh`.

### Dockerfile

The CloudHSM JCE SDK uses native glibc libraries. Use the Ubuntu/Jammy Java image instead of Alpine:

```dockerfile
FROM eclipse-temurin:21-jdk-jammy
```

The Dockerfile must accept `PKCS11_BACKEND` and install the AWS CloudHSM JCE SDK when it is set to `cloudhsm`:

```dockerfile
ARG PKCS11_BACKEND=none
ARG CLOUDHSM_SDK_URL=https://s3.amazonaws.com/cloudhsmv2-software/CloudHsmClient/Jammy/cloudhsm-jce_latest_u22.04_amd64.deb

RUN set -eu; \
    if [ "$PKCS11_BACKEND" = "cloudhsm" ]; then \
      apt-get update \
   && apt-get install -y --no-install-recommends wget ca-certificates \
   && wget -q -O /tmp/cloudhsm-jce.deb "$CLOUDHSM_SDK_URL" \
   && apt-get install -y --no-install-recommends /tmp/cloudhsm-jce.deb \
   && rm -f /tmp/cloudhsm-jce.deb \
   && apt-get purge -y wget && apt-get autoremove -y \
   && rm -rf /var/lib/apt/lists/*; \
    fi
```

This block works as follows:

- `ARG PKCS11_BACKEND=none` makes CloudHSM support optional. With the default `none`, the installation block is skipped.
- `ARG CLOUDHSM_SDK_URL=...` identifies the AWS CloudHSM JCE package for Ubuntu 22.04 on AMD64.
- `set -eu` stops the image build if a command fails or an unset shell variable is used.
- The `if` block runs only when the image is built with `PKCS11_BACKEND=cloudhsm`.
- `wget` and `ca-certificates` download the vendor package over HTTPS.
- Installing `cloudhsm-jce.deb` adds the CloudHSM client utilities, native libraries, and JCE provider JARs under `/opt/cloudhsm`.
- The remaining commands remove the downloaded package, temporary downloader, and APT cache to reduce the final image size.

These are Docker build arguments, not container runtime environment variables. They decide what software is included in the image.

Build the CloudHSM image with:

```bash
docker build --build-arg PKCS11_BACKEND=cloudhsm .
```

The image build workflow must also pass:

```yaml
build-args: |
  PKCS11_BACKEND=cloudhsm
```

For Vault KV only, the CloudHSM SDK is not required and `PKCS11_BACKEND` can remain `none`.

Use matching build-time and runtime configurations:

| Signing mode | Image build | Container runtime |
| --- | --- | --- |
| Vault KV | `PKCS11_BACKEND=none` | Do not set `CLOUDHSM_IP`. |
| CloudHSM | `PKCS11_BACKEND=cloudhsm` | Set `CLOUDHSM_IP` and the `pkcs11` provider variables. |

If the image is built with `PKCS11_BACKEND=none` but `CLOUDHSM_IP` is set at runtime, startup fails because `/opt/cloudhsm/bin/configure-jce` is not installed.

### Docker entrypoint

The entrypoint uses the normal application launch when CloudHSM is not configured:

```sh
LAUNCH="-jar app.jar"
```

When `CLOUDHSM_IP` is configured, the wallet branch uses this complete block:

```sh
if [ -n "${CLOUDHSM_IP:-}" ]; then

    if [ "${CLOUDHSM_DISABLE_KEY_AVAILABILITY_CHECK:-false}" = "true" ]; then
        /opt/cloudhsm/bin/configure-jce -a "$CLOUDHSM_IP" --disable-key-availability-check
        echo "WARNING: key availability check disabled -- keys may exist on a single HSM." >&2
    else
        /opt/cloudhsm/bin/configure-jce -a "$CLOUDHSM_IP"
    fi

    LAUNCH="-cp /opt/app/app.jar:/opt/cloudhsm/java/* org.springframework.boot.loader.launch.JarLauncher"

    echo "CloudHSM client configured for $CLOUDHSM_IP."
fi
```

This block works as follows:

- The outer `if` runs only when `CLOUDHSM_IP` is present. Without it, the connector keeps the normal `java -jar` launch.
- `configure-jce -a "$CLOUDHSM_IP"` writes the cluster address into the AWS CloudHSM JCE client configuration.
- `CLOUDHSM_DISABLE_KEY_AVAILABILITY_CHECK` defaults to `false`, so the normal key-availability check remains enabled.
- Setting it to `true` adds `--disable-key-availability-check` and prints a warning. Use this only for a temporary single-HSM non-production cluster.
- `LAUNCH` changes because the CloudHSM provider JAR is outside `app.jar`. The classpath includes `/opt/cloudhsm/java/*`, and Spring Boot starts through `JarLauncher`.
- The final message confirms that the CloudHSM client was configured; it does not confirm that Vault login or HSM key loading succeeded. The later `Signing for ... through CloudHSM` log confirms the signing key was opened.

The entrypoint must forward the provider settings to Java:

```sh
"-DkeyProvider=${KEY_PROVIDER:-vault-kv}" \
"-DhsmCredPath=${HSM_CRED_PATH:-}" \
"-DkeyRefPathPrefix=${KEY_REF_PATH_PREFIX:-pivotal/keyref}" # gitleaks:allow
```

The reviewed entrypoint uses `KEY_REF_PATH` in the final mapping. Replace it with `KEY_REF_PATH_PREFIX`.

## 5. OAuth changes

OAuth adds a bearer token to outbound callbacks sent through the Hub API gateway. It is enabled when all three OAuth values are configured.

### Dockerfile and entrypoint

The Dockerfile can provide empty defaults for the non-secret values:

```dockerfile
ENV FSPIOP_OAUTH_TOKEN_URL=""
ENV FSPIOP_OAUTH_CLIENT_ID=""
```

Forward them from `docker-entrypoint.sh`:

```sh
"-DfspiopOauthTokenUrl=${FSPIOP_OAUTH_TOKEN_URL:-}" \
"-DfspiopOauthClientId=${FSPIOP_OAUTH_CLIENT_ID:-}" \
```

Do not put `FSPIOP_OAUTH_CLIENT_SECRET` in the Dockerfile or pass it as a `-D` argument. The connector reads it directly from the container environment.

### Server variables

```yaml
env:
  FSPIOP_OAUTH_TOKEN_URL: https://keycloak.stg.paynana.org/realms/dfsps/protocol/openid-connect/token
  FSPIOP_OAUTH_CLIENT_ID: pivotal-trust-manager
  FSPIOP_OAUTH_CLIENT_SECRET:
    valueFrom:
      secretKeyRef:
        name: trust-manager-mcm
        key: MCM_CLIENT_SECRET
```

Configure all three values or none. A partial configuration stops application startup.

OAuth enabled log:

```text
Outbound callbacks will carry a Hub token for client 'pivotal-trust-manager'.
```

OAuth disabled log:

```text
No Hub token configured; outbound callbacks will be sent without one.
```

## 6. Mutual TLS changes

Mutual TLS makes the wallet present a client certificate to the Hub and verify the Hub certificate using the configured CA.

### Dockerfile and entrypoint

The Dockerfile can provide these defaults:

```dockerfile
ENV FSPIOP_USE_MUTUAL_TLS="false"
ENV FSPIOP_MTLS_CA_PATH=""
ENV FSPIOP_MTLS_CLIENT_CERT_PATH=""
ENV FSPIOP_MTLS_CLIENT_KEY_PATH=""
ENV FSPIOP_MTLS_RELOAD_INTERVAL_MS="60000"
```

Forward them from `docker-entrypoint.sh`:

```sh
"-DfspiopUseMutualTls=${FSPIOP_USE_MUTUAL_TLS:-false}" \
"-DfspiopMtlsCaPath=${FSPIOP_MTLS_CA_PATH:-}" \
"-DfspiopMtlsClientCertPath=${FSPIOP_MTLS_CLIENT_CERT_PATH:-}" \
"-DfspiopMtlsClientKeyPath=${FSPIOP_MTLS_CLIENT_KEY_PATH:-}" \
"-DfspiopMtlsReloadIntervalMs=${FSPIOP_MTLS_RELOAD_INTERVAL_MS:-60000}" \
```

### Server variables

```yaml
env:
  FSPIOP_USE_MUTUAL_TLS: "true"
  FSPIOP_MTLS_CLIENT_CERT_PATH: /etc/pivotal/hub-client-tls/tls.crt
  FSPIOP_MTLS_CLIENT_KEY_PATH: /etc/pivotal/hub-client-tls/tls.key
  FSPIOP_MTLS_CA_PATH: /etc/pivotal/hub-ca/hub-ca.pem
  FSPIOP_MTLS_RELOAD_INTERVAL_MS: "60000"
```

Mount the certificate files at those paths:

```yaml
volumeMounts:
  - name: cloudhsm-customer-ca
    mountPath: /opt/cloudhsm/etc/customerCA.crt
    subPath: customerCA.crt
    readOnly: true
  - name: hub-client-tls
    mountPath: /etc/pivotal/hub-client-tls
    readOnly: true
  - name: hub-ca
    mountPath: /etc/pivotal/hub-ca
    readOnly: true

volumes:
  - name: cloudhsm-customer-ca
    configMap:
      name: cloudhsm-customer-ca
  - name: hub-client-tls
    secret:
      secretName: pivotal-hub-client-cert
  - name: hub-ca
    secret:
      secretName: hub-ca-bundle
```

Each mount has a separate purpose:

- `cloudhsm-customer-ca` supplies `customerCA.crt` at the location expected by the AWS CloudHSM client. It is needed only for the CloudHSM profile.
- `pivotal-hub-client-cert` must contain `tls.crt` and `tls.key`. The certificate must be PEM, and the matching private key must be an unencrypted PKCS#8 RSA PEM key.
- `hub-ca-bundle` must contain `hub-ca.pem`, which must trust the Hub gateway certificate.

Because `customerCA.crt` is mounted with `subPath`, replacing its ConfigMap does not update that file in an already-running pod. Restart the pod after rotating the CloudHSM customer CA.

Mutual TLS enabled log:

```text
Hub-facing mutual TLS is enabled; material is re-read every 60000 ms.
```

Mutual TLS disabled log:

```text
Hub-facing mutual TLS is disabled; outbound callbacks will not present a certificate.
```

Missing or invalid certificate files stop startup when mutual TLS is enabled. Mounted files are checked again using `FSPIOP_MTLS_RELOAD_INTERVAL_MS`.

## 7. OAuth and mutual TLS together

For the Hub API gateway, combine the two server configurations. JWS can then be enabled independently with either `KEY_PROVIDER=vault-kv` or `KEY_PROVIDER=pkcs11`.
