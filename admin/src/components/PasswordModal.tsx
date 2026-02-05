import { useState } from 'react';
import './PasswordModal.css';

interface PasswordModalProps {
  isOpen: boolean;
  onSubmit: (password: string) => void;
  onSkip?: () => void;
}

export const PasswordModal = ({ isOpen, onSubmit, onSkip }: PasswordModalProps) => {
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
          과목 수정/삭제/추가 기능을 사용하려면 관리자 비밀번호를 입력하세요.
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
            {onSkip && (
              <button type="button" onClick={onSkip} className="password-skip-button">
                나중에하기
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};
