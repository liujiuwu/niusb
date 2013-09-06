package niusb;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class TestSb {

	public static void main(String[] args) throws ClientProtocolException, IOException {
		String baseUrl = "http://sbcx.saic.gov.cn/trade/servlet?";
		String url = baseUrl + "Search=FL_REG_List&RegNO=9698384";
		String url2 = baseUrl + "Search=TI_REG&RegNO=9698384&IntCls=25&iYeCode=0";
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpGet);

		try {
			System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			System.out.println(EntityUtils.toString(entity));
			
			httpGet = new HttpGet(url2);
			response = httpclient.execute(httpGet);
			entity = response.getEntity();
			System.out.println(EntityUtils.toString(entity));

		} finally {
			httpGet.releaseConnection();
		}
	}
}