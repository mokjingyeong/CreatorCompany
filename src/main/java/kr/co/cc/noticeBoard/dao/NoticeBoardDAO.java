package kr.co.cc.noticeBoard.dao;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;

import kr.co.cc.noticeBoard.dto.NoticeBoardDTO;

public interface NoticeBoardDAO {

   ArrayList<NoticeBoardDTO> list();

   int write(String id, String subject, String content);

   void upHit(String id);

   NoticeBoardDTO detail(String id);

   int del(String id);

void noticeFile(String fileName, String newFileName, String classification, String id);



}