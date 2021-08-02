# 13. 연관관계 매핑 기초 : 실전 예제 2 - 연관관계 매핑 시작

# 1. 실전 예제

실전 예제 1은 외래 키를 엔티티에 그대로 가져오는 문제가 있었다. 엔티티에서 외래 키로 사용한 필드는 제거하고 참조를 사용하도록 변경해보자.

## 1) 테이블 구조

- 테이블 구조는 [실전 예제 1](https://hyeon-blog.tistory.com/31)과 같다.

  ![https://i.imgur.com/VCteVQh.png](https://i.imgur.com/VCteVQh.png)

## 2) 객체 구조

- 외래 키를 직접 사용하는 것에서 참조를 사용하도록 변경했다.

![https://i.imgur.com/xByfsjS.png](https://i.imgur.com/xByfsjS.png)

## 3) 일대다, 다대일 연관관계 매핑

회원과 주문은 일대다 관계고 그 반대인 주문과 회원은 다대일 관계다.

외래 키가 있는 Order.member가 연관관계의 주인이기 때문에 주인이 아닌 Member.orders에 @OneToMany 속성에 mappdeBy를 선언해서 연관관계 주인인 member를 지정했다.

Member

```java
@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;
    private String name;
    private String city;
    private String street;
    private String zipcode;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

}
```

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

    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;     //주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태

    //연관관계 편의 메소드
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

}
```

- **연관관계 편의 메소드**

  양방향 연관관계인 두 엔티티 간에 관계를 맺을 때는 원래 다음처럼 설정해야 한다.

    ```java
    Member member = new Member();
    Order order = new Order();

    member.getOrders().add(order);
    order.setMember(member);
    ```

  Order 엔티티에 다음과 같은 연관관계 편의 메소드를 추가했다.

    ```java
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    ```

  따라서 다음처럼 관계를 설정하면 된다.

    ```java
    Member member = new Member();
    Order order = new Order();

    order.setMember(member);
    ```

주문과 주문상품은 일대다 관계고 그 반대는 다대일 관계다.

외래 키가 있는 OrderItem.order가 연관관계의 주인이기 때문에 Order.orderItems 필드에는 mappedBy 속성을 사용해서 주인이 아님을 표시했다.

OrderItem

```java
@Entity
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "ORDER_ITEM_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ORDER_ID")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "ITEM_ID")
    private Item item;

    private int orderPrice; //주문 가격
    private int count;      //주문 수량

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(int orderPrice) {
        this.orderPrice = orderPrice;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

}
```

상품에서 주문상품을 참조할 일은 거의 없기 때문에, 주문상품과 상품은 다대일 단방향 관계로 설정했다.



### 참고

- [김영한님의 자바 ORM 표준 JPA 프로그래밍 - 기본편 강의](https://www.inflearn.com/course/ORM-JPA-Basic/dashboard)
- 김영한님의 자바 ORM 표준 JPA 프로그래밍 책