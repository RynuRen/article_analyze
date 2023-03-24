package com.test.news.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.test.news.dto.Board;
import com.test.news.dto.NewsForm;

@Mapper
public interface BoardMapper {

    List<Board> selectAll();

    Board selectById(Integer id);

    List<Integer> selectByNewsId(Integer id);

    List<String> selectByComment(Integer id);

    void inputCurNews(NewsForm.response curNews);

    void write(Board board);

    void putList(@Param("id") Integer id, @Param("boardIdList") List<Integer> boardIdList,
            @Param("boardNewsComment") List<String> boardNewsComment);

    void update(Board board);

    void updateComment(@Param("boardComment") List<String> boardComment, @Param("boardId") Integer id,
            @Param("newsIdList") List<Integer> newsIdList);

    void delete(int id);

    public List<Board> getBoardList(@Param("startRow") int startRow, @Param("pageSize") int pageSize);

    public int getBoardCount();

    public List<Board> searchBoardList(@Param("keyword") String keyword, @Param("searchType") String searchType,
            @Param("startRow") int startRow,
            @Param("pageSize") int pageSize);

    public int getBoardCountByKeyword(@Param("keyword") String keyword);

}
