document.addEventListener('DOMContentLoaded', () => {
    const subjectSelect = document.getElementById('subject');
    const numberSelect = document.getElementById('number');
    const modal = document.getElementById('time-modal');
    const btnOpen = document.getElementById('btn-open-modal');
    const btnClose = document.getElementById('btn-close-modal');
    const btnSave = document.getElementById('btn-save-time');

    const timeList = document.getElementById('time-list');
    const hiddenContainer = document.getElementById('hidden-times-container');

    // Subject select box event handler
    if (subjectSelect && numberSelect) {
        subjectSelect.addEventListener('change', function() {
            const subject = this.value;

            // Show loading state for course number box
            // When teacher not choose any course subject
            numberSelect.innerHTML = '<option value=""></option>';

            if (!subject) {
                numberSelect.innerHTML = '<option value="">Select a subject first...</option>';
                return;
            }

            // Fetch the course numbers from selected subject
            fetch(`/courses/find?subject=${subject}`)
                .then(response => response.json())
                .then(numbers => {
                    numberSelect.innerHTML = '';
                    
                    // If that subject have no course
                    if (numbers.length === 0) {
                        numberSelect.innerHTML = '<option value="">No courses found for this subject</option>';
                    
                    // If subject have course
                    } else {
                        numberSelect.innerHTML = '<option value="" disabled selected>Select a course number</option>';
                        
                        // Add each number as an option in the dropdown
                        numbers.forEach(num => {
                            const option = document.createElement('option');
                            option.value = num;
                            option.textContent = num;
                            numberSelect.appendChild(option);
                        });
                    }
                })
                .catch(error => {
                    console.error('Error fetching course numbers:', error);
                    numberSelect.innerHTML = '<option value="">Error loading courses</option>';
                });
        });
    }

    // Handling the Course Time Input Form Pop-Up
    // Open & Close button 
    btnOpen.addEventListener('click', () => modal.style.display = 'flex');
    btnClose.addEventListener('click', () => modal.style.display = 'none');

    // Save Time Logic
    btnSave.addEventListener('click', () => {
        
        // Get selected day
        const selectedDay = document.querySelector('input[name="day"]:checked');
        if (!selectedDay) {
            alert("Please select a day.");
            return;
        }

        // Get times directly from the dropdowns
        const start = `${document.getElementById('start-hh').value}:${document.getElementById('start-mm').value}`;
        const end = `${document.getElementById('end-hh').value}:${document.getElementById('end-mm').value}`;
        
        // Format the date & time
        const formattedDate = `${selectedDay.value} ${start} - ${end}`;

        // Create a UI element to show the user
        const timeBadge = document.createElement('div');
        timeBadge.className = 'alert alert-info py-2 px-3 mb-0 d-flex justify-content-between align-items-center';
        timeBadge.innerHTML = `
            <span><i class="far fa-clock me-2"></i> ${formattedDate}</span>
            <button type="button" class="btn-close" style="font-size: 0.8rem;"></button>
        `;

        // Create a hidden input to send to Spring Boot
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'courseTimes';
        hiddenInput.value = formattedDate;

        // Handle delete button
        timeBadge.querySelector('.btn-close').addEventListener('click', () => {
            timeBadge.remove();
            hiddenInput.remove();
        });

        timeList.appendChild(timeBadge);
        hiddenContainer.appendChild(hiddenInput);

        // Reset form and close modal
        selectedDay.checked = false;
        modal.style.display = 'none';
    });
});
