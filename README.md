# multi tenant jdbc

`AbstractRoutingDataSource` を使って、認証したユーザーの `tenant_id` に応じて
接続先データベースを切り替えるサンプル。認証は HTTP Basic、DB アクセスは
`JdbcTemplate`。

- Spring Boot 4.1.1 / Java 17
- Gradle マルチモジュール
  - `app` … アプリケーション本体
  - `migration` … Flyway マイグレーション（アプリ起動時には流さない独立モジュール）
- 依存バージョンは `gradle/libs.versions.toml` が正
- コード整形は Spotless（Google Java Format）: `./gradlew spotlessApply`

## モジュール構成

```
settings.gradle              app / migration を include
build.gradle                 allprojects 共通設定（toolchain, Spotless）
gradle/libs.versions.toml    バージョンカタログ（正）

app/
  src/main/java/cn/gekal/sample/multitenantjdbc/
    datasource/  MultiTenantDataSource（ルーティング）, DataSourceConfiguration
    security/    JdbcUserDetailsService, MultiTenantUser, SecurityConfiguration
    router/      RouterConfiguration（GET /customers）
    model/       Customer

migration/
  Dockerfile
  src/main/java/.../migration/MigrationRunner.java   3つのDBへ順に migrate
  src/main/resources/db/migration/
    auth/                     認証DB: users テーブル + 初期ユーザー
    tenant/schema/            テナントDB共通: customer テーブル
    tenant/data/tenant1/      テナント1の初期データ
    tenant/data/tenant2/      テナント2の初期データ
```

## データベース

| コンテナ | ホストポート | 役割 |
|---------|------------|------|
| `db0`   | 5430       | 認証DB（`users` テーブル） |
| `db1`   | 5431       | テナント1（`customer` テーブル） |
| `db2`   | 5432       | テナント2（`customer` テーブル） |

`users.tenant_id` がリクエストのルーティング先テナントを決める。認証前に参照する
必要があるため、認証DBはテナントDBとは分けている。

## 1. データベースを起動する

```shell
docker compose up -d db0 db1 db2
```

## 2. マイグレーションを実行する

アプリは起動時にマイグレーションを流さない。次のいずれかで明示的に実行する。

```shell
# ローカルの Gradle から（既定でローカルの db0/db1/db2 に接続）
./gradlew :migration:run

# または compose の migration サービス（db0/db1/db2 が healthy になってから実行）
docker compose run --rm migration
```

## 3. アプリケーションを起動する

```shell
./gradlew :app:bootRun
```

## 4. マルチテナントの動作確認

ユーザーは認証DB（`db0`）の `users` テーブルから読み込まれ、`tenant_id` で
接続先テナントDBが切り替わる。

1. テナント1

    ```shell
    $ curl -u "user11:pw" http://localhost:8080/customers
    [{"id":1,"name":"user11"},{"id":2,"name":"user12"}]
    ```

2. テナント2

    ```shell
    $ curl -u "user21:pw" http://localhost:8080/customers
    [{"id":1,"name":"user21"},{"id":2,"name":"user22"}]
    ```

同じ `curl` を何度繰り返しても `200` が返る（下記「解消した問題」を参照）。

## 解消した問題

以前は `curl` を数回実行すると次の例外が発生した。

```
java.lang.IllegalArgumentException: There is no PasswordEncoder mapped for the id "null"
```

### 原因

`UserDetailsService` がルックアップのたびに **同一の** `MultiTenantUser`
インスタンスを返していた。認証成功後に `ProviderManager`
（`eraseCredentialsAfterAuthentication = true`）が principal の
`eraseCredentials()` を呼び、`password` フィールドが `null` になる。次の
リクエストでは保存済みパスワードが `null` のため、`DelegatingPasswordEncoder`
が `{id}` プレフィックスを取り出せず上記の例外を投げていた。

### 対応

- `JdbcUserDetailsService` がルックアップごとに **新しい** `MultiTenantUser`
  を生成するようにし、credentials 消去が共有状態を壊さないようにした。
- `MultiTenantUser` はコンストラクタ内でエンコードせず、エンコード済み
  パスワードを受け取る（`UserDetails` の標準的な契約）。ハッシュ化済み
  パスワードは認証DBに保存する。
