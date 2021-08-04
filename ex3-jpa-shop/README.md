# 18. 다양한 연관관계 매핑 : 실전 예제 3 - 다양한 연관관계 매핑

# 1. 실전 예제

다음 요구사항이 추가되었다.

- 상품을 주문할 때 배송 정보를 입력할 수 있다. 주문과 배송은 일대일(@OneToOne) 관계이다.
- 상품을 카테고리로 구분할 수 있다. 상품과 카테고리는 다대다(@ManyToMany) 관계이다.

![https://i.imgur.com/HAPHhBs.png](https://i.imgur.com/HAPHhBs.png)

배송 엔티티와 카테고리 엔티티를 추가했다.

## 1) 테이블 구조

![https://i.imgur.com/PCKq2a9.png](https://i.imgur.com/PCKq2a9.png)

## 2) 주문과 배송

- 주문과 배송은 일대일 관계다. 객체 관계를 고려할 때 주문에서 배송으로 자주 접근할 예정이므로 외래 키를 주문테이블에 두었다. 일대일 관계이므로 ORDERS 테이블에 있는 DELIVERY_ID 외래 키에는 유니크 제약조건을 주는 것이 좋다.

Order

```java
@Entity
@Table(name = "ORDERS")
public class Order {

    @Id @GeneratedValue
    @Column(name = "ORDER_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

    **@OneToOne
    @JoinColumn(name = "DELIVERY_ID")
    private Delivery delivery;**  //배송정보

    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;     //주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태

		//getter, setter
}
```

Delivery

```java
@Entity
public class Delivery {

    @Id @GeneratedValue
    private Long id;

    **@OneToOne(mappedBy = "delivery")
    private Order order;**

    private String city;
    private String street;
    private String zipcode;

    **@Enumerated(EnumType.STRING)
    private DeliveryStatus status;**

		//getter, setter

}
```

배송 엔티티는 배송상태를 가진다.

```java
public enum DeliveryStatus {
    READY, COMP
}
```

Order와 Delivery는 일대일 관계다. Order가 매핑된 ORDERS를 주 테이블로 보고 주 테이블에 외래 키를 두었다. 따라서 외래 키가 있는 Order.delivery가 연관관계의 주인이다. 주인이 아닌 Delivery.order 필드에는 mappedBy 속성을 사용해서 주인이 아님을 표시했다.

## 3) 상품과 카테고리

- 한 상품은 여러 카테고리에 속할 수 있고, 한 카테고리도 여러 상품을 가질 수 있으므로 둘은 다대다 관계다. 테이블로 이런 다대다 관계를 표현하기는 어려우므로 CATEGORY_ITEM 연결 테이블을 추가해서 다대다 관계를 일대다, 다대일 관계로 풀어냈다.

Category

```java
@Entity
public class Category {

    @Id  @GeneratedValue
    @Column(name = "CATEGORY_ID")
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = "CATEGORY_ITEM",
            joinColumns = @JoinColumn(name = "CATEGORY_ID"),
            inverseJoinColumns = @JoinColumn(name = "ITEM_ID"))
    private List<Item> items = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<Category>();

		//getter, setter
}
```

Item

```java
@Entity
public class Item {

    @Id
    @GeneratedValue
    @Column(name = "ITEM_ID")
    private Long id;

    private String name;        //이름
    private int price;          //가격
    private int stockQuantity;  //재고수량

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

		//getter, setter
}
```

Categroy와 Item은 다대다 관계다. Category.items 필드를 보면 @ManyToMany와 @JoinTable을 사용해서 CATEGORY_ITEM 연결 테이블을 바로 매핑했다. 그리고 여기서는 Category를 연관관계의 주인으로 정했다. 주인이 아닌 Item.categori es 필드에는 mappedBy 속성을 사용해서 주인이 아님을 표시했다.

다대다 관계는 중간 테이블을 이용해서 일대다, 다대일 관계로 바꿔야 하지만 실무에서는 중간 테이블이 단순하지 않고 중간 테이블에 필드가 추가되면 더는 사용할 수 없으므로 실무에서 활용하기에는 무리가 있다.

# 2. @JoinColumn

![https://i.imgur.com/4cDgzi6.png](https://i.imgur.com/4cDgzi6.png)

# 3. @ManyToOne - 주요 속성

![https://i.imgur.com/arzOaER.png](https://i.imgur.com/arzOaER.png)

- 다대일은 mappedBy가 없다. 다대일을 쓰면 꼭 연관관계의 주인이 되어야 한다는 것을 의미한다.

# 4. @OneToMany - 주요 속성

![https://i.imgur.com/5quoHII.png](https://i.imgur.com/5quoHII.png)



### 참고

- [김영한님의 자바 ORM 표준 JPA 프로그래밍 - 기본편 강의](https://www.inflearn.com/course/ORM-JPA-Basic/dashboard)
- 김영한님의 자바 ORM 표준 JPA 프로그래밍 책