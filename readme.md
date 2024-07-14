
项目中引入Maven

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
    <version>1.0.1</version>
</dependency>
```

application.yaml配置：

```yaml
cn:
  esdeath:
    client:
      server-mode: standalone
      cluster-address:
        - "127.0.0.1:50051"
        - "127.0.0.1:50052"
        - "127.0.0.1:50053"
      server-address: "127.0.0.1:50051"
```

如果你使用的单机，配置server-mode: standalone，如果是集群，则配置server-mode: cluster。

address对应broker的esdeathConf.yaml配置中的server_addr地址。

消息生产：

在需要的地方

```java
@Resource
private EsdeathBootProducer esdeathBootProducer;
```

发送消息：

```java
long delay = System.currentTimeMillis();
Message testPayload = new Message(delay, "test payload ");
SendResult result = esdeathBootProducer.send(testPayload,"topic_test", "tag_test");
```



消费：

```java
@EsdeathConsume
public class TestSubscriber extends AbstractSubscriber<String> {
    private final static Logger logger = LoggerFactory.getLogger(TestSubscriber.class);
    public static final String TOPIC = "topic_test", TAG = "tag_test";

    @Override
    public AckStatus consume(String messageJson) {
        logger.info("{} topic 拉取的消息: {}", subTag(), messageJson);
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