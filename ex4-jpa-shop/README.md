# 20. 고급 매핑 : 실전 예제 4 - 상속관계 매핑

# 1. 실전 예제

다음 요구사항이 추가되었다.

- 상품의 종류는 음반, 도서, 영화가 있고 이후 더 확장될 수 있다.
- 모든 데이터는 등록일과 수정일이 필수다.

![https://i.imgur.com/ASoJu74.png](https://i.imgur.com/ASoJu74.png)

상품 모델을 상속 관계로 만들었다.

## 1) 테이블 설계

![https://i.imgur.com/tnzpnZF.png](https://i.imgur.com/tnzpnZF.png)

## 2) 상속 관계 매핑

상품 클래스는 직접 생성해서 사용하지 않으므로 abstract를 추가해서 추상 클래스로 만들었다.

**부모 엔티티 매핑**

```java
@Getter @Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "ITEM_ID")
    private Long id;

    private String name;        //이름
    private int price;          //가격
    private int stockQuantity;  //재고수량

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

}
```

- 상속 관계를 매핑하기 위해 @Inheritance을 사용하고 strategy 속성에 InheritanceType.SINGLE_TABLE을 선택하여 단일 테이블 전략을 선택했다.
- 단일 테이블 전략은 구분 컬럼을 필수로 사용해야 하기 때문에 @DiscriminatorColumn을 사용했다.

**자식 엔티티 매핑**

```java
@Getter @Setter
@Entity
public class Album extends Item{

    private String artist;
    private String etc;
}

---------------------------------
@Getter @Setter
@Entity
public class Book extends Item{

    private String author;
    private String isbn;
}

---------------------------------
@Getter @Setter
@Entity
public class Movie extends Item {

    private String director;
    private String actor;
}
```

**실행 시 DDL**

```sql
Hibernate: 
    
    create table Item (
       DTYPE varchar(31) not null,
        ITEM_ID bigint not null,
        name varchar(255),
        price integer not null,
        stockQuantity integer not null,
        actor varchar(255),
        director varchar(255),
        author varchar(255),
        isbn varchar(255),
        artist varchar(255),
        etc varchar(255),
        primary key (ITEM_ID)
    )
```

데이터를 INSERT 해보자.

**JpaMain**

```java
Book book = new Book();
book.setName("JPA");
book.setAuthor("김영한");

em.persist(book);

tx.commit();//트랜잭션 커밋
```

**실행 시 DDL**

```sql
Hibernate: 
    /* insert jpabook.jpashop.domain.Book
        */ insert 
        into
            Item
            (name, price, stockQuantity, author, isbn, DTYPE, ITEM_ID) 
        values
            (?, ?, ?, ?, ?, 'Book', ?)
```

**DB 확인하기**

![https://i.imgur.com/GQSVmfm.png](https://i.imgur.com/GQSVmfm.png)

## 3) @MappedSuperclass 매핑

두 번째 요구사항을 만족하려면 모든 테이블에 등록일과 수정일 컬럼을 추가해야 하는데, @MappedSuperclass를 사용하여 부모 클래스를 상속받는 것이 효과적이다.

**부모 엔티티 매핑**

```java
@Getter @Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "ITEM_ID")
    private Long id;

    private String name;        //이름
    private int price;          //가격
    private int stockQuantity;  //재고수량

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

}
```

**매핑 정보 상속**

```java
@Entity
public class Member extends BaseEntity {
		...
}

@Entity
@Table(name = "ORDERS")
public class Order extends BaseEntity {
		...
}
```

**실행 시 DDL**

```sql
Hibernate: 
    
    create table Member (
       MEMBER_ID bigint not null,
        **createdDate timestamp,
        lastModifiedDate timestamp,**
        city varchar(255),
        name varchar(255),
        street varchar(255),
        zipcode varchar(255),
        primary key (MEMBER_ID)
    )
```

상속받은 매핑 정보가 추가되어 있다. 나머지 테이블은 생략했다.



### 참고

- [김영한님의 자바 ORM 표준 JPA 프로그래밍 - 기본편 강의](https://www.inflearn.com/course/ORM-JPA-Basic/dashboard)
- 김영한님의 자바 ORM 표준 JPA 프로그래밍 책