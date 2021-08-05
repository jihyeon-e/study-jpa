# 19. 고급 매핑 : 상속관계 매핑

# 1. 상속 관계 매핑

- 객체는 상속관계가 존재하지만, 관계형 데이터베이스는 상속 관계가 없다.
- 관계형 DB의 슈퍼타입 서브타입 관계라는 모델링 기법이 객체의 상속 개념과 가장 유사하다.
- 상속관계 매핑이라는 것은 객체의 상속 구조와 DB의 슈퍼타입 서브타입 관계를 매핑하는 것이다.

![https://i.imgur.com/zuoqIlQ.png](https://i.imgur.com/zuoqIlQ.png)

왼쪽부터 슈퍼타입 서브타입 논리 모델, 객체 상속 모델이다.

### **슈퍼타입 서브타입 논리 모델을 실제 물리 모델인 테이블로 구현하는 방법**

JPA에서는 3가지 방법 전부 지원한다.

- 각각 테이블로 변환 : 조인 전략
- 통합 테이블로 변환 : 단일 테이블 전략
- 서브타입 테이블로 변환 : 구현 클래스마다 테이블 전략

### **주요 어노테이션**

- @Inheritance(strategy=InheritanceType.XXX)
    - JOINED : 조인 전략
    - SINGLE_TABLE : 단일 테이블 전략
    - TABLE_PER_CLASS: 구현 클래스마다 테이블 전략
- @DiscriminatorColumn(name=“DTYPE”)
- @DiscriminatorValue(“XXX”)

각각 엔티티를 작성하고 실행하면 아래와 같이 한 테이블이 생성된다.

```sql
Hibernate: 
    
    create table Item (
       DTYPE varchar(31) not null,
        id bigint not null,
        name varchar(255),
        price integer not null,
        artist varchar(255),
        author varchar(255),
        isbn varchar(255),
        actor varchar(255),
        director varchar(255),
        primary key (id)
    )
```

JPA 기본 전략이 단일 테이블 전략이기 때문이다.

## 1) 조인 전략

![https://i.imgur.com/MWqvrVB.png](https://i.imgur.com/MWqvrVB.png)

- 엔티티 각각을 모두 테이블로 만들고 자식 테이블이 부모 테이블의 기본 키를 받아서 기본 키 + 외래 키로 사용하는 가장 정규화된 전략
- 조회할 때 조인을 자주 사용한다.
- 주의할 점은 객체는 타입으로 구분할 수 있지만 테이블은 타입의 개념이 없기 때문에 타입을 구분하는 컬럼을 추가해야 한다. 여기서는 DTYPE 컬럼을 구분 컬럼으로 사용한다.

### (1) 조인 전략을 사용한 예제 코드

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Item {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private int price;

}
```

**매핑 정보 분석하기**

- @Inheritance(strategy = InheritanceType.JOINED)
    - 상속 매핑 시 부모 클래스에 사용하는 어노테이션
    - 매핑 전략을 지정해야하는데 여기서는 조인전략을 사용하므로 InheritanceType.JOINED을 사용했다.

**실행 시 DDL**

- 조인 전략에 맞는 테이블 4개가 생성되었다.

```sql
Hibernate: 
    
    create table Album (
       artist varchar(255),
        id bigint not null,
        primary key (id)
    )
Hibernate: 
    
    create table Book (
       author varchar(255),
        isbn varchar(255),
        id bigint not null,
        primary key (id)
    )
Hibernate: 
    
    create table Item (
       id bigint not null,
        name varchar(255),
        price integer not null,
        primary key (id)
    )
Hibernate: 
    
    create table Movie (
       actor varchar(255),
        director varchar(255),
        id bigint not null,
        primary key (id)
    )
```

### (2) 데이터를 INSERT 한 뒤 SELECT 하기

**JpaMain**

```java
Movie movie = new Movie();
movie.setDirector("aaaa");
movie.setActor("bbbb");
movie.setName("바람과함께사라지");
movie.setPrice(10000);

em.persist(movie);

em.flush();
em.clear();

Movie findMovie = em.find(Movie.class, movie.getId());
System.out.println("findMovie = " + findMovie);

tx.commit();
```

**실행 시 DDL**

```sql
Hibernate: 
    /* insert hellojpa.Movie
        */ insert 
        into
            Item
            (name, price, DTYPE, id) 
        values
            (?, ?, 'Movie', ?)
Hibernate: 
    /* insert hellojpa.Movie
        */ insert 
        into
            Movie
            (actor, director, id) 
        values
            (?, ?, ?)

Hibernate: 
    select
        movie0_.id as id1_2_0_,
        movie0_1_.name as name2_2_0_,
        movie0_1_.price as price3_2_0_,
        movie0_.actor as actor1_3_0_,
        movie0_.director as director2_3_0_ 
    from
        Movie movie0_ 
    inner join
        Item movie0_1_ 
            on movie0_.id=movie0_1_.id 
    where
        movie0_.id=?
findMovie = hellojpa.Movie@a5272be
```

상속관계인 경우 JPA가 join이 필요하면 알아서 join을 하고, INSERT 할 때도 두 개의 쿼리가 나간다.

**DB 확인하기**

![https://i.imgur.com/GJGxNfx.png](https://i.imgur.com/GJGxNfx.png)

### (3) @DiscriminatorColumn 사용하기

**Item**

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
public class Item {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private int price;

}
```

**Movie**

```java
@Entity
@DiscriminatorValue("M")
public class Movie extends Item {

    private String director;
    private String actor;
}
```

**매핑 정보 분석하기**

- @DiscriminatorColumn(name = "DTYPE")
    - 부모 클래스에 구분 컬럼을 지정하여 저장된 자식 테이블을 구분한다.
    - 기본값이 DTYPE이므로 @DiscriminatorColumn로 줄여서 사용해도 된다.
- @DiscriminatorValue("M")
    - 엔티티를 저장할 때 구분 컬럼에 입력할 값을 지정한다.

**DB 확인하기**

![https://i.imgur.com/XZkfXXI.png](https://i.imgur.com/XZkfXXI.png)

### (4) @PrimaryKeyJoinColumn 사용하기

**Book**

```java
@Entity
@DiscriminatorValue("B")
@PrimaryKeyJoinColumn(name = "BOOK_ID")
public class Book extends Item {

    private String author;
    private String isbn;
}
```

**매핑 정보 분석하기**

- @PrimaryKeyJoinColumn(name = "BOOK_ID")
    - 자식 테이블의 기본 키 컬럼명을 변경하고 싶을 때 사용한다.
    - BOOK 테이블의 ITEM_ID 기본 키 컬럼명을 BOOK_ID로 변경했다.

### (5) 정리

기본적으로는 조인 전략을 추천한다.

- 장점
    - 테이블 정규화
    - 외래 키 참조 무결성 제약 조건 활용가능
    - 저장공간 효율화
- 단점
    - 조회 시 조인을 많이 사용, 성능 저하
    - 조회 쿼리가 복잡함
    - 데이터 저장 시 INSERT SQL 2번 실행

## 2) 단일 테이블 전략

![https://i.imgur.com/jN7uVy8.png](https://i.imgur.com/jN7uVy8.png)

- 테이블을 하나만 사용하고 구분 컬럼(DTYPE)으로 어떤 자식 데이터가 저장되었는지 구분한다.
- 주의할 점은 자식 엔티티가 매핑한 컬럼은 모두 null을 허용해야 한다는 점이다.

### (1) 단일 테이블 전략을 사용한 예제 코드

**Item**

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
public class Item {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private int price;

}
```

**매핑 정보 분석하기**

- @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    - strategy를 SINGLE_TABLE로 변경하면 끝난다.
        - JPA의 장점이다. 테이블 구조의 변동이 일어났는데, 어노테이션만 수정했다.
        - 만약 JPA를 쓰지 않았더라면, 테이블 구조의 변경이 일어나면 거의 모든 코드를 손대야 할 것이다.
    - 테이블 하나에 모든 것을 통합하므로 구분 컬럼을 필수로 사용해야 한다.
    - @DiscriminatorColumn을 선언해 주지 않아도 구분 컬럼이 자동으로 생성된다.

**실행 시 DDL**

- 단일 테이블 전략에 맞는 테이블 1개가 생성되었다.

```sql
Hibernate: 
    
    create table Item (
       DTYPE varchar(31) not null,
        id bigint not null,
        name varchar(255),
        price integer not null,
        artist varchar(255),
        author varchar(255),
        isbn varchar(255),
        actor varchar(255),
        director varchar(255),
        primary key (id)
    )

Hibernate: 
    /* insert hellojpa.Movie
        */ insert 
        into
            Item
            (name, price, actor, director, DTYPE, id) 
        values
            (?, ?, ?, ?, 'M', ?)
Hibernate: 
    select
        movie0_.id as id2_0_0_,
        movie0_.name as name3_0_0_,
        movie0_.price as price4_0_0_,
        movie0_.actor as actor8_0_0_,
        movie0_.director as director9_0_0_ 
    from
        Item movie0_ 
    where
        movie0_.id=? 
        and movie0_.DTYPE='M'
```

**DB 확인하기**

![https://i.imgur.com/i35B6iN.png](https://i.imgur.com/i35B6iN.png)

### (2) 정리

- 장점
    - 조인이 필요 없으므로 일반적으로 조회 성능이 빠름
    - 조회 쿼리가 단순함
- 단점
    - 자식 엔티티가 매핑한 컬럼은 모두 null 허용해야 함 → 데이터 무결성 제약 조건에 치명적 단점
    - 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있음
    - 일반적으로는 조인 전략보다 성능이 빠르지만, 상황에 따라서 조회 성능이 오히려 느려질 수 있음

## 3) 구현 클래스마다 테이블 전략

![https://i.imgur.com/Od0aMu4.png](https://i.imgur.com/Od0aMu4.png)

- 자식 엔티티마다 각각에 필요한 컬럼이 모두 있는 테이블을 만든다.
- NAME, PRICE 컬럼들의 중복을 허용하는 전략이다.

### (1) 구현 클래스마다 테이블 전략을 사용한 예제 코드

```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Item {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private int price;

}
```

**매핑 정보 분석하기**

- @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
    - 자식 엔티티마다 테이블을 만든다.
    - 구분 컬럼이 필요없기 때문에 @DiscriminatorColumn을 선언하지 않는다.

**실행 시 DDL**

- 테이블 3개만 생성된다.

```sql
Hibernate: 
    
    create table Album (
       id bigint not null,
        name varchar(255),
        price integer not null,
        artist varchar(255),
        primary key (id)
    )
Hibernate: 
    
    create table Book (
       id bigint not null,
        name varchar(255),
        price integer not null,
        author varchar(255),
        isbn varchar(255),
        primary key (id)
    )
Hibernate: 
    
    create table Movie (
       id bigint not null,
        name varchar(255),
        price integer not null,
        actor varchar(255),
        director varchar(255),
        primary key (id)
    )
Hibernate: 
    /* insert hellojpa.Movie
        */ insert 
        into
            Movie
            (name, price, actor, director, id) 
        values
            (?, ?, ?, ?, ?)
Hibernate: 
    select
        movie0_.id as id1_2_0_,
        movie0_.name as name2_2_0_,
        movie0_.price as price3_2_0_,
        movie0_.actor as actor1_3_0_,
        movie0_.director as director2_3_0_ 
    from
        Movie movie0_ 
    where
        movie0_.id=?
```

**DB 확인하기**

![https://i.imgur.com/D3RKyyc.png](https://i.imgur.com/D3RKyyc.png)

### (2) 만약 객체를 부모 타입으로 조회한다면?

- 객체지향 프로그래밍에서는 MOVIE, ALBUM, BOOK 객체를 부모 타입(ITEM)으로도 조회할 수 있다.

**JpaMain**

```java
Item item = em.find(Item.class, movie.getId());
System.out.println("item = " + item);
```

**실행 시 DDL**

- union all로 전체 하위 테이블을 다 찾는다.
- INSERT는 심플하나, SELECT는 굉장히 비효율적으로 동작한다.

```sql
Hibernate: 
    select
        item0_.id as id1_2_0_,
        item0_.name as name2_2_0_,
        item0_.price as price3_2_0_,
        item0_.artist as artist1_0_0_,
        item0_.author as author1_1_0_,
        item0_.isbn as isbn2_1_0_,
        item0_.actor as actor1_3_0_,
        item0_.director as director2_3_0_,
        item0_.clazz_ as clazz_0_ 
    from
        ( select
            id,
            name,
            price,
            artist,
            null as author,
            null as isbn,
            null as actor,
            null as director,
            1 as clazz_ 
        from
            Album 
        union
        all select
            id,
            name,
            price,
            null as artist,
            author,
            isbn,
            null as actor,
            null as director,
            2 as clazz_ 
        from
            Book 
        union
        all select
            id,
            name,
            price,
            null as artist,
            null as author,
            null as isbn,
            actor,
            director,
            3 as clazz_ 
        from
            Movie 
    ) item0_ 
where
    item0_.id=?
```

### (3) 정리

이 전략은 데이터베이스 설계자와 ORM 전문가 둘 다 추천하지 않는다.

- 장점
    - 서브 타입을 명확하게 구분해서 처리할 때 효과적
    - not null 제약 조건 사용 가능
- 단점
    - 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL 필요)
    - 자식 테이블을 통합해서 쿼리하기 어려움

# 2. @MappedSuperclass

- 상속 관계 매핑이 아니다.
- 부모 클래스는 테이블과 매핑하지 않고, 자식 클래스에게만 매핑 정보를 제공하고 싶을 때 사용한다.
- 비유하자면 추상 클래스와 비슷한데 @Entity는 실제 테이블과 매핑되지만 @MappedSuperclass는 실제 테이블과 매핑되지 않는다.
- 조회, 검색 불가(em.find(BaseEntity) 불가)하고, 단순히 매핑 정보를 상속할 목적으로만 사용한다.

![https://i.imgur.com/A3eXUxG.png](https://i.imgur.com/A3eXUxG.png)

- 객체 입장에서 id, name 컬럼처럼 공통 매핑 정보가 필요할 때 사용한다.

## 1) @MappedSuperclass 매핑 예제 코드

테이블은 그대로 두고 객체 모델의 id, name 두 공통 속성을 부모 클래스로 모으고 객체 상속 관계로 만들어보자.

```java
@Getter @Setter
**@MappedSuperclass**
public **abstract** class BaseEntity {

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}

-------------------------------------------
@Entity
public class Member extends BaseEntity{
		...
}

-------------------------------------------
@Entity
public class Team extends BaseEntity{
		...
}
```

- BaseEntity에 객체들의 공통 매핑 정보를 정의하고, 자식 엔티티들은 상속을 통해 BaseEntity의 매핑 정보를 물려받는다.
- BaseEntity는 테이블과 매핑할 필요가 없고 자식 엔티티에게 공통으로 사용되는 매핑 정보만 제공하면 되므로 @MappedSuperclass를 사용했다.

**JpaMain**

```java
Member member = new Member();
member.setUsername("user1");
member.setCreatedBy("kim");
member.setCreatedDate(LocalDateTime.now());

em.persist(member);

em.flush();
em.clear();

tx.commit();
```

**실행 시 DDL**

```sql
Hibernate: 
    
    create table Member (
       MEMBER_ID bigint not null,
        createdBy varchar(255),
        createdDate timestamp,
        lastModifiedBy varchar(255),
        lastModifiedDate timestamp,
        USERNAME varchar(255),
        LOCKER_ID bigint,
        TEAM_ID bigint,
        primary key (MEMBER_ID)
    )
Hibernate: 
    
    create table Team (
       TEAM_ID bigint not null,
        createdBy varchar(255),
        createdDate timestamp,
        lastModifiedBy varchar(255),
        lastModifiedDate timestamp,
        name varchar(255),
        primary key (TEAM_ID)
    )
```


### 참고

- [김영한님의 자바 ORM 표준 JPA 프로그래밍 - 기본편 강의](https://www.inflearn.com/course/ORM-JPA-Basic/dashboard)
- 김영한님의 자바 ORM 표준 JPA 프로그래밍 책