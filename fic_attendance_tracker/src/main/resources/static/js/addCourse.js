document.addEventListener('DOMContentLoaded', () => {
    const pageUserRole = document.body.dataset.userRole;
    if (pageUserRole !== 'ADMIN') {
        return;
    }

    const departmentSelect = document.getElementById('department-select');
    const courseNumberSelect = document.getElementById('course-number-select');
    const form = document.querySelector('form[action="/courses/add"]');
    const hiddenContainer = document.getElementById('hidden-times-container');
    const timeList = document.getElementById('time-list');
    const submitButton = document.querySelector('button[type="submit"]');
    const timeError = document.getElementById('course-times-error');
    const modal = document.getElementById('time-modal');
    const btnOpen = document.getElementById('btn-open-modal');
    const btnClose = document.getElementById('btn-close-modal');
    const btnSave = document.getElementById('btn-save-time');

    if (!departmentSelect || !courseNumberSelect || !form || !hiddenContainer || !timeList || !submitButton || !timeError || !modal || !btnOpen || !btnClose || !btnSave) {
        return;
    }

    const resetCourseNumbers = (message, disabled) => {
        courseNumberSelect.innerHTML = `<option value="">${message}</option>`;
        courseNumberSelect.disabled = disabled;
    };

    const loadDepartments = () => {
        fetch('/sfu/departments')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Unable to load departments');
                }

                return response.json();
            })
            .then(departments => {
                departmentSelect.innerHTML = '<option value="" disabled selected>Select a department</option>';

                if (!departments.length) {
                    departmentSelect.innerHTML = '<option value="">No departments available</option>';
                    resetCourseNumbers('Select a department first', true);
                    return;
                }

                departments.forEach(department => {
                    const option = document.createElement('option');
                    option.value = department;
                    option.textContent = department;
                    departmentSelect.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Error fetching departments:', error);
                departmentSelect.innerHTML = '<option value="">Unable to load departments</option>';
                resetCourseNumbers('Unable to load course numbers', true);
            });
    };

    const updateSubmitState = () => {
        const hasTimes = hiddenContainer.querySelectorAll('input[name="courseTimes"]').length > 0;
        submitButton.disabled = !hasTimes;
        timeError.style.display = 'none';
        timeError.textContent = '';
    };

    const observeTimeChanges = () => {
        const observer = new MutationObserver(updateSubmitState);
        observer.observe(timeList, { childList: true, subtree: true });
        observer.observe(hiddenContainer, { childList: true, subtree: true });
        updateSubmitState();
    };

    form.addEventListener('submit', event => {
        const hasTimes = hiddenContainer.querySelectorAll('input[name="courseTimes"]').length > 0;
        if (!hasTimes) {
            event.preventDefault();
            timeError.style.display = 'block';
            timeError.textContent = 'Add at least one course time before submitting.';
        }
    });

    btnOpen.addEventListener('click', () => {
        modal.style.display = 'flex';
    });

    btnClose.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    btnSave.addEventListener('click', () => {
        const selectedDay = document.querySelector('input[name="day"]:checked');
        if (!selectedDay) {
            alert('Please select a day.');
            return;
        }

        const start = `${document.getElementById('start-hh').value}:${document.getElementById('start-mm').value}`;
        const end = `${document.getElementById('end-hh').value}:${document.getElementById('end-mm').value}`;
        const formattedDate = `${selectedDay.value} ${start} - ${end}`;

        const timeBadge = document.createElement('div');
        timeBadge.className = 'alert alert-info py-2 px-3 mb-0 d-flex justify-content-between align-items-center';
        timeBadge.innerHTML = `
            <span><i class="far fa-clock me-2"></i> ${formattedDate}</span>
            <button type="button" class="btn-close" style="font-size: 0.8rem;"></button>
        `;

        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'courseTimes';
        hiddenInput.value = formattedDate;

        timeBadge.querySelector('.btn-close').addEventListener('click', () => {
            timeBadge.remove();
            hiddenInput.remove();
            updateSubmitState();
        });

        timeList.appendChild(timeBadge);
        hiddenContainer.appendChild(hiddenInput);

        selectedDay.checked = false;
        modal.style.display = 'none';
        updateSubmitState();
    });

    departmentSelect.addEventListener('change', function() {
        const department = this.value;

        if (!department) {
            resetCourseNumbers('Select a department first', true);
            return;
        }

        resetCourseNumbers('Loading course numbers...', true);

        fetch(`/sfu/course-numbers?department=${encodeURIComponent(department)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Unable to load course numbers');
                }

                return response.json();
            })
            .then(numbers => {
                if (!numbers.length) {
                    resetCourseNumbers('No course numbers found', true);
                    return;
                }

                courseNumberSelect.innerHTML = '<option value="" disabled selected>Select a course number</option>';
                numbers.forEach(number => {
                    const option = document.createElement('option');
                    option.value = number;
                    option.textContent = number;
                    courseNumberSelect.appendChild(option);
                });
                courseNumberSelect.disabled = false;
            })
            .catch(error => {
                console.error('Error fetching course numbers:', error);
                resetCourseNumbers('Unable to load course numbers', true);
            });
    });

    loadDepartments();
    observeTimeChanges();
});