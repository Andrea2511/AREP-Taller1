package co.eci.edu.arep;

import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class URLReader {
    public URLReader() {
    }

    public static void main(String[] args) throws Exception {
        String site = "https://google.com";
        URL siteURL = new URL(site);
        URLConnection urlConnection = siteURL.openConnection();
        Map<String, List<String>> headers = urlConnection.getHeaderFields();
        Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
        Iterator var6 = entrySet.iterator();

        while(var6.hasNext()) {
            Map.Entry<String, List<String>> entry = (Map.Entry)var6.next();
            String headerName = (String)entry.getKey();
            if (headerName != null) {
                System.out.print(headerName + ":");
            }

            List<String> headerValues = (List)entry.getValue();
            Iterator var10 = headerValues.iterator();

            while(var10.hasNext()) {
                String value = (String)var10.next();
                System.out.print(value);
            }

            System.out.println("");
        }

    }
}
