import MainLayout from "@/components/layout/MainLayout";
import DashboardStatCards from "@/components/dashboard/DashboardStatCards";

// STYLING

import "@/styling/dashboard/DashboardStatCards.css"

export default function DashboardPage() {
  return (
      <MainLayout>
        <DashboardStatCards/>
      </MainLayout>
  );
}