import type { ApiClient } from "../api-client"
import { GamificationProfile } from '@/models/gamification';
import { ApiResponse } from '@/models/api-response';

export const createGamificationApi = (client: ApiClient) => ({
    getGamificationProfile: async (): Promise<ApiResponse<GamificationProfile>> => {
        return client.get('/api/gamification/profile');
    },

    // Admin testing
    addGamificationPoints: async (points: number, userId?: string): Promise<ApiResponse<void>> => {
        const params = new URLSearchParams();
        params.append('points', points.toString());
        if (userId) params.append('userId', userId);
        return client.post(`/api/gamification/test/add-points?${params.toString()}`);
    }
});
