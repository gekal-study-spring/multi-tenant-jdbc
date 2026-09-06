-- 認証用データベース: ユーザー表
-- tenant_id はリクエストのルーティング先テナントを決めるため、認証前に参照できる必要がある。
-- よってテナントDBではなく専用の認証DB(db0)に置く。
create table users
(
    username  varchar(255) not null primary key,
    password  varchar(255) not null, -- DelegatingPasswordEncoder 形式（{bcrypt}...）
    enabled   boolean      not null default true,
    tenant_id integer      not null
);
