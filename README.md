# multi tenant jdbc

## databases

| container | host port | role |
|-----------|-----------|------|
| `db0`     | 5430      | auth database (`users` table) |
| `db1`     | 5431      | tenant 1 (`customer` table) |
| `db2`     | 5432      | tenant 2 (`customer` table) |

```shell
docker-compose up -d
```

On startup the application populates each database from `src/main/resources`
(`auth-schema.sql` / `auth-data.sql` for `db0`, `schema.sql` / `dsN-data.sql` for
the tenants).

## start application

```shell
./gradlew bootRun
```

## test multi tenant

Users are loaded from the `users` table in the auth database (`db0`); the
`tenant_id` column decides which tenant database the request is routed to.

1. test for tenant 1

    ```shell
    $ curl -u "user11:pw" http://localhost:8080/customers
    [{"id":1,"name":"user11"},{"id":2,"name":"user12"}]
    ```

2. test for tenant 2

    ```shell
    $ curl -u "user21:pw" http://localhost:8080/customers
    [{"id":1,"name":"user21"},{"id":2,"name":"user22"}]
    ```

Repeating the same `curl` many times keeps returning `200` (see below).

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

- `JdbcUserDetailsService` now builds a **new** `MultiTenantUser` for every
  lookup, so credential erasure never corrupts shared state.
- `MultiTenantUser` takes an already-encoded password (the standard
  `UserDetails` contract) instead of encoding inside its constructor; the
  hashed password is stored in the database.