# ビルドステージ: migration モジュールの実行用ディストリビューションを作る
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew --no-daemon :migration:installDist

# ランタイムステージ: マイグレーションを実行して終了する
FROM eclipse-temurin:21-jre
WORKDIR /opt/migration
COPY --from=build /workspace/migration/build/install/migration/ ./
ENTRYPOINT ["bin/migration"]
