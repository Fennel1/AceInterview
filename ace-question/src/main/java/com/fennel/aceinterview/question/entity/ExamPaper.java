package com.fennel.aceinterview.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 考试试卷信息表
 * @TableName qms_exam_paper
 */
@TableName(value ="qms_exam_paper")
@Data
public class ExamPaper {
    /**
     * 试卷唯一ID
     */
    @TableId(type = IdType.INPUT) // MyBatis-Plus: 告诉MP这个ID是外部输入的 (UUID)
    private String paperId;

    /**
     * 创建该试卷模板的用户ID (关联用户表)
     */
    private Long userId;

    /**
     * 试卷标题
     */
    private String title;

    /**
     * 试卷描述或说明
     */
    private String description;

    /**
     * 该试卷包含的题目ID列表 (例如: [101, 105, 203], 对应 qms_question.id)
     */
    private Object questionIds;

    /**
     * 试卷总分 (字符串类型, 例如 "100", "150", 或特定描述如 "合格/不合格")
     */
    private String totalPossibleScore;

    /**
     * 建议考试时长 (单位: 分钟)
     */
    private Integer durationMinutes;

    /**
     * 试卷模板创建时间
     */
    private Date creationTime;

    /**
     * 试卷模板更新时间
     */
    private Date updateTime;

    /**
     * 试卷状态 (例如: DRAFT草稿, PUBLISHED已发布, ARCHIVED已归档)
     */
    private String status;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ExamPaper other = (ExamPaper) that;
        return (this.getPaperId() == null ? other.getPaperId() == null : this.getPaperId().equals(other.getPaperId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getQuestionIds() == null ? other.getQuestionIds() == null : this.getQuestionIds().equals(other.getQuestionIds()))
            && (this.getTotalPossibleScore() == null ? other.getTotalPossibleScore() == null : this.getTotalPossibleScore().equals(other.getTotalPossibleScore()))
            && (this.getDurationMinutes() == null ? other.getDurationMinutes() == null : this.getDurationMinutes().equals(other.getDurationMinutes()))
            && (this.getCreationTime() == null ? other.getCreationTime() == null : this.getCreationTime().equals(other.getCreationTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPaperId() == null) ? 0 : getPaperId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getQuestionIds() == null) ? 0 : getQuestionIds().hashCode());
        result = prime * result + ((getTotalPossibleScore() == null) ? 0 : getTotalPossibleScore().hashCode());
        result = prime * result + ((getDurationMinutes() == null) ? 0 : getDurationMinutes().hashCode());
        result = prime * result + ((getCreationTime() == null) ? 0 : getCreationTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", paperId=").append(paperId);
        sb.append(", userId=").append(userId);
        sb.append(", title=").append(title);
        sb.append(", description=").append(description);
        sb.append(", questionIds=").append(questionIds);
        sb.append(", totalPossibleScore=").append(totalPossibleScore);
        sb.append(", durationMinutes=").append(durationMinutes);
        sb.append(", creationTime=").append(creationTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", status=").append(status);
        sb.append("]");
        return sb.toString();
    }
}