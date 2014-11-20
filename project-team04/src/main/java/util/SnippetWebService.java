package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SnippetWebService {
  private String urlPreffix = "http://metal.lti.cs.cmu.edu:30002/pmc/";
  public URL getUrl(String pmid){
    URL url = null;
    try {
      url = new URL(urlPreffix+pmid);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
//    System.out.println("url: "+url.toString());
    return url;
  }
  public JSONObject getSnippets(String pmid){
    return getSnippets(getUrl(pmid));
  }
  public JSONObject getSnippets(URL url){
    HttpURLConnection conn = null;
    InputStream stream =null;
    StringBuilder inputString = new StringBuilder();
    inputString.append("[");
      try {
        conn = (HttpURLConnection) url.openConnection();
        stream = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(stream);
        int cha;      
        while ((cha = isr.read()) != -1) {
          inputString.append((char)cha);
        }
        inputString = inputString.append("]");
 //       System.out.println(inputString);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally{
        if(conn !=null)
          conn.disconnect();
      }
     
     
      JSONObject json = null;
      if(inputString!=null){
        JSONArray resultArray = new JSONArray(inputString.toString());
    //    System.out.println(resultArray);
        json = resultArray.optJSONObject(0);
 //       System.out.println(json);
      }
//     System.out.println(json);
//     JSONArray jar = (JSONArray) json.get("sections");
//     System.out.println(jar.getString(0));
    return json;
  }
  public static void main(String[] asrgs){   
    SnippetWebService sws = new SnippetWebService();
    sws.getSnippets(sws.getUrl("23193287"));
  //  String s ="\u2002\u2003\u2018abc\u2019123";
//    String s = "/w";
//    s = s.replaceAll("\\u2002", "");
//    System.out.println(s);
//    s = s.replaceAll("\\u2003", "");
//    System.out.println(s);
//    s = s.replaceAll("\\u2018(/w*)\\2019", "");
//    System.out.println(s);
  }
}
