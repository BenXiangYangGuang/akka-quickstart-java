package cn.neucloud.example.producer;

import cn.neucloud.example.utils.KafkaProducerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author: fei2
 * @Date:2018/6/12 17:23
 * @Description:
 * @Refer To:
 */
@Slf4j
@Component
public class KafkaCSVProducer implements CommandLineRunner {

    //    private static String topic = "topic_zYy5r8J0";
    private static String topic = "Suct_Data";
    private static long time = 3000;
    int v = 1 ;
    public void sendCsvData(){
        Random random = new Random();
//        String value = random.nextInt(10000)+","+random.nextInt(10000)+","+random.nextInt(10000)+"";
        String value = new Date().getTime() + "," + getRandomValue() + "," + getRandomValue() + "," + getRandomValue();
        KafkaProducerUtil.sendToKafka(topic,value);
        log.info("send {} to topic :{}" ,value,topic);

    }
/*    public static void main(String[] args) {
        int key = 1;
        while (true){
            Random random = new Random();
            String value = random.nextInt(100)+","+random.nextInt(100)+","+random.nextInt(100)+"";
            KafkaProducerUtil.sendToKafka(topic,value);
            key++;
        }
    }*/

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
        return (int)(Math.random() * 100);
    }
    
}
