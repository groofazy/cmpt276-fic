document.addEventListener('DOMContentLoaded', () => {
    const seats = document.querySelectorAll('.seat');
    const confirmBtn = document.getElementById('confirmBtn');
    const statusMsg = document.getElementById('statusMsg');
    let selectedSeat = null;

    seats.forEach(seat => {
        seat.addEventListener('click', function() {
            seats.forEach(s => s.classList.remove('selected'));
            this.classList.add('selected');
            selectedSeat = this;

            if (confirmBtn) {
                confirmBtn.disabled = false;
            }

            if (statusMsg) {
                statusMsg.innerHTML = "";
            }
        });
    });

    if (confirmBtn) {
        confirmBtn.addEventListener('click', () => {
            if (selectedSeat && statusMsg) {
                const row = selectedSeat.dataset.row;
                const desk = selectedSeat.dataset.desk;
                const seatNum = selectedSeat.dataset.seat;

                statusMsg.innerHTML = `
                    <div class="alert alert-success">
                        Attendance confirmed ✅<br>
                        Row ${row}, Desk ${desk}, Seat ${seatNum}
                    </div>
                `;
            }
        });
    }
});