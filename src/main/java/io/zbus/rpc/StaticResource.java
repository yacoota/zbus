package io.zbus.rpc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import io.zbus.kit.FileKit;
import io.zbus.kit.HttpKit;
import io.zbus.kit.HttpKit.UrlInfo;
import io.zbus.rpc.annotation.Route;
import io.zbus.transport.Message;
import io.zbus.transport.http.Http;

@Route(exclude=true) //default exclude all methods
public class StaticResource {
	private String basePath = ".";
	private String urlPrefix = "";
	private FileKit fileKit = new FileKit();
	
	private File absoluteBasePath = new File(basePath).getAbsoluteFile();
	 
	public void setBasePath(String basePath) {
		if(basePath == null) {
			basePath = ".";
		}
		this.basePath = basePath; 
		File file = new File(this.basePath);
		if(file.isAbsolute()) {
			absoluteBasePath = file;
		} else {
			absoluteBasePath = new File(System.getProperty("user.dir"), basePath).getAbsoluteFile();
		}
		try {
			absoluteBasePath = absoluteBasePath.toPath().toRealPath().toFile();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	 
	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}
	 
	public void setCacheEnabled(boolean cacheEnabled) {
		this.fileKit.setCacheEnabled(cacheEnabled);
	}
	
	@Route("/")
	public Message file(Message req) {
		Message res = new Message();
		String url = req.getUrl();
		if(url.startsWith(this.urlPrefix)) {
			url = url.substring(this.urlPrefix.length());
		}
		UrlInfo info = HttpKit.parseUrl(url);
		String urlFile = info.urlPath;
		if(urlFile == null) { //missing replace with default
			urlFile = "index.html";
		}
		// gz first
		boolean isGzip = false;
		File tgtFile;
		File gzip = new File(absoluteBasePath, urlFile+".gz");
		if (gzip.exists()) {
			res.setHeader("Content-Encoding", "gzip");
			isGzip = true;
			tgtFile = gzip;
		} else {
			tgtFile = new File(absoluteBasePath, urlFile);
		}

		Path realPath;
		try {
			realPath = tgtFile.toPath().toRealPath();
		} catch (Throwable e) {
			res.setStatus(404);
			res.setHeader(Http.CONTENT_TYPE, "text/plain; charset=utf8");
			res.setBody(urlFile + " Not Found");
			return res;
		}
		// 安全检查，必须要在basePath目录里的文件
		if (!realPath.startsWith(absoluteBasePath.toPath())) {
			res.setStatus(404);
			res.setHeader(Http.CONTENT_TYPE, "text/plain; charset=utf8");
			res.setBody(urlFile + " Not Found");
			return res;
		}

		String contentType = HttpKit.contentType(urlFile);
		if(contentType == null) {
			contentType = "application/octet-stream";
		}
		
		res.setHeader(Http.CONTENT_TYPE, contentType);   
		res.setStatus(200); 
		try {
			String file = realPath.toFile().getAbsolutePath();
			byte[] data = fileKit.loadFileBytes(file);
			if(!isGzip && HttpKit.isText(contentType)) {
				res.setBody(new String(data, "utf8")); //TODO
			} else {
				res.setBody(data);
			}
		} catch (IOException e) {
			res.setStatus(404);
			res.setHeader(Http.CONTENT_TYPE, "text/plain; charset=utf8");
			res.setBody(urlFile + " Not Found");
		}  
		return res;
	}
}
