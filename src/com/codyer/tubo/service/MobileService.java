package com.codyer.tubo.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;

import com.codyer.tubo.utils.Constants;

import android.util.Xml;

public class MobileService {
	/**
	 * 手机归属地查询
	 * @param mobile 电话号码
	 * @return
	 * @throws Exception
	 */
	public static String getMobilAdress(String mobile) throws Exception {
		InputStream inputStream = MobileService.class.getClassLoader()
				.getResourceAsStream("mobile_soap.xml");
		byte[] data = ReadInputStram(inputStream);
		String xml = new String(data);
		String soap = xml.replaceAll("\\$mobile", mobile);
		String path = Constants.PHONE_SERVISE_URL;
		data = soap.getBytes();
		HttpURLConnection conn = (HttpURLConnection) new URL(path)
				.openConnection();
		conn.setReadTimeout(5000);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type",
				"application/soap+xml; charset=utf-8");
		conn.setRequestProperty("Content-Length", String.valueOf(data.length));
		OutputStream out = conn.getOutputStream();
		out.write(data);
		out.flush();
		out.close();
		if (conn.getResponseCode() == 200) {
			return parseXML(conn.getInputStream());
		}
		return null;
	}

	/**
	 * 解析返回的xml数据
	 * 
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static String parseXML(InputStream inputStream) throws Exception {
		XmlPullParser pullparse = Xml.newPullParser();
		pullparse.setInput(inputStream, "UTF-8");
		int event = pullparse.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if ("getMobileCodeInfoResult".equals(pullparse.getName())) {
					return pullparse.nextText();
				}
				break;
			}
			event = pullparse.next();
		}
		return null;
	}

	/**
	 * 读取上输入流到字节
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static byte[] ReadInputStram(InputStream inputStream)
			throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = inputStream.read(buf)) != -1) {
			outStream.write(buf, 0, len);
		}
		return outStream.toByteArray();
	}
}