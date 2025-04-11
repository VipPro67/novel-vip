export interface ReaderSettings {
  backgroundColor: string;
  textColor: string;
  fontSize: number;
  fontFamily: string;
  lineHeight: number;
  letterSpacing: number;
  paragraphSpacing: number;
  maxWidth: number;
}

export const DEFAULT_READER_SETTINGS: ReaderSettings = {
  backgroundColor: '#ffffff',
  textColor: '#333333',
  fontSize: 18,
  fontFamily: 'Arial, sans-serif',
  lineHeight: 1.8,
  letterSpacing: 0,
  paragraphSpacing: 1.5,
  maxWidth: 800
};

export const FONT_FAMILIES = [
  { value: 'Arial, sans-serif', label: 'Arial' },
  { value: 'Times New Roman, serif', label: 'Times New Roman' },
  { value: 'Roboto, sans-serif', label: 'Roboto' },
  { value: 'Open Sans, sans-serif', label: 'Open Sans' },
  { value: 'Merriweather, serif', label: 'Merriweather' }
];

export const BACKGROUND_COLORS = [
  { value: '#ffffff', label: 'White' },
  { value: '#f8f9fa', label: 'Light Gray' },
  { value: '#f0f4f8', label: 'Light Blue' },
  { value: '#f5f5dc', label: 'Beige' },
  { value: '#e6f3ff', label: 'Light Blue' },
  { value: '#f0fff0', label: 'Mint' },
  { value: '#000000', label: 'Black' }
];  

export const TEXT_COLORS = [
  { value: '#333333', label: 'Dark Gray' },
  { value: '#000000', label: 'Black' },
  { value: '#2c3e50', label: 'Navy' },
  { value: '#3a3a3a', label: 'Charcoal' },
  { value: '#4a4a4a', label: 'Medium Gray' },
  { value: '#ffffff', label: 'White' }
]; 