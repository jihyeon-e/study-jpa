# 10. 연관관계 매핑 기초 : 단방향 연관관계

### 목표

- 객체와 테이블 연관관계의 차이를 이해
- 객체의 참조와 테이블의 외래 키를 매핑
- 용어이해
    - **방향**(Direction) : 단방향, 양방향
    - **다중성**(Multiplicity) : 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M) 이해
    - **연관관계의 주인**(Owner) : 객체 양방향 연관관계는 관리 주인이 필요

# 1. 단방향 연관관계

회원과 팀의 관계를 통해 다대일 단방향 관계를 알아보자.

**예제 시나리오**

- 회원과 팀이 있다.
- 회원은 하나의 팀에만 소속될 수 있다.
- 회원과 팀은 다대일 관계다.

## 1) **객체를 테이블에 맞추어 모델링**

### (1) **연관관계가 없는 객체**

![https://i.imgur.com/8BN3sM8.png](https://i.imgur.com/8BN3sM8.png)

여러 명의 회원이 하나의 팀에 소속될 수 있고, 반대로 하나의 팀에 여러 명의 회원이 소속될 수 있다.

### (2) 참조 대신에 외래 키를 그대로 사용

Member

```java
@Entity
    public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "TEAM_ID")
    private Long teamId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}
```

Team

```java
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

### (3) 외래 키 식별자를 직접 다룸

JpaMain

```java
public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeamId(team.getId());
            em.persist(member);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
```

데이터베이스 확인

![https://i.imgur.com/9AWOh3v.png](https://i.imgur.com/9AWOh3v.png)

### (4) 식별자로 다시 조회

객체 지향적인 방법은 아니다.

JpaMain

```java
//조회
Member findMember = em.find(Member.class, member.getId());
//연관관계가 없음
Team findTeam = em.find(Team.class, team.getId());
```

### ⇒ 객체를 테이블에 맞추어 데이터 중심으로 모델링하면, 협력 관계를 만들 수 없다.

- 테이블은 외래 키로 조인을 사용해서 연관된 테이블을 찾는다.
- 객체는 참조를 사용해서 연관된 객체를 찾는다.
- 테이블과 객체 사이에는 이런 큰 간격이 있다.

## 2) 단방향 연관관계 - 객체 지향 모델링

### (1) 객체 연관관계 사용

![https://i.imgur.com/bzBCNZW.png](https://i.imgur.com/bzBCNZW.png)

### (2) 객체의 참조와 테이블의 외래 키를 매핑

Member

```java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

//    @Column(name = "TEAM_ID")
//    private Long teamId;

    private Team team;

}
```

Team 객체를 필드로 생성하면 에러가 난다. → Member, Team의 연관관계를 명시해주어야 한다.

```java
@ManyToOne
@JoinColumn(name = "TEAM_ID")
private Team team;
```

- @ManyToOne : 관계가 무엇인지
    - Team 객체가 다대일 관계에서 일이기 때문에 @ManyToOne을 사용한다.
- @JoinColumn(name = "TEAM_ID") : 조인할 컬럼
    - Member 테이블의 foreign key와 매핑해야 하기 때문에 @JoinColumn(name = "TEAM_ID")을 사용한다.

### (3) ORM 매핑

![https://i.imgur.com/Grnm82H.png](https://i.imgur.com/Grnm82H.png)

Team과 "TEAM_ID" foreign key를 매핑했다.

### (4) 연관관계 저장, 참조로 연관관계 조회 - 객체 그래프 탐색

JpaMain

```java
//저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeam(team); //단방향 연관관계 설정, 참조 저장
em.persist(member);

//조회
Member findMember = em.find(Member.class, member.getId());

Team findTeam = findMember.getTeam();
System.out.println("findTeam = " + findTeam.getName());

tx.commit();
```

실행한 결과

![https://i.imgur.com/a5RtjPH.png](https://i.imgur.com/a5RtjPH.png)

### (5) 연관관계 수정

JpaMain

```java
//수정
Team newTeam = em.find(Team.class, 100L);
findMember.setTeam(newTeam);
```



### 참고

- 김영한님의 자바 ORM 표준 JPA 프로그래밍 - 기본편 강의
- 김영한님의 자바 ORM 표준 JPA 프로그래밍 책