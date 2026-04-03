document.addEventListener('DOMContentLoaded', () => {
    const seats = document.querySelectorAll('.seat');
    const confirmBtn = document.getElementById('confirmBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    const statusMsg = document.getElementById('statusMsg');
    const seatIndexInput = document.getElementById('seatIndexInput');

    let selectedSeat = null;
    let originalState = null;

    const existingMineSeat = document.querySelector('.seat.mine');
    if (existingMineSeat) {
        selectedSeat = existingMineSeat;
        originalState = 'mine';
    }

    const setButtonState = (enabled) => {
        if (confirmBtn) {
            confirmBtn.disabled = !enabled;
            confirmBtn.classList.toggle('btn-success', enabled);
            confirmBtn.classList.toggle('btn-secondary', !enabled);
        }

        if (cancelBtn) {
            cancelBtn.disabled = !enabled;
            cancelBtn.classList.toggle('btn-danger', enabled);
            cancelBtn.classList.toggle('btn-secondary', !enabled);
        }
    };

    setButtonState(false);

    seats.forEach(seat => {
        if (seat.classList.contains('occupied')) {
            return;
        }

        seat.addEventListener('click', function () {
            // if user already has a saved grey seat, block all other seats
            if (selectedSeat && selectedSeat.classList.contains('mine') && selectedSeat !== this) {
                if (statusMsg) {
                    statusMsg.innerHTML = `
                        <div class="alert alert-warning">
                            You already have a confirmed seat. Cancel it first before choosing another seat.
                        </div>
                    `;
                }
                return;
            }

            // if user is currently selecting a seat, also block other seats
            if (selectedSeat && selectedSeat.classList.contains('selecting') && selectedSeat !== this) {
                if (statusMsg) {
                    statusMsg.innerHTML = `
                        <div class="alert alert-warning">
                            You already selected a seat. Please cancel it first before choosing another seat.
                        </div>
                    `;
                }
                return;
            }

            const row = this.dataset.row;
            const desk = this.dataset.desk;
            const seatNum = this.dataset.seat;

            // clicking grey seat = switch it to blue for possible cancel
            if (this.classList.contains('mine')) {
                this.classList.remove('mine');
                this.classList.add('selecting');
                selectedSeat = this;
                originalState = 'mine';

                if (seatIndexInput) {
                    seatIndexInput.value = this.dataset.seatIndex;
                }

                setButtonState(true);

                if (statusMsg) {
                    statusMsg.innerHTML = `
                        <div class="alert alert-info">
                            Your current seat is selected. Confirm to keep it, or cancel to remove it.
                        </div>
                    `;
                }
                return;
            }

            if (selectedSeat === this) {
                return;
            }

            const confirmed = window.confirm(
                `Do you confirm selecting Row ${row}, Desk ${desk}, Seat ${seatNum}?`
            );

            if (!confirmed) {
                return;
            }

            this.classList.remove('available');
            this.classList.add('selecting');
            selectedSeat = this;
            originalState = 'available';

            if (seatIndexInput) {
                seatIndexInput.value = this.dataset.seatIndex;
            }

            setButtonState(true);

            if (statusMsg) {
                statusMsg.innerHTML = `
                    <div class="alert alert-info">
                        Selected: Row ${row}, Desk ${desk}, Seat ${seatNum}
                    </div>
                `;
            }
        });
    });

    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            if (selectedSeat) {
                selectedSeat.classList.remove('selecting');
                selectedSeat.classList.remove('mine');
                selectedSeat.classList.add('available');

                selectedSeat = null;
                originalState = null;
            }

            if (seatIndexInput) {
                seatIndexInput.value = "";
            }

            setButtonState(false);

            if (statusMsg) {
                statusMsg.innerHTML = `
                    <div class="alert alert-warning">
                        Selection cancelled. Please choose another seat.
                    </div>
                `;
            }
        });
    }
});