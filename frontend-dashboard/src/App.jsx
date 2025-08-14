import React, { useState, useEffect, useRef } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Bar } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';

// Register Chart.js components
ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

// --- Reusable UI Components ---

const StatusIndicator = ({ connected }) => (
    <div className="flex items-center space-x-3 p-4 bg-gray-800 rounded-lg shadow-lg">
        <div className={`w-4 h-4 rounded-full ${connected ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`}></div>
        <span className="font-semibold text-lg">{connected ? 'Connected' : 'Disconnected'}</span>
    </div>
);

const StatCard = ({ title, value, icon, color }) => (
    <div className={`p-6 rounded-xl shadow-lg flex items-center space-x-4 bg-gray-800 border-l-4 ${color}`}>
        <div className="text-4xl">{icon}</div>
        <div>
            <p className="text-gray-400 text-sm font-medium uppercase">{title}</p>
            <p className="text-3xl font-bold">{value.toLocaleString()}</p>
        </div>
    </div>
);

// --- Main Dashboard Component ---

const App = () => {
    const [analytics, setAnalytics] = useState({
        totalRequests: 0,
        allowedRequests: 0,
        blockedRequests: 0,
    });
    const [isConnected, setIsConnected] = useState(false);
    const stompClient = useRef(null);

    useEffect(() => {
        const connect = () => {
            // The URL to your monitoring service's WebSocket endpoint
            const socket = new SockJS('http://localhost:8081/ws-monitoring');
            stompClient.current = Stomp.over(socket);

            // Disable debug logging in production
            stompClient.current.debug = (str) => {
                // console.log(str); // Uncomment for debugging
            };

            stompClient.current.connect({}, (frame) => {
                setIsConnected(true);
                console.log('Connected: ' + frame);

                // Subscribe to the analytics topic
                stompClient.current.subscribe('/topic/analytics', (message) => {
                    const newAnalytics = JSON.parse(message.body);
                    setAnalytics(newAnalytics);
                });
            }, (error) => {
                console.error('Connection error: ', error);
                setIsConnected(false);
                // Attempt to reconnect after a delay
                setTimeout(connect, 5000);
            });
        };

        connect();

        // Cleanup on component unmount
        return () => {
            if (stompClient.current && stompClient.current.connected) {
                stompClient.current.disconnect();
                console.log("Disconnected");
            }
        };
    }, []);

    const chartData = {
        labels: ['Allowed', 'Blocked'],
        datasets: [
            {
                label: 'Request Status',
                data: [analytics.allowedRequests, analytics.blockedRequests],
                backgroundColor: [
                    'rgba(75, 192, 192, 0.6)',
                    'rgba(255, 99, 132, 0.6)',
                ],
                borderColor: [
                    'rgba(75, 192, 192, 1)',
                    'rgba(255, 99, 132, 1)',
                ],
                borderWidth: 1,
                borderRadius: 5,
            },
        ],
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false,
            },
            title: {
                display: true,
                text: 'Allowed vs. Blocked Requests',
                color: '#E5E7EB',
                font: {
                    size: 18,
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    color: '#9CA3AF',
                    stepSize: 1,
                },
                grid: {
                    color: 'rgba(255, 255, 255, 0.1)',
                }
            },
            x: {
                ticks: {
                    color: '#9CA3AF',
                },
                grid: {
                    display: false,
                }
            },
        },
    };

    return (
        <div className="bg-gray-900 text-white min-h-screen p-4 sm:p-6 md:p-8">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <header className="flex justify-between items-center mb-8">
                    <h1 className="text-3xl font-bold tracking-tight">API Rate Limiter Dashboard</h1>
                    <StatusIndicator connected={isConnected} />
                </header>

                {/* Main Content */}
                <main>
                    {/* Stat Cards */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                        <StatCard
                            title="Total Requests"
                            value={analytics.totalRequests}
                            icon="📈"
                            color="border-blue-500"
                        />
                        <StatCard
                            title="Allowed Requests"
                            value={analytics.allowedRequests}
                            icon="✅"
                            color="border-green-500"
                        />
                        <StatCard
                            title="Blocked Requests"
                            value={analytics.blockedRequests}
                            icon="🚫"
                            color="border-red-500"
                        />
                    </div>

                    {/* Chart */}
                    <div className="bg-gray-800 p-6 rounded-xl shadow-lg">
                        <div className="h-80">
                            <Bar data={chartData} options={chartOptions} />
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default App;
