package at.esque.kafka.cluster;

import at.esque.kafka.alerts.ErrorAlert;
import at.esque.kafka.topics.DescribeTopicWrapper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KafkaesqueAdminClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaesqueAdminClient.class);

    private AdminClient adminClient;
    private Properties props = new Properties();

    public void init(String bootstrapServers) {
        Objects.requireNonNull(bootstrapServers);

        if (isInitialized(bootstrapServers)) {
            LOGGER.info("Admin client for [{}] has been already properly initialized", bootstrapServers);
            return;
        }

        props.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(AdminClientConfig.CLIENT_ID_CONFIG, String.format("kafkaesque-%s", UUID.randomUUID()));

        if (adminClient != null) {
            adminClient.close();
        }

        adminClient = AdminClient.create(props);
    }

    private boolean isInitialized(String bootstrapServers) {
        return adminClient != null &&
                bootstrapServers.equals(props.getProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
    }

    public Set<String> getTopics() {
        ListTopicsOptions options = new ListTopicsOptions();
        options.listInternal(true);
        ListTopicsResult result = adminClient.listTopics(options);
        try {
            return result.names().get();
        } catch (Exception e) {
            ErrorAlert.show(e);
        }
        return new HashSet<>();
    }

    public List<Integer> getTopicPatitions(String topic) {
        DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topic));
        try {
            return result.values().get(topic).get().partitions().stream()
                    .map(TopicPartitionInfo::partition).collect(Collectors.toList());
        } catch (Exception e) {
            ErrorAlert.show(e);
        }
        return null;
    }

    public void deleteTopic(String name) throws ExecutionException, InterruptedException {
        DeleteTopicsResult result = adminClient.deleteTopics(Collections.singletonList(name));
        result.values().get(name).get();
    }

    public void createTopic(String name, int partitions, short replicationFactor, Map<String, String> configs) throws ExecutionException, InterruptedException {
        NewTopic topic = new NewTopic(name, partitions, replicationFactor);
        topic.configs(configs);
        CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(topic));
        result.all().get();
        if (result.all().isCompletedExceptionally()) {
            throw new RuntimeException("Exeption during Topic creation");
        }
    }

    public DescribeTopicWrapper describeTopic(String topic) {
        DescribeTopicsResult describeResult = adminClient.describeTopics(Collections.singletonList(topic));
        ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
        DescribeConfigsResult configsResult = adminClient.describeConfigs(Collections.singletonList(configResource));
        try {
            TopicDescription topicDescription = describeResult.values().get(topic).get(10, TimeUnit.SECONDS);
            Config config = configsResult.values().get(configResource).get(10, TimeUnit.SECONDS);

            return new DescribeTopicWrapper(topicDescription, config);
        } catch (Exception e) {
            ErrorAlert.show(e);
        }
        return null;
    }

}
