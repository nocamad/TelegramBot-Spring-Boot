package com.telegram.springboot;

import com.telegram.springboot.Json.Weather;
import com.telegram.springboot.ParsingDirectionOfSpecialties.DataParsingSpecialties;
import com.telegram.springboot.ParsingDirectionOfSpecialties.TestParserSpecialties;
import com.telegram.springboot.Parsing_news.DataParseNews;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static javax.swing.UIManager.get;
import static org.apache.tomcat.util.IntrospectionUtils.capitalize;


@Service
public class Bot extends TelegramLongPollingBot {

    @PersistenceContext
    private EntityManager em;

    public static long chat_id;
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        chat_id = update.getMessage().getChatId();
        String answer = onUpdate(message.getText());
        if (answer.matches("\\S")) {
            if (!answer.matches("[0-9]")) {
                answer = capitalize(answer);
            }
            send_Photo(chat_id, answer);
        } else {
            sendMsg(message, answer);
        }
    }

    public String onUpdate(String msg){
        List<DataParsingSpecialties> f=null;
        List<DataParseNews> d=null;
        TestParserSpecialties r =new TestParserSpecialties();
        String query="SELECT  c.description FROM Schedule c WHERE concat(c.data_ ,' ',c.group_) = '"+msg+"'";

        if (!msg.isEmpty()) {
            if (msg.equals("Время")) {
                Date date = new Date();
                SimpleDateFormat formatForDateNow = new SimpleDateFormat("E dd.MM.yyyy 'и время' hh:mm:ss a zzz");
                return ("Привет, сегодня " + formatForDateNow.format(date));
            } else if (!em.createQuery(query)
                    .getResultList().isEmpty()) {

                return em.createQuery(query)
                        .getResultList().toString();
            } else if(msg.matches("[Гг]де находится ((корпус [АаГгДдИиКк])|(общежитие №[1-7]))")) {
                char ch=msg.charAt(msg.length()-1);
                return String.valueOf(ch);
            }else if(msg.matches("[Кк]акая погода сегодня")){
                Client client = ClientBuilder.newBuilder().build();
                WebTarget target = client.target("http://api.openweathermap.org/data/2.5/weather?q=Taganrog,ru&units=metric&appid=293da20ad6da8e2bb2974cc9760fbf87");
                Response response = target.request().get();
                Weather weather = response.readEntity(Weather.class);
                return weather.toString();

            }else if(msg.matches("[Нн]овости ЮФУ")){


                d=r.getDataParses();


                String data="";
                for (int i = 0; i < 5; i++) {
                    data+=d.get(i).getName()+"\n"+d.get(i).getContent()+"\n\n\n";
                }
                return data;
            }else if(msg.matches("[Нн]аправление [*]?[0-9][0-9].[0-9][0-9].[0-9][0-9]")){

                f=r.getDataParsingSpecialties();
                String spec=msg.substring(11);
                int number_start_simvol=0;
                if(spec.charAt(1)=='*'){
                    number_start_simvol=1;
                    spec=spec.substring(1);
                }else {
                    spec=spec.substring(1);
                }




                for (int i = 0; i <f.size() ; i++) {
                    if(spec.equals(f.get(i).id_name)){
                        String data=f.get(i).name_spec.substring(number_start_simvol)+"\n"+f.get(i).subjects+"\n"+f.get(i).marks+"\n"+f.get(i).structural;
                        return data;
                    }
                }
            }
            else if(msg.equals("/help")) {

                return "Привет, я могу тебе помочь: " +
                        "\n узнать время (Время) " +
                        "\n узнать расписание ЮФУ (19.03.2018 КТбо3-2) " +
                        "\n узнать местоположение корпуса ЮФУ(Где находится корпус А) " +
                        "\n узнать местоположение общежития (Где находится общежитие №1)"+
                        "\n узнать какая погода в Таганроге сегодня (Какая погода сегодня)"+
                        "\n узнать новости ЮФУ (Новости ЮФУ)"+
                        "\n проходной балл для поступления на указанную специальность (Направление 27.03.04," +
                        "Направление 11.05.04) ";

            }else{
                return "Больше ничего пока не знаю!";
            }
        }
        return null;
    }

    private void send_Photo(long chat_id,String index) {
        String query_map="SELECT  c.url FROM GoogleMaps c WHERE c.index = '"+index+"'";
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);
        String image = em.createQuery(query_map).getSingleResult().toString();
        sendPhoto.setPhoto(image);
        try {
            sendPhoto(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(Message message, String s) {
        SendMessage sendMessage =new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(s);
        try{
            sendMessage(sendMessage);

        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }


    @Override
    public String getBotUsername() {
      
        return "Ivan777_bot";
    }

    @Override
    public String getBotToken() {
        return "555686449:AAH9vle-Bbj-jYeWSArxeiDa_lvL0vb1h_0";
    }
}
