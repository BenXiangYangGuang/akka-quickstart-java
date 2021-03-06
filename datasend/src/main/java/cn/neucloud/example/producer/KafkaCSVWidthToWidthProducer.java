package cn.neucloud.example.producer;

import cn.neucloud.example.utils.KafkaProducerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author: fei2
 * @Date:2018/6/12 17:23
 * @Description:
 * @Refer To:
 */
@Slf4j
@Component
public class KafkaCSVWidthToWidthProducer implements CommandLineRunner {

    private static String topic = "csv_width_to_width";
    private static long time = 3000;
    int v = 1 ;
    public void sendCsvData(){
        for (int i = 1; i < 4; i++){
            String value = System.currentTimeMillis() + "," + getRandomValue() + "," + getRandomValue() + "," + getRandomValue();
            KafkaProducerUtil.sendToKafka(topic,value);
            log.info("send {} to topic :{}" ,value,topic);
        }
    }

    /**
     * 项目启动自动 执行的方法
     *
     * @param strings
     * @throws Exception
     */
    @Override
    public void run(String... strings) throws Exception {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendCsvData();
            }
        }, 0, time);

    }
    private static int getRandomValue(){
        return (int)(Math.random() * 1000);
    }
    
}
