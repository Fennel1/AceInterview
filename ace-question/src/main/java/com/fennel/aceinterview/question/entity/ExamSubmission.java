package com.fennel.aceinterview.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户试卷提交记录表
 * @TableName qms_exam_submission
 */
@TableName(value ="qms_exam_submission")
@Data
public class ExamSubmission {
    /**
     * 提交记录唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long submissionId;

    /**
     * 对应的试卷ID (关联 exam_paper.paper_id)
     */
    private String paperId;

    /**
     * 提交试卷的用户ID (关联用户表)
     */
    private Long userId;

    /**
     * 用户提交的答案 (例如: {"101": "选项A的文本或ID", "105": "用户填写的解答文本", "203": {"selected_option_id": 5, "answer_text":"补充说明"}})
     */
    private Object answers;

    /**
     * 用户开始答题时间
     */
    private Date startTime;

    /**
     * 用户提交答卷时间
     */
    private Date submissionTime;

    /**
     * 得分 (字符串类型, 例如 "85", "92.5", "优秀", "通过")
     */
    private String score;

    /**
     * 判卷详情
     */
    private Object gradingDetails;

    /**
     * 提交状态 (例如: IN_PROGRESS进行中, SUBMITTED已提交, GRADING批改中, GRADED已批改)
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
        ExamSubmission other = (ExamSubmission) that;
        return (this.getSubmissionId() == null ? other.getSubmissionId() == null : this.getSubmissionId().equals(other.getSubmissionId()))
            && (this.getPaperId() == null ? other.getPaperId() == null : this.getPaperId().equals(other.getPaperId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getAnswers() == null ? other.getAnswers() == null : this.getAnswers().equals(other.getAnswers()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getSubmissionTime() == null ? other.getSubmissionTime() == null : this.getSubmissionTime().equals(other.getSubmissionTime()))
            && (this.getScore() == null ? other.getScore() == null : this.getScore().equals(other.getScore()))
            && (this.getGradingDetails() == null ? other.getGradingDetails() == null : this.getGradingDetails().equals(other.getGradingDetails()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSubmissionId() == null) ? 0 : getSubmissionId().hashCode());
        result = prime * result + ((getPaperId() == null) ? 0 : getPaperId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getAnswers() == null) ? 0 : getAnswers().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getSubmissionTime() == null) ? 0 : getSubmissionTime().hashCode());
        result = prime * result + ((getScore() == null) ? 0 : getScore().hashCode());
        result = prime * result + ((getGradingDetails() == null) ? 0 : getGradingDetails().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", submissionId=").append(submissionId);
        sb.append(", paperId=").append(paperId);
        sb.append(", userId=").append(userId);
        sb.append(", answers=").append(answers);
        sb.append(", startTime=").append(startTime);
        sb.append(", submissionTime=").append(submissionTime);
        sb.append(", score=").append(score);
        sb.append(", gradingDetails=").append(gradingDetails);
        sb.append(", status=").append(status);
        sb.append("]");
        return sb.toString();
    }
}