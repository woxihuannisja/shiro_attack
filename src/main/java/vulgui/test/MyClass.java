package vulgui.test;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigInteger;
import java.net.URLEncoder;
import vulgui.deser.util.ClassFiles;

/**
 * @className MyClass
 * @Description TODO
 * @Author sunnylast0
 * @Date 2020/11/7 10:00
 * @Version 1.0
 **/
public class MyClass {

  public static String toHexString(String input) {
    return String.format("%x", new BigInteger(1, input.getBytes()));
  }

  public static String fromHexString(String hex) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < hex.length(); i += 2) {
      str.append((char) Integer.parseInt(hex.substring(i, i + 2), 16));
    }
    return str.toString();
  }

  public static void main(String args[]) throws Exception {
    String dest="SimpleFilter-urlencode.txt";
    byte[] data = ClassFiles.classAsBytes(x.SimpleFilter.class);
    String b64Data = org.apache.shiro.codec.Base64.encodeToString(data);
    String urlData=URLEncoder.encode(b64Data,"UTF-8");
    // BufferedWriter out = new BufferedWriter(new FileWriter("BehOldDemoServlet.txt"));
    // out.write(b64Data);
    // out.close();


    BufferedWriter out = new BufferedWriter(new FileWriter(dest));
    out.write(urlData);
    out.close();
    System.out.println("文件创建成功！");

    // System.out.println(b64Data);
  }


}
