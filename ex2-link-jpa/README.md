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

# 11. 연관관계 매핑 기초 : 양방향 연관관계와 연관관계의 주인 1- 기본

객체는 참조, 테이블은 외래키로 조인

# 1. 양방향 연관관계

![https://i.imgur.com/tN9SpSr.png](https://i.imgur.com/tN9SpSr.png)

- 테이블의 연관관계는 외래 키를 넣으면 양쪽으로 서로의 연관관계를 다 알 수 있다.
- 문제는 객체이다.
  - 이전에 단방향 연관관계일 때는 Member에서 Team으로는 갈 수 있었지만, Team에서 Member로는 갈 수 없었다. Member 객체에는 Team 객체를 넣어주고, Team 객체에 members라는 List를 넣어주어야 양쪽으로 다 접근할 수 있는 것이다.
- Member → Team : 다대일  @ManyToOne 사용
- Team → Member : 일대다  @OneToMany 사용

## 1) 양방향 연관관계 매핑

양방향 관계를 매핑해보자.

### (1) Member 엔티티는 단방향과 동일

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
}
```

### (2) Team 엔티티는 컬렉션 추가

```java
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;
    private String name;

		@OneToMany(mappedBy = "team")
		private List<Member> members = new ArrayList<Member>();
}
```

팀과 회원은 일대다 관계이기 때문에 팀 엔티티에 컬렉션인 List<Member> members를 추가했다.

또한 일대다 관계를 매핑하기 위해 @OneToMany 매핑 정보를 사용했다.

mappedBy 속성은 양방향 매핑일 때 사용하는데, 반대쪽 매핑의 필드 변수를 값으로 준다.

### (3) 일대다 컬렉션 조회

```java
//저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeam(team); //단방향 연관관계 설정, 참조 저장
em.persist(member);

em.flush();
em.clear();

//조회
Member findMember = em.find(Member.class, member.getId());
List<Member> members = findMember.getTeam().getMembers();

for(Member m : members) {
    System.out.println("m = " + m.getUsername());
}

tx.commit();
```

Team 객체를 생성한 뒤 setName으로 "TeamA"를 넣고, 영속성 컨텍스트에 team 객체를 올린다.

Member도 마찬가지이다.

em.flush() 를 통해 DB에 쿼리를 날린다. Team 테이블과 Member 테이블에 id 가 자동으로 생성된다.

em.clear() 를 통해 영속성 컨텍스트를 완전히 초기화시킨다.

member.getId()의 id값을 가지고 있는 Member 객체를 조회한다.

조회한 Member 객체의 팀의 members를 조회한다.

**실행한 결과**

![https://i.imgur.com/Gg8r4N9.png](https://i.imgur.com/Gg8r4N9.png)

### (4) 반대 방향으로 객체 그래프 탐색

```java
//조회
Team findTeam = em.find(Team.class, team.getId());

int memberSize = findTeam.getMembers().size(); //역방향 조회
```

# 2. 연관관계의 주인

객체와 테이블간에 연관관계를 맺는 차이를 이해해야 한다.

## 1) 객체와 테이블이 관계를 맺는 차이

- 객체 연관관계 : 2개
  - 회원 → 팀 연관관계 1개 (단방향)
  - 팀 → 회원 연관관계 1개 (단방향)
  - 각각의 객체에 참조값을 하나씩 넣어야 연관관계를 맺을 수 있다.
- 테이블 연관관계 : 1개
  - 회원 ↔  팀의 연관관계 1개 (양방향)
  - foreign key 값 하나만 있으면 양쪽으로 연관관계를 맺을 수 있다.

![https://i.imgur.com/GgjhcEv.png](https://i.imgur.com/GgjhcEv.png)

### (1) 객체의 양방향 관계

- 객체의 양방향 관계는 사실 양방향 관계가 아니라 서로 다른 단 뱡향 관계 2개다.
- 객체를 양방향으로 참조하려면 단방향 연관관계를 2개 만들어 야 한다.

### (2) 테이블의 양방향 관계

- 테이블은 외래 키 하나로 두 테이블의 연관관계를 관리
- MEMBER.TEAM_ID 외래 키 하나로 양방향 연관관계 가짐

```sql
SELECT *
FROM MEMBER M
JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID

SELECT *
FROM TEAM T
JOIN MEMBER M ON T.TEAM_ID = M.TEAM_ID
```

양쪽으로 조인할 수 있다.

만약 Member를 바꾸고 싶거나, 새로운 Team에 들어가고 싶을 때, Member의 team 값을 바꿔야 할까 Team의 members의 값을 바꿔야 할까?

## 2) 연관관계의 주인

![https://i.imgur.com/lyF031R.png](https://i.imgur.com/lyF031R.png)

엔티티를 양방향 연관관계로 설정하면 객체의 참조는 둘인데 외래 키는 하나라는 차이가 발생한다. 따라서 JPA에서는 두 객체 연관관계 중 하나를 정해서 테이블의 외래 키를 관리해야 하는데 이것을 연관관계의 주인이라 한다.

### (1) 양방향 매핑 규칙

- 객체의 두 관계 중 하나를 연관관계의 주인으로 지정해야 한다.
- **연관관계의 주인만이 외래 키를 관리(등록, 수정)**
- 주인이 아닌 쪽은 읽기만 가능
- 주인은 mappedBy 속성 사용X
- 주인이 아니면 mappedBy 속성으로 주인 지정

### (2) 연관관계의 주인은 외래 키가 있는 곳

그렇다면 Member.team 과 Team.members 둘 중 어떤 것을 연관관계의 주인으로 정해야 할까?

![https://i.imgur.com/Hm02L8W.png](https://i.imgur.com/Hm02L8W.png)

- 연관관계의 주인을 정한다는 것은 사실 외래 키 관리자를 선택하는 것이다.
- 외래 키가 있는 곳을 주인으로 정해라.
  - 일대다 관계에서 다(N) 쪽이 연관관계의 주인이다.
  - 만약 Team을 주인으로 정했을 때, Team의 members를 바꾸면 MEMBER 테이블에 UPDATE 쿼리가 날라가기 때문에 헷갈릴 가능성이 높다.
- 여기서는 Member.team이 연관관계의 주인이다.
- 데이터베이스 테이블의 다대일, 일대다 관계에서는 항상 다 쪽이 외래 키를 가진다. 다 쪽인 @ManyToOne은 항상 연관관계의 주인이 되므로 mappedBy를 설정할 수 없다.


# 12. 연관관계 매핑 기초 : 양방향 연관관계와 연관관계의 주인 2 - 주의점, 정리

# 1. 양방향 연관관계의 주의점

## 1) 양방향 매핑시 가장 많이 하는 실수

가장 많이 하는 실수는 연관관계의 주인에는 값을 입력하지 않고, 주인이 아닌 곳에만 값을 입력하는 것이다. 데이터베이스에 외래 키 값이 정상적으로 저장되지 않으면 이것부터 의심해보자.

### (1) 연관관계의 주인에 값을 입력하지 않았을 때

```java
Member member = new Member();
member.setUsername("member1");
em.persist(member);

Team team = new Team();
team.setName("TeamA");
//역방향(주인이 아닌 방향)만 연관관계 설정
team.getMembers().add(member);
em.persist(team);

em.flush();
em.clear();

tx.commit();
```

연관관계의 주인인 Member 객체의 team 컬럼에 값을 입력하지 않았기 때문에 데이터베이스에 null 값이 들어간다.

![https://i.imgur.com/A2TR33I.png](https://i.imgur.com/A2TR33I.png)

### (2) 연관관계의 주인에 값을 입력했을 때

연관관계의 주인에 값을 입력하면 데이터베이스에 잘 들어간다.

```java
Team team = new Team();
team.setName("TeamA");
//team.getMembers().add(member);
em.persist(team);

Member member = new Member();
member.setUsername("member1");
//연관관계의 주인에 값 설정
member.setTeam(team);
em.persist(member);

em.flush();
em.clear();

tx.commit();
```

![https://i.imgur.com/YBezv0L.png](https://i.imgur.com/YBezv0L.png)

## 2) 순수한 객체까지 고려한 양방향 연관관계

연관관계의 주인에만 값을 저장하고 주인이 아닌 곳에는 값을 저장하지 않아도 될까?

예제를 통해 알아보자.

```java
Team team = new Team();
team.setName("TeamA");
System.out.println("TeamId = " + team.getId());
System.out.println("========");
em.persist(team);
System.out.println("========");
System.out.println("TeamId = " + team.getId());

Member member = new Member();
member.setUsername("member1");
member.setTeam(team);
em.persist(member);

//team.getMembers().add(member);

//em.flush();
//em.clear();

Team findTeam = em.find(Team.class, team.getId());
List<Member> members = findTeam.getMembers();

System.out.println("========");
for (Member m : members) {
    System.out.println("m = " + m.getUsername());
}
System.out.println("========");

tx.commit();
```

코드를 보면 Member.team에만 연관관계를 설정하고 반대 방향은 연관관계를 설정하지 않았기 때문에 m.getUsername()의 값이 출력되지 않는다.

![https://i.imgur.com/W5Hr7NX.png](https://i.imgur.com/W5Hr7NX.png)

- 사실은 **객체 관점에서 양쪽 방향에 모두 값을 입력해주는 것이 가장 안전하다.**

```java
Team team = new Team();
team.setName("TeamA");
System.out.println("TeamId = " + team.getId());
System.out.println("========");
em.persist(team);
System.out.println("========");
System.out.println("TeamId = " + team.getId());

Member member = new Member();
member.setUsername("member1");
member.setTeam(team);
em.persist(member);

team.getMembers().add(member);

//em.flush();
//em.clear();

Team findTeam = em.find(Team.class, team.getId());
List<Member> members = findTeam.getMembers();

System.out.println("========");
for (Member m : members) {
    System.out.println("m = " + m.getUsername());
}
System.out.println("========");

tx.commit();
```

Team 객체를 생성하여 setName()로 "TeamA"를 넣어주고, persist()로 영속성 컨텍스트에 올리면 Team id의 값을 가지고 올 수 있다.

양쪽에 연관관계를 설정해줬기 때문에 m.getUsername()의 값이 출력된다.

**실행한 결과**

![https://i.imgur.com/SaHhaeI.png](https://i.imgur.com/SaHhaeI.png)



- 따라서 양방향 연관관계는 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정해야 한다.
  - 연관관계 편의 메소드를 생성하자

    ```java
    @Entity
    public class Member {

        public void setTeam(Team team) {
            this.team = team;
            team.getMembers().add(this);
        }
    }
    ```

  Member에 Team을 세팅하는 시점에 Team에도 member를 세팅하므로 실수를 줄여준다.

    ```java
    @Entity
    public class Member {

        public void changeTeam(Team team) {
            this.team = team;
            team.getMembers().add(this);
        }
    }
    ```

  개인적인 취향으로, 연관관계 편의 메소드나 JPA 상태를 변경할 때는 set()보다 메소드의 이름을 바꿔서 사용한다.

- 양방향 매핑 시에 무한 루프를 조심해야 한다.
  - 예: toString(), lombok, JSON 생성 라이브러리

    Member

      ```java
      @Override
      public String toString() {
          return "Member{" +
                  "id=" + id +
                  ", username='" + username + '\'' +
                  ", team=" + team +
                  '}';
      }
      ```

    Team

      ```java
      @Override
          public String toString() {
              return "Team{" +
                      "id=" + id +
                      ", name='" + name + '\'' +
                      ", members=" + members +
                      '}';
          }
      ```

    계속 서로를 호출하기 때문에 무한 루프에 걸린다.

[예제소스](https://github.com/jihyeon-e/study-jpa/tree/master/ex2-link-jpa)

# 2. 정리

- 단방향 매핑만으로도 이미 연관관계 매핑은 완료된 것이다.
- 결국 단방향과 비교해서 양방향의 장점은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐이다.
- JPQL에서 역방향으로 탐색할 일이 많다.
- 단방향 매핑을 잘하고 양방향은 필요할 때 추가해도 된다.(테이블에 영향을 주지 않음)
- 연관관계의 주인을 정하는 기준
  - 연관관계의 주인은 외래 키의 위치를 기준으로 정해야 한다.



### 참고

- [김영한님의 자바 ORM 표준 JPA 프로그래밍 - 기본편 강의](https://www.inflearn.com/course/ORM-JPA-Basic/dashboard)
- 김영한님의 자바 ORM 표준 JPA 프로그래밍 책