package com.backend.naildp.common;

import java.util.List;

public class FileExtensionChecker {

	private static final List<String> VIDEO_EXTENSION = List.of("mp4");
	private static final List<String> PHOTO_EXTENSION = List.of("jpg", "png", "gif", "jpeg");

	public static boolean isPhotoExtension(String fileName) {
		String fileExtension = getExtension(fileName);
		return PHOTO_EXTENSION.contains(fileExtension);
	}

	public static boolean isVideoExtension(String fileName) {
		String fileExtension = getExtension(fileName);
		return VIDEO_EXTENSION.contains(fileExtension);
	}

	private static String getExtension(String fileName) {
		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == fileName.length()) {
			return "";
		}
		return fileName.substring(lastDotIndex + 1);
	}
}
