import "@/styling/dashboard/DashboardStatCards.css";

const mockStats = [
    {
        label: "Hochgeladene Dateien",
        value: "198",
    },
    {
        label: "Erfolgreiche Dateien",
        value: "160",
        change: "80,81%",
        trend: "up",
    },
    {
        label: "Fehlgeschlagene Dateien",
        value: "38",
        change: "19,19%",
        trend: "down",
    },
];

export default function DashboardStatCards() {
    return (
        <div className="stat-cards">
            {mockStats.map((stat) => (
                <div className="stat-card" key={stat.label}>
                    <span className="stat-card__label">{stat.label}</span>
                    <span className="stat-card__value">{stat.value}</span>
                    <span className={`stat-card__change stat-card__change--${stat.trend}`}>
                        {stat.change}
                    </span>
                </div>
            ))}
        </div>
    );
}