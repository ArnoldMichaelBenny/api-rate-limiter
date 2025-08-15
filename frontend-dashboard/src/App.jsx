import React, { useState, useEffect, useRef, useCallback, memo } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Bar, Doughnut } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    ArcElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';
import { LayoutDashboard, Settings, CheckCircle, XCircle, BarChart2, Wifi, WifiOff, Sun, Moon } from 'lucide-react';

// --- Chart.js Setup ---
ChartJS.register(
    CategoryScale, LinearScale, BarElement, ArcElement,
    Title, Tooltip, Legend
);

// --- Custom Hook for WebSocket Logic ---
const useWebSocketAnalytics = (url) => {
    const [analytics, setAnalytics] = useState({ totalRequests: 0, allowedRequests: 0, blockedRequests: 0 });
    const [history, setHistory] = useState(Array(30).fill(0));
    const [lastActivity, setLastActivity] = useState([]);
    const [isConnected, setIsConnected] = useState(false);
    const stompClient = useRef(null);

    useEffect(() => {
        const connect = () => {
            const socket = new SockJS(url);
            stompClient.current = Stomp.over(socket);
            stompClient.current.debug = () => {};

            stompClient.current.connect({}, () => {
                setIsConnected(true);
                stompClient.current.subscribe('/topic/analytics', (message) => {
                    const newAnalytics = JSON.parse(message.body);

                    setAnalytics(prev => {
                        const totalChange = newAnalytics.totalRequests - (prev.totalRequests || 0);
                        setHistory(prevHistory => [...prevHistory.slice(1), totalChange]);

                        const allowedChange = newAnalytics.allowedRequests > (prev.allowedRequests || 0);
                        const newActivity = {
                            id: Date.now(), // Unique key for list rendering
                            status: allowedChange ? 'ALLOWED' : 'BLOCKED',
                            path: '/api/v1/data',
                            time: new Date().toLocaleTimeString(),
                        };
                        setLastActivity(prevActivity => [newActivity, ...prevActivity.slice(0, 4)]);
                        return newAnalytics;
                    });
                });
            }, () => {
                setIsConnected(false);
                setTimeout(connect, 5000);
            });
        };

        connect();
        return () => stompClient.current?.disconnect();
    }, [url]);

    return { analytics, history, lastActivity, isConnected };
};

// --- Memoized UI Components for Performance ---

const Sidebar = memo(({ theme }) => (
    <aside className={`w-64 p-4 flex-col flex-shrink-0 hidden lg:flex transition-colors duration-300 ${theme === 'dark' ? 'bg-gray-900 border-r border-gray-700' : 'bg-white border-r border-gray-200'}`}>
        <div className="flex items-center space-x-3 mb-10 px-2">
            <div className={`${theme === 'dark' ? 'bg-gray-700' : 'bg-gray-800'} p-2 rounded-lg`}>
                <BarChart2 size={24} className="text-white" />
            </div>
            <h1 className={`text-xl font-bold ${theme === 'dark' ? 'text-gray-100' : 'text-gray-800'}`}>Rate Limiter</h1>
        </div>
        <nav className="flex flex-col space-y-2">
            <a href="#" className={`flex items-center space-x-3 px-3 py-2 rounded-lg font-semibold ${theme === 'dark' ? 'bg-gray-800 text-blue-400' : 'bg-gray-100 text-blue-600'}`}>
                <LayoutDashboard size={20} />
                <span>Dashboard</span>
            </a>
            <a href="#" className={`flex items-center space-x-3 px-3 py-2 rounded-lg transition-colors ${theme === 'dark' ? 'text-gray-400 hover:bg-gray-800' : 'text-gray-500 hover:bg-gray-100'}`}>
                <Settings size={20} />
                <span>Settings</span>
            </a>
        </nav>
        <div className="mt-auto text-center text-xs text-gray-500">
            <p>&copy; 2025 Rate Limiter Inc.</p>
        </div>
    </aside>
));

const Header = memo(({ connected, theme, toggleTheme }) => (
    <header className={`flex-shrink-0 flex justify-between items-center p-6 transition-colors duration-300 ${theme === 'dark' ? 'bg-gray-900 border-b border-gray-700' : 'bg-white border-b border-gray-200'}`}>
        <h2 className={`text-2xl font-bold ${theme === 'dark' ? 'text-gray-100' : 'text-gray-800'}`}>Analytics Dashboard</h2>
        <div className="flex items-center space-x-4">
            <button onClick={toggleTheme} className={`p-2 rounded-full transition-all duration-300 ${theme === 'dark' ? 'text-gray-400 hover:bg-gray-700 hover:text-yellow-300' : 'text-gray-500 hover:bg-gray-200 hover:text-indigo-500'}`}>
                {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
            </button>
            <div className={`flex items-center space-x-2 px-3 py-1.5 rounded-full text-sm font-medium ${connected ? (theme === 'dark' ? 'bg-green-500/20 text-green-400' : 'bg-green-100 text-green-700') : (theme === 'dark' ? 'bg-red-500/20 text-red-400' : 'bg-red-100 text-red-700')}`}>
                {connected ? <Wifi size={16} /> : <WifiOff size={16} />}
                <span>{connected ? 'Real-time Active' : 'Disconnected'}</span>
            </div>
        </div>
    </header>
));

const StatCard = memo(({ title, value, icon, theme }) => (
    <div className={`p-5 rounded-xl shadow-sm transition-all duration-300 ${theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-white border border-gray-200'}`}>
        <p className={`text-sm font-medium ${theme === 'dark' ? 'text-gray-400' : 'text-gray-500'}`}>{title}</p>
        <div className="mt-2 flex items-baseline space-x-2">
            <p className={`text-4xl font-bold ${theme === 'dark' ? 'text-gray-100' : 'text-gray-800'}`}>{value.toLocaleString()}</p>
            {icon}
        </div>
    </div>
));

const RecentActivity = memo(({ lastActivity, theme }) => (
    <div className={`p-5 rounded-xl shadow-sm h-full transition-colors duration-300 ${theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-white border border-gray-200'}`}>
        <h3 className={`font-semibold mb-4 ${theme === 'dark' ? 'text-gray-200' : 'text-gray-800'}`}>Recent Activity</h3>
        <div className="space-y-4">
            {lastActivity.length === 0 && <p className={`text-sm ${theme === 'dark' ? 'text-gray-500' : 'text-gray-400'}`}>No recent activity. Send requests to see live updates.</p>}
            {lastActivity.map((activity) => (
                <div key={activity.id} className="flex items-center space-x-3 text-sm animate-fade-in">
                    {activity.status === 'ALLOWED' ? <CheckCircle className="text-green-500 flex-shrink-0" size={20} /> : <XCircle className="text-red-500 flex-shrink-0" size={20} />}
                    <div className="flex-grow min-w-0">
                        <p className={`font-medium truncate ${theme === 'dark' ? 'text-gray-300' : 'text-gray-700'}`}>{activity.path}</p>
                        <p className={`${theme === 'dark' ? 'text-gray-500' : 'text-gray-400'}`}>{activity.time}</p>
                    </div>
                    <span className={`font-mono text-xs px-2 py-0.5 rounded-full ${activity.status === 'ALLOWED' ? (theme === 'dark' ? 'bg-green-500/20 text-green-400' : 'bg-green-100 text-green-700') : (theme === 'dark' ? 'bg-red-500/20 text-red-400' : 'bg-red-100 text-red-700')}`}>
            {activity.status}
          </span>
                </div>
            ))}
        </div>
    </div>
));

// --- Main App Component ---

const App = () => {
    const { analytics, history, lastActivity, isConnected } = useWebSocketAnalytics('http://localhost:8081/ws-monitoring');
    const [theme, setTheme] = useState('light');

    const toggleTheme = useCallback(() => {
        setTheme(prevTheme => (prevTheme === 'light' ? 'dark' : 'light'));
    }, []);

    const barChartData = { labels: Array(30).fill(''), datasets: [{ data: history, backgroundColor: '#3B82F6', borderRadius: 4 }] };
    const doughnutChartData = { labels: ['Allowed', 'Blocked'], datasets: [{ data: [analytics.allowedRequests, analytics.blockedRequests], backgroundColor: ['#10B981', '#EF4444'], borderColor: theme === 'dark' ? '#1F2937' : '#FFFFFF', borderWidth: 4, hoverOffset: 8 }] };

    const chartOptions = {
        bar: {
            responsive: true,
            maintainAspectRatio: false,
            // ✅ FIX: Add layout padding to keep labels inside the container
            layout: {
                padding: {
                    left: 10,
                    bottom: 10
                }
            },
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false }, ticks: { display: false } },
                y: {
                    grid: { color: theme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : '#E5E7EB' },
                    ticks: { color: theme === 'dark' ? '#9CA3AF' : '#6B7280', stepSize: 1 }
                }
            }
        },
        doughnut: { responsive: true, maintainAspectRatio: false, cutout: '70%', plugins: { legend: { display: false } } }
    };

    return (
        <div className={`flex h-screen font-sans transition-colors duration-300 ${theme === 'dark' ? 'bg-gray-800' : 'bg-gray-50'}`}>
            <Sidebar theme={theme} />
            <div className="flex-1 flex flex-col overflow-hidden">
                <Header connected={isConnected} theme={theme} toggleTheme={toggleTheme} />
                <main className={`flex-1 overflow-x-hidden overflow-y-auto p-6 transition-colors duration-300 ${theme === 'dark' ? 'bg-gray-900' : 'bg-gray-100'}`}>
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        <div className="lg:col-span-2 space-y-6">
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                <StatCard title="Total Requests" value={analytics.totalRequests} icon={<BarChart2 className="text-gray-400" />} theme={theme} />
                                <StatCard title="Allowed Requests" value={analytics.allowedRequests} icon={<CheckCircle className="text-green-500" />} theme={theme} />
                                <StatCard title="Blocked Requests" value={analytics.blockedRequests} icon={<XCircle className="text-red-500" />} theme={theme} />
                            </div>
                            <div className={`p-5 rounded-xl shadow-sm h-96 transition-colors duration-300 ${theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-white border border-gray-200'}`}>
                                <h3 className={`font-semibold mb-4 ${theme === 'dark' ? 'text-gray-200' : 'text-gray-800'}`}>Activity</h3>
                                <Bar data={barChartData} options={chartOptions.bar} />
                            </div>
                        </div>
                        <div className="space-y-6">
                            <div className={`p-5 rounded-xl shadow-sm transition-colors duration-300 ${theme === 'dark' ? 'bg-gray-800 border border-gray-700' : 'bg-white border border-gray-200'}`}>
                                <h3 className={`font-semibold mb-4 ${theme === 'dark' ? 'text-gray-200' : 'text-gray-800'}`}>Request Distribution</h3>
                                <div className="h-60 relative flex items-center justify-center">
                                    <Doughnut data={doughnutChartData} options={chartOptions.doughnut} />
                                    <div className="absolute flex flex-col items-center justify-center">
                                        <span className={`text-3xl font-bold ${theme === 'dark' ? 'text-gray-100' : 'text-gray-800'}`}>{analytics.totalRequests}</span>
                                        <span className={`text-sm ${theme === 'dark' ? 'text-gray-400' : 'text-gray-500'}`}>Total</span>
                                    </div>
                                </div>
                            </div>
                            <RecentActivity lastActivity={lastActivity} theme={theme} />
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default App;
