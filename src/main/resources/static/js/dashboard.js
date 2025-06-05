// dashboard.js
class DashboardManager {
    constructor() {
        this.currentDate = new Date();
        this.challengeData = {};
        this.init();
    }

    init() {
        // DOM이 로드된 후 초기화
        document.addEventListener('DOMContentLoaded', () => {
            this.loadChallengeData();
            this.generateCalendar();
            this.bindEvents();
        });
    }

    // 서버에서 전달받은 챌린지 데이터 로드
    loadChallengeData() {
        // 타임리프에서 전달받은 데이터를 JavaScript 객체로 변환
        if (window.challengeCalendarData) {
            this.challengeData = window.challengeCalendarData;
        }
    }

    // 이벤트 바인딩
    bindEvents() {
        // 챌린지 완료 버튼
        document.querySelectorAll('.complete-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleCompleteChallenge(e));
        });

        // 챌린지 삭제 버튼
        document.querySelectorAll('.delete-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleDeleteChallenge(e));
        });

        // 챌린지 수정 버튼
        document.querySelectorAll('.edit-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleEditChallenge(e));
        });

        // 모달 외부 클릭 시 닫기
        const modal = document.getElementById('dayDetailModal');
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.closeDayModal();
                }
            });
        }

        // ESC 키로 모달 닫기
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeDayModal();
            }
        });
    }

    // 캘린더 생성
    generateCalendar() {
        const calendar = document.getElementById('calendar');
        if (!calendar) return;

        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();

        // 캘린더 제목 업데이트
        const calendarTitle = document.getElementById('calendarTitle');
        if (calendarTitle) {
            calendarTitle.textContent = `${year}년 ${month + 1}월`;
        }

        // 캘린더 초기화
        calendar.innerHTML = '';

        // 요일 헤더
        const daysOfWeek = ['일', '월', '화', '수', '목', '금', '토'];
        daysOfWeek.forEach(day => {
            const dayHeader = document.createElement('div');
            dayHeader.className = 'calendar-day-header';
            dayHeader.textContent = day;
            calendar.appendChild(dayHeader);
        });

        // 이번 달 첫날과 마지막날
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const firstDayOfWeek = firstDay.getDay();
        const daysInMonth = lastDay.getDate();

        // 이전 달 마지막 날들
        const prevMonth = new Date(year, month, 0);
        const daysInPrevMonth = prevMonth.getDate();

        for (let i = firstDayOfWeek - 1; i >= 0; i--) {
            const dayElement = document.createElement('div');
            dayElement.className = 'calendar-day other-month';
            dayElement.innerHTML = `<div class="day-number">${daysInPrevMonth - i}</div>`;
            calendar.appendChild(dayElement);
        }

        // 이번 달 날들
        for (let day = 1; day <= daysInMonth; day++) {
            const dayElement = document.createElement('div');
            dayElement.className = 'calendar-day';

            const dateString = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            dayElement.dataset.date = dateString;

            // 오늘 날짜 표시
            const today = new Date();
            if (year === today.getFullYear() && month === today.getMonth() && day === today.getDate()) {
                dayElement.classList.add('today');
            }

            // 날짜 번호
            const dayNumber = document.createElement('div');
            dayNumber.className = 'day-number';
            dayNumber.textContent = day;
            dayElement.appendChild(dayNumber);

            // 챌린지 데이터가 있는 경우
            if (this.challengeData[dateString]) {
                dayElement.classList.add('has-challenges');

                const indicatorsContainer = document.createElement('div');
                indicatorsContainer.className = 'challenge-indicators';

                const challenges = this.challengeData[dateString];
                Object.entries(challenges).forEach(([challenge, status]) => {
                    const dot = document.createElement('div');
                    dot.className = `challenge-dot ${status}`;
                    dot.title = `${challenge}: ${this.getStatusText(status)}`;
                    indicatorsContainer.appendChild(dot);
                });

                dayElement.appendChild(indicatorsContainer);
            }

            dayElement.onclick = () => this.showDayDetail(dateString, day);
            calendar.appendChild(dayElement);
        }

        // 다음 달 첫날들
        const totalCells = calendar.children.length - 7; // 헤더 제외
        const remainingCells = 42 - totalCells; // 6주 × 7일

        for (let day = 1; day <= remainingCells; day++) {
            const dayElement = document.createElement('div');
            dayElement.className = 'calendar-day other-month';
            dayElement.innerHTML = `<div class="day-number">${day}</div>`;
            calendar.appendChild(dayElement);
        }
    }

    // 상태 텍스트 반환
    getStatusText(status) {
        switch (status) {
            case 'completed':
                return '완료';
            case 'missed':
                return '실패';
            case 'pending':
                return '예정';
            default:
                return '예정';
        }
    }

    // 날짜 상세 정보 표시
    showDayDetail(dateString, day) {
        const challenges = this.challengeData[dateString];
        if (!challenges || Object.keys(challenges).length === 0) {
            alert('해당 날짜에는 챌린지가 없습니다.');
            return;
        }

        const modal = document.getElementById('dayDetailModal');
        const modalTitle = document.getElementById('modalTitle');
        const dayChallenges = document.getElementById('dayChallenges');

        if (!modal || !modalTitle || !dayChallenges) return;

        modalTitle.textContent = `${this.currentDate.getMonth() + 1}월 ${day}일 내 챌린지`;

        dayChallenges.innerHTML = '';

        Object.entries(challenges).forEach(([challenge, status]) => {
            const challengeItem = document.createElement('div');
            challengeItem.className = `day-challenge-item ${status}`;
            challengeItem.innerHTML = `
                <div class="day-challenge-title">${challenge}</div>
                <div class="day-challenge-status ${status}">${this.getStatusText(status)}</div>
                ${status === 'pending' ? `<button class="quick-complete-btn" onclick="dashboardManager.quickComplete('${dateString}', '${challenge}')">빠른 완료</button>` : ''}
            `;
            dayChallenges.appendChild(challengeItem);
        });

        modal.classList.add('show');
    }

    // 빠른 완료 기능
    async quickComplete(dateString, challenge) {
        try {
            const response = await fetch('/api/challenges/complete', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    date: dateString,
                    challengeName: challenge
                })
            });

            if (response.ok) {
                // 로컬 데이터 업데이트
                if (this.challengeData[dateString] && this.challengeData[dateString][challenge]) {
                    this.challengeData[dateString][challenge] = 'completed';
                }

                this.generateCalendar(); // 캘린더 새로고침
                this.closeDayModal();
                this.showSuccessMessage(`${challenge} 챌린지가 완료되었습니다! 🎉`);

                // 페이지 새로고침으로 최신 데이터 반영
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                throw new Error('챌린지 완료 처리 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('Error completing challenge:', error);
            alert('챌린지 완료 처리 중 오류가 발생했습니다.');
        }
    }

    // 모달 닫기
    closeDayModal() {
        const modal = document.getElementById('dayDetailModal');
        if (modal) {
            modal.classList.remove('show');
        }
    }

    // 이전 달
    previousMonth() {
        this.currentDate.setMonth(this.currentDate.getMonth() - 1);
        this.generateCalendar();
    }

    // 다음 달
    nextMonth() {
        this.currentDate.setMonth(this.currentDate.getMonth() + 1);
        this.generateCalendar();
    }

    // 챌린지 완료 처리
    async handleCompleteChallenge(event) {
        const btn = event.target;
        const challengeItem = btn.closest('.challenge-item');
        const challengeId = challengeItem.dataset.challengeId;

        if (btn.classList.contains('completed')) {
            // 완료 취소
            try {
                const response = await fetch(`/api/challenges/${challengeId}/uncomplete`, {
                    method: 'POST'
                });

                if (response.ok) {
                    btn.textContent = '✓ 오늘 완료';
                    btn.classList.remove('completed');
                    btn.style.background = '#28a745';
                    this.generateCalendar();
                }
            } catch (error) {
                console.error('Error uncompleting challenge:', error);
            }
        } else {
            // 완료 처리
            try {
                const response = await fetch(`/api/challenges/${challengeId}/complete`, {
                    method: 'POST'
                });

                if (response.ok) {
                    btn.textContent = '✅ 완료됨';
                    btn.classList.add('completed');
                    btn.style.background = '#6c757d';
                    this.generateCalendar();
                    this.showSuccessMessage('챌린지가 완료되었습니다! 🎉');
                }
            } catch (error) {
                console.error('Error completing challenge:', error);
            }
        }
    }

    // 챌린지 삭제 처리
    async handleDeleteChallenge(event) {
        const btn = event.target;
        const challengeItem = btn.closest('.challenge-item');
        const challengeId = challengeItem.dataset.challengeId;
        const challengeTitle = challengeItem.querySelector('.challenge-title').textContent;

        if (!confirm(`정말로 "${challengeTitle}" 챌린지를 삭제하시겠습니까?`)) {
            return;
        }

        try {
            const response = await fetch(`/api/challenges/${challengeId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                challengeItem.style.opacity = '0.5';
                setTimeout(() => {
                    challengeItem.remove();
                    // 챌린지가 모두 삭제되었으면 empty state 표시
                    this.checkEmptyState();
                }, 300);
                this.showSuccessMessage('챌린지가 삭제되었습니다.');
            } else {
                throw new Error('챌린지 삭제 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('Error deleting challenge:', error);
            alert('챌린지 삭제 중 오류가 발생했습니다.');
        }
    }

    // 챌린지 수정 처리
    handleEditChallenge(event) {
        const btn = event.target;
        const challengeItem = btn.closest('.challenge-item');
        const challengeId = challengeItem.dataset.challengeId;

        // 챌린지 관리 페이지로 이동 (수정 모드)
        window.location.href = `/challenges/edit/${challengeId}`;
    }

    // 빈 상태 확인
    checkEmptyState() {
        const challengesList = document.querySelector('.my-challenges');
        const challengeItems = challengesList.querySelectorAll('.challenge-item');

        if (challengeItems.length === 0) {
            const emptyState = document.createElement('div');
            emptyState.className = 'empty-state';
            emptyState.innerHTML = `
                <p>등록된 챌린지가 없습니다.</p>
                <p>새로운 챌린지를 추가해보세요!</p>
            `;
            challengesList.appendChild(emptyState);
        }
    }

    // 성공 메시지 표시
    showSuccessMessage(message) {
        // 간단한 토스트 메시지 구현
        const toast = document.createElement('div');
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #28a745;
            color: white;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            z-index: 1000;
            animation: slideIn 0.3s ease-out;
        `;
        toast.textContent = message;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideOut 0.3s ease-in forwards';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    // 통계 원형 차트 업데이트
    updateAchievementChart(percentage) {
        const circleChart = document.querySelector('.circle-chart');
        if (circleChart) {
            const degree = (percentage / 100) * 360;
            circleChart.style.background = `conic-gradient(#667eea ${degree}deg, #e9ecef ${degree}deg)`;
        }

        const percentElement = document.querySelector('.achievement-percent');
        if (percentElement) {
            percentElement.textContent = `${percentage}%`;
        }
    }

    // 참여자 카드 클릭 처리
    handleParticipantClick(participantId) {
        window.location.href = `/participants/${participantId}`;
    }
}

// 전역 인스턴스 생성
const dashboardManager = new DashboardManager();

// 전역 함수들 (타임리프에서 호출할 수 있도록)
function previousMonth() {
    dashboardManager.previousMonth();
}

function nextMonth() {
    dashboardManager.nextMonth();
}

function closeDayModal() {
    dashboardManager.closeDayModal();
}

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);