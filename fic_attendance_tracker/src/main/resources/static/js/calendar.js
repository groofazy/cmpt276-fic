document.addEventListener('DOMContentLoaded', () => {
    
    // Set date for each column header
    const dayHeaders = document.querySelectorAll('.day-header');
    const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    
    // Get the current date and day of the week
    const today = new Date();
    const currentDay = today.getDay();
    
    // Calculate the exact date of the last recent Monday
    let distanceToMonday = -6; // Sunday
    if (currentDay !== 0) {
        distanceToMonday = 1 - currentDay;
    }
    const mondayDate = new Date(today);
    mondayDate.setDate(today.getDate() + distanceToMonday);

    // Loop through the 7 column headers and add the dates
    if (dayHeaders.length > 0) {
        dayHeaders.forEach((header, index) => {
            const colDate = new Date(mondayDate);
            colDate.setDate(mondayDate.getDate() + index);
            
            // Format to DD/MM/YYYY
            const dd = String(colDate.getDate()).padStart(2, '0');
            const mm = String(colDate.getMonth() + 1).padStart(2, '0');
            const yyyy = colDate.getFullYear();
            
            // Add the day name with the date underneath it
            header.innerHTML = `${dayNames[index]}<br>
                <span class="text-muted" style="font-weight: normal; font-size: 0.85em;">
                    ${dd}/${mm}/${yyyy}
                </span>`;
        });
    }

    // Display the available classroom on the calendar
    const classrooms = document.querySelectorAll('.map-value');
    if (classrooms.length === 0) {
        return;
    }

    const hourHeight = 50;

    const dayColumns = [
        document.getElementById('day-0'),
        document.getElementById('day-1'),
        document.getElementById('day-2'),
        document.getElementById('day-3'),
        document.getElementById('day-4'),
        document.getElementById('day-5'),
        document.getElementById('day-6')
    ];

    // Find start and end hours
    let minHour = 24;
    let maxHour = 0;
    const parsedClasses = [];

    // Helper function to convert time to decimal
    function parseTimeToDecimal(timeStr) {
        const times = timeStr.split(':');
        return parseInt(times[0]) + (parseInt(times[1]) / 60);
    }

    // Scan all available classroom
    classrooms.forEach((classroom) => {
        const mapId = classroom.getAttribute('data-id');
        const className = classroom.getAttribute('data-name');
        const lectureDateStr = classroom.getAttribute('data-date');
        const classTimeStr = classroom.getAttribute('data-time');

        if (!lectureDateStr || !classTimeStr){
            return;
        }
        
        // Convert date string to date
        const lectureDate = new Date(lectureDateStr + 'T00:00:00');
        const dayIndex = (lectureDate.getDay() + 6) % 7; 
        
        // Check if any day index is error
        if (dayIndex < 0 || dayIndex > 6) {
            return;
        }

        // Parse the time string to find start and end times
        const timeMatch = classTimeStr.match(/(\d{2}:\d{2})\s*-\s*(\d{2}:\d{2})/);
        
        if (timeMatch) {
            const startDecimal = parseTimeToDecimal(timeMatch[1]);
            const endDecimal = parseTimeToDecimal(timeMatch[2]);

            // Update min and max hours dynamically
            minHour = Math.min(minHour, Math.floor(startDecimal));
            maxHour = Math.max(maxHour, Math.ceil(endDecimal));

            // Save classes that already parsed to array
            parsedClasses.push({mapId, className, dayIndex, startDecimal, endDecimal});
        }
    });

    // Default values if no available classes
    // Default will be from 8am to 5pm
    if (minHour === 24) minHour = 8;
    if (maxHour === 0) maxHour = 17;

    // Draw Dynamic Time Labels based on the min and max hours
    const timeLabelsContainer = document.querySelector('.time-labels');
    if (timeLabelsContainer) {
        timeLabelsContainer.innerHTML = '<div class="time-header"></div>';
        
        for (let i = minHour; i < maxHour; i++) {
            // Handle am/pm
            const ampm = (i >= 12 && i < 24) ? 'pm' : 'am';
            let displayHour = i % 12;
            if (displayHour === 0) displayHour = 12;
            
            const timeSlot = document.createElement('div');
            timeSlot.className = 'time-slot';
            timeSlot.textContent = `${displayHour} ${ampm}`;
            timeLabelsContainer.appendChild(timeSlot);
        }
    }

    // Adjust the height of the calendar grid lines
    const gridHeight = (maxHour - minHour) * hourHeight;
    dayColumns.forEach(col => {
        if (col) col.style.height = `${gridHeight}px`;
    });

    // Place the Class Blocks to the Calendar
    parsedClasses.forEach(classroom => {

        // Calculate position relative to the new dynamic minHour
        const topPos = (classroom.startDecimal - minHour) * hourHeight;
        const height = (classroom.endDecimal - classroom.startDecimal) * hourHeight;

        const block = document.createElement('a');
        block.href = `/maps/view/${classroom.mapId}`;
        block.className = 'class-block';
        block.style.top = `${topPos}px`;
        block.style.height = `${height}px`;

        block.innerHTML = `
            <div class="fw-bold">${classroom.className}</div>
            <div>LEC</div>
            <i class="fas fa-thumbtack pin"></i>
        `;

        // Append to the correct column
        if (dayColumns[classroom.dayIndex]) {
            dayColumns[classroom.dayIndex].appendChild(block);
        }
    });
});
