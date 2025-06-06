package com.fennel.aceinterview.question.dao;

import com.fennel.aceinterview.question.entity.QuestionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 八股文题目和解答
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@Mapper
public interface QuestionDao extends BaseMapper<QuestionEntity> {
    /**
     * 根据条件随机获取指定数量的题目ID
     *
     * @param questionTypes 题目类型列表。
     *                      如果为 null 或为空，或者列表中包含 -1，则表示查询所有有效类型。
     * @param count         需要获取的题目ID数量
     * @return 随机获取的题目ID列表
     *
     * 注意: ORDER BY RAND() 在大数据量时性能可能较差。
     * 对于非常大的表，请考虑其他随机抽样策略，例如：
     * 1. 先查询符合条件的ID总数，然后在Java中生成随机偏移量分多次查询。
     * 2. 如果ID是连续的，可以在ID范围内随机选取。
     * 3. 某些数据库有更高效的 TABLESAMPLE 功能。
     */
    @Select("<script>" +
            "SELECT id FROM ace_qms.qms_question " +
            "WHERE del_flag = 0 AND ENABLE = 1 " +
            // 条件：当 questionTypes 不为null，不为空，并且不包含 -1 时，才应用类型过滤
            "<if test='questionTypes != null and !questionTypes.isEmpty() and !questionTypes.contains(-1)'>" +
            "  AND TYPE IN " +
            "  <foreach item='itemType' collection='questionTypes' open='(' separator=',' close=')'>" +
            "    #{itemType}" +
            "  </foreach>" +
            "</if>" +
            "ORDER BY RAND() " + // MySQL 使用 RAND(), PostgreSQL 使用 RANDOM(), SQL Server 使用 NEWID()
            "LIMIT #{count}" +
            "</script>")
    List<Long> findRandomQuestionIds(@Param("questionTypes") List<Integer> questionTypes, @Param("count") int count);

    /**
     * 计算指定ID列表中，存在且有效的题目数量
     *
     * @param questionIds 题目ID列表
     * @return 列表中有效且存在的题目数量
     */
    @Select("<script>" +
            "SELECT COUNT(id) FROM ace_qms.qms_question " +
            "WHERE del_flag = 0 AND ENABLE = 1 AND id IN " +
            "  <foreach item='idItem' collection='questionIds' open='(' separator=',' close=')'>" +
            "    #{idItem}" +
            "  </foreach>" +
            "</script>")
    int countExistingAndEnabledQuestions(@Param("questionIds") List<Long> questionIds);
}
