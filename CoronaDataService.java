package com.yujongu.coronatracker.services;

import com.yujongu.coronatracker.Models.CountryStats;
import com.yujongu.coronatracker.Models.Events;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class CoronaDataService {
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private ArrayList<CountryStats> koreaStats;

    @PostConstruct
    @Scheduled(cron = "1 * * * * *")//sec min hour day month year
    public void fetchData() throws IOException, InterruptedException {
        ArrayList<CountryStats> newStats = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader bodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(bodyReader);
        CSVRecord header = records.iterator().next();

        //events자

        //대구 신천지 31번째 확진
        DateTime febEighteenth = new DateTime(2020, 2, 18, 0, 0, 0);
        String febEighteenthString = String.format("%d/%d/%s", febEighteenth.getMonthOfYear(), febEighteenth.getDayOfMonth(), String.valueOf(febEighteenth.getYear()).substring(2));

        //이태원 클럽 방문 날짜
        DateTime maySecond = new DateTime(2020, 5, 2, 0, 0, 0);
        String maySecondString = String.format("%d/%d/%s", maySecond.getMonthOfYear(), maySecond.getDayOfMonth(), String.valueOf(maySecond.getYear()).substring(2));

        //이태원 클럽남 확진 판정
        DateTime maySix = new DateTime(2020, 5, 6, 0, 0, 0);
        String maySixString = String.format("%d/%d/%s", maySix.getMonthOfYear(), maySix.getDayOfMonth(), String.valueOf(maySix.getYear()).substring(2));

        //인천 학원 강사 이태원 클럽 방문 후 확진 판정.
        DateTime mayNinth = new DateTime(2020, 5, 9, 0, 0, 0);
        String mayNinthString = String.format("%d/%d/%s", mayNinth.getMonthOfYear(), mayNinth.getDayOfMonth(), String.valueOf(mayNinth.getYear()).substring(2));

        //인천 학원 강사 -> 학원 수강생 -> 코인노래방 -> 택시기사 -> 택시기사가 투잡으로 사진사로 방문한 돌잔치 -> 쿠팡 확진자 확진판정
        DateTime mayTwentyFourth = new DateTime(2020, 5, 24, 0, 0, 0);
        String mayTwentyFourthString = String.format("%d/%d/%s", mayTwentyFourth.getMonthOfYear(), mayTwentyFourth.getDayOfMonth(), String.valueOf(mayTwentyFourth.getYear()).substring(2));


        ArrayList<Events> events = new ArrayList<>();
        events.add(new Events(febEighteenthString, "대구 신천지 31번째 확진자 확진판정"));
        events.add(new Events(maySecondString, "이태원 확진자 클럽 방문 날짜"));
        events.add(new Events(maySixString, "이태원 확진자 확진 판정"));
        events.add(new Events(mayNinthString, "인천 학원 강사 이태원 클럽 방문 후 확진 판정"));
        events.add(new Events(mayTwentyFourthString, "인천 학원강사 -> 학원 수강생 -> 코인노래방 -> 택시기사 -> 택시기사가 프리랜서 사진사로 방문한 돌잔치 -> 돌잔치 손님인 쿠팡 확진자 확진판정"));

        int eventInd = 0;
        for (CSVRecord record : records) {
            String regionName = record.get(1);
            if (regionName.equals("Korea, South")){
                int diff = 0;
                for (int i = 4; i < record.size(); i++){
                    CountryStats curr = new CountryStats(header.get(i), Integer.parseInt(record.get(i)));
                    if (curr.getDate().equals(events.get(eventInd).getDate())){
                        curr.setMemo(events.get(eventInd).getEvent());
                        eventInd++;
                        if(eventInd >= events.size()){
                            eventInd = 0;
                        }
                    }
                    diff = curr.getCount() - diff;
                    curr.setDiff(diff);
                    diff = curr.getCount();
                    newStats.add(curr);

                }
                break;
            }
        }




        DateTime today = new DateTime();
        today = today.minusDays(1);
        String latestDate = String.format("%d/%d/%s", today.getMonthOfYear(), today.getDayOfMonth(), String.valueOf(today.getYear()).substring(2));
        if (!newStats.get(newStats.size() - 1).getDate().equals(latestDate)){
            Document doc = Jsoup.connect("http://ncov.mohw.go.kr/").get();
            Elements newsHeadlines = doc.getElementsByClass("liveNum");
            int start = newsHeadlines.text().indexOf("(누적)") + 4;
            int end = newsHeadlines.text().indexOf(" 전일대비");
            String todayCount = newsHeadlines.text().substring(start, end);
            todayCount = todayCount.replaceAll("[,]", "");

            newStats.add(new CountryStats(latestDate, Integer.parseInt(todayCount)));
        }


        Collections.reverse(newStats);
        this.koreaStats = newStats;
    }

    public ArrayList<CountryStats> getKoreaStats() {
        return koreaStats;
    }
}
