package rawrdl;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.regex.*;

public class Main {

	private static String HTTPpost(String urlString, String data)
			throws IOException {
		HttpURLConnection con = (HttpURLConnection) (new URL(urlString))
				.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded; ");
		con.setRequestProperty("charset", "UTF-8");

		String urlParameters = data;

		// Send post request
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(urlParameters.getBytes((Charset.forName("UTF-8"))));
		os.close();

		// int responseCode = con.getResponseCode();
		// System.out.println("\nSending 'POST' request to URL : " + urlString);
		// System.out.println("Post parameters : " + urlParameters);
		// System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();

	}

	static String HTTPget(String urlString) throws IOException {
		URL rawrURL = new URL(urlString);
		InputStream inStream = rawrURL.openStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	public static String find(String patString, String text) {
		Matcher matcher = Pattern.compile(patString).matcher(text);
		if (matcher.find())
			return matcher.group(1);
		else
			return null;
	}

	public static String progresStr(double currentVal, double targetVal) {
		int percent = (int) ((((double) currentVal) / ((double) targetVal)) * 100d);
		String bar = "[                                                  ] - ";
		for (int i=0; i < percent/2; i++) {
			bar = bar.replaceFirst(" ", "#");
		}
		return bar + percent + "%";
	}

	public static void saveUrl(final String urlString, final String filename)
			throws IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) (new URL(urlString).openConnection());
			in = new BufferedInputStream(con.getInputStream());
			fout = new FileOutputStream(filename);
			long completeFileSize = con.getContentLength();
			
			File file = new File(filename);
			if(file.exists() && file.length() == completeFileSize){
				System.out.println("\n\t" + filename + " - done");
				return;
			}

			final byte data[] = new byte[1024];
			int count;
			int currentProgress = 0;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
				currentProgress += count;
				System.out.print(progresStr(currentProgress, completeFileSize) +"\r");
			}
			System.out.println("\n\t" + filename + " - done");

		} finally {
			if (con != null) {
				in.close();
			}
			if (in != null) {
				in.close();
			}
			if (fout != null) {
				fout.close();
			}
		}
	}

	public static void downloadRawr(final String url, final String filename)
			throws IOException {

		String page = HTTPget(url);
		String baseUrl = find("ipb.vars\\['base_url'\\].*?'(.*?)'", page);
		String secureHash = find("ipb.vars\\['secure_hash'\\].*?'(.*?)'", page);
		String id = find("<div .*? rn='(\\d+?)'>", page);
		System.out.println("id:" + id);
		String vid = HTTPpost(baseUrl
				+ "&app=anime&module=ajax&section=anime_watch_handler",
				"md5check=" + secureHash + "&do=getvid&id=" + id);
		System.out.println(vid);

		String srcUrl = find("src=\"(http://.*?)\"", vid);
		if (srcUrl != null) {
			final String dataUrl = find(
					"\"(http://\\w+.arkvid.tv/s/.*?\\.mp4.*?)\"",
					HTTPget(srcUrl));
			if (dataUrl != null) {
				System.out.println("dataUrl:" + dataUrl);
				new Thread() {
					public void run() {
						try {
							saveUrl(dataUrl, filename);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();

			}
		} else {
			System.out.println("not from arkvid");
		}
	}

	public static void downloadEpisode(String title, String rawrURL, int episode)
			throws IOException {
		downloadRawr("http://rawranime.tv/" + rawrURL + "/" + episode
				+ "/subbed", "E:\\TV\\Anime\\" + title + "\\" + episode
				+ ".mp4");
	}

	public static void main(String[] args) throws IOException {
		for (int i=4; i <= 12; i++){
			downloadEpisode("Katanagatari", "702-katanagatari", i);
		}
	}
}
