/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.javastack.ramljstorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RAML-JStorage for RAML API Designer
 * 
 * @author Guillermo Grandes / guillermo.grandes[at]gmail.com
 */
public class RAMLJStorage extends HttpServlet {
	private static final String CONF_FILE = "/raml-jstorage.properties";
	private static final String STORAGE_PARAM = "raml.jstorage.directory";
	private static final Charset LATIN1 = Charset.forName("ISO-8859-1");
	private static final long serialVersionUID = 42L;
	private File storeDir = null;

	public RAMLJStorage() {
	}

	@Override
	public void init() throws ServletException {
		String cfgDir = null;
		// Try System Property
		if (cfgDir == null) {
			try {
				cfgDir = System.getProperty(STORAGE_PARAM);
			} catch (Exception e) {
			}
		}
		// Try System Environment
		if (cfgDir == null) {
			try {
				cfgDir = System.getenv(STORAGE_PARAM);
			} catch (Exception e) {
			}
		}
		// Try Config file
		if (cfgDir == null) {
			final Properties p = new Properties();
			try {
				log("Searching " + CONF_FILE.substring(1) + " in classpath");
				p.load(this.getClass().getResourceAsStream(CONF_FILE));
			} catch (IOException e) {
				throw new ServletException(e);
			}
			// getServletContext().getRealPath("/WEB-INF/storage/")
			log("Searching " + STORAGE_PARAM + " in config file");
			cfgDir = p.getProperty(STORAGE_PARAM);
		}
		// Throw Error
		if (cfgDir == null) {
			throw new ServletException("Invalid param for: " + STORAGE_PARAM);
		}
		try {
			this.storeDir = new File(cfgDir).getCanonicalFile();
		} catch (IOException e) {
			throw new ServletException(e);
		}
		log("Storage Path: " + storeDir);
		storeDir.mkdirs();
	}

	@Override
	public void destroy() {
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final String key = getPathInfoKey(request.getPathInfo());
		if (key == null) {
			log("Invalid request: path=null");
			final PrintWriter out = response.getWriter();
			sendError(response, out, HttpServletResponse.SC_BAD_REQUEST, "Bad request");
			return;
		}
		InputStream is = null;
		OutputStream os = null;
		try {
			final File f = fileForKey(key);
			if (f.length() >= Integer.MAX_VALUE) {
				final PrintWriter out = response.getWriter();
				log("Invalid request: data too large");
				sendError(response, out, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Too large");
				return;
			}
			is = openInput(key);
			os = response.getOutputStream();
			response.setContentType("application/raml+yaml");
			response.setContentLength((int) f.length());
			copyStream(is, os);
		} catch (FileNotFoundException e) {
			final PrintWriter out = response.getWriter();
			log("Invalid request: file not found");
			sendError(response, out, HttpServletResponse.SC_NOT_FOUND, "Not Found");
			return;
		} catch (Exception e) {
			log("Invalid request: " + e.toString(), e);
			final PrintWriter out = response.getWriter();
			sendError(response, out, HttpServletResponse.SC_BAD_REQUEST, "Bad request");
			return;
		} finally {
			closeQuietly(os);
			closeQuietly(is);
		}
	}

	@Override
	protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final PrintWriter out = response.getWriter();
		final String key = getPathInfoKey(request.getPathInfo());
		if (key == null) {
			log("Invalid request: path=null");
			sendError(response, out, HttpServletResponse.SC_BAD_REQUEST, "Bad request");
			return;
		}
		if (request.getContentLength() >= Integer.MAX_VALUE) {
			log("Invalid request: data too large");
			sendError(response, out, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Too large");
			return;
		}
		InputStream is = null;
		OutputStream os = null;
		try {
			is = request.getInputStream();
			os = openOutput(key);
			copyStream(is, os);
			response.setContentType("application/json");
			out.println("{\"status\":\"success\",\"message\":\"The file was successfully updated.\"}");
		} catch (Exception e) {
			log("Invalid request: " + e.toString(), e);
			sendError(response, out, HttpServletResponse.SC_BAD_REQUEST, "Bad request");
			return;
		} finally {
			closeQuietly(os);
			closeQuietly(is);
			out.flush();
		}
	}

	@Override
	protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final PrintWriter out = response.getWriter();
		final String key = getPathInfoKey(request.getPathInfo());
		if (key == null) {
			log("Invalid request: path=null");
			sendError(response, out, HttpServletResponse.SC_BAD_REQUEST, "Bad request");
			return;
		}
		try {
			final File f = fileForKey(key);
			f.delete();
		} catch (Exception e) {
			log("Invalid request: " + e.toString(), e);
			sendError(response, out, HttpServletResponse.SC_BAD_REQUEST, "Bad request");
			return;
		}
		response.setContentType("application/json");
		response.setContentLength(2);
		out.print("{}");
		out.flush();
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final String p_operation = getPathInfoKey(request.getPathInfo());
		if (p_operation == null) {
			final PrintWriter out = response.getWriter();
			sendError(response, out, HttpServletResponse.SC_BAD_REQUEST, "Bad request");
			return;
		}

		try {
			if (p_operation.equals("/test")) {
				final String res = "{\"status\":\"ok\"}\n";
				final PrintWriter out = response.getWriter();
				response.setContentType("application/json");
				response.setContentLength(res.length());
				out.print(res);
				out.flush();
				return;
			} else if (p_operation.equals("/list")) {
				listFiles(request, response);
				return;
			} else if (p_operation.equals("/rename")) {
				final String p_source = request.getParameter("source");
				final String p_destination = request.getParameter("destination");
				log("POST operation=" + p_operation + " source=" + p_source + " destination=" + p_destination);
				final File fsrc = fileForKey(getPathInfoKey(p_source));
				final File fdst = fileForKey(getPathInfoKey(p_destination));
				fsrc.renameTo(fdst);
			} else if (p_operation.equals("/createFolder")) {
				final String p_path = request.getParameter("path");
				log("POST operation=" + p_operation + " path=" + p_path);
				final File fpath = fileForKey(getPathInfoKey(p_path));
				fpath.mkdirs();
			} else {
				final PrintWriter out = response.getWriter();
				sendError(response, out, HttpServletResponse.SC_BAD_REQUEST, "Bad request");
				return;
			}
		} catch (Exception e) {
			log("Invalid request: " + e.toString(), e);
			final PrintWriter out = response.getWriter();
			sendError(response, out, HttpServletResponse.SC_BAD_REQUEST, "Bad request");
			return;
		}
		final PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setContentLength(2);
		out.print("{}");
		out.flush();
	}

	private final File fileForKey(final String key) throws IOException {
		final File f = new File(storeDir, key).getCanonicalFile();
		final String root = storeDir.getPath() + File.separatorChar;
		if (!f.getPath().startsWith(root)) {
			log("INVALID PATH: key=" + key + " (" + f.getPath() + ")");
			throw new FileNotFoundException("Invalid path");
		}
		log("Resolved key=" + key + " to=" + f.getPath() + " chroot=" + storeDir.getPath());
		return f;
	}

	private final InputStream openInput(final String key) throws IOException {
		final File f = fileForKey(key);
		return new FileInputStream(f);
	}

	private final OutputStream openOutput(final String key) throws IOException {
		final File f = fileForKey(key);
		return new FileOutputStream(f);
	}

	private final void listFiles(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException {
		final String list = listFiles(storeDir);
		final byte[] b = list.getBytes(LATIN1);
		final OutputStream os = response.getOutputStream();
		response.setContentType("application/json");
		response.setContentLength(b.length);
		os.write(b);
		os.flush();
	}

	private static final String listFiles(final File file) {
		final StringBuilder sb = new StringBuilder();
		listFiles(sb, file, file.getPath().length());
		if (sb.charAt(sb.length() - 1) == ',')
			sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	private static final StringBuilder listFiles(final StringBuilder sb, final File file, final int rootLength) {
		final String path = mapPath(file.getPath().substring(rootLength));
		final String name = (path.length() == 1 ? "" : file.getName());
		sb.append("{");
		sb.append("\"path\":\"").append(path).append("\",");
		sb.append("\"name\":\"").append(name).append("\",");
		sb.append("\"type\":\"").append(file.isFile() ? "file" : "folder").append("\",");
		sb.append("\"children\":[");
		final File[] children = file.listFiles();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				final File child = children[i];
				listFiles(sb, child, rootLength);
			}
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
		}
		sb.append("]");
		sb.append("},");
		return sb;
	}

	private static final String mapPath(final String path) {
		if (path.isEmpty()) {
			return "/";
		}
		if (File.separatorChar != '/') {
			return path.replace(File.separatorChar, '/');
		}
		return path;
	}

	private static final void closeQuietly(final InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	private static final void closeQuietly(final OutputStream os) {
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
			}
		}
	}

	private static final void copyStream(final InputStream is, final OutputStream os) throws IOException {
		final byte[] b = new byte[4096];
		while (true) {
			final int rlen = is.read(b);
			if (rlen < 0)
				break;
			os.write(b, 0, rlen);
		}
		os.flush();
	}

	private static final void sendError(final HttpServletResponse response, final PrintWriter out,
			final int status, final String msg) {
		response.setContentType("application/json");
		response.setStatus(status);
		out.println("{\"status\":\"error\",\"error\":\"" + msg + "\"}");
	}

	private static final String getPathInfoKey(final String pathInfo) {
		if (pathInfo == null)
			return null;
		if (pathInfo.isEmpty())
			return null;
		if (!checkSafeURLString(pathInfo))
			return null;
		return pathInfo;
	}

	private static final boolean checkSafeURLString(final String in) {
		final int len = in.length();
		for (int i = 0; i < len; i++) {
			final char c = in.charAt(i);
			// [A-Za-z0-9._-]+
			if ((c >= 'A') && (c <= 'Z'))
				continue;
			if ((c >= 'a') && (c <= 'z'))
				continue;
			if ((c >= '0') && (c <= '9'))
				continue;
			switch (c) {
				case '.':
				case '_':
				case '-':
				case '/': // Sub directories
					continue;
			}
			return false;
		}
		return true;
	}
}
