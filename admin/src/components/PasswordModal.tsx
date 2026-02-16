import { useState } from 'react';
import './PasswordModal.css';

interface PasswordModalProps {
  isOpen: boolean;
  onSubmit: (password: string) => void;
  message?: string;
}

export const PasswordModal = ({ isOpen, onSubmit, message }: PasswordModalProps) => {
  const [password, setPassword] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (password.trim()) {
      onSubmit(password);
      setPassword('');
    }
  };

  return (
    <div className="password-modal-overlay">
      <div className="password-modal-content" onClick={(e) => e.stopPropagation()}>
        <h2>관리자 인증</h2>
        <p className="password-modal-description">
          {message ||
            '관리자 전용 페이지입니다. 조회/수정/삭제/생성 기능 사용을 위해 관리자 비밀번호가 필요합니다.'}
        </p>
        <form onSubmit={handleSubmit}>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호 입력"
            className="password-input"
            autoFocus
          />
          <div className="password-modal-actions">
            <button type="submit" className="password-submit-button">
              확인
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
