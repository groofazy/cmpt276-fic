// Handle clicked seat
// Iteration 1 only change the color when seat is clicked
// Future Iteration will have the logic with selected seat
document.addEventListener('DOMContentLoaded', () => {
    const seats = document.querySelectorAll('.seat');
    seats.forEach(seat => {
        seat.addEventListener('click', function() {
            this.classList.toggle('selected');
        });
    });
});
