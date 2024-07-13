package cn.pan.esdeath.starter.core;


import cn.pan.esdeathcli.core.ClusterConfig;
import cn.pan.esdeathcli.core.Config;
import cn.pan.esdeathcli.core.StandaloneConfig;
import cn.pan.esdeathcli.core.consume.AbstractSubscriber;
import cn.pan.esdeathcli.core.consume.EsdeathConsumer;
import cn.pan.esdeathcli.core.consume.SubscriberRegister;
import cn.pan.esdeathcli.core.produce.EsdeathProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.stream.Collectors;

import static cn.pan.esdeath.starter.core.EsdeathClientProperties.ServerMode.cluster;


@EnableConfigurationProperties({EsdeathClientProperties.class})
@AutoConfigureAfter({ConfigurationPropertiesAutoConfiguration.class})
public class AutoConfiguration {
    private final static Logger logger = LoggerFactory.getLogger(AutoConfiguration.class);
    private final Config config;
    private EsdeathConsumer esdeathConsumer = null;

    @Autowired
    public AutoConfiguration(EsdeathClientProperties properties,
                             @Autowired(required = false) List<AbstractSubscriber<?>> subscribers) {
        EsdeathClientProperties.ServerMode serverMode = properties.getServerMode();
        if (serverMode == null) {
            throw new IllegalArgumentException("server mode is must to set");
        }
        switch (serverMode) {
            case standalone:
                this.config = new StandaloneConfig(properties.getServerAddress(), properties.getConsumerGroup());
                break;
            case cluster:
                this.config = new ClusterConfig(properties.getClusterAddress(), properties.getConsumerGroup());
                break;
            default:
                throw new IllegalArgumentException("server mode not support");
        }
        if (subscribers != null && !subscribers.isEmpty()) {
            AbstractSubscriber<?>[] subscribersArray = subscribers.toArray(new AbstractSubscriber<?>[0]);
            this.esdeathConsumer = new EsdeathConsumer(this.config, subscribersArray);
        }
    }

    @Bean
    public EsdeathBootProducer esdeathBootProducer() {
        return new EsdeathBootProducer(new EsdeathProducer(this.config));
    }

    @PostConstruct
    public void start() {
        if (esdeathConsumer == null) {
            return;
        }
        esdeathConsumer.subscribers
                .stream()
                .map(AbstractSubscriber::topic)
                .collect(Collectors.toSet())
                .forEach(ele -> {
                    new Thread(() -> {
                        while (true) {
                            try {
                                esdeathConsumer.pullMessage(ele);
                                logger.info("topic {} consumer pull", ele);
                            } catch (Exception e) {
                                logger.error("pull message error", e);
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException ex) {
                                    logger.error("sleep error", ex);
                                }
                            }
                        }
                    },"consume topic "+ele+" thread").start();
        });
    }

    @PreDestroy
    public void close() {
        esdeathConsumer.close();
    }

}
