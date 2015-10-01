package saturn.utils;




import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;




public class Download{




	public static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/535.3 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2";

	private static boolean isSSLDisabled = false;

	private HashMap<String, String> head = new HashMap<String, String>();
	private boolean followRedirects = true;
	private Proxy proxy;
	private int readTimeout = 120000;
	private int connectionTimeout = readTimeout;




	public Download setHeadRequest(String key, String value){

		head.put(key, value);

		return this;

	}




	public Download setFollowRedirects(boolean followRedirects){

		this.followRedirects = followRedirects;

		return this;

	}




	public Download setProxy(Proxy proxy){

		this.proxy = proxy;

		return this;

	}




	public boolean urlExists(String url) throws Exception{

		HttpURLConnection conn = null;

		try{

			conn = getConnection(url);
			conn.setRequestMethod("HEAD");

			return conn.getResponseCode() == HttpURLConnection.HTTP_OK;

		}catch(Exception e){

			throw e;

		}finally{

			if(conn != null){
				conn.disconnect();
			}

		}

	}




	public String getURLContent(String url) throws Exception{
		return getURLContentWithCookieAndPost(url, null, null);
	}




	public String getURLContentWithPost(String url, String post) throws Exception{
		return getURLContentWithCookieAndPost(url, null, post);
	}




	public String getURLContentWithCookie(String url, String cookie) throws Exception{
		return getURLContentWithCookieAndPost(url, cookie, null);
	}




	public String getURLContentWithCookieAndPost(String url, String cookie, String post) throws Exception{

		DownloadByteResult result = getURLBytesWithCookieAndPost(url, cookie, post);

		if(result.charset != null && !result.charset.equals("")){
			return new String(result.bytes, result.charset);
		}else{
			return new String(result.bytes);
		}

	}




	public DownloadByteResult getURLBytes(String url) throws Exception{
		return getURLBytesWithCookieAndPost(url, null, null);
	}




	public DownloadByteResult getURLBytesWithCookieAndPost(String url, String cookie, String post) throws Exception{

		HttpURLConnection conn = null;
		InputStream is = null;

		try{

			conn = getConnection(url);

			if(cookie != null){
				conn.setRequestProperty("Cookie", cookie);
			}

			// apply the head
			for(String key : head.keySet()){
				conn.setRequestProperty(key, head.get(key));
			}

			// post
			if(post != null){

				conn.setDoOutput(true);
				conn.setRequestMethod("POST");

				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
				wr.writeBytes(post);
				wr.flush();
				wr.close();

			}

			// downloads the content
			is = conn.getInputStream();

			// checks the types of data
			String encoding = conn.getContentEncoding();

			if(encoding != null && encoding.equalsIgnoreCase("gzip")){

				is = new GZIPInputStream(is);

			}else if(encoding != null && encoding.equalsIgnoreCase("deflate")){

				is = new InflaterInputStream((is), new Inflater(true));

			}

			byte[] bytes = IOUtils.toByteArray(is);

			// charset
			String contentType = conn.getHeaderField("Content-Type");
			String charset = null;

			if(contentType != null){

				for(String param : contentType.replace(" ", "").split(";")){

					if(param.startsWith("charset=")){

						charset = param.split("=", 2)[1]; // 2 ? !!

						break;

					}

				}

			}

			if(charset == null){

				try{

					String content = new String(bytes, "UTF-8");
					int posCharset = content.indexOf("charset");

					if(posCharset > 0){

						content = content.substring(posCharset);

						int posEnd = content.indexOf("\"");

						if(posEnd == -1){
							posEnd = content.indexOf(";");
						}

						if(posEnd == -1){
							posEnd = content.indexOf("\\");
						}

						if(posEnd != -1){

							content = content.substring(0, posEnd);
							content = StringUtils.remove(content, " ");

							charset = content.substring(8);

						}

					}

				}catch(Exception e){
					e.printStackTrace();
				}

			}

			// asks for the cookies
			List<String> cookies = null;

			if(conn.getHeaderFields().containsKey("Set-Cookie")){
				cookies = conn.getHeaderFields().get("Set-Cookie");
			}

			// response
			DownloadByteResult result = new DownloadByteResult();
			result.charset = charset;
			result.bytes = bytes;
			result.cookies = cookies;
			result.responseCode = conn.getResponseCode();
			result.headers = conn.getHeaderFields();

			return result;

		}catch(Exception e){

			throw e;

		}finally{

			if(is != null){
				is.close();
			}

			if(conn != null){
				conn.disconnect();
			}

		}

	}




	public String getURLHeaderWithCookieAndPost(String url, String key, String cookie, String post) throws Exception{

		HttpURLConnection conn = null;

		try{

			conn = getConnection(url);

			if(cookie != null){
				conn.setRequestProperty("Cookie", cookie);
			}

			// post
			if(post != null){

				conn.setDoOutput(true);
				conn.setRequestMethod("POST");

				OutputStream os = conn.getOutputStream();
				DataOutputStream wr = new DataOutputStream(os);
				wr.writeBytes(post);
				wr.flush();
				wr.close();

			}

			return conn.getHeaderField(key);

		}catch(Exception e){

			throw e;

		}finally{

			if(conn != null){
				conn.disconnect();
			}

		}

	}




	public Map<String, List<String>> getHead(String url) throws Exception{

		HttpURLConnection conn = null;

		try{

			conn = getConnection(url);			
			conn.setRequestMethod("HEAD");

			return conn.getHeaderFields();

		}catch(Exception e){

			throw e;

		}finally{

			if(conn != null){
				conn.disconnect();
			}

		}

	}




	public int getResponseCode(String url) throws Exception{

		HttpURLConnection conn = null;

		try{

			conn = getConnection(url);		
			conn.setRequestMethod("HEAD");

			return conn.getResponseCode();

		}catch(Exception e){

			throw e;

		}finally{

			if(conn != null){
				conn.disconnect();
			}

		}

	}



	private HttpURLConnection getConnection(String url) throws Exception{

		HttpURLConnection conn;

		if(url.equals(URLDecoder.decode(url, "UTF-8"))){
			url = URIUtil.encodeQuery(url);
		}

		if(url.startsWith("https://")){

			disableSSLValidation();

			if(proxy == null){
				conn = (HttpsURLConnection) new URL(url).openConnection();
			}else{
				conn = (HttpsURLConnection) new URL(url).openConnection(proxy);
			}

		}else{

			if(proxy == null){
				conn = (HttpURLConnection) new URL(url).openConnection();
			}else{
				conn = (HttpURLConnection) new URL(url).openConnection(proxy);
			}

		}

		conn.setRequestProperty("User-Agent", USER_AGENT);	
		conn.setRequestProperty("Accept","*/*");
		conn.setRequestProperty("Accept-Encoding", "deflate, gzip");

		conn.setInstanceFollowRedirects(followRedirects);			
		conn.setReadTimeout(readTimeout);			
		conn.setConnectTimeout(connectionTimeout);

		return conn;

	}




	private static synchronized void disableSSLValidation() throws Exception{

		if(!isSSLDisabled){

			TrustManager[] trustAllCerts = new TrustManager[]{

					new X509TrustManager(){

						@Override
						public java.security.cert.X509Certificate[] getAcceptedIssuers(){
							return null;
						}

						@Override
						public void checkClientTrusted(X509Certificate[] certs, String authType){}

						@Override
						public void checkServerTrusted(X509Certificate[] certs, String authType){}

					}

			};

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			HostnameVerifier allHostsValid = new HostnameVerifier(){

				@Override
				public boolean verify(String hostname, SSLSession session){
					return true;
				}

			};

			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

			isSSLDisabled = true;

		}

	}

}