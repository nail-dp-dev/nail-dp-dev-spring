package com.backend.naildp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.backend.naildp.dto.post.FileRequestDto;
import com.backend.naildp.exception.CustomException;
import com.backend.naildp.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

	private final AmazonS3 amazonS3;
	private Set<String> uploadedFileNames = new HashSet<>();
	private Set<Long> uploadedFileSizes = new HashSet<>();

	@Value("${cloud.aws.s3.bucketName}")
	private String bucket;

	// 여러장의 파일 저장
	public List<FileRequestDto> saveFiles(List<MultipartFile> multipartFiles) {
		List<FileRequestDto> uploadedUrls = new ArrayList<>();

		for (MultipartFile multipartFile : multipartFiles) {

			if (isDuplicate(multipartFile)) {
				throw new CustomException("isDuplicate error", ErrorCode.FILE_EXCEPTION);
			}

			FileRequestDto uploadedUrl = saveFile(multipartFile, false);
			uploadedUrls.add(uploadedUrl);
		}

		clear();
		return uploadedUrls;
	}

	// 파일 삭제
	public void deleteFile(String fileUrl) {
		String[] urlParts = fileUrl.split("/");
		String fileBucket = urlParts[2].split("\\.")[0];

		if (!fileBucket.equals(bucket)) {
			throw new CustomException("Not Exist File", ErrorCode.FILE_EXCEPTION);
		}

		log.info(fileBucket);
		log.info(bucket);
		String objectKey = String.join("/", Arrays.copyOfRange(urlParts, 3, urlParts.length));

		log.info(objectKey);
		log.info(bucket);
		if (!amazonS3.doesObjectExist(bucket, objectKey)) {
			throw new CustomException("Not Exist File", ErrorCode.FILE_EXCEPTION);
		}

		try {
			amazonS3.deleteObject(bucket, objectKey);
		} catch (AmazonS3Exception e) {
			log.error("File delete fail : " + e.getMessage());
			throw new CustomException("File delete fail : ", ErrorCode.FILE_EXCEPTION);
		} catch (SdkClientException e) {
			log.error("AWS SDK client error : " + e.getMessage());
			throw new CustomException("AWS SDK client error : ", ErrorCode.FILE_EXCEPTION);
		}

		log.info("File delete complete: " + objectKey);
	}

	// 단일 파일 저장
	public FileRequestDto saveFile(MultipartFile file, boolean skipExtensionCheck) {
		if (file == null || file.isEmpty()) { //.isEmpty()도 되는지 확인해보기
			throw new CustomException("Not Input files", ErrorCode.FILE_EXCEPTION);
		}
		String randomFilename = generateRandomFilename(file, skipExtensionCheck);

		log.info("File upload started: " + randomFilename);

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		try {
			amazonS3.putObject(bucket, randomFilename, file.getInputStream(), metadata);
		} catch (AmazonS3Exception e) {
			log.error("Amazon S3 error while uploading file: " + e.getMessage());
			throw new CustomException("Amazon S3 error while uploading file: ", ErrorCode.FILE_EXCEPTION);
		} catch (SdkClientException e) {
			log.error("AWS SDK client error while uploading file: " + e.getMessage());
			throw new CustomException("AWS SDK client error while uploading file: ", ErrorCode.FILE_EXCEPTION);
		} catch (IOException e) {
			log.error("IO error while uploading file: " + e.getMessage());
			throw new CustomException("IO error while uploading file: ", ErrorCode.FILE_EXCEPTION);
		}

		log.info("File upload completed: " + randomFilename);

		FileRequestDto fileRequestDto = new FileRequestDto();
		fileRequestDto.setFileUrl(randomFilename);
		fileRequestDto.setFileName(file.getOriginalFilename());
		fileRequestDto.setFileSize(file.getSize());
		return fileRequestDto;
	}

	// 요청에 중복되는 파일 여부 확인
	private boolean isDuplicate(MultipartFile multipartFile) {
		String fileName = multipartFile.getOriginalFilename();
		Long fileSize = multipartFile.getSize();

		if (uploadedFileNames.contains(fileName) && uploadedFileSizes.contains(fileSize)) {
			return true;
		}

		uploadedFileNames.add(fileName);
		uploadedFileSizes.add(fileSize);

		return false;
	}

	private void clear() {
		uploadedFileNames.clear();
		uploadedFileSizes.clear();
	}

	// 랜덤파일명 생성 (파일명 중복 방지)
	private String generateRandomFilename(MultipartFile multipartFile, boolean skipExtensionCheck) {
		String originalFilename = multipartFile.getOriginalFilename();
		String fileExtension =
			skipExtensionCheck ? getFileExtension(originalFilename) : validateFileExtension(originalFilename);
		String randomFilename = UUID.randomUUID() + "." + fileExtension;
		return randomFilename;
	}

	// 파일 확장자 체크
	private String validateFileExtension(String originalFilename) {
		log.info(originalFilename);
		String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
		List<String> allowedExtensions = Arrays.asList("jpg", "png", "gif", "jpeg", "mp4", "mov");

		if (!allowedExtensions.contains(fileExtension)) {
			throw new CustomException("Invalid File Extension", ErrorCode.INVALID_FILE_EXTENSION);
		}
		return fileExtension;
	}

	private String getFileExtension(String originalFilename) {
		return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
		// 용량 제한 필요
	}
}