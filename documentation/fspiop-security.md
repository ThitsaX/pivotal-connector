# FSPIOP Security Configuration

This document describes the security applied to outbound FSPIOP callbacks sent by the Pivotal Connector to the Hub. It covers:

- JWS message signing with an RSA private key loaded from Vault KV
- JWS message signing inside AWS CloudHSM
- OAuth 2.0 client-credentials authentication
- Mutual TLS for the Hub-facing connection

These controls are independent and serve different purposes:

| Control | Purpose | Applied as |
| --- | --- | --- |
| JWS | Proves which DFSP created the FSPIOP message and protects signed request fields and body | `FSPIOP-Signature` header |
| OAuth | Authenticates and authorizes the connector at the Hub API gateway | `Authorization: Bearer <token>` header |
| Mutual TLS | Authenticates both ends of the connection and encrypts traffic | TLS handshake |

For an API-gateway deployment, the normal production profile enables all three. Internal deployments that do not pass through the gateway may omit OAuth. JWS and mutual TLS should be enabled according to the Hub's security policy.

For the wallet-specific JWS, OAuth, mutual-TLS, Dockerfile, and entrypoint changes, see [ThitsaWallet Connector FSPIOP Security Configuration](wallet-connector-fspiop-security-migration.md).

## How configuration is read

The connector's configuration class primarily reads **Java system properties**, not arbitrary process environment variables. It accepts both lower camel-case and uppercase underscore names as system properties. For example, either of these JVM arguments works:

```text
-DfspiopUseJws=true
-DFSPIOP_USE_JWS=true
```

Downstream connector entrypoints commonly translate Kubernetes environment variables into `-D` arguments. Confirm that the deployable connector does this for every non-secret setting shown in this document. Merely defining `FSPIOP_USE_JWS` as an environment variable does not make it a Java system property.

`FSPIOP_OAUTH_CLIENT_SECRET` is the exception. It is read directly from the process environment as a fallback and should be injected from a Kubernetes Secret. A Java system property named `fspiopOauthClientSecret` can override that environment value, but passing secrets on the JVM command line is discouraged.

Configuration values are case-sensitive. In particular, `KEY_PROVIDER` must be exactly `vault-kv` or `pkcs11`.

## Configuration summary

| Deployment name | Java property | Default | Required when |
| --- | --- | --- | --- |
| `CONNECTOR_ID` | `connectorId` | `dfsp` | Always; set it to the participant/DFSP ID |
| `FSPIOP_USE_JWS` | `fspiopUseJws` | `false` | Set to `true` to enable signing |
| `KEY_PROVIDER` | `keyProvider` | `vault-kv` | JWS is enabled |
| `VAULT_URL` | `vaultUrl` | empty | JWS is enabled |
| `VAULT_ROLE` | `vaultRole` | empty | JWS is enabled |
| `VAULT_KUBERNETES_AUTH_PATH` | `vaultKubernetesAuthPath` | `kubernetes` | Vault uses a non-default auth mount |
| `VAULT_KV_MOUNT` | `vaultKvMount` | `secret` | Vault KV v2 uses a non-default mount |
| `VAULT_SERVICE_ACCOUNT_TOKEN_PATH` | `vaultServiceAccountTokenPath` | `/var/run/secrets/kubernetes.io/serviceaccount/token` | The token is mounted elsewhere |
| `VAULT_JWS_KEY_PATH_PREFIX` | `vaultJwsKeyPathPrefix` | `pivotal/jwskey` | `KEY_PROVIDER=vault-kv` |
| `HSM_CRED_PATH` | `hsmCredPath` | empty | `KEY_PROVIDER=pkcs11` |
| `KEY_REF_PATH_PREFIX` | `keyRefPathPrefix` | `pivotal/keyref` | `KEY_PROVIDER=pkcs11` |
| `FSPIOP_OAUTH_TOKEN_URL` | `fspiopOauthTokenUrl` | empty | OAuth is enabled |
| `FSPIOP_OAUTH_CLIENT_ID` | `fspiopOauthClientId` | empty | OAuth is enabled |
| `FSPIOP_OAUTH_CLIENT_SECRET` | `fspiopOauthClientSecret` | empty | OAuth is enabled; inject directly as an environment variable |
| `FSPIOP_USE_MUTUAL_TLS` | `fspiopUseMutualTls` | `false` | Set to `true` to enable mutual TLS |
| `FSPIOP_MTLS_CA_PATH` | `fspiopMtlsCaPath` | empty | Mutual TLS is enabled |
| `FSPIOP_MTLS_CLIENT_CERT_PATH` | `fspiopMtlsClientCertPath` | empty | Mutual TLS is enabled |
| `FSPIOP_MTLS_CLIENT_KEY_PATH` | `fspiopMtlsClientKeyPath` | empty | Mutual TLS is enabled |
| `FSPIOP_MTLS_RELOAD_INTERVAL_MS` | `fspiopMtlsReloadIntervalMs` | `60000` | Optional mutual-TLS reload interval |

## JWS signing

When `FSPIOP_USE_JWS=true`, every outbound FSPIOP callback is signed using RS256 (`SHA256withRSA`). The connector signs the standard detached JWS input:

```text
base64url(protected-header) + "." + base64url(exact-request-body)
```

The protected header contains the FSPIOP method, URI, source, optional destination, and optional date used by the request. The request body is signed as the exact bytes sent by the connector.

JWS configuration is fail-closed. Selecting a provider while JWS is enabled means signing is required. Missing, incomplete, malformed, or inaccessible key data stops startup instead of silently allowing unsigned callbacks.

### Common Vault configuration

Both JWS providers use Vault KV v2. The connector authenticates with its Kubernetes service-account JWT:

1. Read the JWT from `VAULT_SERVICE_ACCOUNT_TOKEN_PATH`.
2. POST it with `VAULT_ROLE` to `<VAULT_URL>/v1/auth/<VAULT_KUBERNETES_AUTH_PATH>/login`.
3. Use the returned short-lived Vault token to read secrets below `VAULT_KV_MOUNT`.

Required settings:

```text
FSPIOP_USE_JWS=true
CONNECTOR_ID=payeefsp
VAULT_URL=https://vault.example.com
VAULT_ROLE=pivotal-connector-payeefsp
VAULT_KUBERNETES_AUTH_PATH=kubernetes
VAULT_KV_MOUNT=secret
VAULT_SERVICE_ACCOUNT_TOKEN_PATH=/var/run/secrets/kubernetes.io/serviceaccount/token
```

The Vault role should grant the connector only the paths belonging to its `CONNECTOR_ID`.

Vault is read during provider initialization and an explicit refresh, not for every callback. Once loaded, the signing key or HSM handle is cached in memory.

### Vault KV key provider

Use this provider when Vault stores an exportable PEM private key:

```text
FSPIOP_USE_JWS=true
KEY_PROVIDER=vault-kv
CONNECTOR_ID=payeefsp
VAULT_JWS_KEY_PATH_PREFIX=pivotal/jwskey # gitleaks:allow
```

The complete secret path below the configured KV v2 mount is:

```text
<VAULT_JWS_KEY_PATH_PREFIX>/<CONNECTOR_ID>
```

For the example above:

```text
secret/pivotal/jwskey/payeefsp
```

The Vault data must have this shape:

```json
{
  "privateKey": "<PKCS#8 RSA private-key PEM>"
}
```

Key requirements:

- RSA private key
- PKCS#8 PEM with `BEGIN PRIVATE KEY`
- Unencrypted key material
- PKCS#1 `BEGIN RSA PRIVATE KEY` is not supported

Convert an existing PKCS#1 key to unencrypted PKCS#8 with:

```bash
openssl pkcs8 -topk8 -nocrypt -in pkcs1.pem -out pkcs8.pem
```

The key is read into the connector process and used by the JVM's RSA provider. Protect the Vault path with a tenant-specific policy and audit access to it.

Startup fails when:

- The Vault URL or role is missing.
- Vault authentication or the secret read fails.
- The secret does not exist.
- `privateKey` is missing or blank.
- The PEM is not a valid PKCS#8 RSA private key.

On an explicit refresh, the new key is parsed before it replaces the active key. A validation or parsing failure therefore leaves the last known-good key reference in the provider, while the refresh call reports the failure.

### AWS CloudHSM key provider

Use this provider when the RSA private key must remain inside AWS CloudHSM:

```text
FSPIOP_USE_JWS=true
KEY_PROVIDER=pkcs11
CONNECTOR_ID=payeefsp
HSM_CRED_PATH=pivotal/hsmcred/payeefsp # gitleaks:allow
KEY_REF_PATH_PREFIX=pivotal/keyref # gitleaks:allow
```

The configuration value is named `pkcs11` because it selects the HSM-backed custody profile. The Java implementation uses the AWS CloudHSM JCE provider rather than the JDK's `SunPKCS11` provider. CloudHSM does not store the certificate object that the JDK PKCS#11 keystore expects when pairing a private key, whereas the AWS provider can resolve the key directly by label.

#### Credential secret

`HSM_CRED_PATH` is a complete path below `VAULT_KV_MOUNT`. Its Vault data must contain:

```json
{
  "username": "pivotal-payeefsp",
  "password": "<CloudHSM crypto-user password>"
}
```

Both fields must be non-blank. Do not put the crypto-user password in a Deployment, ConfigMap, JVM argument, or repository file.

The connector reads the Vault values and sets the `HSM_USER` and `HSM_PASSWORD` properties expected by the AWS provider. Operators should not separately configure those values in the pod.

#### Key-reference secret

The complete key-reference path is:

```text
<KEY_REF_PATH_PREFIX>/<CONNECTOR_ID>
```

For the example above:

```text
secret/pivotal/keyref/payeefsp
```

The Vault data must contain:

```json
{
  "keyRef": "<exact CloudHSM key label>"
}
```

`keyRef` is the exact CloudHSM key label. The key must:

- Be an RSA private key suitable for `SHA256withRSA`.
- Exist in the configured cluster.
- Be owned by or shared with the configured crypto user.
- Be visible under the exact label stored in `keyRef`.

#### Runtime prerequisites

The connector runtime image or host must include:

- The AWS CloudHSM client and JCE SDK.
- The CloudHSM JCE provider JAR on the JVM classpath.
- The SDK's native libraries.
- A valid CloudHSM client configuration containing the cluster address.
- Network connectivity from the pod or host to the CloudHSM cluster.

The connector loads this provider class reflectively:

```text
com.amazonaws.cloudhsm.jce.provider.CloudHsmProvider
```

The JAR is deliberately not a Maven dependency because it is distributed with the device SDK rather than a public Maven repository.

CloudHSM credentials are process-wide system properties. One connector JVM must therefore represent one crypto user and one participant. Do not use a single JVM to sign for multiple tenants.

Startup fails when:

- `HSM_CRED_PATH` or `KEY_REF_PATH_PREFIX` is blank.
- Either Vault secret does not exist.
- `username`, `password`, or `keyRef` is missing or blank.
- The crypto-user credentials are rejected.
- The referenced key does not exist or is not visible to the crypto user.
- The JCE provider JAR, native SDK, or cluster configuration is unavailable.

The provider publishes its active key and JCE provider only after the key has been resolved successfully. A failed validation or load does not replace those active object references.

## OAuth 2.0 client credentials

OAuth is optional and is enabled by configuring all three values:

```text
FSPIOP_OAUTH_TOKEN_URL=https://identity.example.com/oauth2/token
FSPIOP_OAUTH_CLIENT_ID=pivotal-connector-payeefsp
FSPIOP_OAUTH_CLIENT_SECRET=<injected from a Kubernetes Secret>
```

All three values must be configured together. If none are configured, callbacks are sent without a bearer token. If only some are configured, startup fails.

The connector requests a token with an `application/x-www-form-urlencoded` POST:

```text
grant_type=client_credentials
client_id=<FSPIOP_OAUTH_CLIENT_ID>
client_secret=<FSPIOP_OAUTH_CLIENT_SECRET>
```

The implementation does not currently send `scope`, `audience`, or custom token parameters. The identity provider must accept the three fields above.

The successful response must be JSON:

```json
{
  "access_token": "<bearer token>",
  "expires_in": 300
}
```

`access_token` is required. `expires_in` is interpreted as seconds. When it is absent, the connector assumes a 60-second lifetime.

Token behavior:

- A token is cached and shared by all concurrent callbacks.
- The normal path for a valid cached token does not acquire a lock.
- One thread refreshes an expired token while other threads wait for that refresh.
- Refresh occurs up to 30 seconds before expiry, capped at half the token lifetime for short-lived tokens.
- Every protected callback receives `Authorization: Bearer <access_token>`.
- An HTTP `401` closes the rejected response, invalidates that token, obtains a new token, and retries the callback once.
- A second `401` is returned to the callback service; the interceptor does not loop.
- Token errors report the client ID and HTTP status but do not log the client secret or identity-provider response body.

The token request uses the framework's base HTTP client. It does not receive the Hub client certificate, JWS signature, or Hub bearer-token interceptor because the identity provider is a separate destination.

### Kubernetes OAuth secret

Inject the client secret directly into the expected environment variable:

```yaml
env:
  FSPIOP_OAUTH_CLIENT_SECRET:
    valueFrom:
      secretKeyRef:
        name: trust-manager-mcm
        key: MCM_CLIENT_SECRET
```

This is the Helm `values.yaml` format used by the deployable connector chart. The chart renders it as a standard Kubernetes `env` list. Here, `MCM_CLIENT_SECRET` is the key inside the Kubernetes Secret and `FSPIOP_OAUTH_CLIENT_SECRET` is the environment name visible to Java.

Do not put the client secret in `JAVA_TOOL_OPTIONS`, the container command, or a `-D` argument. Environment variables sourced from Kubernetes Secrets avoid exposing it in the Java command line.

Kubernetes does not update environment variables in a running container when the underlying Secret changes. Restart or roll out the connector pods after rotating the OAuth client secret.

## Mutual TLS

Mutual TLS is optional and applies only to the outbound Hub-facing client. Enable it with:

```text
FSPIOP_USE_MUTUAL_TLS=true
FSPIOP_MTLS_CA_PATH=/etc/pivotal/mtls/ca.pem
FSPIOP_MTLS_CLIENT_CERT_PATH=/etc/pivotal/mtls/tls.crt
FSPIOP_MTLS_CLIENT_KEY_PATH=/etc/pivotal/mtls/tls.key
FSPIOP_MTLS_RELOAD_INTERVAL_MS=60000
```

All three files are required when mutual TLS is enabled:

| File | Contents |
| --- | --- |
| CA path | One or more PEM X.509 certificates trusted for the Hub server |
| Client certificate path | PEM leaf certificate followed by any issuer/intermediate certificates |
| Client key path | Unencrypted PKCS#8 RSA private key matching the client certificate |

The certificate and key must be a matching pair. The connector creates an in-memory key store and trust store; it does not write converted material to disk.

Startup fails when:

- A configured path is unreadable.
- A PEM file is malformed.
- Only one half of the client certificate/key pair is present.
- No client certificate is configured.
- No CA trust anchor is configured.
- The private key is not an unencrypted PKCS#8 RSA key.

Mutual TLS does not fall back to an unauthenticated connection when it is enabled but invalid.

### Kubernetes certificate mount

A typical Secret mount is:

```yaml
volumeMounts:
  - name: fspiop-mtls
    mountPath: /etc/pivotal/mtls
    readOnly: true

volumes:
  - name: fspiop-mtls
    secret:
      secretName: pivotal-connector-mtls
```

The Secret should expose keys that produce these mounted files:

```text
/etc/pivotal/mtls/ca.pem
/etc/pivotal/mtls/tls.crt
/etc/pivotal/mtls/tls.key
```

If the Secret uses different key names, use `items` in the volume declaration or update the three path settings accordingly.

### Certificate rotation

The connector polls the three files using `FSPIOP_MTLS_RELOAD_INTERVAL_MS`, which defaults to 60,000 milliseconds. A positive value is required in practice.

On each pass:

1. Read the CA, certificate chain, and private key.
2. Calculate a SHA-256 fingerprint of their contents.
3. Do nothing when the content has not changed.
4. Build replacement key and trust managers when it has changed.
5. Atomically switch new TLS handshakes to the replacement material.

Existing pooled connections continue using the TLS session they already negotiated. New connections use the reloaded certificate and trust anchors.

If a reload sees a transient, incomplete, unreadable, or invalid Secret update, the connector logs the failure, keeps the current working TLS material, and retries at the next interval. Initial startup remains fail-closed.

## Combined production example

The following uses the deployable connector chart's Helm `values.yaml` format. It illustrates a connector using CloudHSM, OAuth, and mutual TLS. The chart must forward the non-secret values as Java properties; the OAuth secret remains a direct process environment variable:

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
  FSPIOP_PARTIES_URL: https://extapi.example.com
  FSPIOP_QUOTES_URL: https://extapi.example.com
  FSPIOP_TRANSFERS_URL: https://extapi.example.com
  FSPIOP_OAUTH_TOKEN_URL: https://identity.example.com/oauth2/token
  FSPIOP_OAUTH_CLIENT_ID: pivotal-connector-payeefsp
  FSPIOP_OAUTH_CLIENT_SECRET:
    valueFrom:
      secretKeyRef:
        name: trust-manager-mcm
        key: MCM_CLIENT_SECRET
  FSPIOP_USE_MUTUAL_TLS: "true"
  FSPIOP_MTLS_CA_PATH: /etc/pivotal/mtls/ca.pem
  FSPIOP_MTLS_CLIENT_CERT_PATH: /etc/pivotal/mtls/tls.crt
  FSPIOP_MTLS_CLIENT_KEY_PATH: /etc/pivotal/mtls/tls.key
  FSPIOP_MTLS_RELOAD_INTERVAL_MS: "60000"
```

Do not add `FSPIOP_TLS_VERIFY_SERVER_CERT=false` or `FSPIOP_TLS_VERIFY_DOMAIN=false` to this profile. The core connector does not consume those settings. It validates the Hub certificate against `FSPIOP_MTLS_CA_PATH` and performs normal HTTPS hostname verification.

The resulting outbound callback performs these independent steps:

1. Serialize the FSPIOP body and add the FSPIOP request headers.
2. Add the detached JWS signature using the configured key provider.
3. Obtain or reuse the OAuth token and add the bearer header.
4. Establish or reuse an HTTPS connection using the configured mutual-TLS material.

The OAuth `Authorization` header is not part of the FSPIOP JWS protected header. JWS signing, OAuth authentication, and mutual TLS therefore remain independently configurable.

## Rotation guidance

### Vault KV signing key

1. Write the new PKCS#8 RSA key to the connector's Vault path.
2. Trigger the connector's explicit JWS refresh mechanism or restart the connector.
3. Verify the log confirms the key was loaded.
4. Coordinate public-key distribution and verification at the Hub before retiring the old key.

### CloudHSM signing key

1. Generate or import the new RSA key in CloudHSM.
2. Ensure the connector crypto user owns or can use it.
3. Update the Vault `keyRef` only after the key is available under that label.
4. Trigger an explicit JWS refresh or restart the connector.
5. Coordinate the corresponding public key with the Hub.

### OAuth client secret

1. Rotate the secret at the identity provider.
2. Update the Kubernetes Secret.
3. Roll out/restart the connector pods so the environment value is recreated.
4. Verify that token acquisition succeeds without logging the secret.

### Mutual-TLS certificate

1. Update the mounted Kubernetes Secret with a complete matching certificate, key, and CA set.
2. Keep an overlap during which both old and new certificates are accepted.
3. Wait for the configured reload interval and verify the reload log.
4. Existing connections may continue with the old TLS session until they are replaced.

## Troubleshooting

### Callbacks are unsigned

- Confirm `FSPIOP_USE_JWS=true` reached the JVM as a system property.
- Confirm `KEY_PROVIDER` is exactly `vault-kv` or `pkcs11`.
- Check the startup logs. Current providers fail startup rather than intentionally continuing unsigned when key data is unavailable.

### Vault authentication fails

- Verify `VAULT_URL`, `VAULT_ROLE`, and the auth mount.
- Verify the service-account JWT exists at `VAULT_SERVICE_ACCOUNT_TOKEN_PATH`.
- Verify the Vault Kubernetes role is bound to the pod's service account and namespace.
- Verify the role policy can read the exact KV v2 secret path.

### Vault PEM key is rejected

- Confirm the field is named `privateKey`.
- Confirm it is an RSA PKCS#8 key with `BEGIN PRIVATE KEY`.
- Convert PKCS#1 keys using the command in the Vault KV section.

### CloudHSM provider cannot start

- Confirm the complete AWS CloudHSM SDK, not only the JAR, is installed.
- Confirm the JCE provider JAR is on the launch classpath.
- Confirm the native libraries can be loaded.
- Confirm the SDK configuration contains the correct cluster address.
- Confirm the pod can reach the cluster.

### CloudHSM key is not visible

- Confirm `keyRef` exactly matches the HSM label.
- Confirm the key exists in the configured cluster.
- Confirm the key is owned by or shared with the configured crypto user.
- Confirm the credential secret contains non-blank `username` and `password` fields.

### OAuth is not enabled

- Configure the token URL, client ID, and client secret together.
- Confirm `FSPIOP_OAUTH_CLIENT_SECRET` exists in the running container environment.
- Confirm the deployable entrypoint forwards the token URL and client ID as Java properties.
- Confirm the token endpoint accepts form-based client credentials without additional `scope` or `audience` parameters.

### Mutual-TLS handshake fails

- Confirm the Hub URL uses HTTPS and points to the expected gateway/Hub endpoint.
- Confirm the client certificate and private key match.
- Confirm the certificate chain includes required intermediates.
- Confirm the CA file trusts the Hub's server certificate chain.
- Confirm certificate validity dates and hostname verification.
- Confirm all paths are readable by the connector process.

## Security cautions

- Never commit OAuth secrets, CloudHSM passwords, private keys, or real bearer tokens.
- Keep CloudHSM credentials in Vault and the OAuth client secret in a Kubernetes Secret.
- Scope Vault policies per connector and per `CONNECTOR_ID`.
- Do not disable TLS certificate validation or hostname verification.
- Do not add broad Gitleaks exclusions for secret-bearing directories or configuration files.
- Use targeted `gitleaks:allow` comments only for verified non-secret fixtures such as test Vault paths.
- Treat logs and exception messages as public operational data; do not add secret values to them.
