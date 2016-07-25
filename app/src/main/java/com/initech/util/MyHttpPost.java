package com.initech.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * Title: Client HTTP Request class
 * </p>
 * <p>
 * Description: this class helps to send POST HTTP requests with various form
 * data, including files. Cookies can be added to be included in the request.
 * </p>
 * 
 * @author Vlad Patryshev
 * @version 1.0
 */
public final class MyHttpPost {
	private String fileContentType;
	private URLConnection connection;
	private OutputStream os = null;
	private Map<String, String> cookies = new HashMap<String, String>();

	public void setFileContentType(final String fileContentType) {
		this.fileContentType = fileContentType;
	}

	protected void connect() throws IOException {
		if (os == null)
			os = connection.getOutputStream();
	}

	protected void write(char c) throws IOException {
		connect();
		os.write(c);
	}

	protected void write(String s) throws IOException {
		connect();
		os.write(s.getBytes());
	}

	protected void newline() throws IOException {
		connect();
		write("\r\n");
	}

	protected void writeln(String s) throws IOException {
		connect();
		write(s);
		newline();
	}

	private static Random random = new Random();

	protected static String randomString() {
		return Long.toString(random.nextLong(), 36);
	}

	String boundary = "---------------------------" + randomString()
			+ randomString() + randomString();

	private void boundary() throws IOException {
		write("--");
		write(boundary);
	}

	/**
	 * Creates a new multipart POST HTTP request on a freshly opened
	 * URLConnection
	 * 
	 * @param connection
	 *            an already open URL connection
	 * @throws IOException
	 */
	public MyHttpPost(URLConnection connection) throws IOException {
		this.connection = connection;
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
	}

	/**
	 * Creates a new multipart POST HTTP request for a specified URL
	 * 
	 * @param url
	 *            the URL to send request to
	 * @throws IOException
	 */
	public MyHttpPost(URL url) throws IOException {
		this(url.openConnection());
	}

	/**
	 * Creates a new multipart POST HTTP request for a specified URL string
	 * 
	 * @param urlString
	 *            the string representation of the URL to send request to
	 * @throws IOException
	 */
	public MyHttpPost(String urlString) throws IOException {
		this(new URL(urlString));
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private void postCookies() {
		final StringBuilder cookieList = new StringBuilder();

		for (Iterator i = cookies.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) (i.next());
			cookieList.append(entry.getKey().toString() + "="
					+ entry.getValue());

			if (i.hasNext()) {
				cookieList.append("; ");
			}
		}
		if (cookieList.length() > 0) {
			connection.setRequestProperty("Cookie", cookieList.toString());
		}
	}

	/**
	 * adds a cookie to the requst
	 * 
	 * @param name
	 *            cookie name
	 * @param value
	 *            cookie value
	 * @throws IOException
	 */
	public void setCookie(String name, String value) throws IOException {
		cookies.put(name, value);
	}

	/**
	 * adds cookies to the request
	 * 
	 * @param cookies
	 *            the cookie "name-to-value" map
	 * @throws IOException
	 */
	public void setCookies(Map<String, String> cookies) throws IOException {
		if (cookies == null)
			return;
		this.cookies.putAll(cookies);
	}

	/**
	 * adds cookies to the request
	 * 
	 * @param cookies
	 *            array of cookie names and values (cookies[2*i] is a name,
	 *            cookies[2*i + 1] is a value)
	 * @throws IOException
	 */
	public void setCookies(String[] cookies) throws IOException {
		if (cookies == null)
			return;
		for (int i = 0; i < cookies.length - 1; i += 2) {
			setCookie(cookies[i], cookies[i + 1]);
		}
	}

	private void writeName(String name) throws IOException {
		newline();
		write("Content-Disposition: form-data; name=\"");
		write(name);
		write('"');
	}

	/**
	 * adds a string parameter to the request
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws IOException
	 */
	public void setParameter(String name, String value) throws IOException {
		boundary();
		writeName(name);
		newline();
		newline();
		writeln(value);
	}

	private static void pipe(final InputStream in, final OutputStream out)
			throws IOException {
		byte[] buf = new byte[8096];
		int nread;
		// int navailable;
		int total = 0;
		synchronized (in) {
			while ((nread = in.read(buf, 0, buf.length)) >= 0) {
				out.write(buf, 0, nread);
				total += nread;
			}
		}
		out.flush();
		buf = null;
	}

	public void setParameter(final String name, final String filename, final byte[] bytes)
			throws IOException {
		boundary();
		writeName(name);
		write("; filename=\"");
		write(filename);
		write('"');
		newline();
		write("Content-Type: ");
		if (fileContentType == null) {
			fileContentType = URLConnection.guessContentTypeFromName(filename);
			if (fileContentType == null) {
				fileContentType = "application/octet-stream";
			}
		}
		writeln(fileContentType);
		newline();
		os.write(bytes);
		newline();
	}

	/**
	 * adds a file parameter to the request
	 * 
	 * @param name
	 *            parameter name
	 * @param filename
	 *            the name of the file
	 * @param is
	 *            input stream to read the contents of the file from
	 * @throws IOException
	 */
	public void setParameter(String name, String filename, InputStream is)
			throws IOException {
		boundary();
		writeName(name);
		write("; filename=\"");
		write(filename);
		write('"');
		newline();
		write("Content-Type: ");
		if (fileContentType == null) {
			fileContentType = URLConnection.guessContentTypeFromName(filename);
			if (fileContentType == null) {
				fileContentType = "application/octet-stream";
			}
		}
		writeln(fileContentType);
		newline();
		pipe(is, os);
		newline();
	}

	/**
	 * adds a file parameter to the request
	 * 
	 * @param name
	 *            parameter name
	 * @param file
	 *            the file to upload
	 * @throws IOException
	 */
	public void setParameter(String name, File file) throws IOException {
		setParameter(name, file.getPath(), new FileInputStream(file));
	}

	/**
	 * adds a parameter to the request; if the parameter is a File, the file is
	 * uploaded, otherwise the string value of the parameter is passed in the
	 * request
	 * 
	 * @param name
	 *            parameter name
	 * @param object
	 *            parameter value, a File or anything else that can be
	 *            stringified
	 * @throws IOException
	 */
	public void setParameter(final String name, final Object object)
			throws IOException {
		if (object instanceof File) {
			setParameter(name, (File) object);
		} else {
			setParameter(name, object.toString());
		}
	}

	/**
	 * adds parameters to the request
	 * 
	 * @param parameters
	 *            "name-to-value" map of parameters; if a value is a file, the
	 *            file is uploaded, otherwise it is stringified and sent in the
	 *            request
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public void setParameters(Map parameters) throws IOException {
		if (parameters == null)
			return;
		for (Iterator i = parameters.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			setParameter(entry.getKey().toString(), entry.getValue());
		}
	}

	/**
	 * adds parameters to the request
	 * 
	 * @param parameters
	 *            array of parameter names and values (parameters[2*i] is a
	 *            name, parameters[2*i + 1] is a value); if a value is a file,
	 *            the file is uploaded, otherwise it is stringified and sent in
	 *            the request
	 * @throws IOException
	 */
	public void setParameters(final String[] parameters) throws IOException {
		if (parameters == null)
			return;
		for (int i = 0; i < parameters.length - 1; i += 2) {
			setParameter(parameters[i].toString(), parameters[i + 1]);
		}
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added
	 * 
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post() throws IOException {
		boundary();
		writeln("--");
		os.close();
		return connection.getInputStream();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added before (if any), and with parameters that are passed in
	 * the argument
	 * 
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post(final Map<String, String> parameters)
			throws IOException {
		setParameters(parameters);
		return post();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added before (if any), and with parameters that are passed in
	 * the argument
	 * 
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post(final String[] parameters) throws IOException {
		setParameters(parameters);
		return post();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added before (if any), and with cookies and parameters that are
	 * passed in the arguments
	 * 
	 * @param cookies
	 *            request cookies
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post(final Map<String, String> cookies,
							final Map<String, String> parameters) throws IOException {
		setCookies(cookies);
		setParameters(parameters);
		return post();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added before (if any), and with cookies and parameters that are
	 * passed in the arguments
	 * 
	 * @param cookies
	 *            request cookies
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post(final String[] cookies, final String[] parameters)
			throws IOException {
		setCookies(cookies);
		setParameters(parameters);
		return post();
	}

	/**
	 * post the POST request to the server, with the specified parameter
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post(final String name, final String value)
			throws IOException {
		setParameter(name, value);
		return post();
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post(final String name1, final String value1,
							final String name2, final String value2) throws IOException {
		setParameter(name1, value1);
		return post(name2, value2);
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @param name3
	 *            third parameter name
	 * @param value3
	 *            third parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post(final String name1, final String value1,
							String name2, final String value2, final String name3,
							final String value3) throws IOException {
		setParameter(name1, value1);
		return post(name2, value2, name3, value3);
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @param name3
	 *            third parameter name
	 * @param value3
	 *            third parameter value
	 * @param name4
	 *            fourth parameter name
	 * @param value4
	 *            fourth parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream post(final String name1, final String value1,
							final String name2, final String value2, final String name3,
							final String value3, final String name4, final String value4)
			throws IOException {
		setParameter(name1, value1);
		return post(name2, value2, name3, value3, name4, value4);
	}

	/**
	 * posts a new request to specified URL, with parameters that are passed in
	 * the argument
	 * 
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public static InputStream post(final URL url,
								   final Map<String, String> parameters) throws IOException {
		return new MyHttpPost(url).post(parameters);
	}

	/**
	 * posts a new request to specified URL, with parameters that are passed in
	 * the argument
	 * 
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public static InputStream post(final URL url, final String[] parameters)
			throws IOException {
		return new MyHttpPost(url).post(parameters);
	}

	/**
	 * posts a new request to specified URL, with cookies and parameters that
	 * are passed in the argument
	 * 
	 * @param cookies
	 *            request cookies
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public static InputStream post(final URL url,
								   final Map<String, String> cookies, Map<String, String> parameters)
			throws IOException {
		return new MyHttpPost(url).post(cookies, parameters);
	}

	/**
	 * posts a new request to specified URL, with cookies and parameters that
	 * are passed in the argument
	 * 
	 * @param cookies
	 *            request cookies
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public static InputStream post(final URL url, final String[] cookies,
								   String[] parameters) throws IOException {
		return new MyHttpPost(url).post(cookies, parameters);
	}

	/**
	 * post the POST request specified URL, with the specified parameter
	 * 
	 *            parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public static InputStream post(final URL url, final String name1,
								   final String value1) throws IOException {
		return new MyHttpPost(url).post(name1, value1);
	}

	/**
	 * post the POST request to specified URL, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public static InputStream post(final URL url, final String name1,
								   final String value1, final String name2, final String value2)
			throws IOException {
		return new MyHttpPost(url).post(name1, value1, name2, value2);
	}

	/**
	 * post the POST request to specified URL, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @param name3
	 *            third parameter name
	 * @param value3
	 *            third parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public static InputStream post(final URL url, final String name1,
								   final String value1, final String name2, final String value2,
								   final String name3, final String value3) throws IOException {
		return new MyHttpPost(url).post(name1, value1, name2, value2, name3,
				value3);
	}

	/**
	 * post the POST request to specified URL, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @param name3
	 *            third parameter name
	 * @param value3
	 *            third parameter value
	 * @param name4
	 *            fourth parameter name
	 * @param value4
	 *            fourth parameter value
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public static InputStream post(final URL url, final String name1,
								   final String value1, final String name2, final String value2,
								   final String name3, final String value3, final String name4,
								   final String value4) throws IOException {
		return new MyHttpPost(url).post(name1, value1, name2, value2, name3,
				value3, name4, value4);
	}
}
