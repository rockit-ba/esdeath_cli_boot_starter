
# 项目中引入Maven

```xml
<repositories>
    <repository>
        <id>central-repos1</id>
        <name>Central Repository 2</name>
        <url>https://repo1.maven.org/maven2/</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>io.github.rockit-ba</groupId>
    <artifactId>esdeath_cli_boot_starter</artifactId>
    <version>1.0.2</version>
</dependency>
```

# application.yaml配置：

```yaml
cn:
  esdeath:
    client:
      server-mode: standalone
      # 可以将IP换位 121.4.116.60，这是已经搭好的集群
      cluster-address:
        - "127.0.0.1:50051"
        - "127.0.0.1:50052"
        - "127.0.0.1:50053"
      server-address: "127.0.0.1:50051"
```

如果你使用的单机，配置server-mode: standalone，如果是集群，则配置server-mode: cluster。

address对应broker的esdeathConf.yaml配置中的server_addr地址。

# 消息生产：

## 消息对象
```java
public class Order {
    private String orderId;
    private String orderName;
    private String info;

    public Order() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", orderName='" + orderName + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}
```
# 消费
## tag1 消费者
```java
@EsdeathConsume
public class Order1Subscriber extends AbstractSubscriber<Order> {
    private final static Logger logger = LoggerFactory.getLogger(Order1Subscriber.class);
    public static final String TOPIC = "topic_order", TAG = "tag_1";

    @Override
    public AckStatus consume(Order order) {
        logger.info("{} topic 拉取的消息: {}", subTag(), order);
        return AckStatus.ACK;
        //return ConsumeStatus.RECONSUME;
    }

    @Override
    public Tag subTag() {
        return new Tag(TAG);
    }

    @Override
    protected Topic subTopic() {
        return new Topic(TOPIC);
    }
}
```

## tag2 消费者
```java
@EsdeathConsume
public class Order2Subscriber extends AbstractSubscriber<Order> {
    private final static Logger logger = LoggerFactory.getLogger(Order2Subscriber.class);
    public static final String TOPIC = "topic_order", TAG = "tag_2";

    @Override
    public AckStatus consume(Order order) {
        logger.info("{} topic 拉取的消息: {}", subTag(), order);
        return AckStatus.ACK;
        //return ConsumeStatus.RECONSUME;
    }

    @Override
    public Tag subTag() {
        return new Tag(TAG);
    }

    @Override
    protected Topic subTopic() {
        return new Topic(TOPIC);
    }
}
```

# 发送消息：

```java
@RestController("/test")
public class TestController {
    private final static Logger logger = LoggerFactory.getLogger(TestController.class);
    @Resource
    private EsdeathBootProducer esdeathBootProducer;

    @GetMapping("/1")
    public void hello1() {
        long delay = System.currentTimeMillis()+10*1000L;
        Order order = new Order();
        order.setOrderId("1");
        order.setOrderName("order1");
        order.setInfo("order1 info");
        Message testPayload = new Message(delay, order);
        logger.info("发送消息: {}", testPayload);
        SendResult result = esdeathBootProducer.send(testPayload, Order1Subscriber.TOPIC, Order1Subscriber.TAG);
        logger.info("发送结果: {}", result);
    }

    @GetMapping("/2")
    public void hello2() {
        long delay = System.currentTimeMillis()+10*1000L;
        Order order = new Order();
        order.setOrderId("2");
        order.setOrderName("order2");
        order.setInfo("order2 info");
        Message testPayload = new Message(delay, order);
        logger.info("发送消息: {}", testPayload);
        SendResult result = esdeathBootProducer.send(testPayload, Order2Subscriber.TOPIC, Order2Subscriber.TAG);
        logger.info("发送结果: {}", result);
    }
}
```