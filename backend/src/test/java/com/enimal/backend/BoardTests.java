package com.enimal.backend;

import com.enimal.backend.dto.Board.BoardRegistDto;
import com.enimal.backend.entity.Board;
import com.enimal.backend.entity.User;
import com.enimal.backend.repository.*;
import com.enimal.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BoardTests {
    UserRepository userRepository;
    BoardRepository boardRepository;
    @Autowired
    public BoardTests( UserRepository userRepository, BoardRepository boardRepository){
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
    }
    @Test
    void 자유게시판_등록(){
        String userId = "test";
        String title = "자유게시판 제목";
        String content = "자유게시판 내용";
        Byte[] picture = {1,2,3,4,5,3,2,1};
        BoardRegistDto boardRegistDto = new BoardRegistDto();
        boardRegistDto.setUserId(userId);
        boardRegistDto.setTitle(title);
        boardRegistDto.setContent(content);
        boardRegistDto.setPicture(picture);
        Board board = new Board();
        Optional<User> user = userRepository.findById(userId);
        board.setUser(user.get());
        board.setTitle(boardRegistDto.getTitle());
        board.setContent(boardRegistDto.getContent());
        board.setPicture(boardRegistDto.getPicture());
        board.setCreatedate(LocalDateTime.now());
        board.setModifydate(LocalDateTime.now());
        board.setView(0);
        boardRepository.save(board);
        System.out.println(board.getUser().getId());
        System.out.println(board.getTitle());
        System.out.println(board.getContent());
        System.out.println(board.getPicture());
    }
    @Test
    void 자유게시판_삭제(){
        Integer idx = 1;
        boardRepository.deleteById(idx);
    }
}
