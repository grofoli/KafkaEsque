package at.esque.kafka.tasks;

import at.esque.kafka.cluster.KafkaesqueAdminClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class TopicsForCluster extends BackgroundRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicsForCluster.class);

    private KafkaesqueAdminClient adminClient;
    private Consumer<FilteredList<String>> callback;

    public TopicsForCluster(KafkaesqueAdminClient adminClient, Consumer<FilteredList<String>> callback) {
        this.adminClient = adminClient;
        this.callback = callback;
    }

    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            LOGGER.info("Starting getting topics for cluster");
            backGroundTaskHolder.setIsInProgress(true);
            ObservableList<String> topics = FXCollections.observableArrayList(adminClient.getTopics());
            FilteredList<String> filteredTopics = new FilteredList<>(topics.sorted(), t -> true);
            Platform.runLater(()-> callback.accept(filteredTopics));
        } finally {
            stopWatch.stop();
            LOGGER.info("Finished getting topics for cluster[{}]", stopWatch);
            backGroundTaskHolder.backgroundTaskStopped();
        }
    }

}
