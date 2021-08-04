# 14. 다양한 연관관계 매핑

# 1. 연관관계 매핑 시 고려사항 3가지

## 1) 다중성

보통 다대일과 일대다 관계를 가장 많이 사용하고 다대다 관계는 실무에서 거의 사용하지 않는다.

- 다대일(@ManyToOne)
- 일대다(@OneToMany)
- 일대일(@OneToOne)
- 다대다(@ManyToMany)

## 2) 단방향, 양방향

- 테이블
    - 외래 키 하나로 양쪽 조인 가능
    - 사실상 방향이라는 개념이 없음
- 객체
    - 참조용 필드가 있는 쪽으로만 참조 가능
    - 한쪽만 참조하면 단방향 관계
    - 양쪽이 서로 참조하면 양방향 관계(사실은 단방향이 두 개인 것)

## 3) 연관관계 주인

- 테이블은 외래 키 하나로 두 테이블이 연관관계를 맺음
- 객체 양방향 관계는 A->B, B->A 처럼 참조가 2군데
- 객체 양방향 관계는 참조가 2군데 있음. 둘중 테이블의 외래 키를 관리할 곳을 지정해야함
- 연관관계의 주인 : 외래 키를 관리하는 참조
- 주인의 반대편 : 외래 키에 영향을 주지 않음, 단순 조회만 가능

# 2. 다대일

## 1) 다대일 단방향 [N:1]

![https://i.imgur.com/MbFZQm8.png](https://i.imgur.com/MbFZQm8.png)

- 가장 많이 사용하는 연관관계
- 다대일의 반대는 일대다
- 관계형 DB에서는 다 쪽에 항상 외래 키가 있어야 함

```java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

		@ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    //getter, setter

}
```

```java
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;
    private String name;

		//getter, setter
}
```

회원은 Member.team으로 팀 엔티티를 팀조할 수 있지만, 반대로 팀에서는 회원을 참조할 필드가 없다. → 회원과 팀은 다대일 단방향 연관관계다.

```java
@ManyToOne
@JoinColumn(name = "TEAM_ID")
private Team team;
```

@JoinColumn(name = "TEAM_ID") 를 사용해서 Member.team 필드를 TEAM_ID 외래 키와 매핑했다. 따라서 Member.team 필드로 회원 테이블의 TEAM_ID 외래키를 관리한다.

## 2) 다대일 양방향 [N:1, 1:N]

![https://i.imgur.com/K1hlprU.png](https://i.imgur.com/K1hlprU.png)

- 회원에서 팀을 참조할 수도 있고 팀에서 회원을 참조할 수 있는 관계
- 외래 키가 있는 쪽이 연관관계의 주인이다.
    - 일대다와 다대일 연관관계는 항상 다에 외래 키가 있다.
    - 실선(Member.team)이 연관관계의 주인이고, 점선(Team.members)은 연관관계의 주인이 아니다.
- 양쪽을 서로 참조하도록 개발한다.
    - 항상 서로 참조하게 하려면 연관관계 편의 메소드를 작성하는 것이 좋은데 회원의 setTeam(), 팀의 addMember() 메소드가 편의 메소드들이다. 편의 메소드는 양쪽 다 작성하면 무한루프에 빠지므로 주의해야 한다.

```java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

		@ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    //getter, setter

}
```

```java
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;
    private String name;

		@OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<Member>();

		//getter, setter
}
```

- **@OneToMany**
    - 연관관계의 주인이 아니다.
    - 어디에 매핑 됐는지에 관한 정보 (mappedBy = "team")을 넣는다.
    - (mappedBy="team")이란 Team의 members 필드가 Member 엔티티의 team 필드에 의해 매핑되어졌다는 것을 의미한다.

# 3. 일대다

- 다대일 관계의 반대 방향이다.
- 일대다(1:N)에서 일(1)이 연관관계의 주인이 되는 관계이다.
- 일 방향에서 외래키를 관리하겠다는 의미가 된다.
- 엔티티를 하나 이상 참조할 수 있으므로 자바 컬렉션인 Collection, List, Set, Map 중에 하나를 사용해야 한다.

## 1) 일대다 단방향 [1:N]

![https://i.imgur.com/RIhy4od.png](https://i.imgur.com/RIhy4od.png)

- 팀과 멤버가 일대다 관계이다.
- 팀은 회원들을 참조하지만 반대로 회원은 팀을 참조하지 않으면 둘의 관계는 단방향이다.
- 테이블은 무조건 일대다의 다(N)쪽에 외래키가 있다.
- 객체와 테이블 차이 때문에 반대쪽 테이블의 외래 키를 관리하는 특이한 구조이다.
    - Team에서 members가 바뀌면, DB의 Member 테이블에 업데이트 쿼리가 나간다.
- @JoinColumn을 꼭 사용해야 한다. 그렇지 않으면 조인 테이블 방식을 사용한다.(중간에 테이블을 하나 추가함)

  ![https://i.imgur.com/G5iZvR6.png](https://i.imgur.com/G5iZvR6.png)

    - Team_Member 라는 중간 테이블이 하나 더 생겨서 조인 테이블을 사용해야 한다.

```java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    //getter, setter

}
```

```java
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;
    private String name;

    @OneToMany
    @JoinColumn(name = "TEAM_ID")
    private List<Member> members = new ArrayList<Member>();

		//getter, setter
}
```

JpaMain

```java
Member member = new Member();
member.setUsername("member1");

em.persist(member);

Team team = new Team();
team.setName("teamA");
team.getMembers().add(member);

em.persist(team);

tx.commit();
```

실행한 결과

![https://i.imgur.com/ZyJ4gas.png](https://i.imgur.com/ZyJ4gas.png)

Member 테이블에 UPDATE 쿼리가 추가로 나간다.

- 일대다 단반향 매핑의 단점
    - 매핑한 객체가 관리하는 외래 키가 다른 테이블에 있다.
    - 연관관계 처리를 위해 추가로 UPDATE 쿼리를 추가로 실행해야 한다.
    - 표준스펙에서 지원하지만 실무에서는 권장하지 않는다.
- 일대다 단방향 매핑보다는 다대일 양방향 매핑을 사용하자.
    - 다대일 양방향 매핑은 관리해야 하는 외래 키가 본인 테이블에 있기 때문에 일대다 단방향 매핑 같은 문제가 발생하지 않는다.

## 2) 일대다 양방향 [1:N, N:1]

- 이런 매핑은 공식적으로는 존재하지 않는다.

```java
@Entity
public class Member {

		@Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

		@ManyToOne
		@JoinColumn(name = "TEAM_ID", insertable = false, updatable = false)
		private Team team;
	 
		//setter, getter
}
```

- @JoinColumn(name = "team_id", insertable = false, updatable = false)를 사용한다.
- @ManyToOne과 @JoinColumn을 사용해서 연관관계를 매핑하면, 다대일 단방향 매핑이 되어버린다. 근데 반대쪽 Team에서 이미 일대다 단방향 매핑이 설정되어있다. 이런 상황에서는 두 엔티티 모두 테이블의 외래 키를 관리 하게 되는 상황이 벌어진다.
- 그걸 막기 위해 insertable, updatable 설정을 false로 하면 읽기 전용 필드가 되어 양방향 매핑처럼 사용된다.
- 하지만 결론은 다대일 양방향을 사용하자. 테이블이 수십 개 있는데, 매핑이나 설계가 단순해야 팀원 누구나 사용할 수 있기 때문이다.

# 4. 일대일

- 일대일 관계는 그 반대도 일대일이다.
- 특이하게 주 테이블이나 대상 테이블 중에 외래 키를 넣을 테이블을 선택 가능하다.
    - 주 테이블에 외래 키 저장
    - 대상 테이블에 외래 키 저장
- 외래 키에 데이터베이스 유니크(UNI) 제약조건 추가되어야 일대일 관계가 된다.

## 1) 주 테이블에 외래 키

### (1) 단방향

![https://i.imgur.com/bs352Ri.png](https://i.imgur.com/bs352Ri.png)

- 회원은 딱 하나의 사물함을 가지고, 반대로 사물함도 회원 한 명만 할당 받는 비즈니스 룰이 있을 때, 둘의 관계는 일대일 관계이다.
- Member를 주 테이블로, Locker를 대상 테이블로 봤을 때 Member.locker 필드와 외래 키에 유니크 제약 조건을 추가하여 연관관계 매핑을 할 수 있다.
- 다대일(@ManyToOne) 단방향 관계 매핑과 어노테이션만 달라지고, 거의 유사하다.

### (2) 양방향

![https://i.imgur.com/pc3GYp0.png](https://i.imgur.com/pc3GYp0.png)

- 다대일 양방향 매핑처럼 **외래키가 있는 곳이 연관관계의 주인**이다.

```java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    private Locker locker;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID", insertable = false, updatable = false)
    private Team team;
		
		//setter, getter
}
```

- @OneToOne 을 사용하여 일대일 단방향 관계를 매핑하고, @JoinColumn을 넣어준다
- 지금까지는 단방향 관계를 매핑한 것이다.

```java
@Entity
public class Locker {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToOne(mappedBy = "locker")
    private Member member;

}
```

- 반대편에 mappedBy를 적용시켜주면 일대일 양방향 관계 매핑이 된다.
- (mappedBy = "locker") 는 Member.locker 필드와 매핑되었다는 것을 의미한다.

## 2) 대상 테이블에 외래 키

### (1) 단방향

- JPA에서 지원하지 않는 관계이다.

### (2) 양방향

![https://i.imgur.com/JxoZqa4.png](https://i.imgur.com/JxoZqa4.png)

- 사실 일대일 주 테이블에 외래 키 양방향과 매핑 방법과 같다.
- 주 엔티티인 Member 엔티티 대신에 대상 엔티티인 Locker를 연관관계의 주인으로 만들어서 LOCKER 테이블의 외래 키를 관리하도록 했다.

## 3) 정리

### (1) 주 테이블에 외래 키

- 주 객체가 대상 객체의 참조를 가지는 것 처럼 주 테이블에 외래 키를 두고 대상 테이블을 찾는 방식
- 객체지향 개발자들이 선호하고, JPA 매핑이 편리하다.
- 장점 : 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인이 가능하다.
- 단점 : 값이 없으면 외래 키에 NULL을 허용해야 한다. DB입장에서는 치명적이다.

### (2) 대상 테이블에 외래키

- 대상 테이블에 외래 키가 존재한다.
- 전통적인 데이터베이스 개발자들이 선호하는 방식이다. NULL을 허용해야 하는 문제도 없다.
- 장점 : 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조를 유지할 수 있다.
- 단점
    - 주로 멤버 엔티티에서 락커 엔티티를 많이 접근하는데, 어쩔 수 없이 양방향 매핑을 해야 한다.
    - JPA가 제공하는 기본 프록시 기능의 한계로 **지연 로딩으로 설정해도 항상 즉시 로딩 된다.**
        - JPA 입장에서 일대일 관계의 주 테이블에 외래 키를 저장하는 상황에서는, 멤버 객체를 로딩할 때 멤버 테이블의 외래 키에 값이 있는지 없는지만 판단하면 된다. 있으면 프록시 객체를 넣어주고, 없으면 null을 넣으면 된다. 나중에 진짜 락커 필드에 접근할 때, 그때 쿼리가 나간다.
        - 그런데 대상 테이블에 외래 키를 저장한다면, JPA가 Member.locker 를 조회하는 상황에서 Member 테이블만 조회해서는 모른다. 어차피 Locker 테이블을 찾아서 MEMBER_ID가 있는지 확인해야(쿼리가 나가야) 알 수 있다. 어차피 쿼리가 나간단 이야기는 프록시를 만들 필요가 없다는 이야기이다. 그래서 하이버네이트 구현체 같은 경우는 지연 로딩으로 세팅해도 항상 즉시 로딩 된다.

# 5. 다대다 [N:N]

- 관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없다.
- 연결 테이블(조인 테이블)을 추가해서 일대다, 다대일 관계로 풀어내야 한다.

![https://i.imgur.com/yG0Eo1E.png](https://i.imgur.com/yG0Eo1E.png)

- **객체는 컬렉션을 사용해서 객체 2개로 다대다 관계 가능**하다.

![https://i.imgur.com/zYotjHw.png](https://i.imgur.com/zYotjHw.png)

- @ManyToMany 어노테이션을 사용
- @JoinTable로 연결 테이블을 지정
- 다대다 매핑: 단방향, 양방향 가능

## 1) 단방향

```java
@Entity
public class Member {

		...

    @ManyToMany
    @JoinTable(name = "MEMBER_PRODUCT")
    private List<Product> products = new ArrayList<>();

		//setter, getter

}
```

```java
@Entity
public class Product {

    @Id @GeneratedValue
    private Long id;

    private String name;

		//setter, getter

}
```

실행한 결과

```sql
Hibernate: 
    
    create table MEMBER_PRODUCT (
       Member_MEMBER_ID bigint not null,
        products_id bigint not null
    )

Hibernate: 
    
    alter table Member 
       add constraint FK332130jlg9s5hyeuk7gfgi052 
       foreign key (LOCKER_ID) 
       references Locker

Hibernate: 
    
    alter table Member 
       add constraint FKl7wsny760hjy6x19kqnduasbm 
       foreign key (TEAM_ID) 
       references Team
```

- 조인 테이블 하나와 외래 키 제약 조건 두 가지가 설정된다.

## 2) 양방향

```java
@Entity
public class Product {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "products")
    private List<Member> members = new ArrayList<>();

		//setter, getter
}
```

## 3) 다대다 매핑의 한계와 극복

- 한계

  ![https://i.imgur.com/cSRfmyS.png](https://i.imgur.com/cSRfmyS.png)

    - **편리해 보이지만 실무에서 사용하지 않는 걸 권장한다.**
    - 연결 테이블이 단순히 연결만 하고 끝나지 않는다.
    - 주문시간, 수량 같은 데이터가 들어올 수 있는데, 매핑 정보만 들어가고 중간 테이블에 추가 정보를 쓸 수 없다.
- 극복
    - 연결 테이블용 엔티티 추가한다.(연결 테이블을 엔티티로 승격)
    - @ManyToMany를 각각 일대다(@OneToMany), 다대일(@ManyToOne)로 관계를 매핑한다.
    - 위의 다대다 매핑의 한계 첨부 그림에서는 MemberProduct의 MEMBER_ID, PRODUCT_ID를 묶어서 PK로 썻지만, 실제로는 아래처럼 모든 테이블의 id에 @GeneratedValue를 사용하는 것을 권장한다.
    - id가 종속되어 있지 않으면 더 유연하게 개발할 수 있다. 만약 시스템을 운영하면서 비즈니스적인 제약 조건이 커지면 PK를 운영 중에 업데이트하는 상황이 발생할 수도 있다.

![https://i.imgur.com/xdiWe7m.png](https://i.imgur.com/xdiWe7m.png)

```java
@Entity
public class Member {
    ...
        
    @OneToMany(mappedBy = "member")
    private List<MemberProduct> memberProducts = new ArrayList<>();

    //setter, getter
}
```

```java
@Entity
public class Product {
    ...

    @OneToMany(mappedBy = "product")
    private List<MemberProduct> memberProducts = new ArrayList<>();
		
		//setter, getter
}
```

Member 엔티티와 Product 엔티티의 @ManyToMany을 @OneToMany로 변경한다.

```java
@Entity
public class MemberProduct {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

}
```

- 연결 테이블을 엔티티로 승격시키고, @ManyToOne로 매핑한다.(연관관계 주인)
- 여기서 추가 데이터가 들어간다면 아예 의미 있는 엔티티 이름(Order)으로 변경될 것이다.



### 참고

- [김영한님의 자바 ORM 표준 JPA 프로그래밍 - 기본편 강의](https://www.inflearn.com/course/ORM-JPA-Basic/dashboard)
- 김영한님의 자바 ORM 표준 JPA 프로그래밍 책