export interface Achievement {
    code: string;
    name: string;
    description: string;
    iconUrl: string;
    isEquipped: boolean;
}

export interface GamificationProfile {
    points: number;
    rankName: string;
    rankLevel: number;
    rankStyle: string; // e.g. "color: gold;"
    nextRankPoints: number;
    achievements: Achievement[];
    equippedAchievements: Achievement[];
}
