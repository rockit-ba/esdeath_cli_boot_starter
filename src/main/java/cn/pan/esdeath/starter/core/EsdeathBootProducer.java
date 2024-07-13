package cn.pan.esdeath.starter.core;

import cn.pan.esdeathcli.core.produce.CancelResult;
import cn.pan.esdeathcli.core.produce.EsdeathProducer;
import cn.pan.esdeathcli.core.produce.Message;
import cn.pan.esdeathcli.core.produce.SendResult;

import javax.annotation.PreDestroy;


public class EsdeathBootProducer {
    private final EsdeathProducer esdeathProducer;

    public EsdeathBootProducer(EsdeathProducer esdeathProducer) {
        this.esdeathProducer = esdeathProducer;
    }

    public SendResult send(Message msg, String topic, String tag) {
        return esdeathProducer.send(msg,topic, tag);
    }

    public CancelResult cancel(String msgId) {
        return esdeathProducer.cancel(msgId);
    }

    @PreDestroy
    public void close() {
        esdeathProducer.close();
    }
}
