# multi tenant jdbc

## prepare databases

```shell
docker-compose up -d
```

## start application

```shell
./gradlew bootRun
```

## test multi tenant

1. test for tenant 1

    ```shell
    $ curl -u "user11:pw" http://localhost:8080/customers
    [{"id":1,"name":"user11"},{"id":2,"name":"user12"}]
    ```

2. test for tenant 1

    ```shell
    $ curl -u "user21:pw" http://localhost:8080/customers
    [{"id":1,"name":"user21"},{"id":2,"name":"user22"}]
    ```

## resolved problem

Previously, running `curl` several times threw:

```
java.lang.IllegalArgumentException: There is no PasswordEncoder mapped for the id "null"
```

### cause

`UserDetailsService` returned the **same** `MultiTenantUser` instance on every lookup.
After the first successful authentication, `ProviderManager`
(`eraseCredentialsAfterAuthentication = true`) calls `eraseCredentials()` on the
authenticated principal, which sets its `password` field to `null`. On the next
request the stored password is `null`, so `DelegatingPasswordEncoder` cannot
extract an `{id}` prefix and throws the exception above.

### fix

- `SecurityConfiguration#userDetailsService` now builds a **new** `MultiTenantUser`
  for every lookup, so credential erasure never corrupts the shared state.
- The password is encoded once at startup; `MultiTenantUser` now takes an
  already-encoded password (the standard `UserDetails` contract) instead of
  encoding inside its constructor.