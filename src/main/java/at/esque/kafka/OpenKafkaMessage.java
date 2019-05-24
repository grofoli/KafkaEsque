package at.esque.kafka;

import at.esque.kafka.alerts.ErrorAlert;
import at.esque.kafka.topics.KafkaMessage;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class OpenKafkaMessage {

    public static void inTextEditor(KafkaMessage kafkaMessage, String suffix) {
        try {
            File temp = File.createTempFile("kafkaEsque-export", "." + suffix);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
                bw.write(kafkaMessage.getValue());
            }

            Desktop.getDesktop().open(temp);
        } catch (IOException e) {
            ErrorAlert.show(e);
        }
    }
}
