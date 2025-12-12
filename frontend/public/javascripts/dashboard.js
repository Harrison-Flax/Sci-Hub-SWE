// Dashboard Configuration is handled here
// Decided to do it here instead of in the rajobList.scala.html
// Since it's easier in JavaScript to understand and write along with easier debugging
// Charts are done with Chart.js as specified in Sprint 1

// This code is also refactored from the rajobList.scala.html for better learning

const DashboardState = {
    allJobs: [],
    deptChart: null,
    payChart: null
};

// Initialization of Dashboard
document.addEventListener('DOMContentLoaded', function() {
    console.log("Initializing Dashboard...");
    
    // Load data from RA_JOBS_DATA array in HTML
    if (window.RA_JOBS_DATA && window.RA_JOBS_DATA.length > 0) {
        DashboardState.allJobs = window.RA_JOBS_DATA;

        // For the filter dropdown menu
        populateFilters(DashboardState.allJobs);

        // Initial charts to show
        renderDashboard(DashboardState.allJobs);
    } else {
        console.warn("No job data found for dashboard.");
        document.getElementById("total-jobs-display").innerText = "0";
    }

    // Filters event listeners
    ['deptFilter', 'modeFilter', 'statusFilter'].forEach(id => {
        document.getElementById(id)?.addEventListener('change', filterDashboard);
    });

    // Reset Button
    document.getElementById("resetDashButton")?.addEventListener('click', function() {
        // Reset filters
        document.getElementById('deptFilter').value = "";
        document.getElementById('modeFilter').value = "";
        document.getElementById('statusFilter').value = "";
        // Reset charts
        renderDashboard(DashboardState.allJobs);
    });

    // Export to file button
    document.getElementById("exportDashButton")?.addEventListener('click', function() {
         // Export current department chart as PNG (easier than csv, etc.)
         const link = document.createElement('a');
         link.href = DashboardState.deptChart.toBase64Image();
         link.download = 'department_chart.png';
         link.click();
    });
});

// Populate filter dropdowns
function populateFilters(jobs) {
    const addOptions = (id, field) => {
        const select = document.getElementById(id);
        if(!select) return;

        const uniqueValues = new Set(jobs.map(job => job[field] || "Unknown"));

        // Default option
        select.innerHTML = '<option value="">All</option>'; 

        uniqueValues.forEach(value => {
            const option = document.createElement('option');
            option.value = value;
            option.text = value;
            select.appendChild(option);
        });
    };

    // Add to dropdown
    addOptions('deptFilter', 'organization');
    addOptions('modeFilter', 'location');
    addOptions('statusFilter', 'status');
}

// Filtering logic
function filterDashboard() {
    const dept = document.getElementById('deptFilter').value;
    const mode = document.getElementById('modeFilter').value;
    const status = document.getElementById('statusFilter').value;

    const filteredJobs = DashboardState.allJobs.filter(job => {
        return (dept === "" || job.organization === dept) &&
               (mode === "" || job.location === mode) &&
               (status === "" || job.status === status);
    });

    renderDashboard(filteredJobs);
}

// Render dashboard charts and stats
function renderDashboard(jobs) {
    updateSummaryStats(jobs);
    renderDeptChart(jobs);
    renderPayChart(jobs);
    renderLocationChart(jobs);
}

function updateSummaryStats(jobs) {
    // Total jobs
    document.getElementById("total-jobs-display").innerText = jobs.length;

    // Departments
    const depts = new Set(jobs.map(job => job.organization || "Unknown")); 
    document.getElementById("total-depts-display").innerText = depts.size;

    // Average Pay
    let totalPay = 0;
    let count = 0;
    jobs.forEach(job => {
        const min = job.minSalary || 0;
        const max = job.maxSalary || 0;
        if (max > 0) {
            totalPay += (min + max) / 2;
            count++;
        }
    });
    // Calculate average pay in one line instead of if-else statements
    const avg = count > 0 ? (totalPay / count).toFixed(2) : "0.00";
    document.getElementById("average-pay-display").innerText = "$" + avg + "/hr";
}

function renderDeptChart(jobs) {
    const ctx = document.getElementById('deptChartCanvas').getContext('2d');

    // Jobs per department
    const deptCounts = {};
    jobs.forEach(job => {
        const dept = job.organization || "Unknown";
        deptCounts[dept] = (deptCounts[dept] || 0) + 1;
    });

    const labels = Object.keys(deptCounts);
    const data = Object.values(deptCounts);

    // Destroy old chart
    if (DashboardState.deptChart) {
        DashboardState.deptChart.destroy();
    }
    
    // Building bar chart with Chart.js as specified in Sprint 1
    // Reference: https://www.chartjs.org/docs/latest/samples/bar/vertical.html
    DashboardState.deptChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Jobs per Department',
                data: data,
                backgroundColor: 'rgba(54, 162, 235, 0.6)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 } }
            }
        }
    });
}

function renderPayChart(jobs) {
    const ctx = document.getElementById('payRangeCanvas').getContext('2d');

    // Pay Ranges
    let buckets = {
        '<$15/hr': 0,
        '$15-$20/hr': 0,
        '$20+': 0,
        'Unpaid': 0
    };

    jobs.forEach(job => {
        const min = job.minSalary || 0;
        const max = job.maxSalary || 0;
        const avg = (min + max) / 2;

        if (avg < 15) {
            buckets['<$15/hr']++;
        } else if (avg < 20) {
            buckets['$15-$20/hr']++;
        } else if (avg > 20) {
            buckets['$20+']++;
        } else {
            buckets['Unpaid']++;
        }
    });

    const labels = Object.keys(buckets);
    const data = Object.values(buckets);

    // Destroy old chart
    if (DashboardState.payChart) {
        DashboardState.payChart.destroy();
    }

    // Building bar chart with Chart.js as specified in Sprint 1
    // Reference: https://www.chartjs.org/docs/latest/samples/bar/vertical.html
    DashboardState.payChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Jobs per Pay Range',
                data: data,
                backgroundColor: 'rgba(75, 192, 192, 0.6)'
            }]
        },
        options: {
            responsive: true
        }
    });
}

// Multi Series Pie Chart for Job Locations
function renderLocationChart(jobs) {
    const ctx = document.getElementById('locationChart').getContext('2d');
    const locationCounts = {};

    jobs.forEach(job => {
        const location = job.location || "Unknown";
        locationCounts[location] = (locationCounts[location] || 0) + 1;
    });

    const labels = Object.keys(locationCounts);
    const data = Object.values(locationCounts);

    // Destroy old chart
    if (DashboardState.locationChart) {
        DashboardState.locationChart.destroy();
    }

    // Building multi series pie chart with Chart.js as an additional visualization
    // Showing up as a main pie chart since it's only reading in the location which is fine
    // Reference: https://www.chartjs.org/docs/latest/samples/other-charts/multi-series-pie.html
    DashboardState.locationChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                label: 'Jobs per Location',
                data: data,
                backgroundColor: [
                    'rgba(255, 99, 132, 0.6)',
                    'rgba(54, 162, 235, 0.6)',
                    'rgba(255, 206, 86, 0.6)',
                    'rgba(75, 192, 192, 0.6)',
                    'rgba(153, 102, 255, 0.6)',
                    'rgba(255, 159, 64, 0.6)'
                ],
                borderColor: [
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 159, 64, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 } }
            }
        }
    });
}