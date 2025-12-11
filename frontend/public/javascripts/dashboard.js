// Dashboard Configuration is handled here
// Decided to do it here instead of in the researchInterest.scala.html
// Since it's easier in JavaScript to understand and write along with easier debugging

const AppState = {
    rawData: null,
    filteredData: null,
    selectedYear: null,

    // From API
    jobSummary: {
        totalJobs: 0,
        avgPay: "Loading...",
        departments: []
    }
};

// Chart Instances
let charts = {
    donut: null,
    stackedBar: null
}

// Color Palette
const colorPalette = [
    '#008FFB', '#00E396', '#FEB019', '#FF4560', '#775DD0',
    '#546E7A', '#26A69A', '#D10CE8', '#2E93fA', '#66DA26'
];
let topicColorMap = {};

// Initialization of Dashboard
document.addEventListener('DOMContentLoaded', function() {
    console.log("Initializing Dashboard...");
    initDashboard();
});

function initDashboard() {
    // URL from HTML configuration
    const url = window.DASHBOARD_CONFIGURATION.dataURL;

    // Fetch Data
    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error('Network Error!');
            return response.json();
        })
        .then(jsonData => {
            // Store the data in State
            AppState.rawData = jsonData;
            
            // Call setup helpers for charts to show
            setupHelpers(jsonData);

            // Initial Filter
            // First available year (index[0])
            const years = Object.keys(jsonData).sort();
            AppState.selectedYear = years[0];

            render();
        })
        .catch(error => console.error("Error loading dashboard data:", error));

    // Reset Button Event Listener
    document.getElementById("resetFiltersBtn")?.addEventListener('click', handleReset);

    // Switch between charts
    window.toggleView = toggleView;
}

function render() {
    console.log("Rendering Dashboard for Year:", AppState.selectedYear);
    
    // Filter Data based on selected year
    const yearData = AppState.rawData[AppState.selectedYear] || {};

    // Empty data check if there are no jobs found
    const hasData = Object.keys(yearData).length > 0;

    if(!hasData) {
        showEmptyState();
        return;
    } else {
        hideEmptyState();
    }

    // Update Charts
    updateDonutChart(yearData);
    updateStackedBarChart();
    updateSummaryStats();
}

// Additional functions for logic (helpers)

// Job data and summary stats
function updateSummaryStats() {
    // Handle summary stats update
    const jobURL = window.DASHBOARD_CONFIGURATION.jobListAPIURL;

    // Fetch the job data
    fetch(jobURL)
        .then(response => {
            if (!response.ok) throw new Error('Failed to fetch job data!');
            return response.json();
        })
        .then(apiResponse => {
            // Should be data array for API from job data
            const jobs = apiResponse.data || [];

            // Processing the job data
            AppState.jobSummary = {
                totalJobs: jobs.length,
                avgPay: calculateAveragePay(jobs),
                departments: extractDepartments(jobs)
            };
            // Update the DOM Elements
            const totalElements = document.getElementById("totalJobsDisplay");
            const payElements = document.getElementById("average-pay-display");
            
            if (totalElements) totalElements.innerText = AppState.jobSummary.totalJobs;
            if (payElements) payElements.innerText = AppState.jobSummary.avgPay;
        })
        .catch(error => 
            console.error("Error fetching job data:", error));
            // Fall back to 0 if API fails to load (a precaution)
            const totalElements = document.getElementById("totalJobsDisplay");
            if (totalElements) totalElements.innerText = "0";
}

function calculateAveragePay(jobs) {
    if (!jobs || jobs.length === 0) return "$0.00/hr";

    // Sum the average for each job
    const totalPay = jobs.reduce((sum, job) => {
        const min = job.minSalary || 0;
        const max = job.maxSalary || 0;
        // Account for 0 and midpoint
        const jobAverage = (min + max) > 0 ? (min + max) / 2 : 0;
        return sum + jobAverage;
    }, 0);

    const finalAverage = totalPay / jobs.length;
    // Average pay rounded to 2 decimal places
    return `$${finalAverage.toFixed(2)}/hr`;
}

// Counting organizations and departments
function extractDepartments(jobs) {
    if (!jobs || jobs.length === 0) return [];

    const departments = new Set(jobs.map(job => job.organization || "Unknown"));
    return Array.from(departments);
}

function handleReset() {
    const years = Object.keys(AppState.rawData).sort();
    AppState.selectedYear = years[0];

    // Reset the dropdown and calls render with trigger
    $('#yearSelector').val(AppState.selectedYear).trigger('change');
}

function showEmptyState() {
    document.getElementById("pieChart").style.display = 'none';
    document.getElementById("noDataMessage").style.display = 'block';

    // Message for no jobs found
    const msg = document.getElementById("noDataMessage");
    if (msg) msg.style.display = 'block';
}

function hideEmptyState() {
    document.getElementById("pieChart").style.display = 'block';
    document.getElementById("noDataMessage").style.display = 'none';

    // Message for no jobs found
    const msg = document.getElementById("noDataMessage");
    if (msg) msg.style.display = 'block';
}

function setupHelpers(jsonData) {
    // Populate Year Selector
    // Logic from buildYearSelector
    const allYears = Object.keys(jsonData).sort();
    AppState.allYears = allYears;

    const selector = document.getElementById("yearSelector");
    if (selector) {
        selector.innerHTML = ""; 
        allYears.forEach(y => {
            const opt = document.createElement("option");
            opt.value = y;
            opt.text = y;
            selector.appendChild(opt);
        });

        // Initialize Select2
        $('#yearSelector').select2({ width: '100%' }).on('change', function() {
            AppState.selectedYear = $(this).val();
            render();
        });
    }

    // Populate Color Map
    // Logic from topicColorMap
    // Years and Topics
    const allTopicsSet = new Set();
    
    // For each year, find a topic
    allYears.forEach(year => {
        const topics = Object.keys(jsonData[year]);
        topics.forEach(topic => allTopicsSet.add(topic));
    });

    // AppState storage
    AppState.allTopics = Array.from(allTopicsSet).sort();

    // Each topic has a color with the map created
    let colorIndex = 0;
    AppState.allTopics.forEach(topic => {
        // Load through palette if topics exceed colors
        topicColorMap[topic] = colorPalette[colorIndex % colorPalette.length];
        colorIndex++;
    });
}

// Charts Functions
function updateDonutChart(yearData) {
    const labels = Object.keys(yearData);
    const series = Object.values(yearData);

    const donutOptions = {
        chart: {
            type: 'donut',
            height: 350,
            // For exporting and downloading
            toolbar: { show: true }
        },
        series: series,
        labels: labels,
        // Map each topic label to its assigned color
        colors: labels.map(label => topicColorMap[label] || '#999999'),
        legend: {
            position: 'bottom',
            offsetY: -10
        },
        plotOptions: {
            pie: {
                donut: {
                    size: '65%'
                        }
                    }
                },
                noData: { 
                    text: "No data available"
                }
    };

    if (charts.donut) {
        charts.donut.destroy();
    }
    charts.donut = new ApexCharts(document.getElementById("pieChart"), donutOptions);
    charts.donut.render();
}

// Stacked Bar Chart
function updateStackedBarChart() {
    const allTopics = AppState.allTopics;
    const allYears = AppState.allYears;

    // Prepare series data: each topic => array of frequencies across years
    const series = allTopics.map(topic => {
        const dataArr = allYears.map(year => {
            const yearData = AppState.rawData[year] || {};
            return yearData[topic] || 0;
    });
    return { name: topic, data: dataArr };
});

const stackedOptions = {
    chart: {
        type: 'bar',
        stacked: true,
        height: '450px',
        toolbar: { show: true }
    },
    series: series,
    xaxis: {
        categories: allYears
    },
    // Use each series name (topic) to pick the corresponding color
    colors: series.map(s => topicColorMap[s.name] || '#999999'),
    legend: {
        position: 'right'
    },
    plotOptions: {
        bar: {
            horizontal: false
        }
    },
};

if (charts.stackedBar) {
    charts.stackedBar.destroy();
}
    charts.stackedBar = new ApexCharts(document.getElementById("stackedBarChart"), stackedOptions);
    charts.stackedBar.render();
}

// Apply the view toggling
function toggleView(viewType) {
    const pieElement = document.getElementById("pieChart");
    const barElement = document.getElementById("stackedBarChart");

    if (viewType === 'all') {
        pieElement.style.display = 'block';
        barElement.style.display = 'block';
    } else if (viewType === 'pieChart') {
        pieElement.style.display = 'block';
        barElement.style.display = 'none';
    } else if (viewType === 'stackedBarChart') {
        pieElement.style.display = 'none';
        barElement.style.display = 'block';
    }
}