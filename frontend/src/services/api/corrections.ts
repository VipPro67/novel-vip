import { ApiClient } from "../api-client";

export function createCorrectionApi(api: ApiClient) {
  return {
    async submitCorrectionRequest(data: {
      novelId: string;
      chapterId: string;
      chapterNumber: number;
      charIndex?: number;
      paragraphIndex?: number;
      paragraphIndices?: number[];
      originalText: string;
      suggestedText: string;
      reason?: string;
    }) {
      return api.request("/api/corrections", {
        method: "POST",
        body: JSON.stringify(data),
      });
    },

    async getPendingCorrections(params: {
      page?: number;
      size?: number;
      direction?: "ASC" | "DESC";
    } = {}) {
      const { page = 0, size = 20, direction = "DESC" } = params;
      return api.request(`/api/corrections/admin/pending?page=${page}&size=${size}&direction=${direction}`);
    },

    async getCorrectionsByStatus(status: string, params: {
      page?: number;
      size?: number;
    } = {}) {
      const { page = 0, size = 20 } = params;
      return api.request(`/api/corrections/admin/by-status/${status}?page=${page}&size=${size}`);
    },

    async approveCorrection(id: string) {
      return api.request(`/api/corrections/admin/${id}/approve`, {
        method: "POST",
      });
    },

    async rejectCorrection(id: string, reason?: string) {
      return api.request(`/api/corrections/admin/${id}/reject`, {
        method: "POST",
        body: reason ? JSON.stringify(reason) : undefined,
      });
    },

    async getCorrectionById(id: string) {
      return api.request(`/api/corrections/${id}`);
    },
  };
}
