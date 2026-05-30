```mermaid
-- 1. 의존성이 없는 독립 테이블 생성 (부모 테이블)

CREATE TABLE users (
    is_deleted boolean not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    updated_at timestamp(6) not null,
    user_id uuid not null,
    email varchar(255) not null unique,
    nickname varchar(255) not null,
    password varchar(255) not null,
    primary key (user_id)
);

CREATE TABLE articles (
    is_deleted boolean not null,
    comment_count bigint not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    publish_date timestamp(6) not null,
    updated_at timestamp(6) not null,
    view_count bigint not null,
    article_id uuid not null,
    source varchar(20) not null check (source in ('NAVER')),
    source_url varchar(2048) not null,
    summary TEXT,
    title varchar(255) not null,
    primary key (article_id)
);

CREATE TABLE interests (
    created_at timestamp(6) not null,
    subscription_count bigint not null,
    updated_at timestamp(6) not null,
    interest_id uuid not null,
    name varchar(255),
    primary key (interest_id)
);


-- 2. 위 테이블들을 참조하는 종속 테이블 생성 (자식 테이블)

CREATE TABLE comments (
    is_deleted boolean not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    like_count bigint not null,
    updated_at timestamp(6) not null,
    article_id uuid not null,
    comment_id uuid not null,
    user_id uuid not null,
    content varchar(500) not null,
    primary key (comment_id),
    constraint FKk4ib6syde10dalk7r7xdl0m5p foreign key (article_id) references articles(article_id),
    constraint FK8omq0tc18jd43bu5tjh6jvraq foreign key (user_id) references users(user_id)
);

CREATE TABLE article_views (
    created_at timestamp(6) not null,
    article_id uuid not null,
    article_view_id uuid not null,
    user_id uuid not null,
    primary key (article_view_id),
    constraint idx_article_view_article_user unique (article_id, user_id),
    constraint FKcy2gs222qhspyjswnhhewweml foreign key (article_id) references articles(article_id),
    constraint FKchqnr7kjrpxlbtcurs6xj8p0j foreign key (user_id) references users(user_id)
);

CREATE TABLE comment_likes (
    created_at timestamp(6) not null,
    comment_id uuid not null,
    comment_like_id uuid not null,
    user_id uuid not null,
    primary key (comment_like_id),
    constraint idx_comment_like_comment_user unique (comment_id, user_id),
    constraint FK3wa5u7bs1p1o9hmavtgdgk1go foreign key (comment_id) references comments(comment_id),
    constraint FK6h3lbneryl5pyb9ykaju7werx foreign key (user_id) references users(user_id)
);

CREATE TABLE interest_keywords (
    interest_id uuid not null,
    interest_keyword_id uuid not null,
    keyword varchar(255) not null,
    primary key (interest_keyword_id),
    constraint FKsr5skpgs5moostt42kobkehfn foreign key (interest_id) references interests(interest_id)
);

CREATE TABLE subscriptions (
    created_at timestamp(6) not null,
    interest_id uuid not null,
    subscription_id uuid not null,
    user_id uuid not null,
    primary key (subscription_id),
    constraint idx_subscription_interest_user unique (interest_id, user_id),
    constraint FK4pvf2prugu6d6govem8ovhyjh foreign key (interest_id) references interests(interest_id),
    constraint FKhro52ohfqfbay9774bev0qinr foreign key (user_id) references users(user_id)
);
```