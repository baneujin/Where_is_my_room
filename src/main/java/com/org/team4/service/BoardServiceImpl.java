package com.org.team4.service;


import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.org.team4.dao.BoardDAO;
import com.org.team4.dao.FilesDAO;
import com.org.team4.dto.BoardDTO;
import com.org.team4.dto.BoardCheckDTO;
import com.org.team4.dto.BoardDetailDTO;
import com.org.team4.dto.FileDTO;
import com.org.team4.util.Util;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class BoardServiceImpl implements BoardService{

	@Autowired
	@Qualifier(value="boardDAO")
	private BoardDAO boardDAO;
	
	@Autowired
	private FilesDAO filesDAO;
	
	@Autowired
	private S3Service s3Service;
	
	
	
	@Override
	public int writerCheck(BoardCheckDTO boardCheckDTO) throws Exception {
		// TODO Auto-generated method stub
		return boardDAO.writerCheck(boardCheckDTO);
	}
	
	

	@Override
	public BoardDetailDTO getUpdateInfo(long id) throws Exception {
		// TODO Auto-generated method stub
		BoardDetailDTO boardDetailDTO =  boardDAO.getUpdateInfo(id);
		List<FileDTO> files = filesDAO.getFileList(id);
		for(FileDTO dto : files) {
			String u = Util.originImgFolder + dto.getRealFileName();
			log.info(u);
			dto.setUrl(s3Service.getFileURL("team4", Util.thumbImgFolder + dto.getRealFileName()));
			log.info(dto.toString());
		}
		boardDetailDTO.setFiles(files);
		log.info(boardDetailDTO.toString());
		return boardDetailDTO;
	}



	@Override
	@Transactional
	public long insertBoard(BoardDTO boardDTO, Map<String, MultipartFile> files) throws Exception {
		try {
			boardDAO.insertBoard(boardDTO);
			long boardId = boardDTO.getId();
			Set<String> fileSet = files.keySet();
			for(String fileNum : fileSet) {
				MultipartFile file = files.get(fileNum);
				if(file.getOriginalFilename().length() == 0) continue;
				String newFileName = Util.makeFileName(file);
				String filePath = Util.getCurrentUploadPath() + newFileName;
				log.info(newFileName);
				FileDTO fileDTO = new FileDTO(boardId, 0, file.getOriginalFilename(), filePath, null);
				s3Service.fileUpload("team4", Util.originImgFolder + filePath, file.getBytes());
				s3Service.fileUpload("team4", Util.thumbImgFolder + filePath, Util.mamkeThumbnail(Util.getType(file.getOriginalFilename()) , s3Service.getFileURL("team4", Util.originImgFolder + filePath)));
				filesDAO.insertFile(fileDTO);
			}
			return boardId;
		} catch (Exception e) {
			log.info(e.getMessage());
			throw e;
		}
	}
	
	

	@Override
	@Transactional
	public void update(BoardDTO boardDTO, List<Long> imgs, Map<String, MultipartFile> files) throws Exception {
		// TODO Auto-generated method stub
		int x = boardDAO.updateBoard(boardDTO);
		if(x == 0) throw new Exception();
		filesDAO.updateAndDelete(imgs);
		Set<String> fileSet = files.keySet();
		long boardId = boardDTO.getId();
		for(String fileNum : fileSet) {
			MultipartFile file = files.get(fileNum);
			if(file.getOriginalFilename().length() == 0) continue;
			String newFileName = Util.makeFileName(file);
			String filePath = Util.getCurrentUploadPath() + newFileName;
			log.info(newFileName);
			FileDTO fileDTO = new FileDTO(boardId, 0, file.getOriginalFilename(), filePath, null);
			s3Service.fileUpload("team4", Util.originImgFolder + filePath, file.getBytes());
			s3Service.fileUpload("team4", Util.thumbImgFolder + filePath, Util.mamkeThumbnail(Util.getType(file.getOriginalFilename()) , s3Service.getFileURL("team4", Util.originImgFolder + filePath)));
			filesDAO.insertFile(fileDTO);
		}
	}



	@Override
	@Transactional
	public BoardDetailDTO detail(long id) throws Exception {
		// TODO Auto-generated method stub
		try {
			BoardDetailDTO boardDetailDTO = boardDAO.detail(id);
			if(boardDetailDTO == null) throw new Exception("존재하지 않는 게시글입니다");
			List<FileDTO> files = filesDAO.getFileList(id);
			for(FileDTO dto : files) {
				String u = Util.originImgFolder + dto.getRealFileName();
				log.info(u);
				dto.setUrl(s3Service.getFileURL("team4", Util.originImgFolder + dto.getRealFileName()));
			}
			boardDetailDTO.setFiles(files);
			log.info(boardDetailDTO.toString());
			return boardDetailDTO;
		} catch(Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	@Override
	public int deleteBoard(BoardCheckDTO boardDeleteDTO) throws Exception {
		// TODO Auto-generated method stub
		int status = boardDAO.delete(boardDeleteDTO); 
		if(status == 0) throw new Exception();
		return status;
	}
}