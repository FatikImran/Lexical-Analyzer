from pathlib import Path
from docx import Document

base = Path(r"c:\Coding\Sem6\CC\A1\23i0655-23i0524-C\docs")
files = [
    base / "Automata Design.docx",
    base / "Comparsion Report.docx",
]

for docx_path in files:
    if not docx_path.exists():
        print(f"Missing: {docx_path}")
        continue
    doc = Document(docx_path)
    lines = []
    for paragraph in doc.paragraphs:
        text = paragraph.text.strip()
        if text:
            lines.append(text)
    out_path = docx_path.with_suffix(".txt")
    out_path.write_text("\n".join(lines), encoding="utf-8")
    print(f"Extracted: {out_path}")
