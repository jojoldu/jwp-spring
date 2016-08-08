package next.service;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;

import next.CannotOperateException;
import next.dao.AnswerDao;
import next.dao.QuestionDao;
import next.model.Answer;
import next.model.Question;
import next.model.User;

public class QnaService {
	private static QnaService qnaService;

	private QuestionDao questionDao;
	private AnswerDao answerDao;

	private QnaService() {
	}

	public static QnaService getInstance() {
		if (qnaService == null) {
			qnaService = new QnaService();
		}
		return qnaService;
	}

	public Question findById(long questionId) {
		return questionDao.findById(questionId);
	}

	public List<Answer> findAllByQuestionId(long questionId) {
		return answerDao.findAllByQuestionId(questionId);
	}

	public boolean deleteQuestion(long questionId, User user) throws CannotOperateException {
		Question question = questionDao.findById(questionId);
		if (question == null) {
			throw new EmptyResultDataAccessException("존재하지 않는 질문입니다.", 1);
		}

		if (!question.isSameUser(user)) {
			throw new CannotOperateException("다른 사용자가 쓴 글을 삭제할 수 없습니다.");
		}

		List<Answer> answers = answerDao.findAllByQuestionId(questionId);
		if (answers.isEmpty()) {
			questionDao.delete(questionId);
			return false;
		}

		boolean canDelete = true;
		for (Answer answer : answers) {
			String writer = question.getWriter();
			if (!writer.equals(answer.getWriter())) {
				canDelete = false;
				break;
			}
		}

		if (!canDelete) {
			throw new CannotOperateException("다른 사용자가 추가한 댓글이 존재해 삭제할 수 없습니다.");
		}

		questionDao.delete(questionId);
		return true;
	}

	public void updateQuestion(long questionId, Question newQuestion, User user) throws CannotOperateException {
		Question question = questionDao.findById(questionId);
        if (question == null) {
        	throw new EmptyResultDataAccessException("존재하지 않는 질문입니다.", 1);
        }
        
        if (!question.isSameUser(user)) {
            throw new CannotOperateException("다른 사용자가 쓴 글을 수정할 수 없습니다.");
        }
        
        question.update(newQuestion);
        questionDao.update(question);
	}
}
