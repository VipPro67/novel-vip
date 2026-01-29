import React from 'react';

interface RankBadgeProps {
    rankName: string;
    rankLevel: number; // 0-10
    className?: string;
}

const RankBadge: React.FC<RankBadgeProps> = ({ rankName, rankLevel, className = '' }) => {

    // Determine style based on rank level
    let badgeStyle = "text-gray-500"; // Default Phàm Nhân
    let effectStyle = "";

    if (rankLevel >= 1 && rankLevel <= 3) {
        badgeStyle = "text-green-500 font-medium"; // Luyện Khí
    } else if (rankLevel >= 4 && rankLevel <= 6) {
        badgeStyle = "text-blue-500 font-bold border border-blue-400 rounded px-2"; // Trúc Cơ - Kim Đan
        effectStyle = "shadow-[0_0_10px_rgba(59,130,246,0.5)]"; // Glow
    } else if (rankLevel >= 7 && rankLevel <= 9) {
        badgeStyle = "text-purple-500 font-bold border-2 border-purple-500 rounded px-2 bg-purple-900/10"; // Nguyên Anh - Hóa Thần
        effectStyle = "animate-pulse shadow-[0_0_15px_rgba(168,85,247,0.7)]"; // Pulse + Strong Glow
    } else if (rankLevel >= 10) {
        badgeStyle = "text-red-500 font-extrabold border-2 border-red-500 rounded px-3 bg-red-900/20"; // Đại Thừa +
        effectStyle = "animate-[bounce_2s_infinite] shadow-[0_0_20px_rgba(239,68,68,0.9)] ring-2 ring-red-400"; // "Cháy" effect metaphor
    }

    return (
        <span className={`inline-block ${badgeStyle} ${effectStyle} ${className} transition-all duration-300`}>
            {rankName}
        </span>
    );
};

export default RankBadge;
