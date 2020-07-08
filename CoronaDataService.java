package com.yujongu.coronatracker.services;

import com.yujongu.coronatracker.Models.CountryStats;
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

        for (CSVRecord record : records) {
            String regionName = record.get(1);
            if (regionName.equals("Korea, South")){
                int diff = 0;
                for (int i = 4; i < record.size(); i++){
                    CountryStats curr = new CountryStats(header.get(i), Integer.parseInt(record.get(i)));
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
