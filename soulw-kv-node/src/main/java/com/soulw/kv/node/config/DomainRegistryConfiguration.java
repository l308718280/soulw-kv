package com.soulw.kv.node.config;

import com.soulw.kv.node.core.cluster.gateway.NodeRequestGateway;
import com.soulw.kv.node.core.cluster.model.Cluster;
import com.soulw.kv.node.core.cluster.repository.NodeRepository;
import com.soulw.kv.node.core.log.model.LogWriter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 11:41
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "domain.registry")
public class DomainRegistryConfiguration {

    private String dir;
    @Resource
    private NodeRequestGateway nodeRequestGateway;
    @Resource
    private NodeRepository nodeRepository;
    @Resource
    private Environment environment;

    @Bean
    public Cluster cluster() {
        Cluster cluster = new Cluster(nodeRepository, nodeRequestGateway, environment);
        cluster.init();
        return cluster;
    }

    @Bean
    public LogWriter logWriter(Cluster cluster) {
        LogWriter logWriter = new LogWriter(dir, cluster);
        logWriter.init();
        return logWriter;
    }

}
