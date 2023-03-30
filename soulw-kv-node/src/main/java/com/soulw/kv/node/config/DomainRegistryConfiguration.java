package com.soulw.kv.node.config;

import com.soulw.kv.node.core.cluster.model.Cluster;
import com.soulw.kv.node.core.log.model.LogWriter;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public Cluster cluster() {
        Cluster cluster = new Cluster();
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
