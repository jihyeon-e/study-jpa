# 21. 프록시와 연관관계 관리 : 프록시

- Member를 조회할 때 Team도 함께 조회해야 할까?

    ```java
    public class JpaMain {

        public static void main(String[] args) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

            EntityManager em = emf.createEntityManager();

            EntityTransaction tx = em.getTransaction();
            tx.begin();

            try {

                Member member = em.find(Member.class, 1L);

                printMember(member);

                //printMemberAndTeam(member);

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
            } finally {
                em.close();
            }
            emf.close();
        }

        private static void printMember(Member member) {
            System.out.println("member = " + member.getUsername());
        }

        private static void printMemberAndTeam(Member member) {
            String username = member.getUsername();
            System.out.println("username = " + username);

            Team team = member.getTeam();
            System.out.println("team = " + team.getName());
        }
    }
    ```

    - 실제로 필요한 비즈니스 로직에 따라 다르다.
    - 만약 Member만 필요한 비지니스 로직이라면, 항상 Team을 함께 조회하는 것은 효율적이지 않다.
    - JPA는 이런 문제를 해결하려고 지연로딩과 프록시라는 개념으로 해결한다.

# 1. 프록시

## 1) 프록시 기초

- JPA에서 제공하는 두 가지 메소드
- em.find() vs em.getReference()
    - **em.find()** : 데이터베이스를 통해서 실제 엔티티 객체 조회
    - **em.getReference()** : 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회

### (1) **em.find() 사용하여 조회하기**

**Member**

```java
@Getter @Setter
@Entity
public class Member extends BaseEntity{

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID", insertable = false, updatable = false)
    private Team team;

}
```

**JpaMain**

```java
Member member = new Member();
member.setUsername("hello");

em.persist(member);

em.flush();
em.clear();

Member findMember = em.find(Member.class, member.getId());

tx.commit();
```

em.find()을 사용하여 조회하면 아래와 같이 쿼리가 바로 나간다.

```sql
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (createdBy, createdDate, lastModifiedBy, lastModifiedDate, USERNAME, MEMBER_ID) 
        values
            (?, ?, ?, ?, ?, ?)
Hibernate: 
    select
        member0_.MEMBER_ID as MEMBER_I1_3_0_,
        member0_.createdBy as createdB2_3_0_,
        member0_.createdDate as createdD3_3_0_,
        member0_.lastModifiedBy as lastModi4_3_0_,
        member0_.lastModifiedDate as lastModi5_3_0_,
        member0_.TEAM_ID as TEAM_ID7_3_0_,
        member0_.USERNAME as USERNAME6_3_0_,
        team1_.TEAM_ID as TEAM_ID1_7_1_,
        team1_.createdBy as createdB2_7_1_,
        team1_.createdDate as createdD3_7_1_,
        team1_.lastModifiedBy as lastModi4_7_1_,
        team1_.lastModifiedDate as lastModi5_7_1_,
        team1_.name as name6_7_1_ 
    from
        Member member0_ 
    left outer join
        Team team1_ 
            on member0_.TEAM_ID=team1_.TEAM_ID 
    where
        member0_.MEMBER_ID=?
```

### (2) em.getReference() **사용하여 조회하기**

**JpaMain**

```java
Member member = new Member();
member.setUsername("hello");

em.persist(member);

em.flush();
em.clear();

Member findMember = em.getReference(Member.class, member.getId());

tx.commit();
```

실행 시 DDL

```sql
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (createdBy, createdDate, lastModifiedBy, lastModifiedDate, USERNAME, MEMBER_ID) 
        values
            (?, ?, ?, ?, ?, ?)
```

INSERT 쿼리만 나가고 SELECT 쿼리는 나가지 않는다.

em.getReference()로 멤버를 조회하면 실제로 필요한 시점에 쿼리가 나간다.

**JpaMain**

```java
Member member = new Member();
member.setUsername("hello");

em.persist(member);

em.flush();
em.clear();

Member findMember = em.getReference(Member.class, member.getId());
System.out.println("findMember = " + findMember.getClass());
//id는 member.getId()을 사용해서 파라미터로 넣었기 때문에 DB에서 조회하지 않아도 된다.
System.out.println("findMember.id = " + findMember.getId());
System.out.println("findMember.username = " + findMember.getUsername());

tx.commit();
```

**실행 시 DDL**

```sql
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (createdBy, createdDate, lastModifiedBy, lastModifiedDate, USERNAME, MEMBER_ID) 
        values
            (?, ?, ?, ?, ?, ?)
findMember = class hellojpa.Member$HibernateProxy$7kI3NbZO 
findMember.id = 1
Hibernate: 
    select
        member0_.MEMBER_ID as MEMBER_I1_3_0_,
        member0_.createdBy as createdB2_3_0_,
        member0_.createdDate as createdD3_3_0_,
        member0_.lastModifiedBy as lastModi4_3_0_,
        member0_.lastModifiedDate as lastModi5_3_0_,
        member0_.TEAM_ID as TEAM_ID7_3_0_,
        member0_.USERNAME as USERNAME6_3_0_,
        team1_.TEAM_ID as TEAM_ID1_7_1_,
        team1_.createdBy as createdB2_7_1_,
        team1_.createdDate as createdD3_7_1_,
        team1_.lastModifiedBy as lastModi4_7_1_,
        team1_.lastModifiedDate as lastModi5_7_1_,
        team1_.name as name6_7_1_ 
    from
        Member member0_ 
    left outer join
        Team team1_ 
            on member0_.TEAM_ID=team1_.TEAM_ID 
    where
        member0_.MEMBER_ID=?
findMember.username = hello
```

실행결과에서 보면 findMember.username 필드를 출력할 때, DB에서 조회가 필요하므로 그때 쿼리가 나간다.

findMember.getClass()로 객체를 확인하면 Member객체가 아니라, 하이버네이트가 내부의 라이브러리를 사용하여 가짜(프록시) 엔티티 객체를 반환한다.

![https://i.imgur.com/yYX6RAZ.png](https://i.imgur.com/yYX6RAZ.png)



### (3) 프록시 객체의 초기화

```java
Member member = em.getReference(Member.class, member.getId());
member.getName();
```

![https://i.imgur.com/5miQTTK.png](https://i.imgur.com/5miQTTK.png)

1. em.getReference()로 프록시 객체를 가져온 다음에, member.getName()을 ****호출해서 실제 데이터를 조회한다.
2. 프록시 객체에 target 값이 존재하지 않을 때, JPA가 영속성 컨텍스트에 초기화 요청을 한다.
3. 영속성 컨텍스트가 DB를 조회해서 실제 엔티티 객체를 생성한다.
4. 프록시 객체는 생성된 실제 엔티티 객체의 참조를 Member target 멤버 변수에 보관한다.
5. 프록시 객체의 target.getName()을 호출해서 결국 member.getName()을 호출한 결과를 받을 수 있다.
6. 프록시 객체에 target이 할당되고 나면, 더이상 프록시 객체의 초기화 동작은 없어도 된다.

### (4) 프록시 특징

![https://i.imgur.com/w1lVoby.png](https://i.imgur.com/w1lVoby.png)

- 하이버네이트가 내부적으로 실제 클래스를 상속받아서 만들어진다.
- 실제 클래스와 겉 모양이 같다.
- 사용하는 입장에서는 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 된다.

![https://i.imgur.com/Fk3cXmC.png](https://i.imgur.com/Fk3cXmC.png)

- 프록시 객체는 실제 객체의 참조(target)을 보관한다.
- 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드를 호출한다.
- 프록시 객체는 처음 사용할 때 한 번만 초기화 된다.
- 프록시 객체를 초기화할 때, 프록시 객체가 실제로 엔티티로 바뀌는 것이 아니다.
    - 초기화되면 프록시 객체를 통해서 실제 엔티티에 접근 가능하다.
    - 바뀌는 게 아니라 내부의 target에 값이 채워지는 것이다.
- **프록시 객체는 원본 엔티티를 상속 받는다. 프록시 객체와 원본 객체가 타입이 다르다. 타입 체크시 유의해야 한다.**
    - == 비교 대신 instanceOf를 사용해야 한다.
        - == 비교

          **JpaMain**

            ```java
            Member member1 = new Member();
            member1.setUsername("member1");
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("member2");
            em.persist(member2);

            em.flush();
            em.clear();

            Member m1 = em.find(Member.class, member1.getId());
            Member m2 = em.getReference(Member.class, member2.getId());

            System.out.println("m1 == m2 : " + (m1.getClass() == m2.getClass()));
            ```

          **실행 시**

            ```java
            m1 == m2 : false
            ```

        - instanceOf 비교

          **JpaMain**

            ```java
            public static void main(String[] args) {
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

                EntityManager em = emf.createEntityManager();

                EntityTransaction tx = em.getTransaction();
                tx.begin();

                try {
                    Member member1 = new Member();
                    member1.setUsername("member1");
                    em.persist(member1);

                    Member member2 = new Member();
                    member2.setUsername("member2");
                    em.persist(member2);

                    em.flush();
                    em.clear();

                    Member m1 = em.find(Member.class, member1.getId());
                    Member m2 = em.getReference(Member.class, member2.getId());

                    logic(m1, m2);

                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                } finally {
                    em.close();
                }
                emf.close();
            }

            private static void logic(Member m1, Member m2) {
                System.out.println("m1 == m2 : " + (m1 instanceof Member));
                System.out.println("m1 == m2 : " + (m2 instanceof Member));
            }
            ```

          **실행 시**

            ```java
            m1 == m2 : true
            m1 == m2 : true
            ```

- **영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해도 프록시가 아닌 실제 엔티티를 반환한다.**
    - JPA는 하나의 영속성 컨텍스트에서 조회하는 같은 엔티티의 동일성을 보장한다.
    - 따라서, 아래의 코드에서 두 객체는 같다. em.gerReference()로 프록시가 아닌 실제 엔티티를 반환한다.

      **JpaMain**

        ```java
        Member find = em.find(Member.class, member.getId());
        Member reference = em.getReference(Member.class, member.getId());

        System.out.println("find == reference : " + (find == reference));
        ```

      **실행 시**

        ```java
        find == reference : true
        ```

    - 둘 다 getReference() 로 가져오면?
        - 둘 다 같은 프록시 객체이다. JPA는 한 트랜잭션에서 조회하는 같은 엔티티의 동일성을 보장한다. 프록시 객체도 해당한다.

    - 먼저 getReference()로 가져오고, find()로 실제 객체를 조회하면?
        - getReference()로 가져온 것은 프록시 객체, find()로 가져온 것은 실제 객체 일까?

      **JpaMain**

        ```java
        Member reference = em.getReference(Member.class, member.getId());
        Member find = em.find(Member.class, member.getId());

        System.out.println("reference == find : " + (reference == find));
        ```

      **실행 시**

        ```java
        reference == find : true
        ```

        - 한 트랜잭션에서 조회하는 **같은 엔티티의 동일성을 보장하기 위해서** 둘 다 같은 프록시 객체를 반환한다.
        - (reference  ==  find)를 true로 반환하기 위해서 이렇게 동작한다.
        - 중요한 것은 이렇게 내부적으로 JPA가 복잡하게 다 처리해 주지만, 우리가 개발할 때는 프록시든 실제 객체든 개발에 문제가 없게 개발하는 것이 중요하다.
- 실무에서 많이 만나게 되는 문제
- **영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태의 프록시를 초기화하면 문제가 발생한다.**
- 즉, 트랜잭션의 범위 밖에서 프록시를 조회하려고 할 때
    - 하이버네이트는 org.hibernate.LazyInitializationException 예외를 발생한다.

    ```java
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setUsername("member");
            em.persist(member);

            em.flush();
            em.clear();

            Member reference = em.getReference(Member.class, member.getId());

            em.detach(reference);
            //em.close();
    				//em.clear();

            reference.getUsername();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println("e = "+ e);
        } finally {
            em.close();
        }
        emf.close();
    }
    ```

    - 영속성 컨텍스트를 종료해서 member는 준영속 상태이다.
    - reference.getUsername()을 호출하면 프록시를 초기화해야 하는데, 영속성 컨텍스트가 없으므로 실제 엔티티를 조회할 수 없다.

### (5) 프록시 확인

JPA가 제공하는 유틸리티 메소드를 사용하면 프록시 인스턴스의 초기화 여부를 확인할 수 있다. 아직 초기화되지 않은 프록시 인스턴스는 false를 반환하고, 이미 초기화되었거나 프록시 인스턴스가 아니면 true를 반환한다.

- 프록시 인스턴스의 초기화 여부 확인
    - emf.getPersistenceUnitUtil().isLoaded(Object entity);

    ```java
    Member reference = em.getReference(Member.class, member.getId());
    System.out.println("isLoaded = " + emf.getPersistenceUnitUtil().isLoaded(reference));

    //isLoaded = false 반환

    ----------------------------------------------------------------
    Member reference = em.getReference(Member.class, member.getId());
    reference.getUsername();
    System.out.println("isLoaded = " + emf.getPersistenceUnitUtil().isLoaded(reference));

    //isLoaded = true 반환
    ```

- 프록시 클래스 확인 방법
    - entity.getClass().getName() 출력 (..javasist.. or HibernateProxy...)
- 프록시 강제 초기화
    - org.hibernate.Hibernate.initialize(entity);

    ```java
    Member reference = em.getReference(Member.class, member.getId());
    Hibernate.initialize(reference); //강제초기화
    ```

    - 참고로 이것은 Hibernate가 제공하는 것이다. JPA 표준은 강제 초기화 메소드가 없다.

### 참고

- [김영한님의 자바 ORM 표준 JPA 프로그래밍 - 기본편 강의](https://www.inflearn.com/course/ORM-JPA-Basic/dashboard)
- 김영한님의 자바 ORM 표준 JPA 프로그래밍 책