export interface User {
  id: string
  username: string
  email: string
  fullName?: string
  roles: string[]
  wallet?: number
  cultivationPoints?: number
}
