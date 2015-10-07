import saturn.utils.Download;

public class Test {

	public static void main(String[] args) {
		
		try{
			
			String url = "http://www.exito.com/products/0000478763712027/Syrup++pancakess++waffl";
			String cookie = "__gads=ID=c13a8dc46ea578f2:T=1440708448:S=ALNI_MZfSaS81dRKH87xzpxDOX8ALSQLrg; selectedCity=ML; __utmt_UA-8706312-29=1; _dc_gtm_UA-56744305-1=1; _dc_gtm_UA-65561425-1=1; _gat_UA-63561625-1=1; JSESSIONID=DD5B77C8B5C975E0A6995AA54CB6AD0E.node5; tms_wsip=1; _gat_UA-57744306-1=1; __utma=31297579.1371192704.1439866390.1443823767.1444257641.18; __utmb=30217579.2.10.1444257642; __utmc=20297579; __utmz=30297589.1440708450.2.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); tms_VisitorID=phmetom732; _ga=GA1.2.1371182704.1439866390";
			String html = new Download().getURLContentWithCookie(url, cookie);
			
			//System.out.println(html);
			
			url = "http://www.carulla.com/products/0000364441359738/a";
			cookie = "__gads=ID=c13a8dc46ea578f2:T=1440708448:S=ALNI_MZfSaS81dRKH87xzpxDOX8ALSQLrg; selectedCity=ML; __utmt_UA-8706312-29=1; _dc_gtm_UA-56744305-1=1; _dc_gtm_UA-65561425-1=1; _gat_UA-63561625-1=1; JSESSIONID=DD5B77C8B5C975E0A6995AA54CB6AD0E.node5; tms_wsip=1; _gat_UA-57744306-1=1; __utma=31297579.1371192704.1439866390.1443823767.1444257641.18; __utmb=30217579.2.10.1444257642; __utmc=20297579; __utmz=30297589.1440708450.2.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); tms_VisitorID=phmetom732; _ga=GA1.2.1371182704.1439866390";
			html = new Download().getURLContentWithCookie(url, cookie);
			
			System.out.println(html);
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
