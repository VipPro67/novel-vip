import { useState } from "react";
import { Button } from "@/components/ui/button";

export interface CorrectionRequestModalProps {
  open: boolean;
  onClose: () => void;
  selectedText: string;
  onSubmit: (suggestedText: string, reason?: string) => void;
  originalText: string;
}

export function CorrectionRequestModal({
  open,
  onClose,
  selectedText,
  onSubmit,
  originalText,
}: CorrectionRequestModalProps) {
  const [suggestedText, setSuggestedText] = useState(selectedText || "");
  const [reason, setReason] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    await onSubmit(suggestedText, reason);
    setSubmitting(false);
    onClose();
  };

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
      <div className="bg-white dark:bg-background rounded-lg shadow-lg p-6 w-full max-w-md">
        <h2 className="text-lg font-bold mb-2">Request Correction</h2>
        <div className="mb-2 text-sm text-muted-foreground">
          <span className="font-semibold">Original:</span> {originalText}
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Suggested Correction</label>
            <textarea
              className="w-full border rounded p-2 min-h-[60px]"
              value={suggestedText}
              onChange={e => setSuggestedText(e.target.value)}
              placeholder="Leave blank to delete the selected text"
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Reason (optional)</label>
            <textarea
              className="w-full border rounded p-2 min-h-[40px]"
              value={reason}
              onChange={e => setReason(e.target.value)}
            />
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose} disabled={submitting}>
              Cancel
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? "Submitting..." : "Submit Correction"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
