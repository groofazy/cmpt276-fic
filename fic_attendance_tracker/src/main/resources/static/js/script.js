document.addEventListener('DOMContentLoaded', () => {
    const seats = document.querySelectorAll('.seat');
    const confirmBtn = document.getElementById('confirmBtn');
    const statusMsg = document.getElementById('statusMsg');
    const seatIndexInput = document.getElementById('seatIndexInput');

    let selectedSeat = null;

    seats.forEach(seat => {
        if (seat.classList.contains('occupied')) {
            return;
        }

        seat.addEventListener('click', function () {
            seats.forEach(s => s.classList.remove('selecting'));

            this.classList.remove('mine');
            this.classList.add('selecting');
            selectedSeat = this;

            if (seatIndexInput) {
                seatIndexInput.value = this.dataset.seatIndex;
            }

            if (confirmBtn) {
                confirmBtn.disabled = false;
            }

            if (statusMsg) {
                const row = this.dataset.row;
                const desk = this.dataset.desk;
                const seatNum = this.dataset.seat;

                statusMsg.innerHTML = `
                    <div class="alert alert-info">
                        Selected: Row ${row}, Desk ${desk}, Seat ${seatNum}
                    </div>
                `;
            }
        });
    });
});