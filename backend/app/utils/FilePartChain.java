package utils;

import play.mvc.Http;

import java.io.File;
import java.util.List;

/**
 * Created by Gerald on 4/27/2017.
 */
public class FilePartChain {

	public FilePartChain(Http.MultipartFormData.FilePart<File> filePart) {
		this.filePart = filePart;
	}

	public static FilePartChain buildChain(List<Http.MultipartFormData.FilePart<File>> fileParts) {
		FilePartChain root = new FilePartChain(null), past = root, current;
		for(Http.MultipartFormData.FilePart<File> filePart: fileParts) {
			current = new FilePartChain(filePart);
			past.setNext(current);
			past = current;
		}
		return root;
	}

	Http.MultipartFormData.FilePart<File> filePart = null;
	FilePartChain next = null;

	public FilePartChain getNext() {
		if(next == null)
			return null;
		File file = next.getFile();
		if(!file.exists() || file.length() == 0 )
			return next.getNext();
		return next;
	}

	public void setNext(FilePartChain next) {
		this.next = next;
	}

	public File getFile() {
		return filePart.getFile();
	}

	public String getFilename() {
		return filePart.getFilename();
	}

}
