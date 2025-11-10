"use client"

import Link from "next/link"
import { useEffect, useState } from "react"
import { useSearchParams } from "next/navigation"
import { CheckCircle2, Loader2, XCircle } from "lucide-react"

import { Button } from "@/components/ui/button"
import { api } from "@/services/api"
import type { ApiError } from "@/services/api-client"

type Status = "idle" | "loading" | "success" | "error"

export default function VerifyEmailPage() {
  const searchParams = useSearchParams()
  const token = searchParams.get("token")
  const [status, setStatus] = useState<Status>("idle")
  const [message, setMessage] = useState("Preparing verification...")
  const [details, setDetails] = useState<string | null>(null)

  useEffect(() => {
    if (!token) {
      setStatus("error")
      setMessage("Verification token is missing. Please use the link from your email.")
      return
    }

    const verify = async () => {
      setStatus("loading")
      setMessage("Verifying your email...")
      setDetails(null)

      try {
        const response = await api.verifyEmail(token)
        if (response.success) {
          setStatus("success")
          setMessage(response.message || "Email verified successfully. You can sign in now.")
        } else {
          setStatus("error")
          setMessage(response.message || "Unable to verify email.")
        }
      } catch (error) {
        const apiError = error as ApiError | Error
        setStatus("error")
        setMessage(apiError instanceof Error ? apiError.message : "Unable to verify email.")
        const fallbackDetail = (apiError as ApiError)?.body?.message
        if (fallbackDetail) {
          setDetails(fallbackDetail)
        }
      }
    }

    verify()
  }, [token])

  const renderIcon = () => {
    if (status === "loading" || status === "idle") {
      return <Loader2 className="h-12 w-12 animate-spin text-primary" />
    }
    if (status === "success") {
      return <CheckCircle2 className="h-12 w-12 text-green-500" />
    }
    return <XCircle className="h-12 w-12 text-destructive" />
  }

  return (
    <section className="flex min-h-[70vh] items-center justify-center px-4 py-12">
      <div className="w-full max-w-xl rounded-lg border bg-card p-8 text-center shadow-sm">
        <div className="mb-6 flex justify-center">{renderIcon()}</div>
        <h1 className="text-2xl font-semibold">Email Verification</h1>
        <p className="mt-4 text-base text-muted-foreground">{message}</p>
        {details && <p className="mt-2 text-sm text-muted-foreground">{details}</p>}

        <div className="mt-8 flex justify-center">
          <Button asChild>
            <Link href="/">Return to homepage</Link>
          </Button>
        </div>
      </div>
    </section>
  )
}
