document.addEventListener('DOMContentLoaded', () => {
        const subjectSelect = document.getElementById('subject');
    const numberSelect = document.getElementById('number');

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
});
