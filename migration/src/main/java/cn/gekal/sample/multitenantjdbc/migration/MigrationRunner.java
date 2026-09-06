package cn.gekal.sample.multitenantjdbc.migration;

import java.util.List;
import org.flywaydb.core.Flyway;

/**
 * 認証DB・各テナントDB に対して Flyway マイグレーションを実行する独立プログラム。
 *
 * <p>アプリケーション（app モジュール）は起動時にマイグレーションを流さない。複数インスタンスが同時起動すると 競合するため、このランナーを compose の migration
 * サービスや CI/CD の専用ステップから明示的に実行する。
 *
 * <p>接続先は環境変数で上書きできる（未指定ならローカルの docker-compose を想定した既定値を使う）。
 */
public final class MigrationRunner {

  private MigrationRunner() {}

  /** マイグレーション対象の1データベース。 */
  private record Target(String name, String url, List<String> locations) {}

  public static void main(String[] args) {

    var user = env("DB_USER", "user");
    var password = env("DB_PASSWORD", "pw");

    var targets =
        List.of(
            new Target(
                "auth",
                env("AUTH_DB_URL", "jdbc:postgresql://localhost:5430/user"),
                List.of("classpath:db/migration/auth")),
            new Target(
                "tenant1",
                env("TENANT1_DB_URL", "jdbc:postgresql://localhost:5431/user"),
                List.of(
                    "classpath:db/migration/tenant/schema",
                    "classpath:db/migration/tenant/data/tenant1")),
            new Target(
                "tenant2",
                env("TENANT2_DB_URL", "jdbc:postgresql://localhost:5432/user"),
                List.of(
                    "classpath:db/migration/tenant/schema",
                    "classpath:db/migration/tenant/data/tenant2")));

    for (var target : targets) {
      System.out.printf("[migration] %s (%s) を開始%n", target.name(), target.url());
      var result =
          Flyway.configure()
              .dataSource(target.url(), user, password)
              .locations(target.locations().toArray(String[]::new))
              .load()
              .migrate();
      var version = result.targetSchemaVersion != null ? result.targetSchemaVersion : "変更なし";
      System.out.printf(
          "[migration] %s 完了: %d 件適用（現在のバージョン: %s）%n",
          target.name(), result.migrationsExecuted, version);
    }
  }

  private static String env(String key, String defaultValue) {
    var value = System.getenv(key);
    return value == null || value.isBlank() ? defaultValue : value;
  }
}
