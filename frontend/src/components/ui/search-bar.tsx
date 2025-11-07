"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { useSuggest, SuggestItem } from "@/hooks/use-suggest";
import { api } from "@/services/api";

export default function SearchBar() {
	const router = useRouter();
	const { value, setValue, open, setOpen, items, loading, setQueryDebounced } = useSuggest();
	const [highlight, setHighlight] = useState(0);
	const boxRef = useRef<HTMLDivElement>(null);

	useEffect(() => {
		const outside = (e: MouseEvent) => {
			if (!boxRef.current?.contains(e.target as Node)) setOpen(false);
		};
		window.addEventListener("click", outside);
		return () => window.removeEventListener("click", outside);
	}, [setOpen]);

	function goToQuery(q: string) {
		setOpen(false);
		router.push(`/search?q=${encodeURIComponent(q)}`);
	}

	async function goToItem(it: SuggestItem) {
		setOpen(false);
		if (it.id) {
			try {
				const response = await api.getNovelById(it.id);
				if (response.success) {
					router.push(`/novels/${response.data.slug}`);
					return;
				}
			} catch (error) {
				console.error("Failed to resolve novel from suggestion:", error);
			}
			return goToQuery(it.title);
		}
		goToQuery(it.title);
	}

	return (
		<div className="hidden lg:flex items-center space-x-2 flex-1 max-w-md mx-6" ref={boxRef}>
			<div className="relative w-full">
				<Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
				<Input
					placeholder="Search novels..."
					className="pl-10"
					value={value}
					onChange={(e) => {
						const v = e.target.value;
						setValue(v);
						setQueryDebounced(v);
						if (v.trim().length === 0) setOpen(false);
					}}
					onFocus={() => items.length && setOpen(true)}
					onKeyDown={(e) => {
						if (e.key === "Enter") {
							if (open && items.length) return goToItem(items[highlight]);
							if (value.trim()) return goToQuery(value.trim());
						}
						if (e.key === "ArrowDown" && open && items.length) {
							e.preventDefault();
							setHighlight((highlight + 1) % items.length);
						}
						if (e.key === "ArrowUp" && open && items.length) {
							e.preventDefault();
							setHighlight((highlight - 1 + items.length) % items.length);
						}
						if (e.key === "Escape") setOpen(false);
					}}
					aria-autocomplete="list"
					aria-expanded={open}
					aria-controls="search-suggest"
					role="combobox"
				/>

				{open && (
					<div
						id="search-suggest"
						className="absolute z-50 mt-1 w-full rounded-xl bg-background border shadow-lg overflow-hidden"
						role="listbox"
					>
						{loading && items.length === 0 && (
							<div className="px-4 py-2 text-sm text-muted-foreground">Searching…</div>
						)}

						{items.map((s, i) => (
							<button
								key={`${s.id || s.title}-${i}`}
								className={`w-full text-left px-4 py-2 text-sm hover:bg-accent ${i === highlight ? "bg-accent" : ""
									}`}
								role="option"
								aria-selected={i === highlight}
								onMouseEnter={() => setHighlight(i)}
								onMouseDown={(e) => e.preventDefault()}
								onClick={() => goToItem(s)}
							>
								{s.title}
							</button>
						))}

						{!loading && items.length === 0 && (
							<div className="px-4 py-2 text-sm text-muted-foreground">No suggestions</div>
						)}
					</div>
				)}
			</div>
		</div>
	);
}
