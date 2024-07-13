package cn.pan.esdeath.starter.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(EsdeathClientProperties.PREFIX)
public class EsdeathClientProperties {
    public static final String PREFIX = "cn.esdeath.client";

    private ServerMode serverMode;
    /**
     * 服务端地址
     */
    private String serverAddress;

    /**
     * 服务端地址
     * 集群版使用此配置
     */
    private List<String> clusterAddress;

    /**
     * consumerGroup
     * 不同消费组的消费者可以消费同一个topic
     */
    private String consumerGroup;

    public static enum ServerMode {
        standalone,
        cluster
    }

}