'use client';

import React, { useEffect, useState } from 'react';
import { api } from '@/services/api';
import { GamificationProfile } from '@/models/gamification';
import RankBadge from './RankBadge';

const GamificationProfileSection = () => {
    const [profile, setProfile] = useState<GamificationProfile | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await api.getGamificationProfile();
                if (res.data) {
                    setProfile(res.data);
                }
            } catch (error) {
                console.error("Failed to load gamification profile", error);
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, []);

    if (loading) return <div>Loading cultivation data...</div>;
    if (!profile) return null;

    return (
        <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-md mt-6">
            <h2 className="text-xl font-bold mb-4 border-b pb-2 dark:border-gray-700">Tu Tiên Lộ</h2>

            <div className="flex items-center gap-4 mb-6">
                <div className="text-lg">
                    Cảnh giới: <RankBadge rankName={profile.rankName} rankLevel={profile.rankLevel} className="ml-2 text-xl" />
                </div>
                <div className="text-sm text-gray-500 dark:text-gray-400">
                    (Điểm tu luyện: {profile.points})
                </div>
            </div>

            {/* Achievement Section */}
            <div>
                <h3 className="text-lg font-semibold mb-3">Danh Hiệu</h3>
                {profile.achievements && profile.achievements.length > 0 ? (
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                        {profile.achievements.map((ach) => (
                            <div key={ach.code} className={`p-3 border rounded flex items-center gap-2 ${ach.isEquipped ? 'border-yellow-500 bg-yellow-50 dark:bg-yellow-900/10' : 'border-gray-200 dark:border-gray-700'}`}>
                                {/* Placeholder Icon */}
                                <div className="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center text-xs">
                                    {ach.iconUrl ? <img src={ach.iconUrl} alt="" className="w-full h-full rounded-full" /> : "🏆"}
                                </div>
                                <div>
                                    <div className="font-bold text-sm">{ach.name}</div>
                                    <div className="text-xs text-gray-500">{ach.description}</div>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <p className="text-gray-500 italic">Chưa có danh hiệu nào.</p>
                )}
            </div>
        </div>
    );
};

export default GamificationProfileSection;
