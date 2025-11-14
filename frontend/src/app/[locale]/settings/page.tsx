"use client";

import type { ChangeEvent, FormEvent } from "react";
import { useEffect, useMemo, useState } from "react";
import { Bell, Palette, Loader2, RefreshCw } from "lucide-react";

import { AuthGuard } from "@/components/auth/auth-guard";
import { Header } from "@/components/layout/header";
import { useReaderSettings } from "@/components/providers/reader-settings-provider";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useToast } from "@/hooks/use-toast";
import { api } from "@/services/api";
import { useTranslations } from "next-intl";
import { ReaderSettings } from "@/models";

export default function SettingsPage() {
  return (
    <AuthGuard>
      <SettingsContent />
    </AuthGuard>
  );
}

function SettingsContent() {
  const t = useTranslations("Settings");

  return (
    <div className="min-h-screen bg-background">
      <main className="container py-8">
        <div className="max-w-4xl mx-auto space-y-8">
          <div>
            <h1 className="text-3xl font-bold">{t("title")}</h1>
            <p className="text-muted-foreground">{t("subtitle")}</p>
          </div>

          <Tabs defaultValue="appearance" className="space-y-4">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger
                value="notifications"
                className="flex items-center space-x-2"
              >
                <Bell className="h-4 w-4" />
                <span>{t("tabs.notifications")}</span>
              </TabsTrigger>
              <TabsTrigger
                value="appearance"
                className="flex items-center space-x-2"
              >
                <Palette className="h-4 w-4" />
                <span>{t("tabs.appearance")}</span>
              </TabsTrigger>
            </TabsList>

            <TabsContent value="notifications" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>{t("notificationSettings.title")}</CardTitle>
                  <CardDescription>
                    {t("notificationSettings.description")}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">
                    {t("notificationSettings.notImplemented")}
                  </p>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="appearance" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>{t("appearanceSettings.title")}</CardTitle>
                  <CardDescription>
                    {t("appearanceSettings.description")}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <AppearanceSettingsForm />
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </main>
    </div>
  );
}

type BooleanSettingKey =
  | "autoScroll"
  | "keepScreenOn"
  | "showProgress"
  | "showChapterTitle"
  | "showTime"
  | "showBattery"
  | "audioEnabled"
  | "audioAutoNextChapter";

type IntegerSettingKey =
  | "fontSize"
  | "marginSize"
  | "paragraphSpacing"
  | "autoScrollSpeed";

type DecimalSettingKey = "lineHeight" | "audioSpeed";

function AppearanceSettingsForm() {
  const { toast } = useToast();
  const t = useTranslations("Settings");
  const { settings, loading, saving, error, updateSettings, refreshSettings } =
    useReaderSettings();
  const [formValues, setFormValues] = useState<ReaderSettings | null>(null);
  const [fontOptions, setFontOptions] = useState<string[]>([]);
  const [themeOptions, setThemeOptions] = useState<string[]>([]);
  const [optionsLoading, setOptionsLoading] = useState(false);

  useEffect(() => {
    if (settings) {
      setFormValues(settings);
    }
  }, [settings]);

  useEffect(() => {
    let isMounted = true;

    const loadOptions = async () => {
      try {
        setOptionsLoading(true);
        const [fontsResponse, themesResponse] = await Promise.all([
          api.getFontOptions(),
          api.getThemeOptions(),
        ]);

        if (!isMounted) {
          return;
        }

        if (fontsResponse.success) {
          setFontOptions(fontsResponse.data);
        }
        if (themesResponse.success) {
          setThemeOptions(themesResponse.data);
        }
      } catch (err) {
        console.error("Failed to load appearance options", err);
      } finally {
        if (isMounted) {
          setOptionsLoading(false);
        }
      }
    };

    loadOptions();
    return () => {
      isMounted = false;
    };
  }, []);

  const handleIntegerChange =
    (key: IntegerSettingKey) => (event: ChangeEvent<HTMLInputElement>) => {
      if (!formValues) {
        return;
      }
      const value = Number.parseInt(event.target.value, 10);
      if (Number.isNaN(value)) {
        return;
      }

      const constraints: Record<
        IntegerSettingKey,
        { min: number; max: number }
      > = {
        fontSize: { min: 12, max: 40 },
        marginSize: { min: 0, max: 64 },
        paragraphSpacing: { min: 0, max: 48 },
        autoScrollSpeed: { min: 1, max: 10 },
      };

      const { min, max } = constraints[key];
      const nextValue = Math.min(max, Math.max(min, value));

      setFormValues((prev) => (prev ? { ...prev, [key]: nextValue } : prev));
    };

  const handleDecimalChange =
    (key: DecimalSettingKey) => (event: ChangeEvent<HTMLInputElement>) => {
      if (!formValues) {
        return;
      }
      const value = Number.parseFloat(event.target.value);
      if (Number.isNaN(value)) {
        return;
      }

      const constraints: Record<
        DecimalSettingKey,
        { min: number; max: number }
      > = {
        lineHeight: { min: 1.2, max: 2.4 },
        audioSpeed: { min: 0.5, max: 3 },
      };

      const { min, max } = constraints[key];
      const nextValue = Math.min(max, Math.max(min, value));

      setFormValues((prev) =>
        prev ? { ...prev, [key]: Number(nextValue.toFixed(2)) } : prev
      );
    };

  const handleBooleanChange = (key: BooleanSettingKey) => (value: boolean) => {
    setFormValues((prev) => (prev ? { ...prev, [key]: value } : prev));
  };

  const handleColorChange =
    (key: "textColor" | "backgroundColor") =>
    (event: ChangeEvent<HTMLInputElement>) => {
      const color = event.target.value;
      setFormValues((prev) => (prev ? { ...prev, [key]: color } : prev));
    };

  const handleSelectChange =
    (key: "theme" | "fontFamily") => (value: string) => {
      setFormValues((prev) => (prev ? { ...prev, [key]: value } : prev));
    };

  const isDirty = useMemo(() => {
    if (!settings || !formValues) {
      return false;
    }
    return JSON.stringify(settings) !== JSON.stringify(formValues);
  }, [formValues, settings]);

  const loadingState = loading && !formValues;

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!formValues) {
      return;
    }
    try {
      const updated = await updateSettings(formValues);
      if (updated) {
        setFormValues(updated);
        toast({
          title: t("appearanceSettings.appearanceUpdatedTitle"),
          description: t("appearanceSettings.appearanceUpdatedDesc"),
        });
      }
    } catch (err) {
      const message =
        err instanceof Error ? err.message : t("appearanceSettings.updateFailedDesc");
      toast({
        title: t("appearanceSettings.updateFailedTitle"),
        description: message,
        variant: "destructive",
      });
    }
  };

  const handleRefresh = async () => {
    try {
      const refreshed = await refreshSettings();
      if (refreshed) {
        setFormValues(refreshed);
      }
      toast({
        title: t("appearanceSettings.settingsRefreshedTitle"),
        description: t("appearanceSettings.settingsRefreshedDesc"),
      });
    } catch (err) {
      const message =
        err instanceof Error
          ? err.message
          : t("appearanceSettings.refreshFailedDesc");
      toast({
        title: t("appearanceSettings.refreshFailedTitle"),
        description: message,
        variant: "destructive",
      });
    }
  };

  const handleDiscardChanges = () => {
    setFormValues(settings);
  };

  if (loadingState) {
    return (
      <div className="flex items-center gap-2 text-muted-foreground">
        <Loader2 className="h-4 w-4 animate-spin" />
        <span>{t("appearanceSettings.loadingPrefs")}</span>
      </div>
    );
  }

  if (!formValues) {
    return (
      <p className="text-sm text-muted-foreground">
        {t("appearanceSettings.notAvailable")}
      </p>
    );
  }

  const readingToggles: Array<{
    key: BooleanSettingKey;
    label: string;
    description: string;
  }> = [
    {
      key: "autoScroll",
      label: t("appearanceSettings.autoScroll"),
      description: t("appearanceSettings.autoScrollDesc"),
    },
    {
      key: "keepScreenOn",
      label: t("appearanceSettings.keepScreenOn"),
      description: t("appearanceSettings.keepScreenOnDesc"),
    },
    {
      key: "showProgress",
      label: t("appearanceSettings.showProgress"),
      description: t("appearanceSettings.showProgressDesc"),
    },
    {
      key: "showChapterTitle",
      label: t("appearanceSettings.showChapterTitle"),
      description: t("appearanceSettings.showChapterTitleDesc"),
    },
    {
      key: "showTime",
      label: t("appearanceSettings.showTime"),
      description: t("appearanceSettings.showTimeDesc"),
    },
    {
      key: "showBattery",
      label: t("appearanceSettings.showBattery"),
      description: t("appearanceSettings.showBatteryDesc"),
    },
  ];

  const audioToggles: Array<{
    key: BooleanSettingKey;
    label: string;
    description: string;
  }> = [
    {
      key: "audioEnabled",
      label: t("appearanceSettings.audioEnabled"),
      description: t("appearanceSettings.audioEnabledDesc"),
    },
    {
      key: "audioAutoNextChapter",
      label: t("appearanceSettings.audioAutoNextChapter"),
      description: t("appearanceSettings.audioAutoNextChapterDesc"),
    },
  ];

  return (
    <form className="space-y-8" onSubmit={handleSubmit}>
      {error && <p className="text-sm text-destructive">{error}</p>}

      <section className="space-y-4">
        <div>
          <h3 className="text-lg font-semibold">{t("appearanceSettings.typography")}</h3>
          <p className="text-sm text-muted-foreground">
            {t("appearanceSettings.typographyDesc")}
          </p>
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="fontFamily">{t("appearanceSettings.fontFamily")}</Label>
            <Select
              value={formValues.fontFamily}
              onValueChange={handleSelectChange("fontFamily")}
              disabled={optionsLoading || saving}
            >
              <SelectTrigger id="fontFamily">
                <SelectValue placeholder={t("appearanceSettings.selectFont")} />
              </SelectTrigger>
              <SelectContent>
                {fontOptions.length === 0 ? (
                  <SelectItem value={formValues.fontFamily}>
                    {formValues.fontFamily}
                  </SelectItem>
                ) : (
                  fontOptions.map((option) => (
                    <SelectItem key={option} value={option}>
                      {option}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="fontSize">{t("appearanceSettings.fontSize")}</Label>
            <Input
              id="fontSize"
              type="number"
              inputMode="numeric"
              min={12}
              max={40}
              value={formValues.fontSize}
              onChange={handleIntegerChange("fontSize")}
              disabled={saving}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="lineHeight">{t("appearanceSettings.lineHeight")}</Label>
            <Input
              id="lineHeight"
              type="number"
              inputMode="decimal"
              step={0.1}
              min={1.2}
              max={2.4}
              value={formValues.lineHeight}
              onChange={handleDecimalChange("lineHeight")}
              disabled={saving}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="paragraphSpacing">{t("appearanceSettings.paragraphSpacing")}</Label>
            <Input
              id="paragraphSpacing"
              type="number"
              inputMode="numeric"
              min={0}
              max={48}
              value={formValues.paragraphSpacing}
              onChange={handleIntegerChange("paragraphSpacing")}
              disabled={saving}
            />
          </div>
        </div>
      </section>

      <section className="space-y-4">
        <div>
          <h3 className="text-lg font-semibold">{t("appearanceSettings.display")}</h3>
          <p className="text-sm text-muted-foreground">
            {t("appearanceSettings.displayDesc")}
          </p>
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="theme">{t("appearanceSettings.theme")}</Label>
            <Select
              value={formValues.theme}
              onValueChange={handleSelectChange("theme")}
              disabled={optionsLoading || saving}
            >
              <SelectTrigger id="theme">
                <SelectValue placeholder={t("appearanceSettings.selectTheme")} />
              </SelectTrigger>
              <SelectContent>
                {themeOptions.length === 0 ? (
                  <SelectItem value={formValues.theme}>
                    {formValues.theme}
                  </SelectItem>
                ) : (
                  themeOptions.map((option) => (
                    <SelectItem key={option} value={option}>
                      {option}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="marginSize">{t("appearanceSettings.marginSize")}</Label>
            <Input
              id="marginSize"
              type="number"
              inputMode="numeric"
              min={0}
              max={64}
              value={formValues.marginSize}
              onChange={handleIntegerChange("marginSize")}
              disabled={saving}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="textColor">{t("appearanceSettings.textColor")}</Label>
            <Input
              id="textColor"
              type="color"
              value={formValues.textColor}
              onChange={handleColorChange("textColor")}
              disabled={saving}
              className="h-10 w-20"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="backgroundColor">{t("appearanceSettings.backgroundColor")}</Label>
            <Input
              id="backgroundColor"
              type="color"
              value={formValues.backgroundColor}
              onChange={handleColorChange("backgroundColor")}
              disabled={saving}
              className="h-10 w-20"
            />
          </div>
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          {readingToggles.map(({ key, label, description }) => (
            <div
              key={key}
              className="flex items-center justify-between rounded-lg border p-4"
            >
              <div className="pr-4">
                <p className="font-medium leading-none">{label}</p>
                <p className="text-sm text-muted-foreground">{description}</p>
              </div>
              <Switch
                id={key}
                checked={Boolean(formValues[key])}
                onCheckedChange={handleBooleanChange(key)}
                disabled={saving}
              />
            </div>
          ))}
        </div>
      </section>

      <section className="space-y-4">
        <div>
          <h3 className="text-lg font-semibold">{t("appearanceSettings.audioPlayback")}</h3>
          <p className="text-sm text-muted-foreground">
            {t("appearanceSettings.audioPlaybackDesc")}
          </p>
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="audioSpeed">{t("appearanceSettings.audioSpeed")}</Label>
            <Input
              id="audioSpeed"
              type="number"
              inputMode="decimal"
              step={0.1}
              min={0.5}
              max={3}
              value={formValues.audioSpeed}
              onChange={handleDecimalChange("audioSpeed")}
              disabled={saving}
            />
            <p className="text-xs text-muted-foreground">{t("appearanceSettings.audioSpeedHint")}</p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="autoScrollSpeed">{t("appearanceSettings.autoScrollSpeed")}</Label>
            <Input
              id="autoScrollSpeed"
              type="number"
              inputMode="numeric"
              min={1}
              max={10}
              value={formValues.autoScrollSpeed}
              onChange={handleIntegerChange("autoScrollSpeed")}
              disabled={saving}
            />
            <p className="text-xs text-muted-foreground">
              {t("appearanceSettings.autoScrollSpeedHint")}
            </p>
          </div>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          {audioToggles.map(({ key, label, description }) => (
            <div
              key={key}
              className="flex items-center justify-between rounded-lg border p-4"
            >
              <div className="pr-4">
                <p className="font-medium leading-none">{label}</p>
                <p className="text-sm text-muted-foreground">{description}</p>
              </div>
              <Switch
                id={key}
                checked={Boolean(formValues[key])}
                onCheckedChange={handleBooleanChange(key)}
                disabled={saving}
              />
            </div>
          ))}
        </div>
      </section>

      <div className="flex flex-wrap justify-end gap-3">
        <Button
          type="button"
          variant="outline"
          onClick={handleDiscardChanges}
          disabled={!isDirty || saving}
        >
          {t("appearanceSettings.discard")}
        </Button>
        <Button
          type="button"
          variant="outline"
          onClick={handleRefresh}
          disabled={saving}
        >
          <RefreshCw className="mr-2 h-4 w-4" />
          {t("appearanceSettings.refresh")}
        </Button>
        <Button type="submit" disabled={!isDirty || saving}>
          {saving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {t("appearanceSettings.save")}
        </Button>
      </div>
    </form>
  );
}
