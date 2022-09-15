package com.sosuyu.diary.imageUpload;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sosuyu.diary.security.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
@Log
@RequiredArgsConstructor
@Service
public class UploadS3Service {
	
	 @Autowired private final AmazonS3Client amazonS3Client;
	 
	 @Value("${cloud.aws.s3.bucket}") 
	 private String bucketName;
	 private String uploadPath = "member"; 
	 private ObjectMetadata objectMetadata= new ObjectMetadata();
	 
	 @Autowired 
	 private MemberRepository mRepo; 
	 
	 public String uploadFile(MultipartFile multipartFile,String folderPath) throws EmptyFileException,FileUploadFailedException {
		 String originalName = multipartFile.getOriginalFilename();//파일명:모든 경로를 포함한 파일이름 
			String fileName =originalName.substring(originalName.lastIndexOf("/")+1);

			//UUID 
			String uuid =UUID.randomUUID().toString(); //저장할 파일 이름 중간에 "_"를 이용하여 구분 
			String saveName=uploadPath + File.separator + folderPath +File.separator + uuid + "_" +fileName;
			
			objectMetadata.setContentType(multipartFile.getContentType());

			Path savePath = Paths.get(saveName); 
			try(InputStream inputStream =multipartFile.getInputStream()){ 
				amazonS3Client.putObject(new PutObjectRequest(bucketName, saveName,inputStream,objectMetadata )
						.withCannedAcl(CannedAccessControlList.PublicRead));

			} catch (IOException e) { 
				
				throw new FileUploadFailedException(); 
			}
				String url = amazonS3Client.getUrl(bucketName, saveName).toString();
				log.info(url);
				return url;
			}
	
	 


	 
	
}
