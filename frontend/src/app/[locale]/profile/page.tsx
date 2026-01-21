"use client";

import { AuthGuard } from "@/components/auth/auth-guard";
import { Header } from "@/components/layout/header";
import { MyProfile } from "@/components/user/my-profile";
import { RoleRequestForm } from "@/components/user/role-request-form";
import { MyRoleRequests } from "@/components/user/my-role-requests";
import { ChangePasswordForm } from "@/components/user/change-password-form";
import { Button } from "@/components/ui/button";
import { useState } from "react";

export default function ProfilePage() {
  return (
    <AuthGuard>
      <ProfileContent />
    </AuthGuard>
  );
}

function ProfileContent() {
  const [refreshRequests, setRefreshRequests] = useState(0);
  const [showChangePassword, setShowChangePassword] = useState(false);

  const handleRequestSubmitted = () => {
    setRefreshRequests((prev) => prev + 1);
  };

  return (
    <div className="min-h-screen bg-background">
      <main className="container py-4 sm:py-6 md:py-8">
        <div className="max-w-6xl mx-auto space-y-4 sm:space-y-6 md:space-y-8 px-4">
          <div className="space-y-4 sm:space-y-6">
            <MyProfile />

            <div className="space-y-2">
              <Button
                variant="outline"
                onClick={() => setShowChangePassword((prev) => !prev)}
                className="bg-transparent"
              >
                {showChangePassword ? "Hide Change Password" : "Change Password"}
              </Button>
              {showChangePassword && <ChangePasswordForm />}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
              <RoleRequestForm onRequestSubmitted={handleRequestSubmitted} />
              <div key={refreshRequests}>
                <MyRoleRequests />
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
