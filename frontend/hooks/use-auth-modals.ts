import { create } from "zustand"

interface AuthModalsStore {
  loginOpen: boolean
  registerOpen: boolean
  openLogin: () => void
  closeLogin: () => void
  openRegister: () => void
  closeRegister: () => void
  switchToRegister: () => void
  switchToLogin: () => void
}

export const useAuthModals = create<AuthModalsStore>((set) => ({
  loginOpen: false,
  registerOpen: false,
  openLogin: () => set({ loginOpen: true, registerOpen: false }),
  closeLogin: () => set({ loginOpen: false }),
  openRegister: () => set({ registerOpen: true, loginOpen: false }),
  closeRegister: () => set({ registerOpen: false }),
  switchToRegister: () => set({ loginOpen: false, registerOpen: true }),
  switchToLogin: () => set({ registerOpen: false, loginOpen: true }),
}))
