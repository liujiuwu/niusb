package niusb;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class TestSb {

	public static void main(String[] args) throws ClientProtocolException, IOException {
		String url = "http://sbcx.saic.gov.cn/trade/";
		String url2 = "http://sbcx.saic.gov.cn/trade/servlet?Search=FL_REG_List&RegNO=9698384";
		DefaultHttpClient httpclient = new DefaultHttpClient();
		/*HttpGet httpGet = new HttpGet(url);
		HttpResponse response1 = httpclient.execute(httpGet);
		HttpEntity entity1 = response1.getEntity();
		System.out.println(EntityUtils.toString(entity1));*/

		HttpGet httpGet = new HttpGet(url2);

		HttpResponse response2 = httpclient.execute(httpGet);

		try {
			System.out.println(response2.getStatusLine());
			HttpEntity entity2 = response2.getEntity();
			System.out.println(EntityUtils.toString(entity2));
			/*if (entity2.isStreaming()) {
				String destinationFile = "d://tmp//image.jpg";
				OutputStream os = new FileOutputStream(destinationFile);
				entity2.writeTo(os);
			}*/
			// System.out.println(EntityUtils.toString(entity2));
		} finally {
			httpGet.releaseConnection();
		}
	}

}