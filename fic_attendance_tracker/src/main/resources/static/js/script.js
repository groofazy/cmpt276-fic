document.addEventListener('DOMContentLoaded', () => {
    const mapData = document.getElementById('map-data');
    if (!mapData) return;

    const mapId    = parseInt(mapData.dataset.mapId);
    const userId   = parseInt(mapData.dataset.userId);
    const userName = mapData.dataset.userName;
    const userRole = mapData.dataset.userRole;
    const isStudent = userRole === 'STUDENT';
    const isTeacher = userRole === 'TEACHER';

    const instructionText = document.getElementById('instruction-text');
    const confirmBtn    = document.getElementById('confirm-btn');
    const modal         = document.getElementById('confirm-modal');
    const modalLabel    = document.getElementById('modal-seat-label');

    let selectedSeat = null;
    let hasSavedSeat = false;

    // Load existing seat data
    fetch(`/seat/load/${mapId}`)
        .then(res => res.json())
        .then(seats => {
            seats.forEach(s => {
                const btn = document.querySelector(`.seat[data-row="${s.seatRow}"][data-number="${s.seatNumber}"]`);
                if (!btn) return;

                btn.classList.remove('seat-available');
                
                // User is student 
                // Their own seat is green
                // Other taken seat is gray
                if (isStudent && s.studentId === userId) {
                    btn.classList.add('seat-mine');
                    btn.innerHTML = `${s.seatNumber}<br><span class="seat-name">${userName}</span>`;
                    hasSavedSeat = true;

                // User is teacher 
                // Other taken seat is blue and can hover    
                } else if (isTeacher && s.studentId !== null) {
                    btn.classList.add('seat-taken-teacher');
                    btn.title = `Student ID: ${s.studentId}\nStudent Name: ${s.studentName || 'N/A'}`;
                
                } else {
                    btn.classList.add('seat-taken');
                    btn.disabled = true;
                }
            });

            if (hasSavedSeat && instructionText) {
                instructionText.style.display = 'none';
            }
        });

    if (!isStudent) return;

    // Seat click handler
    document.querySelectorAll('.seat').forEach(seat => {
        seat.addEventListener('click', () => {
            if (hasSavedSeat) return;
            if (!seat.classList.contains('seat-available') && seat !== selectedSeat) return;

            // Select different seat (already have selected seat)
            if (selectedSeat && selectedSeat !== seat) {
                selectedSeat.classList.remove('seat-selected');
                selectedSeat.classList.add('seat-available');
            }

            // Un-select the selected seat
            if (selectedSeat === seat) {
                seat.classList.remove('seat-selected');
                seat.classList.add('seat-available');
                selectedSeat = null;
                confirmBtn.style.display = 'none';
                
                // Show the instruction text again
                if (instructionText) {
                    instructionText.style.display = 'block'; 
                }
                return;
            }

            // Select new seat (don't have selected seat)
            seat.classList.remove('seat-available');
            seat.classList.add('seat-selected');
            selectedSeat = seat;
            confirmBtn.style.display = 'inline-block';
            
            // Hide the instruction text
            if (instructionText) {
                instructionText.style.display = 'none'; 
            }
        });
    });

    // Confirm button open pop-up
    confirmBtn.addEventListener('click', () => {
        modalLabel.textContent = `${selectedSeat.dataset.row}-${selectedSeat.dataset.number}`;
        document.getElementById('selected-row').value = selectedSeat.dataset.row;
        document.getElementById('selected-number').value = selectedSeat.dataset.number;
        modal.style.display = 'flex';
    });

    // Cancel button close pop-up
    document.getElementById('modal-no').addEventListener('click', () => {
        modal.style.display = 'none';
    });
});
