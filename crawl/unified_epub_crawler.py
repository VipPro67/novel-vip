#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os, re, time, html, sys, urllib.parse, mimetypes, argparse, threading
import requests
from bs4 import BeautifulSoup
from ebooklib import epub
from concurrent.futures import ThreadPoolExecutor, as_completed
from urllib3.util.retry import Retry
from requests.adapters import HTTPAdapter

# ===================== argparse (verbosity) =====================
def parse_args():
    p = argparse.ArgumentParser(description="Universal TOC → EPUB crawler (+optional backend upload)")
    p.add_argument("--verbose", action="store_true", help="Print extra debug info")
    return p.parse_args()

ARGS = parse_args()

def vprint(*a, **k):
    if ARGS.verbose:
        print(*a, **k)

# ===================== HTTP session (retry + pool) =====================
def build_session():
    s = requests.Session()
    s.headers.update({"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
    retry = Retry(
        total=5, connect=5, read=5,
        backoff_factor=0.8,
        status_forcelist=[429, 500, 502, 503, 504],
        allowed_methods={"GET", "HEAD"},
        raise_on_status=False
    )
    adapter = HTTPAdapter(max_retries=retry, pool_connections=100, pool_maxsize=100)
    s.mount("http://", adapter)
    s.mount("https://", adapter)
    return s

# ===================== Helpers =====================
ALLOWED_INLINE = {"b", "i", "u", "br"}
AD_WORDS = ["ads", "banner", "qc", "quang-cao", "promo", "incontent-ad", "sponsor"]

def is_ad_tag(tag):
    if not tag or not hasattr(tag, "attrs"):
        return False
    for attr in ("id", "class"):
        v = tag.get(attr)
        if not v:
            continue
        text = " ".join(v) if isinstance(v, list) else str(v)
        text = text.lower()
        if any(kw in text for kw in AD_WORDS):
            return True
    return False

def clean_paragraph_tag(tag):
    # giữ b/i/u/br, unwrap phần còn lại
    for child in tag.find_all(True):
        if child.name not in ALLOWED_INLINE:
            child.unwrap()
    s = str(tag)
    s = re.sub(r"\s+", " ", s)
    return s.strip()

def absolutize(base, href):
    return urllib.parse.urljoin(base, href)

def to_slug(s):
    s = s.strip().lower()
    s = re.sub(r"[^a-z0-9]+", "-", s)
    return s.strip("-")

# ===================== Content extract (generic) =====================
def extract_title_generic(soup, chap_num=None, novel_title=None):
    """
    Trích tiêu đề chương an toàn, ưu tiên chính xác vị trí.
    """
    title_text = None

    # 1️⃣ Truyenchuth.info — h3 chứa icon cây bút (fa-pencil-square-o)
    h3_tag = None
    header = soup.select_one(".chapter-header") or soup.select_one(".chapter-title")
    if header:
        for h3 in header.find_all("h3"):
            # chỉ lấy h3 có chữ 'Chương' hoặc có icon fa-pencil-square-o
            if h3.find("i", class_=re.compile("fa-pencil-square")) or re.search(r"Chương\s*\d+", h3.get_text()):
                h3_tag = h3
                break
    if h3_tag:
        title_text = h3_tag.get_text(strip=True)

    # 2️⃣ fallback phổ biến (h1, h2)
    if not title_text:
        for tag in soup.select("h1, h2, h3"):
            text = tag.get_text(strip=True)
            if re.search(r"Chương\s*\d+", text):
                title_text = text
                break

    # 3️⃣ fallback thẻ <title> hoặc meta og:title
    if not title_text:
        meta = soup.select_one("meta[property='og:title']")
        if meta and meta.get("content"):
            title_text = meta["content"]
        else:
            tag = soup.select_one("title")
            if tag:
                title_text = tag.get_text(strip=True)

    # 4️⃣ fallback cuối
    if not title_text:
        title_text = f"Chương {chap_num}" if chap_num else "Không rõ tiêu đề"

    # 5️⃣ loại bỏ phần chứa tên truyện nếu dính
    if novel_title and title_text.lower().startswith(novel_title.lower()):
        parts = title_text.split("-", 1)
        if len(parts) > 1:
            title_text = parts[-1].strip()

    # 6️⃣ clean nhẹ
    title_text = re.sub(r"\s+", " ", title_text).strip()

    vprint(f"[title] Extracted: {title_text}")
    return title_text


def extract_content_generic(soup):
    # phổ biến cho nhiều theme
    content = (
        soup.select_one("#chapter-content")
        or soup.select_one(".chapter-content")
        or soup.select_one("article .entry-content")
        or soup.select_one("article")
        or soup.select_one("div.entry-content")
        or soup.select_one("div.reading-content")
    )
    if not content:
        return None
    for x in content(["script", "style"]):
        x.decompose()
    for tag in content.find_all(True):
        if is_ad_tag(tag):
            tag.decompose()

    parts = []
    for node in content.find_all(["p", "div"], recursive=True):
        txt = clean_paragraph_tag(node)
        if txt and re.search(r"\w", txt, flags=re.UNICODE):
            if not txt.startswith("<p"):
                txt = f"<p>{txt}</p>"
            parts.append(txt)

    parts = [
        p for p in parts
        if not re.search(r"(đọc.*nhanh.*nhất|truy(?:e|ê)n.*nhanh|theo dõi.*page|bản quyền|like.*share)", p, re.I)
    ]
    if not parts:
        return None
    return "\n".join(parts)

# ===================== Content extract (truyenchuth.info) =====================
def _pick_best_selector(soup, candidates):
    best_sel, best_len, best_node = None, 0, None
    for sel in candidates:
        node = soup.select_one(sel)
        if not node:
            continue
        txt = node.get_text(strip=True) if node else ""
        ln = len(txt or "")
        if ln > best_len:
            best_len, best_sel, best_node = ln, sel, node
    return best_sel, best_node

def extract_content_truyenchuth(soup):
    """
    Extract nội dung chương từ truyenchuth.info
    DOM thật: <div id="content" class="w3-justify chapter-content detailcontent">
    """
    # Chọn vùng nội dung chính
    content = soup.select_one("div#content.chapter-content") or soup.select_one("#content")
    if not content:
        vprint("[truyenchuth] ❌ Không tìm thấy div#content")
        return None
    vprint("[truyenchuth] ✅ Tìm thấy div#content.chapter-content")

    # Loại bỏ các thẻ không cần
    for x in content(["script", "style"]):
        x.decompose()
    for tag in content.find_all(True):
        if is_ad_tag(tag):
            tag.decompose()

    # Ghép lại đoạn văn: trang này chỉ có <br> ngăn dòng, không có <p>
    html_parts = []
    buffer = []
    for elem in content.children:
        if getattr(elem, "name", None) == "br":
            # Khi gặp <br><br> => kết thúc một đoạn
            if buffer:
                text = "".join(buffer).strip()
                if text:
                    html_parts.append(f"<p>{html.escape(text)}</p>")
                buffer = []
        elif isinstance(elem, str):
            # text node
            t = elem.strip()
            if t:
                buffer.append(t + " ")
        else:
            # nếu có tag khác như <b><i> trong text
            t = elem.get_text(" ", strip=True)
            if t:
                buffer.append(t + " ")

    # Phần còn lại sau vòng lặp
    if buffer:
        text = "".join(buffer).strip()
        if text:
            html_parts.append(f"<p>{html.escape(text)}</p>")

    # Xóa dòng quảng cáo vô nghĩa
    html_parts = [
        p for p in html_parts
        if not re.search(r"(truyenchuth|đọc.*nhanh.*nhất|like.*share|theo dõi.*page)", p, re.I)
    ]

    return "\n".join(html_parts) if html_parts else None

# ===================== TOC adapters =====================
CH_PATTERN = re.compile(r"/chuong-?(\d+)[^/]*", re.I)

def _parse_chapter_number_from_href(href):
    m = CH_PATTERN.search(href)
    return int(m.group(1)) if m else None

def list_chapters_truyenfull_vision(session, base_url):
    print("🔎 Lấy danh sách chương (truyenfull.vision)…")
    results, seen = [], set()

    if "/trang-" not in base_url:
        base_url = base_url.rstrip("/") + "/trang-1/"

    next_url = base_url
    while next_url:
        print(f"  • Đang duyệt trang danh sách: {next_url}")
        res = session.get(next_url, timeout=20)
        if res.status_code != 200:
            print("    ↳ lỗi tải trang, dừng.")
            break
        soup = BeautifulSoup(res.text, "html.parser")
        list_container = soup.select_one("#list-chapter") or soup.select_one(".list-chapter") or soup
        found_in_page = 0
        for a in list_container.find_all("a", href=True):
            n = _parse_chapter_number_from_href(a["href"])
            if n is None:
                continue
            abs_url = absolutize(next_url, a["href"])
            if abs_url in seen:
                continue
            seen.add(abs_url)
            title = a.get_text(strip=True) or f"Chương {n}"
            results.append((n, abs_url, title))
            found_in_page += 1
        print(f"    ↳ tìm được {found_in_page} chương ở trang này. Tổng tạm thời: {len(results)}")

        nav_next = soup.find("a", attrs={"rel": "next"}) or soup.find("a", string=re.compile(r"Trang\s*sau|Sau|Next|»", re.I))
        if nav_next and nav_next.get("href"):
            next_url = absolutize(next_url, nav_next["href"])
        else:
            m = re.search(r"/trang-(\d+)/", next_url)
            if m:
                nxt = int(m.group(1)) + 1
                probe = re.sub(r"/trang-\d+/", f"/trang-{nxt}/", next_url)
                head = session.head(probe)
                next_url = probe if head.status_code == 200 else None
            else:
                next_url = None

    results.sort(key=lambda x: x[0])
    print(f"✅ Tổng chương lấy được: {len(results)}")
    return results

def list_chapters_truyenchuth_info(session, base_url, max_pages=200, max_empty_pages=1):
    print("🔎 Lấy danh sách chương (truyenchuth.info)…")
    results, seen = [], set()

    parsed = urllib.parse.urlsplit(base_url)
    query = urllib.parse.parse_qs(parsed.query)
    p = int(query.get("p", ["1"])[0])
    empty_in_a_row = 0
    pages = 0

    while pages < max_pages:
        page_url = urllib.parse.urlunsplit((parsed.scheme, parsed.netloc, parsed.path, f"p={p}", ""))
        print(f"  • Đang duyệt trang danh sách: {page_url}")
        res = session.get(page_url, timeout=20)
        if res.status_code != 200:
            print("    ↳ lỗi tải trang, dừng.")
            break

        soup = BeautifulSoup(res.text, "html.parser")

        nav_next = soup.find("a", attrs={"rel": "next"}) or soup.find("a", string=re.compile(r"Trang\s*sau|Sau|Next|»", re.I))

        found_in_page = 0
        # Tìm container chứa danh sách chương
        list_container = soup.select_one("div#divtab.list-chapter")

        # Chỉ lấy <a> trong container này
        for a in list_container.find_all("a", href=True):
            m = CH_PATTERN.search(a["href"])
            if not m:
                continue
            try:
                n = int(m.group(1))
            except ValueError:
                continue

            abs_url = absolutize(page_url, a["href"])
            if abs_url in seen:
                continue
            seen.add(abs_url)

            title = a.get_text(strip=True) or f"Chương {n}"
            results.append((n, abs_url, title))
            found_in_page += 1

        print(f"    ↳ tìm được {found_in_page} chương ở trang này. Tổng tạm thời: {len(results)}")

        pages += 1
        if found_in_page == 0:
            empty_in_a_row += 1
            if empty_in_a_row >= max_empty_pages:
                print("    ↳ gặp trang rỗng, dừng quét.")
                break
        else:
            empty_in_a_row = 0

        if nav_next and nav_next.get("href"):
            next_url = absolutize(page_url, nav_next["href"])
            try:
                nqs = urllib.parse.urlsplit(next_url).query
                np = int(urllib.parse.parse_qs(nqs).get("p", ["0"])[0])
                p = np if np > 0 else p + 1
            except Exception:
                p += 1
        else:
            p += 1

    results.sort(key=lambda x: x[0])
    print(f"✅ Tổng chương lấy được: {len(results)}")
    return results

def list_chapters_truyenchu_net(session, base_url):
    print("🔎 Lấy danh sách chương (truyenchu.net)…")
    results, seen = [], set()
    parsed = urllib.parse.urlsplit(base_url)
    q = urllib.parse.parse_qs(parsed.query)

    def build_page_url(pg):
        query = dict(q)
        query["page"] = [str(pg)]
        query_str = urllib.parse.urlencode({k: v[0] for k, v in query.items()})
        path = parsed.path if parsed.path.endswith("/") else parsed.path + "/"
        return urllib.parse.urlunsplit((parsed.scheme, parsed.netloc, path, query_str, ""))

    if "page" not in q:
        base_url = build_page_url(1)

    next_url = base_url
    while next_url:
        print(f"  • Đang duyệt trang danh sách: {next_url}")
        res = session.get(next_url, timeout=20)
        if res.status_code != 200:
            print("    ↳ lỗi tải trang, dừng.")
            break
        soup = BeautifulSoup(res.text, "html.parser")
        list_container = soup.select_one("#list-chapter") or soup.select_one(".list-chapter") or soup
        found_in_page = 0
        for a in list_container.find_all("a", href=True):
            n = _parse_chapter_number_from_href(a["href"])
            if n is None:
                continue
            abs_url = absolutize(next_url, a["href"])
            if abs_url in seen:
                continue
            seen.add(abs_url)
            title = a.get_text(strip=True) or f"Chương {n}"
            results.append((n, abs_url, title))
            found_in_page += 1
        print(f"    ↳ tìm được {found_in_page} chương ở trang này. Tổng tạm thời: {len(results)}")

        nav_next = soup.find("a", attrs={"rel": "next"}) or soup.find("a", string=re.compile(r"Trang\s*sau|Sau|Next|»", re.I))
        if nav_next and nav_next.get("href"):
            next_url = absolutize(next_url, nav_next["href"])
        else:
            m = re.search(r"[?&]page=(\d+)", next_url)
            if m:
                nxt = int(m.group(1)) + 1
                probe = re.sub(r"([?&]page=)\d+", rf"\g<1>{nxt}", next_url)
                head = session.head(probe)
                next_url = probe if head.status_code == 200 else None
            else:
                next_url = None

    results.sort(key=lambda x: x[0])
    print(f"✅ Tổng chương lấy được: {len(results)}")
    return results

# ===================== Router for TOC =====================
def list_chapters_from_base(session, base_url):
    host = urllib.parse.urlsplit(base_url).netloc
    if "truyenfull.vision" in host:
        return list_chapters_truyenfull_vision(session, base_url)
    if "truyenchuth.info" in host:
        return list_chapters_truyenchuth_info(session, base_url)
    if "truyenchu.net" in host:
        return list_chapters_truyenchu_net(session, base_url)

    print("ℹ️ Domain chưa có adapter riêng — dùng fallback quét link /chuong-<n>…")
    results, seen = [], set()
    next_url = base_url
    while next_url:
        print(f"  • Đang duyệt trang danh sách (fallback): {next_url}")
        r = session.get(next_url, timeout=20)
        if r.status_code != 200:
            print("    ↳ lỗi tải trang, dừng.")
            break
        soup = BeautifulSoup(r.text, "html.parser")
        found_in_page = 0
        for a in soup.find_all("a", href=True):
            n = _parse_chapter_number_from_href(a["href"])
            if n is None:
                continue
            abs_url = absolutize(next_url, a["href"])
            if abs_url in seen:
                continue
            seen.add(abs_url)
            title = a.get_text(strip=True) or f"Chương {n}"
            results.append((n, abs_url, title))
            found_in_page += 1
        print(f"    ↳ tìm được {found_in_page} chương ở trang này. Tổng tạm thời: {len(results)}")
        nav_next = soup.find("a", attrs={"rel": "next"}) or soup.find("a", string=re.compile(r"Next|Sau|Trang\s*sau|»", re.I))
        next_url = absolutize(next_url, nav_next["href"]) if (nav_next and nav_next.get("href")) else None
    results.sort(key=lambda x: x[0])
    print(f"✅ Tổng chương lấy được: {len(results)}")
    return results

# ===================== Crawl chapter (with progress) =====================
_progress_lock = threading.Lock()
_progress_done = 0
_progress_total = 0
_start_time = 0.0

def _print_progress(prefix="Crawl"):
    with _progress_lock:
        done = _progress_done
        total = _progress_total
        elapsed = max(0.001, time.time() - _start_time)
        rate = done / elapsed
        print(f"  {prefix}: {done}/{total} done | {rate:.2f} chap/s | elapsed {elapsed:.1f}s", end="\r", flush=True)

def fetch_one_chapter(session, chap_num, url, novel_title):
    global _progress_done
    try:
        vprint(f"→ GET {url}")
        res = session.get(url, timeout=20)
        if res.status_code != 200:
            vprint(f"  ↳ HTTP {res.status_code} (skip)")
            with _progress_lock:
                _progress_done += 1
            _print_progress()
            return chap_num, None, None

        soup = BeautifulSoup(res.text, "html.parser")
        title = extract_title_generic(soup, chap_num, novel_title)

        host = urllib.parse.urlsplit(url).netloc
        if "truyenchuth.info" in host:
            body = extract_content_truyenchuth(soup) or extract_content_generic(soup)
        else:
            body = extract_content_generic(soup)

        if not body:
            vprint("  ↳ empty body (skip)")
            with _progress_lock:
                _progress_done += 1
            _print_progress()
            return chap_num, title, None

        html_content = f"<h2>{html.escape(title)}</h2>\n{body}"
        vprint(f"  ↳ OK: {title}")
        with _progress_lock:
            _progress_done += 1
        _print_progress()
        return chap_num, title, html_content

    except Exception as e:
        vprint(f"  ↳ EXCEPTION: {e}")
        with _progress_lock:
            _progress_done += 1
        _print_progress()
        return chap_num, None, None

# ===================== EPUB builder (robust) =====================
def create_epub(novel_title, author, ordered_chapters, output_path):
    print("\n🧱 Đang build EPUB…")
    book = epub.EpubBook()
    book.set_identifier("novel-" + to_slug(novel_title))
    book.set_title(novel_title)
    book.set_language("vi")
    book.add_author(author or "Không rõ")

    items, empty_cnt = [], 0
    for chap_num, title, html_content in ordered_chapters:
        display_title = title or f"Chương {chap_num} (Lỗi)"

        final_body = html_content if (html_content and str(html_content).strip()) else (
            f"<h2>{html.escape(display_title)}</h2><p><i>Không lấy được nội dung chương này.</i></p>"
        )
        if final_body == html_content:
            # ok
            pass
        else:
            empty_cnt += 1

        xhtml = (
            "<?xml version='1.0' encoding='utf-8'?>\n"
            "<!DOCTYPE html>\n"
            f"<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='vi'>"
            f"<head><meta charset='utf-8'/><title>{html.escape(display_title)}</title></head>"
            f"<body>{final_body}</body></html>"
        )

        item = epub.EpubHtml(title=display_title, file_name=f"chap_{chap_num}.xhtml", lang="vi")
        item.set_content(xhtml.encode("utf-8"))  # tránh empty/encoding edge-cases
        book.add_item(item)
        items.append(item)

    if not items:
        # bảo hiểm: vẫn tạo 1 file hợp lệ
        fallback = epub.EpubHtml(title="EMPTY", file_name="chap_0.xhtml", lang="vi")
        fallback.set_content(b"<?xml version='1.0' encoding='utf-8'?><!DOCTYPE html><html xmlns='http://www.w3.org/1999/xhtml' xml:lang='vi'><head><meta charset='utf-8'/><title>EMPTY</title></head><body><h2>EMPTY</h2><p>No content.</p></body></html>")
        book.add_item(fallback)
        items.append(fallback)

    book.toc = tuple(items)
    book.add_item(epub.EpubNcx())
    book.add_item(epub.EpubNav())
    book.spine = ["nav"] + items

    epub.write_epub(output_path, book)
    print(f"✅ EPUB created: {output_path}")
    if empty_cnt:
        print(f"⚠️ Có {empty_cnt} chương placeholder (không lấy được nội dung).")

# ===================== Optional: upload to backend =====================
def upload_to_backend(epub_path, api_base, slug, token, status=None):
    """
    POST {api_base}/api/novels/import-epub?slug=<slug>&status=<status>
    Form-Data: epub=@file
    Header: Authorization: Bearer <token>
    """
    url = api_base.rstrip("/") + "/api/novels/import-epub"
    params = {"slug": slug}
    if status:
        params["status"] = status
    headers = {"Authorization": f"Bearer {token}"} if token else {}
    files = {"epub": (os.path.basename(epub_path), open(epub_path, "rb"), mimetypes.guess_type(epub_path)[0] or "application/epub+zip")}
    print(f"📤 Uploading to {url}?slug={slug}{'&status='+status if status else ''}")
    try:
        r = requests.post(url, headers=headers, params=params, files=files, timeout=90)
        print("   ↳ Response:", r.status_code)
        print("   ↳ Body   :", (r.text[:500] + ("…" if len(r.text) > 500 else "")))
        r.raise_for_status()
        print("✅ Import queued successfully.")
    except Exception as e:
        print("❌ Import failed:", e)

# ===================== Controller =====================
def run_pipeline():
    print("=== 📚 Universal TOC → EPUB (3-in-1) ===")
    novel_title =  "Cửa hàng sủng thú cửa hàng"
    toc_url = "https://truyenchuth.info/truyen-sieu-than-sung-thu-cua-hang"
    start_chap = 1
    end_chap = 1426
    author = "Cổ Hi"

    session = build_session()
    print("⏳ Đang lấy danh sách chương từ trang mục lục…")
    links = list_chapters_from_base(session, toc_url)
    if not links:
        print("❌ Không tìm thấy danh sách chương từ URL mục lục.")
        sys.exit(1)

    wanted = [(n, u, t) for (n, u, t) in links if start_chap <= n <= end_chap]
    print(f"📑 Tổng chương có trong khoảng [{start_chap}..{end_chap}]: {len(wanted)}")
    if not wanted:
        print("❌ Không có chương nào trong khoảng yêu cầu.")
        sys.exit(1)

    # Sample inspect chương đầu để xác nhận selector ổn
    probe_n, probe_u, _ = wanted[0]
    print(f"🧪 Kiểm tra selector bằng chương đầu: {probe_u}")
    _tmp = fetch_one_chapter(session, probe_n, probe_u, novel_title)
    if not _tmp or not _tmp[2]:
        print("⚠️ Cảnh báo: Không trích được nội dung ở chương mẫu. Vẫn tiếp tục crawl, nhưng khả năng FAIL cao nếu selector sai.")

    total = len(wanted)
    threaded = total > 100
    workers = min(20, max(5, total // 5)) if threaded else 1
    print(f"🚀 Bắt đầu crawl: {total} chương | mode={'Threaded' if threaded else 'Single'} | workers={workers}")

    global _progress_done, _progress_total, _start_time
    _progress_done = 0
    _progress_total = total
    _start_time = time.time()

    results = []
    t0 = time.time()
    if threaded:
        with ThreadPoolExecutor(max_workers=workers) as ex:
            futs = [ex.submit(fetch_one_chapter, session, n, u, novel_title) for (n, u, _) in wanted]
            for fut in as_completed(futs):
                results.append(fut.result())
    else:
        for (n, u, _) in wanted:
            results.append(fetch_one_chapter(session, n, u, novel_title))
            time.sleep(0.1)

    print()
    elapsed_crawl = time.time() - t0
    ok_cnt = sum(1 for _, _, h in results if h)
    fail_cnt = total - ok_cnt
    print(f"📊 Crawl xong: OK {ok_cnt} | FAIL {fail_cnt} | thời gian {elapsed_crawl:.1f}s (~{ok_cnt/max(0.001,elapsed_crawl):.2f} chap/s)")

    results.sort(key=lambda x: x[0])
    out = os.path.abspath(f"{to_slug(novel_title)}.epub")
    create_epub(novel_title, author, results, out)

    # print("\n— Bước 4: Gửi lên backend import-epub —")
    # yn = input("Upload lên backend? (y/N): ").strip().lower()
    # if yn == "y":
    #     api_base = input("API base (vd: https://api.novel-vip.xyz): ").strip()
    #     slug = input("Slug (vd: {}): ".format(to_slug(novel_title))).strip() or to_slug(novel_title)
    #     token = input("Bearer token (paste hoặc bỏ trống): ").strip()
    #     status = input("Status (vd: ONGOING/DRAFT – bỏ trống nếu không): ").strip() or None
    #     upload_to_backend(out, api_base, slug, token, status)
    # else:
    #     print("✅ Dừng ở bước 3 (EPUB đã tạo).")

if __name__ == "__main__":
    run_pipeline()
